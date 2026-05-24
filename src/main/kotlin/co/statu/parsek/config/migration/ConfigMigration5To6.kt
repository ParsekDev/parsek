package co.statu.parsek.config.migration

import co.statu.parsek.annotation.Migration
import co.statu.parsek.config.ConfigMigration
import io.vertx.core.json.JsonObject

@Migration
class ConfigMigration5To6 : ConfigMigration(5, 6, "Add client-ip-header to rate-limit") {
    override fun migrate(config: JsonObject) {
        val rateLimit = config.getJsonObject("rate-limit") ?: JsonObject().also { config.put("rate-limit", it) }

        if (!rateLimit.containsKey("client-ip-header")) {
            rateLimit.put("client-ip-header", "CF-Connecting-IP")
        }
    }
}
