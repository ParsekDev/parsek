package co.statu.parsek.config.migration

import co.statu.parsek.annotation.Migration
import co.statu.parsek.config.ConfigMigration
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

@Migration
class ConfigMigration3To4 : ConfigMigration(3, 4, "Add CORS configuration to router section") {
    override fun migrate(config: JsonObject) {
        val router = config.getJsonObject("router") ?: JsonObject().also { config.put("router", it) }

        if (!router.containsKey("allowed-hosts")) {
            router.put("allowed-hosts", JsonArray(listOf("localhost", "127.0.0.1", "0.0.0.0")))
        }

        if (!router.containsKey("allowed-schemes")) {
            router.put("allowed-schemes", JsonArray(listOf("http", "https")))
        }

        if (!router.containsKey("allowed-headers")) {
            router.put(
                "allowed-headers", JsonArray(
                    listOf(
                        "x-requested-with",
                        "Access-Control-Allow-Origin",
                        "origin",
                        "Content-Type",
                        "accept",
                        "X-PINGARUNER",
                        "x-csrf-token"
                    )
                )
            )
        }

        if (!router.containsKey("allowed-methods")) {
            router.put(
                "allowed-methods", JsonArray(
                    listOf(
                        "GET", "POST", "OPTIONS", "DELETE", "PATCH", "PUT"
                    )
                )
            )
        }
    }
}
