/*
 * Copyright (c) 2024 Ruxy
 * Released under the MIT license
 * https://opensource.org/license/mit
 */

package net.rk4z.beacon

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Singleton object that acts as a central hub for event handling within the application.
 * It allows for the registration and unregistration of event hooks, as well as posting events
 * to be handled by registered hooks.
 * Supports both synchronous and asynchronous event handling.
 */
@Suppress("LoggingSimilarMessage", "UNCHECKED_CAST")
object EventBus {
    private val logger: Logger = LoggerFactory.getLogger(EventBus::class.java)

    // Registry of event hooks, mapped by event class to a thread-safe list of hooks.
    private val registry: MutableMap<Class<out Event>, CopyOnWriteArrayList<EventHook<in Event>>> = mutableMapOf()

    // Executor service for handling asynchronous event processing.
    private lateinit var asyncExecutor: ScheduledExecutorService

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

    /**
     * Posts an event to be handled synchronously by all registered hooks for the event's class.
     * Events are handled in order of priority, and handling can be short-circuited by cancellable events.
     * @param T The type of the event.
     * @param event The event to post.
     * @return The event after processing.
     */
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

    /**
     * Posts an event to be handled asynchronously by all registered hooks for the event's class.
     * Similar to the synchronous post-method, but event handling is performed on a separate thread.
     * @param T The type of the event.
     * @param event The event to post asynchronously.
     * @return The event after processing.
     */
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

    /**
     * Posts an event to be handled synchronously by all registered hooks for the event's class.
     * Similar to the post-method, but ensures all handlers are executed on the calling thread.
     * @param T The type of the event.
     * @param event The event to post synchronously.
     * @return The event after processing.
     */
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

    /**
     * Posts multiple events in parallel by all registered hooks for each event's class.
     * @param T The type of the events.
     * @param events The events to post in parallel.
     * @return The events after processing.
     */
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
     * Posts an event to be handled after a specified delay by all registered hooks for the event's class.
     * @param T The type of the event.
     * @param event The event to post.
     * @param delay The delay after which the event should be handled.
     * @param timeUnit The time unit of the delay.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> postDelayed(event: T, delay: Long, timeUnit: TimeUnit): T {
        logger.debug("Calling event with delay: ${event::class.simpleName}")
        val target = registry[event::class.java] ?: return event

        asyncExecutor.schedule({
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
                }

                runCatching {
                    eventHook.handler(event)
                    logger.debug("Handled event with delay: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                }.onFailure {
                    logger.error("Exception while executing handler: ${it.message}", it)
                }
            }
        }, delay, timeUnit)

        return event
    }

    /**
     * Posts an event to be handled within a specified timeout by all registered hooks for the event's class.
     * @param T The type of the event.
     * @param event The event to post.
     * @param timeout The timeout within which the event should be handled.
     * @param timeUnit The time unit of the timeout.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> postWithTimeout(event: T, timeout: Long, timeUnit: TimeUnit): T {
        logger.debug("Calling event with timeout: ${event::class.simpleName}")
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
            }

            val future = asyncExecutor.submit { eventHook.handler(event) }

            runCatching {
                future.get(timeout, timeUnit)
                logger.debug("Handled event with timeout: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
            }.onFailure {
                logger.error("Exception while executing handler: ${it.message}", it)
            }
        }

        return event
    }

    /**
     * Posts an event multiple times in sequence by all registered hooks for the event's class.
     * @param T The type of the event.
     * @param event The event to post.
     * @param repeatCount The number of times to repeat the event.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> postRepeated(event: T, repeatCount: Int): T {
        logger.debug("Calling repeated event: ${event::class.simpleName}")
        val target = registry[event::class.java] ?: return event

        for (i in 1..repeatCount) {
            logger.debug("Repeat count: $i of $repeatCount")
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
                }

                runCatching {
                    eventHook.handler(event)
                    logger.debug("Handled repeated event: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                }.onFailure {
                    logger.error("Exception while executing handler: ${it.message}", it)
                }
            }
        }

        return event
    }

    /**
     * Posts an event to be handled asynchronously, with a callback executed upon completion.
     * @param T The type of the event.
     * @param event The event to post.
     * @param callback The callback to execute upon completion.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> postWithCallback(event: T, callback: (T) -> Unit): T {
        logger.debug("Calling event with callback: ${event::class.simpleName}")
        val target = registry[event::class.java] ?: return event

        asyncExecutor.submit {
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
                }

                runCatching {
                    eventHook.handler(event)
                    logger.debug("Handled event with callback: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                }.onFailure {
                    logger.error("Exception while executing handler: ${it.message}", it)
                }
            }
            callback(event)
        }

        return event
    }

    /**
     * Posts multiple events in the specified order.
     * @param T The type of the events.
     * @param events The events to post in order.
     * @return The events after processing.
     */
    @JvmStatic
    fun <T : Event> postInOrder(vararg events: T): List<T> {
        logger.debug("Calling events in order")
        val processedEvents = mutableListOf<T>()

        for (event in events) {
            val processedEvent = post(event)
            processedEvents.add(processedEvent)
        }

        return processedEvents
    }

    /**
     * Initializes the EventBus, setting up the asynchronous executor service.
     * Also registers a shutdown hook to cleanly shut down the executor service on application exit.
     */
    @JvmStatic
    fun initialize() {
        asyncExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
        Runtime.getRuntime().addShutdownHook(Thread {
            // Cleanly shut down the executor service on application exit
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
