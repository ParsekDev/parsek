package co.statu.parsek

import co.statu.parsek.annotation.Boot
import co.statu.parsek.config.ConfigManager
import co.statu.parsek.route.RouterProvider
import co.statu.parsek.util.TimeUtil
import com.jcabi.manifests.Manifests
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext

@Boot
class Main : CoroutineVerticle() {
    companion object {
        private val options by lazy {
            VertxOptions()
        }

        private val vertx by lazy {
            Vertx.vertx(options)
        }

        private val mode by lazy {
            try {
                Manifests.read("MODE").toString()
            } catch (e: Exception) {
                "RELEASE"
            }
        }

        val ENVIRONMENT =
            if (mode != "DEVELOPMENT" && System.getenv("EnvironmentType").isNullOrEmpty())
                EnvironmentType.RELEASE
            else
                EnvironmentType.DEVELOPMENT

        val VERSION by lazy {
            try {
                Manifests.read("VERSION").toString()
            } catch (e: Exception) {
                System.getenv("ParsekVersion").toString()
            }
        }

        val STAGE by lazy {
            ReleaseStage.valueOf(
                stage =
                try {
                    Manifests.read("BUILD_TYPE").toString()
                } catch (e: Exception) {
                    System.getenv("ParsekBuildType").toString()
                }
            )!!
        }

        @JvmStatic
        fun main(args: Array<String>) {
            vertx.deployVerticle(Main()).onFailure {
                it.printStackTrace()
            }
        }

        enum class EnvironmentType {
            DEVELOPMENT, RELEASE
        }

        internal lateinit var applicationContext: AnnotationConfigApplicationContext
    }

    private val logger by lazy {
        LoggerFactory.getLogger("Parsek")
    }

    private lateinit var router: Router
    private lateinit var configManager: ConfigManager
    private lateinit var pluginManager: PluginManager

    override suspend fun start() {
        println(
            "\n" + "    ____                       __  \n" +
                    "   / __ \\____ ______________  / /__\n" +
                    "  / /_/ / __ `/ ___/ ___/ _ \\/ //_/\n" +
                    " / ____/ /_/ / /  (__  )  __/ ,<   \n" +
                    "/_/    \\__,_/_/  /____/\\___/_/|_|   v${VERSION}\n" +
                    "                                           "
        )

        logger.info("Hello World!")
        logger.info("Current environment: ${ENVIRONMENT.name}")

        init()

        startWebServer()
    }

    private suspend fun executeBlocking(unit: () -> Unit) {
        vertx.executeBlocking {
            unit.invoke()
        }.onFailure {
            it.printStackTrace()
        }.coAwait()
    }

    private suspend fun init() {
        executeBlocking {
            initDependencyInjection()
        }

        initConfigManager()

        executeBlocking {
            initPlugins()
        }

        initRoutes()
    }

    private fun initPlugins() {
        logger.info("Initializing plugin manager")

        pluginManager = applicationContext.getBean(PluginManager::class.java)

        logger.info("Loading plugins")

        pluginManager.loadPlugins()

        logger.info("Enabling plugins")

        pluginManager.startPlugins()
    }

    private fun initDependencyInjection() {
        logger.info("Initializing dependency injection")

        SpringConfig.setDefaults(vertx, logger)

        applicationContext = AnnotationConfigApplicationContext(SpringConfig::class.java)
    }

    private suspend fun initConfigManager() {
        logger.info("Initializing config manager")

        configManager = applicationContext.getBean(ConfigManager::class.java)

        configManager.init()
    }

    private fun initRoutes() {
        logger.info("Initializing routes")

        try {
            val routerProvider = applicationContext.getBean(RouterProvider::class.java)
            routerProvider.initialize()
            router = applicationContext.getBean(Router::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startWebServer() {
        logger.info("Creating HTTP server")

        val serverConfig = configManager.config.server
        val host = serverConfig.host
        val port = serverConfig.port

        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(port, host)
            .onSuccess {
                logger.info("Started listening on port $port, ready to rock & roll! (${TimeUtil.getStartupTime()}s)")
            }
            .onFailure {
                logger.error("Failed to listen on port $port, reason: $it")
            }
    }
}