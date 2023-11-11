package co.statu.parsek.api

import co.statu.parsek.PluginEventManager
import io.vertx.core.Vertx

class PluginContext(
    val pluginId: String,
    val vertx: Vertx,
    val pluginEventManager: PluginEventManager
)