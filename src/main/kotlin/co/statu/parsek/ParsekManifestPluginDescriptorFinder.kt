package co.statu.parsek

import org.pf4j.ManifestPluginDescriptorFinder
import org.pf4j.PluginDescriptor
import org.pf4j.util.StringUtils
import java.util.jar.Manifest

class ParsekManifestPluginDescriptorFinder : ManifestPluginDescriptorFinder() {
    companion object {
        private const val PLUGIN_SOURCE_URL: String = "Plugin-Source-Url"
    }

    override fun createPluginDescriptorInstance(): ParsekPluginDescriptor {
        return ParsekPluginDescriptor()
    }

    override fun createPluginDescriptor(manifest: Manifest): PluginDescriptor {
        val pluginDescriptor = super.createPluginDescriptor(manifest) as ParsekPluginDescriptor

        val attributes = manifest.mainAttributes
        val sourceUrl = attributes.getValue(PLUGIN_SOURCE_URL)
        if (StringUtils.isNotNullOrEmpty(sourceUrl)) {
            pluginDescriptor.sourceUrl = sourceUrl
        }

        return pluginDescriptor
    }
}