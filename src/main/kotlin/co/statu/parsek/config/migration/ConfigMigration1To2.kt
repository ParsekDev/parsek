package co.statu.parsek.config.migration

import co.statu.parsek.annotation.Migration
import co.statu.parsek.config.ConfigManager
import co.statu.parsek.config.ConfigMigration

@Migration
class ConfigMigration1To2(
    override val FROM_VERSION: Int = 1,
    override val VERSION: Int = 2,
    override val VERSION_INFO: String = "Add server config"
) : ConfigMigration() {
    override fun migrate(configManager: ConfigManager) {
        val config = configManager.getConfig()

        config.put(
            "server", mapOf(
                "host" to "0.0.0.0",
                "port" to 8088
            )
        )
    }
}