package co.statu.parsek.api.event

import co.statu.parsek.api.ParsekPlugin


interface PluginLifecycleListener {
    suspend fun onPluginLoad(plugin: ParsekPlugin) {}
    suspend fun onPluginEnable(plugin: ParsekPlugin) {}
    suspend fun onPluginDisable(plugin: ParsekPlugin) {}
    suspend fun onPluginUnload(plugin: ParsekPlugin) {}
    suspend fun onPluginUninstall(plugin: ParsekPlugin) {}
}
