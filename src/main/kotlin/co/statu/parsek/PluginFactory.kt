package co.statu.parsek

import co.statu.parsek.PluginManager.Companion.pluginEventManager
import co.statu.parsek.SpringConfig.Companion.vertx
import co.statu.parsek.api.ParsekPlugin
import kotlinx.coroutines.runBlocking
import org.pf4j.DefaultPluginFactory
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class PluginFactory : DefaultPluginFactory() {
    companion object {
        private val logger = LoggerFactory.getLogger(PluginFactory::class.java)
    }

    override fun createInstance(pluginClass: Class<*>, pluginWrapper: PluginWrapper): Plugin? {
        try {
            val constructor = pluginClass.getConstructor()

            val plugin = constructor.newInstance() as ParsekPlugin

            plugin.pluginId = pluginWrapper.pluginId
            plugin.vertx = vertx
            plugin.pluginEventManager = pluginEventManager
            plugin.environmentType = Main.ENVIRONMENT
            plugin.releaseStage = Main.STAGE
            plugin.applicationContext = Main.applicationContext

            val pluginBeanContext by lazy {
                val pluginBeanContext = AnnotationConfigApplicationContext()

                pluginBeanContext.parent = Main.applicationContext
                pluginBeanContext.classLoader = pluginClass.classLoader
                pluginBeanContext.scan(pluginClass.`package`.name)

                pluginBeanContext.beanFactory.registerSingleton(logger.javaClass.name, logger)
                pluginBeanContext.beanFactory.registerSingleton(pluginEventManager.javaClass.name, pluginEventManager)
                pluginBeanContext.beanFactory.registerSingleton(plugin.javaClass.name, plugin)

                pluginBeanContext.refresh()

                pluginBeanContext
            }

            plugin.pluginBeanContext = pluginBeanContext

            pluginEventManager.initializePlugin(plugin, pluginBeanContext)

            runBlocking {
                plugin.onLoad()
            }

            return plugin
        } catch (e: Exception) {
            logger.error(e.message, e)
        }

        return null
    }
}