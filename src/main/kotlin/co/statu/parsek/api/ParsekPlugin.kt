package co.statu.parsek.api

import co.statu.parsek.Main
import co.statu.parsek.PluginEventManager
import co.statu.parsek.ReleaseStage
import co.statu.parsek.api.event.PluginEventListener
import io.vertx.core.Vertx
import kotlinx.coroutines.runBlocking
import org.pf4j.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.AnnotationConfigApplicationContext

abstract class ParsekPlugin : Plugin() {
    lateinit var pluginId: String
        internal set
    lateinit var vertx: Vertx
        internal set
    lateinit var pluginEventManager: PluginEventManager
        internal set
    lateinit var environmentType: Main.Companion.EnvironmentType
        internal set
    lateinit var releaseStage: ReleaseStage
        internal set
    lateinit var pluginBeanContext: AnnotationConfigApplicationContext
        internal set

    lateinit var pluginGlobalBeanContext: AnnotationConfigApplicationContext
        internal set

    internal lateinit var applicationContext: AnnotationConfigApplicationContext

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val registeredBeans = mutableListOf<Any>()

    fun registerSingletonGlobal(bean: Any) {
        if (registeredBeans.contains(bean)) {
            return
        }

        pluginGlobalBeanContext.beanFactory.registerSingleton(bean.javaClass.name, bean)

        registeredBeans.add(bean)
    }

    fun register(eventListener: PluginEventListener) {
        pluginEventManager.register(this, eventListener)
    }

    fun unRegisterGlobal(bean: Any) {
        if (!registeredBeans.contains(bean)) {
            return
        }

        val registry = pluginGlobalBeanContext.beanFactory as BeanDefinitionRegistry

        registry.removeBeanDefinition(bean.javaClass.name)

        registeredBeans.remove(bean)
    }

    fun unRegister(eventListener: PluginEventListener) {
        pluginEventManager.unRegister(this, eventListener)
    }

    @Deprecated("Use onEnable method.")
    override fun start() {
        runBlocking {
            onEnable()
        }
    }

    @Deprecated("Use onDisable method.")
    override fun stop() {
        pluginBeanContext.close()

        val copyOfRegisteredBeans = registeredBeans.toList()

        copyOfRegisteredBeans.forEach {
            unRegisterGlobal(it)
        }

        pluginEventManager.unregisterPlugin(this)

        runBlocking {
            onDisable()
        }
    }

    open suspend fun onLoad() {}

    open suspend fun onEnable() {}
    open suspend fun onDisable() {}
}