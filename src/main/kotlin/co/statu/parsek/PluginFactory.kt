package co.statu.parsek

import co.statu.parsek.api.ParsekPlugin
import co.statu.parsek.api.PluginContext
import org.pf4j.DefaultPluginFactory
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import org.slf4j.LoggerFactory

internal class PluginFactory : DefaultPluginFactory() {
    companion object {
        private val logger = LoggerFactory.getLogger(PluginFactory::class.java)
    }

    override fun createInstance(pluginClass: Class<*>, pluginWrapper: PluginWrapper): Plugin? {
        val context = PluginContext(
            pluginId = pluginWrapper.pluginId
        )

        try {
            val constructor = pluginClass.getConstructor(PluginContext::class.java)

            return constructor.newInstance(context) as ParsekPlugin
        } catch (e: Exception) {
            logger.error(e.message, e)
        }

        return null
    }
}