package co.statu.parsek

import co.statu.parsek.api.ParsekEvent
import co.statu.parsek.api.ParsekPlugin
import co.statu.parsek.api.PluginEvent

class PluginEventManager {
    @PublishedApi
    internal val pluginEventListeners = mutableMapOf<ParsekPlugin, MutableList<PluginEvent>>()

    companion object {
        internal val parsekEventListeners = mutableMapOf<ParsekPlugin, MutableList<ParsekEvent>>()

        internal inline fun <reified T : ParsekEvent> getEventHandlers() =
            parsekEventListeners.flatMap { it.value }.filterIsInstance<T>()
    }

    private fun initializePluginIfNot(plugin: ParsekPlugin) {
        if (pluginEventListeners[plugin] == null) {
            pluginEventListeners[plugin] = mutableListOf()
        }

        if (parsekEventListeners[plugin] == null) {
            parsekEventListeners[plugin] = mutableListOf()
        }
    }

    fun register(plugin: ParsekPlugin, pluginEvent: PluginEvent) {
        initializePluginIfNot(plugin)

        pluginEventListeners[plugin]!!.add(pluginEvent)
    }

    fun register(plugin: ParsekPlugin, parsekEvent: ParsekEvent) {
        initializePluginIfNot(plugin)

        parsekEventListeners[plugin]!!.add(parsekEvent)
    }

    fun unregister(plugin: ParsekPlugin, pluginEvent: PluginEvent) {
        pluginEventListeners[plugin]?.remove(pluginEvent)
    }

    fun unregister(plugin: ParsekPlugin, parsekEvent: ParsekEvent) {
        parsekEventListeners[plugin]?.remove(parsekEvent)
    }

    inline fun <reified T : PluginEvent> getEventHandlers() =
        pluginEventListeners.flatMap { it.value }.filterIsInstance<T>()
}