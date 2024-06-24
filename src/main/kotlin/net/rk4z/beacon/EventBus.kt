@file:Suppress("UNCHECKED_CAST", "unused")

package net.rk4z.beacon

import net.rk4z.beacon.common.ListenerBase
import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

/**
 * Singleton object that manages the event system.
 * It provides methods to register and unregister event hooks, call events, and manage listeners.
 */
object EventBus {
    // Registry of event hooks, mapped by event class
    private val registry: MutableMap<Class<out Event>, CopyOnWriteArrayList<EventHook<in Event>>> = mutableMapOf()
    // Executor service for asynchronous event handling
    private lateinit var asyncExecutor: ExecutorService
    // Logger for this class
    private val logger: Logger = LoggerFactory.getLogger(EventBus::class.java)
    // List of registered listeners
    @JvmField
    val listeners: MutableList<ListenerBase> = mutableListOf()

    /**
     * Registers an event hook for a specific event class.
     * If the event hook is not already registered, it is added to the list of handlers for that event class.
     * The handlers are then sorted by priority.
     * @param eventClass The class of the event.
     * @param eventHook The event hook to register.
     */
    @JvmStatic
    fun <T: Event> registerEventHook(eventClass: Class<T>, eventHook: EventHook<T>) {
        val handlers = registry.getOrPut(eventClass) { CopyOnWriteArrayList() }

        val hook = eventHook as EventHook<in Event>

        if (!handlers.contains(hook)) {
            handlers.add(hook)
            handlers.sortByDescending { it.priority.value }
            logger.info("Registered event hook for ${eventClass.simpleName} with priority ${hook.priority}")
        }
    }

    /**
     * Unregisters an event hook for a specific event class.
     * @param eventClass The class of the event.
     * @param eventHook The event hook to unregister.
     */
    @JvmStatic
    fun <T: Event> unregisterEventHook(eventClass: Class<T>, eventHook: EventHook<T>) {
        registry[eventClass]?.remove(eventHook as EventHook<in Event>)
    }

    /**
     * Calls an event and handles it with all registered event hooks for its class.
     * If the event is cancellable and has been cancelled, no further event hooks are called.
     * @param event The event to call.
     * @return The event after it has been handled.
     */
    @JvmStatic
    fun <T: Event> callEvent(event: T): T {
        logger.debug("Calling event: ${event::class.simpleName}")
        EventLogger.logEvent(event)
        val target = registry[event::class.java] ?: return event

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
                val future = asyncExecutor.submit { eventHook.handler(event) }
                if (eventHook.timeout != null) {
                    future.get(eventHook.timeout, TimeUnit.MILLISECONDS)
                } else {
                    future.get()
                }
                logger.debug("Handled event: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
            }.onFailure {
                logger.error("Exception while executing handler: ${it.message}", it)
            }
        }

        return event
    }

    /**
     * Calls an event asynchronously and handles it with all registered event hooks for its class.
     * If the event is cancellable and has been cancelled, no further event hooks are called.
     * @param event The event to call.
     * @return The event after it has been handled.
     */
    @JvmStatic
    fun <T: Event> callEventAsync(event: T): T {
        logger.debug("Calling event asynchronously: ${event::class.simpleName}")
        val target = registry[event::class.java] ?: return event

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

    /**
     * Registers all listeners found in a specific package.
     * @param packageName The name of the package to search for listeners.
     */
    @JvmStatic
    fun registerAllListeners(packageName: String) {
        val reflections = Reflections(packageName)
        val listenerClasses = reflections.getSubTypesOf(ListenerBase::class.java)

        listenerClasses.forEach { listenerClass ->
            try {
                val xClass = listenerClass.kotlin
                val primaryConstructor = xClass.primaryConstructor
                val instance = if (primaryConstructor != null) {
                    primaryConstructor.call()
                } else {
                    xClass.objectInstance ?: xClass.createInstance()
                }
                registerListener(instance as ListenerBase)
                logger.info("Registered listener: ${xClass.simpleName}")
            } catch (e: Exception) {
                logger.error("Failed to register listener: ${listenerClass.name}", e)
            }
        }
    }

    /**
     * Registers a listener.
     * @param listener The listener to register.
     */
    @JvmStatic
    fun registerListener(listener: ListenerBase) {
        listeners.add(listener)
    }

    /**
     * Unregisters a listener.
     * @param listener The listener to unregister.
     */
    @JvmStatic
    fun unregisterListener(listener: ListenerBase) {
        listeners.remove(listener)
    }

    /**
     * Changes the priority of an event hook for a specific event class.
     * @param eventClass The class of the event.
     * @param eventHook The event hook whose priority to change.
     * @param newPriority The new priority for the event hook.
     */
    @JvmStatic
    fun <T: Event> changeEventHookPriority(eventClass: Class<T>, eventHook: EventHook<T>, newPriority: Priority) {
        val handlers = registry[eventClass] ?: return
        val hookToChange = handlers.find { it === eventHook as EventHook<in Event> }
        if (hookToChange != null) {
            hookToChange.priority = newPriority
            handlers.sortByDescending { it.priority.value }
            logger.debug("Changed priority for {} to {}", eventClass.simpleName, newPriority)
        }
    }

    /**
     * Initializes the EventBus, setting up the executor service for asynchronous event handling.
     */
    @JvmStatic
    fun initialize() {
        asyncExecutor = Executors.newCachedThreadPool()
        logger.info("EventManager initialized")
    }

    /**
     * Shuts down the EventBus, stopping the executor service.
     */
    @JvmStatic
    fun shutdown() {
        asyncExecutor.shutdown()
        logger.info("EventManager shutdown")
    }
}