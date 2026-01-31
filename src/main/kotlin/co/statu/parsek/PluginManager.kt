package co.statu.parsek

import co.statu.parsek.api.ParsekPlugin
import co.statu.parsek.api.event.PluginLifecycleListener
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

        internal val lifecycleListeners = mutableSetOf<PluginLifecycleListener>()
    }

    fun addLifecycleListener(listener: PluginLifecycleListener) {
        lifecycleListeners.add(listener)
    }

    override fun createPluginRepository(): PluginRepository {
        return CompoundPluginRepository()
            .add(DevelopmentPluginRepository(getPluginsRoots())) { this.isDevelopment }
            .add(JarPluginRepository(getPluginsRoots())) { this.isNotDevelopment }
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
                try {
                    runBlocking {
                        it.load()

                        lifecycleListeners.forEach { listener ->
                            listener.onPluginEnable(it)
                        }

                        it.onEnable()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return result
    }
    override fun startPlugin(pluginId: String?): PluginState? {
        return super.startPlugin(pluginId)
    }

    override fun disablePlugin(pluginId: String): Boolean {
        val plugin = getPlugin(pluginId).plugin as ParsekPlugin

        runBlocking {
            lifecycleListeners.forEach { listener ->
                listener.onPluginDisable(plugin)
            }

            plugin.onDisable()
            plugin.unload()
        }

        return super.disablePlugin(pluginId)
    }
}