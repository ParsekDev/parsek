package co.statu.parsek

import org.pf4j.DefaultPluginDescriptor

class ParsekPluginDescriptor : DefaultPluginDescriptor() {
    lateinit var name: String
    var description: String? = null
    var parsekVersion: String? = null
    lateinit var developer: String
    var sourceUrl: String? = null

    @Deprecated("Do not use", level = DeprecationLevel.HIDDEN)
    override fun getProvider(): String? {
        return super.getProvider()
    }

    @Deprecated("Do not use", level = DeprecationLevel.HIDDEN)
    override fun getPluginDescription(): String? {
        return super.getPluginDescription()
    }


    public override fun setPluginId(pluginId: String): DefaultPluginDescriptor {
        return super.setPluginId(pluginId)
    }

    public override fun setPluginClass(pluginClassName: String?): DefaultPluginDescriptor {
        return super.setPluginClass(pluginClassName)
    }

    public override fun setPluginVersion(version: String?): DefaultPluginDescriptor {
        return super.setPluginVersion(version)
    }

    public override fun setDependencies(dependencies: String?): DefaultPluginDescriptor {
        return super.setDependencies(dependencies)
    }

    public override fun setRequires(requires: String?): DefaultPluginDescriptor {
        return super.setRequires(requires)
    }
}