package co.statu.parsek.config.migration

import co.statu.parsek.annotation.Migration
import co.statu.parsek.config.ConfigMigration
import io.vertx.core.json.JsonObject

@Migration
class ConfigMigration4To5 : ConfigMigration(4, 5, "Add rate-limit configuration") {
    override fun migrate(config: JsonObject) {
        if (!config.containsKey("rate-limit")) {
            config.put(
                "rate-limit", JsonObject()
                    .put("enabled", true)
                    .put(
                        "tiers", JsonObject()
                            .put(
                                "DEFAULT", JsonObject()
                                    .put("max-requests", 300)
                                    .put("refill-ms", 200)
                            )
                    )
            )
        }
    }
}
