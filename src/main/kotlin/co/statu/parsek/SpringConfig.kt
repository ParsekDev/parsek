package co.statu.parsek

import co.statu.parsek.config.ConfigManager
import co.statu.parsek.route.RouterProvider
import io.vertx.core.Vertx
import io.vertx.json.schema.Draft
import io.vertx.json.schema.JsonSchemaOptions
import io.vertx.json.schema.SchemaRepository
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.*
import java.nio.file.Paths


@Configuration
@ComponentScan("co.statu.parsek")
open class SpringConfig {
    companion object {
        internal lateinit var vertx: Vertx
        private lateinit var logger: Logger

        internal fun setDefaults(vertx: Vertx, logger: Logger) {
            SpringConfig.vertx = vertx
            SpringConfig.logger = logger
        }
    }

    private val pluginsDir = System.getProperty("pf4j.pluginsDir", "./plugins")

    @Autowired
    private lateinit var applicationContext: AnnotationConfigApplicationContext

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    open fun vertx() = vertx

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    open fun logger() = logger

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    open fun routerProvider(
        schemaRepository: SchemaRepository,
        configManager: ConfigManager,
        pluginManager: PluginManager,
    ) =
        RouterProvider.create(
            vertx,
            applicationContext,
            schemaRepository,
            configManager,
            pluginManager
        )

    @Bean
    open fun router(routerProvider: RouterProvider) = routerProvider.provide()

    @Bean
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    open fun provideSchemeRepository(): SchemaRepository = SchemaRepository.create(
        JsonSchemaOptions().setDraft(Draft.DRAFT7).setBaseUri("https://parsek.dev/")
    )

    @Bean
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    open fun pluginManager(): PluginManager = PluginManager(listOf(Paths.get(pluginsDir)))
}