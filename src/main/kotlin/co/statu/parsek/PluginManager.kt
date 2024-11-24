package co.statu.parsek

import co.statu.parsek.api.ParsekPlugin
import kotlinx.coroutines.runBlocking
import org.pf4j.*
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.nio.file.Path

class PluginManager(importPaths: List<Path>) : DefaultPluginManager(importPaths) {
    companion object {
        internal val pluginEventManager = PluginEventManager()

        internal val pluginGlobalBeanContext by lazy {
            val pluginGlobalBeanContext = AnnotationConfigApplicationContext()

            pluginGlobalBeanContext.setAllowBeanDefinitionOverriding(true)

            pluginGlobalBeanContext.beanFactory.registerSingleton(SpringConfig.vertx.javaClass.name, SpringConfig.vertx)

            pluginGlobalBeanContext.refresh()

            pluginGlobalBeanContext
        }
    }

    override fun createPluginDescriptorFinder(): CompoundPluginDescriptorFinder {
        return CompoundPluginDescriptorFinder()
            .add(ParsekManifestPluginDescriptorFinder())
    }

    override fun createPluginFactory(): PluginFactory {
        return PluginFactory()
    }

    override fun createPluginLoader(): PluginLoader {
        return CompoundPluginLoader()
            .add(ParsekPluginLoader(this)) { this.isNotDevelopment }
    }

    fun getActivePlugins(): List<ParsekPlugin> = getPlugins(PluginState.STARTED).mapNotNull { plugin ->
        runCatching {
            val pluginWrapper = plugin as ParsekPluginWrapper
            pluginWrapper.plugin as ParsekPlugin
        }.getOrNull()
    }

    fun getPluginWrappers() = plugins.values.map { it as ParsekPluginWrapper }

    override fun createPluginWrapper(
        pluginDescriptor: PluginDescriptor,
        pluginPath: Path,
        pluginClassLoader: ClassLoader
    ): PluginWrapper {
        val pluginWrapper = ParsekPluginWrapper(this, pluginDescriptor, pluginPath, pluginClassLoader)

        pluginWrapper.setPluginFactory(getPluginFactory())

        return pluginWrapper
    }


    override fun enablePlugin(pluginId: String): Boolean {
        val result = super.enablePlugin(pluginId)

        val plugin = getPlugin(pluginId)?.plugin as ParsekPlugin?

        if (result) {
            plugin?.let {
                runBlocking {
                    it.load()
                    it.onEnable()
                    it.onStart()
                }
            }
        }

        return result
    }

    override fun disablePlugin(pluginId: String): Boolean {
        val plugin = getPlugin(pluginId).plugin as ParsekPlugin

        runBlocking {
            plugin.onStop()
            plugin.onDisable()
            plugin.unload()
        }

        return super.disablePlugin(pluginId)
    }
}