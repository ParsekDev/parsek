package co.statu.parsek

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
        return CompoundPluginDescriptorFinder() // Demo is using the Manifest file
            // PropertiesPluginDescriptorFinder is commented out just to avoid error log
            //.add(PropertiesPluginDescriptorFinder())
            .add(ManifestPluginDescriptorFinder())
    }

    override fun createPluginFactory(): PluginFactory {
        return PluginFactory()
    }

    override fun createPluginLoader(): PluginLoader {
        return CompoundPluginLoader()
            .add(ParsekPluginLoader(this)) { this.isNotDevelopment }
    }
}