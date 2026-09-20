package co.statu.parsek.route

import co.statu.parsek.PluginEventManager
import co.statu.parsek.PluginManager
import co.statu.parsek.annotation.Endpoint
import co.statu.parsek.api.ParsekPlugin
import co.statu.parsek.api.event.PluginLifecycleListener
import co.statu.parsek.api.event.RouterEventListener
import co.statu.parsek.config.ConfigManager
import co.statu.parsek.model.Api
import co.statu.parsek.model.Route
import co.statu.parsek.util.RateLimitManager
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.json.schema.SchemaRepository
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class RouterProvider private constructor(
    private val vertx: Vertx,
    private val applicationContext: AnnotationConfigApplicationContext,
    private val schemaRepository: SchemaRepository,
    private val configManager: ConfigManager,
    private val pluginManager: PluginManager
): PluginLifecycleListener {
    companion object {
        fun create(
            vertx: Vertx,
            applicationContext: AnnotationConfigApplicationContext,
            schemaRepository: SchemaRepository,
            configManager: ConfigManager,
            pluginManager: PluginManager
        ) =
            RouterProvider(vertx, applicationContext, schemaRepository, configManager, pluginManager)

        private var isInitialized = false

        fun getIsInitialized() = isInitialized
    }

    private val router by lazy {
        Router.router(vertx)
    }

    private val pendingPlugins = mutableListOf<ParsekPlugin>()
    private val pluginRoutes = mutableMapOf<ParsekPlugin, List<io.vertx.ext.web.Route>>()

    init {
        pluginManager.addLifecycleListener(this)
    }

    fun initialize() {
        if (isInitialized) return

        val routerEventHandlers = PluginEventManager.getParsekEventListeners<RouterEventListener>()

        routerEventHandlers.forEach { eventHandler ->
            eventHandler.onRouterCreate(router)
        }

        val hostRoutes =
            applicationContext.getBeansWithAnnotation(Endpoint::class.java).values.map { it as Route }.toMutableList()

        routerEventHandlers.forEach { eventHandler ->
            eventHandler.onInitRouteList(hostRoutes)
        }

        applyRoutes(hostRoutes)

        pendingPlugins.forEach { plugin ->
            processPluginLoad(plugin)
        }
        pendingPlugins.clear()

        pluginManager.getActivePlugins().forEach { plugin ->
            if (!pluginRoutes.containsKey(plugin)) {
                processPluginLoad(plugin)
            }
        }

        router.route()
            .handler(SessionHandler.create(LocalSessionStore.create(vertx)))

        // Global, per-IP API rate limiting. Vert.x walks routes by `order`, lowest first, and a
        // `router.route()` registered without an explicit order gets its registration index. This
        // route is registered after every endpoint (order = 1 via Route.order), so without the
        // explicit order below the matching endpoint would answer first and the limiter would only
        // ever see requests no endpoint matched. Int.MIN_VALUE keeps it ahead of any endpoint order.
        val rateLimitManager = applicationContext.getBean(RateLimitManager::class.java)
        rateLimitManager.init(configManager.config)

        router.route()
            .order(Int.MIN_VALUE)
            .handler(rateLimitManager.createHandler(configManager.config.router.apiPrefix))

        isInitialized = true
    }

    override suspend fun onPluginLoad(plugin: ParsekPlugin) {
        if (!isInitialized) {
            pendingPlugins.add(plugin)
            return
        }

        processPluginLoad(plugin)
    }

    private fun processPluginLoad(plugin: ParsekPlugin) {
        if (pluginRoutes.containsKey(plugin)) return

        val routes =
            plugin.pluginBeanContext.getBeansWithAnnotation(Endpoint::class.java).values.map { it as Route }.toMutableList()
        val routerEventHandlers = PluginEventManager.getParsekEventListeners<RouterEventListener>()

        routerEventHandlers.forEach { eventHandler ->
            eventHandler.onInitRouteList(routes)
        }

        pluginRoutes[plugin] = applyRoutes(routes)
    }

    override suspend fun onPluginUnload(plugin: ParsekPlugin) {
        if (!isInitialized) {
            pendingPlugins.remove(plugin)
            return
        }

        pluginRoutes[plugin]?.forEach { it.disable(); it.remove() }
        pluginRoutes.remove(plugin)
    }

    private fun applyRoutes(routes: List<Route>): List<io.vertx.ext.web.Route> {
        val routerConfig = configManager.config.router
        val vertxRoutes = mutableListOf<io.vertx.ext.web.Route>()

        routes.forEach { route ->
            route.paths.forEach { path ->
                var url = path.url
                val httpMethod = path.routeType.vertxHttpMethod

                if (route is Api) {
                    val apiPrefix = routerConfig.apiPrefix

                    if (!url.startsWith(apiPrefix)) {
                        url = apiPrefix + url
                    }
                }

                val corsHandler = route.corsHandler(
                    hosts = route.allowedHosts.takeIf { it.isNotEmpty() } ?: routerConfig.allowedHosts,
                    schemes = route.allowedSchemes.takeIf { it.isNotEmpty() } ?: routerConfig.allowedSchemes,
                    headers = route.allowedHeaders.takeIf { it.isNotEmpty() } ?: routerConfig.allowedHeaders,
                    methods = (route.allowedMethods.takeIf { it.isNotEmpty() }?.map { it.name() }?.toSet()
                        ?: routerConfig.allowedMethods).map { HttpMethod.valueOf(it) }.toSet()
                )

                val routedRoute = if (httpMethod != null) {
                    if (corsHandler != null) {
                        router.route(HttpMethod.OPTIONS, url).handler(corsHandler)
                    }
                    router.route(httpMethod, url)
                } else {
                    router.route(url)
                }

                routedRoute
                    .order(route.order)

                val bodyHandler = route.bodyHandler()

                if (bodyHandler != null) {
                    routedRoute.handler(bodyHandler)
                }

                if (corsHandler != null) {
                    routedRoute.handler(corsHandler)
                }

                val validationHandler = route.getValidationHandler(schemaRepository)

                if (validationHandler != null) {
                    routedRoute
                        .handler(validationHandler)
                }

                routedRoute
                    .handler(route.getHandler())
                    .failureHandler(route.getFailureHandler())

                vertxRoutes.add(routedRoute)
            }
        }

        return vertxRoutes
    }

    fun provide(): Router = router
}