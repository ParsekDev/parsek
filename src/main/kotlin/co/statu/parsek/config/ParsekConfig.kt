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
            @SerializedName("api-prefix") var apiPrefix: String = "/api"
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
