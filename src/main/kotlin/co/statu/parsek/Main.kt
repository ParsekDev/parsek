package co.statu.parsek

import co.statu.parsek.annotation.Boot
import co.statu.parsek.config.ConfigManager
import co.statu.parsek.util.TimeUtil
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.jar.Manifest

@Boot
class Main : CoroutineVerticle() {
    companion object {
        const val PORT = 8088

        private val options by lazy {
            VertxOptions()
        }

        private val vertx by lazy {
            Vertx.vertx(options)
        }

        private val mode by lazy {
            try {
                val urlClassLoader = ClassLoader.getSystemClassLoader()
                val manifestUrl = urlClassLoader.getResourceAsStream("META-INF/MANIFEST.MF")
                val manifest = Manifest(manifestUrl)

                manifest.mainAttributes.getValue("MODE").toString()
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
                val urlClassLoader = ClassLoader.getSystemClassLoader()
                val manifestUrl = urlClassLoader.getResourceAsStream("META-INF/MANIFEST.MF")
                val manifest = Manifest(manifestUrl)

                manifest.mainAttributes.getValue("VERSION").toString()
            } catch (e: Exception) {
                System.getenv("ParsekVersion").toString()
            }
        }

        val STAGE by lazy {
            ReleaseStage.valueOf(
                stage =
                try {
                    val urlClassLoader = ClassLoader.getSystemClassLoader()
                    val manifestUrl = urlClassLoader.getResourceAsStream("META-INF/MANIFEST.MF")
                    val manifest = Manifest(manifestUrl)

                    manifest.mainAttributes.getValue("BUILD_TYPE").toString()
                } catch (e: Exception) {
                    System.getenv("ParsekBuildType").toString()
                }
            )
        }

        @JvmStatic
        fun main(args: Array<String>) {
            vertx.deployVerticle(Main())
        }

        enum class EnvironmentType {
            DEVELOPMENT, RELEASE
        }
    }

    private val logger by lazy {
        LoggerFactory.getLogger("Parsek")
    }

    private lateinit var router: Router
    private lateinit var applicationContext: AnnotationConfigApplicationContext
    private lateinit var configManager: ConfigManager

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

        init()

        startWebServer()
    }

    private suspend fun init() {
        initDependencyInjection()

        initConfigManager()

        initRoutes()
    }

    private fun initDependencyInjection() {
        logger.info("Initializing dependency injection")

        SpringConfig.setDefaults(vertx, logger)

        applicationContext = AnnotationConfigApplicationContext(SpringConfig::class.java)
    }

    private suspend fun initConfigManager() {
        logger.info("Initializing config manager")


        configManager = applicationContext.getBean(ConfigManager::class.java)

        try {
            configManager.init()
        } catch (e: Exception) {
            println(e)
        }
    }

    private fun initRoutes() {
        logger.info("Initializing routes")

        try {
            router = applicationContext.getBean(Router::class.java)
        }catch (e: Exception) {
            logger.error(e.toString())
        }
    }

    private fun startWebServer() {
        logger.info("Creating HTTP server")

        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(PORT) { result ->
                if (result.succeeded()) {
                    logger.info("Started listening on port $PORT, ready to rock & roll! (${TimeUtil.getStartupTime()}s)")
                } else {
                    logger.error("Failed to listen on port $PORT, reason: " + result.cause().toString())
                }
            }
    }
}