@file:Suppress("UNCHECKED_CAST", "unused")

package net.rk4z.beacon

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Singleton object that acts as a central hub for event handling within the application.
 * It allows for the registration and unregistration of event hooks, as well as posting events
 * to be handled by registered hooks.
 * Supports both synchronous and asynchronous event handling.
 */
@Suppress("LoggingSimilarMessage")
object EventBus {
    private val logger: Logger = LoggerFactory.getLogger(EventBus::class.java)
    // Registry of event hooks, mapped by event class to a thread-safe list of hooks.
    private val registry: MutableMap<Class<out Event>, CopyOnWriteArrayList<EventHook<in Event>>> = mutableMapOf()
    // Executor service for handling asynchronous event processing.
    private lateinit var asyncExecutor: ExecutorService

    /**
     * Registers an event hook for a specific event class.
     * If the hook is not already registered, it is added to the registry and sorted by priority.
     * @param T The type of the event.
     * @param eventClass The class of the event to register the hook for.
     * @param eventHook The event hook to register.
     */
    @JvmStatic
    fun <T : Event> registerEventHook(eventClass: Class<T>, eventHook: EventHook<T>) {
        val handlers = registry.getOrPut(eventClass) { CopyOnWriteArrayList() }

        val hook = eventHook as EventHook<in Event>

        if (!handlers.contains(hook)) {
            handlers.add(hook)
            handlers.sortBy { it.priority.v }
            logger.info("Registered event hook for ${eventClass.simpleName} with priority ${eventHook.priority}")
        }
    }

    /**
     * Unregisters an event hook for a specific event class.
     * @param T The type of the event.
     * @param eventClass The class of the event to unregister the hook for.
     * @param eventHook The event hook to unregister.
     */
    @JvmStatic
    fun <T : Event> unregisterEventHook(eventClass: Class<T>, eventHook: EventHook<T>) {
        registry[eventClass]?.remove(eventHook as EventHook<in Event>)
    }

    @JvmStatic
    fun <T : Event> post(event: T): T {
        logger.debug("Calling event: ${event::class.simpleName}")
        val target = registry[event::class.java] ?: return event

        for (eventHook in target) {
            if (event is CancellableEvent && event.isCancelled) {
                logger.debug("Event ${event::class.simpleName} is cancelled")
                break
            }

            if (!eventHook.ignoresCondition && !eventHook.handlerClass.handleEvents()) {
                continue
            }

            if (eventHook.condition?.invoke() == false) {
                continue
            } else {
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
        }
        return event
    }

    @JvmStatic
    fun <T : Event> postAsync(event: T): T {
        logger.debug("Calling event asynchronously: ${event::class.simpleName}")
        val target = registry[event::class.java] ?: return event

        for (eventHook in target) {
            if (event is CancellableEvent && event.isCancelled) {
                logger.debug("Event ${event::class.simpleName} is cancelled")
                break
            }

            if (!eventHook.ignoresCondition && !eventHook.handlerClass.handleEvents()) {
                continue
            }

            if (eventHook.condition?.invoke() == false) {
                continue
            } else {
                asyncExecutor.submit {
                    runCatching {
                        eventHook.handler(event)
                        logger.debug("Handled event asynchronously: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                    }.onFailure {
                        logger.error("Exception while executing handler asynchronously: ${it.message}", it)
                    }
                }
            }
        }
        return event
    }

    @JvmStatic
    fun <T : Event> postSynchronous(event: T): T {
        logger.debug("Calling event synchronously: ${event::class.simpleName}")
        val target = registry[event::class.java] ?: return event

        for (eventHook in target) {
            if (event is CancellableEvent && event.isCancelled) {
                logger.debug("Event ${event::class.simpleName} is cancelled")
                break
            }

            if (!eventHook.ignoresCondition && !eventHook.handlerClass.handleEvents()) {
                continue
            }

            if (eventHook.condition?.invoke() == false) {
                continue
            } else {
                runCatching {
                    eventHook.handler(event)
                    logger.debug("Handled event synchronously: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                }.onFailure {
                    logger.error("Exception while executing handler: ${it.message}", it)
                }
            }
        }
        return event
    }

    @JvmStatic
    fun <T : Event> postParallel(vararg events: T): List<T> {
        logger.debug("Calling parallel events")
        val futures = events.map { event ->
            asyncExecutor.submit<T> {
                post(event)
            }
        }

        return futures.map { future ->
            try {
                future.get()
            } catch (e: Exception) {
                logger.error("Exception while executing parallel event: ${e.message}", e)
                throw e
            }
        }
    }

    /**
     * Initializes the EventBus, setting up the asynchronous executor service.
     * Also registers a shutdown hook to cleanly shut down the executor service on application exit.
     */
    @JvmStatic
    fun initialize() {
        asyncExecutor = Executors.newCachedThreadPool()
        Runtime.getRuntime().addShutdownHook(Thread {
            shutdown()
        })
        logger.info("EventBus initialized")
    }

    /**
     * Shuts down the EventBus, terminating the executor service and clearing the event hook registry.
     * Waits for a specified time for all tasks to complete before forcing shutdown.
     */
    @JvmStatic
    fun shutdown() {
        asyncExecutor.shutdown()
        asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)
        registry.clear()
        logger.info("EventBus shutdown")
    }
}