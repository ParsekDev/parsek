package co.statu.parsek

import co.statu.parsek.api.ParsekPlugin
import co.statu.parsek.api.event.EventListener
import co.statu.parsek.api.event.ParsekEventListener
import co.statu.parsek.api.event.PluginEventListener
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class PluginEventManager {
    companion object {
        private val eventListeners = mutableMapOf<ParsekPlugin, MutableList<EventListener>>()

        fun getEventListeners() = eventListeners.toMap()

        internal inline fun <reified T : ParsekEventListener> getParsekEventListeners() =
            eventListeners.flatMap { it.value }.filterIsInstance<T>()


        inline fun <reified T : PluginEventListener> getEventListeners() =
            getEventListeners().flatMap { it.value }.filter { it !is ParsekEventListener }.filterIsInstance<T>()
    }

    internal fun initializePlugin(plugin: ParsekPlugin, pluginBeanContext: AnnotationConfigApplicationContext) {
        if (eventListeners[plugin] == null) {
            eventListeners[plugin] = pluginBeanContext
                .getBeansWithAnnotation(co.statu.parsek.api.annotation.EventListener::class.java)
                .map { it.value as EventListener }
                .toMutableList()
        }
    }

    internal fun unregisterPlugin(plugin: ParsekPlugin) {
        eventListeners.remove(plugin)
    }

    fun register(plugin: ParsekPlugin, eventListener: EventListener) {
        if (eventListeners[plugin]!!.none { it::class == eventListener::class }) {
            eventListeners[plugin]!!.add(eventListener)
        }
    }

    fun unRegister(plugin: ParsekPlugin, eventListener: EventListener) {
        eventListeners[plugin]?.remove(eventListener)
    }
}