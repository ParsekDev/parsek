package co.statu.parsek.config.migration

import co.statu.parsek.annotation.Migration
import co.statu.parsek.config.ConfigMigration
import io.vertx.core.json.JsonObject

@Migration
class ConfigMigration1To2() : ConfigMigration(1, 2, "Add server config") {
    override fun migrate(config: JsonObject) {
        config.put(
            "server", mapOf(
                "host" to "0.0.0.0",
                "port" to 8088
            )
        )
    }
}