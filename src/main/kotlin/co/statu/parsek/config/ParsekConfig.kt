package co.statu.parsek.config

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.vertx.core.json.JsonObject

data class ParsekConfig(
    @SerializedName("config-version") var version: Int,
    var router: RouterConfig = RouterConfig(),
    var server: ServerConfig = ServerConfig()
) {
    companion object {
        data class RouterConfig(
            @SerializedName("api-prefix") var apiPrefix: String = "/api",
            @SerializedName("allowed-hosts") var allowedHosts: Set<String> = setOf("localhost", "127.0.0.1", "0.0.0.0"),
            @SerializedName("allowed-schemes") var allowedSchemes: Set<String> = setOf("http", "https"),
            @SerializedName("allowed-headers") var allowedHeaders: Set<String> = setOf(
                "x-requested-with",
                "Access-Control-Allow-Origin",
                "origin",
                "Content-Type",
                "accept",
                "X-PINGARUNER",
                "x-csrf-token"
            ),
            @SerializedName("allowed-methods") var allowedMethods: Set<String> = setOf(
                "GET", "POST", "OPTIONS", "DELETE", "PATCH", "PUT"
            )
        )

        data class ServerConfig(
            var host: String = "0.0.0.0",
            var port: Int = 8088
        )

        private val gson = GsonBuilder().create()

        fun from(jsonObject: JsonObject): ParsekConfig = gson.fromJson(jsonObject.encode(), ParsekConfig::class.java)
    }

    override fun toString(): String = gson.toJson(this)
}
