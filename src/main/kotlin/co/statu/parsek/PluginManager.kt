package co.statu.parsek

import org.pf4j.*
import java.nio.file.Path

class PluginManager(importPaths: List<Path>) : DefaultPluginManager(importPaths) {
    companion object {
        internal val pluginEventManager = PluginEventManager()
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