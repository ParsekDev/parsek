package co.statu.parsek.api.config

import io.vertx.core.json.JsonObject

abstract class PluginConfigMigration {
    abstract val FROM_VERSION: Int
    abstract val VERSION: Int
    abstract val VERSION_INFO: String

    fun isMigratable(version: Int) = version == FROM_VERSION

    abstract fun migrate(config: JsonObject)

    open fun migrateFully(config: JsonObject) {}
}