@file:Suppress("UNCHECKED_CAST", "unused")

package net.rk4z.beacon

import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

object EventBus {
    private val registry: MutableMap<KClass<out Event>, CopyOnWriteArrayList<EventHook<in Event>>> = mutableMapOf()
    private lateinit var asyncExecutor: ExecutorService
    private val logger: Logger = LoggerFactory.getLogger(EventBus::class.java)
    val listeners: MutableList<Listener> = mutableListOf()

    fun <T: Event> registerEventHook(eventClass: KClass<T>, eventHook: EventHook<T>) {
        val handlers = registry.getOrPut(eventClass) { CopyOnWriteArrayList() }

        val hook = eventHook as EventHook<in Event>

        if (!handlers.contains(hook)) {
            handlers.add(hook)
            handlers.sortByDescending { it.priority.value }
            logger.info("Registered event hook for ${eventClass.simpleName} with priority ${hook.priority}")
        }
    }

    fun <T: Event> unregisterEventHook(eventClass: KClass<T>, eventHook: EventHook<T>) {
        registry[eventClass]?.remove(eventHook as EventHook<in Event>)
    }

    @JvmStatic
    fun <T: Event> callEvent(event: T): T {
        logger.debug("Calling event: ${event::class.simpleName}")
        val target = registry[event::class] ?: return event

        for (eventHook in target) {
            if (event is CancellableEvent && event.isCancelled) {
                break
            }

            if (!eventHook.ignoresCondition && !eventHook.handlerClass.handleEvents()) {
                continue
            }

            if (eventHook.condition?.invoke() == false) {
                continue
            }

            runCatching {
                eventHook.handler(event)
                logger.debug("Handled event: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
            }.onFailure {
                logger.error("Exception while executing handler: ${it.message}", it)
            }
        }

        return event
    }

    @JvmStatic
    fun <T: Event> callEventAsync(event: T): T {
        logger.debug("Calling event asynchronously: ${event::class.simpleName}")
        val target = registry[event::class] ?: return event

        for (eventHook in target) {
            if (event is CancellableEvent && event.isCancelled) {
                break
            }

            if (!eventHook.ignoresCondition && !eventHook.handlerClass.handleEvents()) {
                continue
            }

            if (eventHook.condition?.invoke() == false) {
                continue
            }

            asyncExecutor.submit {
                runCatching {
                    eventHook.handler(event)
                    logger.debug("Handled event asynchronously: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                }.onFailure {
                    logger.error("Exception while executing handler asynchronously: ${it.message}", it)
                }
            }
        }
        return event
    }

    private fun registerListener(listener: Listener) {
        listeners.add(listener)
    }


    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
    }

    @JvmStatic
    fun registerAllListeners(packageName: String) {
        val reflections = Reflections(packageName)
        val listenerClasses = reflections.getSubTypesOf(Listener::class.java)

        listenerClasses.forEach { listenerClass ->
            try {
                val kClass = listenerClass.kotlin
                val primaryConstructor = kClass.primaryConstructor
                val instance = if (primaryConstructor != null) {
                    primaryConstructor.call()
                } else {
                    kClass.objectInstance ?: kClass.createInstance()
                }
                registerListener(instance as Listener)
                logger.info("Registered listener: ${kClass.simpleName}")
            } catch (e: Exception) {
                logger.error("Failed to register listener: ${listenerClass.name}", e)
            }
        }
    }

    fun initialize() {
        asyncExecutor = Executors.newCachedThreadPool()
        logger.info("EventManager initialized")
    }

    @JvmStatic
    fun shutdown() {
        asyncExecutor.shutdown()
        logger.info("EventManager shutdown")
    }
}