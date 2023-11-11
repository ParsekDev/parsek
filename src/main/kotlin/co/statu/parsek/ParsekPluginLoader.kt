package co.statu.parsek

import org.pf4j.JarPluginLoader
import org.pf4j.PluginClassLoader
import org.pf4j.PluginDescriptor
import org.pf4j.PluginManager
import java.nio.file.Path

class ParsekPluginLoader(pluginManager: PluginManager) : JarPluginLoader(pluginManager) {
    companion object {
        var pluginClassLoader: PluginClassLoader? = null
    }

    override fun loadPlugin(pluginPath: Path, pluginDescriptor: PluginDescriptor): ClassLoader {
        if (pluginClassLoader == null) {
            pluginClassLoader = PluginClassLoader(pluginManager, pluginDescriptor, javaClass.classLoader)
        }

        pluginClassLoader!!.addFile(pluginPath.toFile())

        return pluginClassLoader!!
    }
}