package co.statu.parsek.config.migration

import co.statu.parsek.annotation.Migration
import co.statu.parsek.config.ConfigManager
import co.statu.parsek.config.ConfigMigration
import io.vertx.core.json.JsonObject

@Migration
class ConfigMigration1To2(
    override val FROM_VERSION: Int = 1,
    override val VERSION: Int = 2,
    override val VERSION_INFO: String = "Add plugins config"
) : ConfigMigration() {
    override fun migrate(configManager: ConfigManager) {
        configManager.getConfig().put("plugins", JsonObject())
    }
}