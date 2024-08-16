package net.rk4z.beacon

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST", "unused")
object EventBus {
    private val logger: Logger = LoggerFactory.getLogger(EventBus::class.java.simpleName)
    private val registry: MutableMap<Class<out Event>, CopyOnWriteArrayList<EventHook<in Event>>> = mutableMapOf()
    private val returnableRegistry: MutableMap<Class<out ReturnableEvent<*>>, MutableMap<String, ReturnableEventHook<out ReturnableEvent<*>, *>>> = mutableMapOf()
    private lateinit var asyncExecutor: ScheduledExecutorService

    /**
     * Registers an event hook for a specific event class.
     *
     * @param T The type of the event.
     * @param eventClass The class of the event to register the hook for.
     * @param eventHook The event hook to register.
     */
    @JvmStatic
    fun <T : Event> registerEventHook(eventClass: Class<T>, eventHook: EventHook<T>) {
        val handlers = registry.getOrPut(eventClass) { CopyOnWriteArrayList() }

        val hook = eventHook as EventHook<in Event>

        if (handlers.contains(hook).not()) {
            handlers.add(eventHook as EventHook<in Event>)
            handlers.sortBy { it.priority.v }
            logger.info("Registered event hook for ${eventClass.simpleName} with priority ${eventHook.priority}")
        }
    }


    /**
     * Registers a returnable event hook for a specific event class.
     *
     * @param T The type of the event.
     * @param R The type of the return value.
     * @param eventClass The class of the event to register the hook for.
     * @param eventHook The returnable event hook to register.
     */
    @JvmStatic
    fun <T : ReturnableEvent<R>, R> registerReturnableEventHook(eventClass: Class<T>, eventHook: ReturnableEventHook<T, R>) {
        val handlers = returnableRegistry.getOrPut(eventClass) { mutableMapOf() }

        val handlerKey = eventHook.handlerClass::class.simpleName + eventHook.priority.v

        if (handlers.containsKey(handlerKey).not()) {
            handlers[handlerKey] = eventHook as ReturnableEventHook<out ReturnableEvent<*>, *>
            handlers.values.sortedBy { it.priority.v }
            logger.info("Registered returnable event hook for ${eventClass.simpleName} with priority ${eventHook.priority}")
        }
    }

    /**
     * Processes an event based on the specified processing type.
     *
     * @param T The type of the event.
     * @param event The event to process.
     * @param processingType The type of processing (Sync, Async, Synchronous).
     * @param enableDebugLog Whether to enable debug logging.
     * @return The processed event.
     */
    @JvmStatic
    fun <T : Event> processEvent(event: T, processingType: EventProcessingType, enableDebugLog: Boolean? = false): T {
        if (enableDebugLog == true) {
            logger.info("Calling event: ${event::class.simpleName}")
        } else {
            logger.debug("Calling event: ${event::class.simpleName}")
        }

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

            when (processingType) {
                EventProcessingType.Sync -> {
                    val future = asyncExecutor.submit { eventHook.handler(event) }
                    if (eventHook.timeout != null) {
                        future.get(eventHook.timeout, TimeUnit.MILLISECONDS)
                    } else {
                        future.get()
                    }
                }
                EventProcessingType.Async -> {
                    asyncExecutor.submit {
                        runCatching {
                            eventHook.handler(event)
                        }.onFailure {
                            logger.error("Exception while executing handler: ${it.message}", it)
                        }
                    }
                }
                EventProcessingType.Synchronous -> {
                    runCatching {
                        eventHook.handler(event)
                    }.onFailure {
                        logger.error("Exception while executing handler: ${it.message}", it)
                    }
                }
            }

            if (enableDebugLog == true) {
                logger.info("Handled event: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
            } else {
                logger.debug("Handled event: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
            }
        }

        return event
    }

    /**
     * Posts an event synchronously.
     *
     * @param T The type of the event.
     * @param event The event to post.
     * @param enableDebugLog Whether to enable debug logging.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> post(event: T, enableDebugLog: Boolean? = false): T {
        return processEvent(event, EventProcessingType.Sync, enableDebugLog)
    }

    /**
     * Posts an event asynchronously.
     *
     * @param T The type of the event.
     * @param event The event to post.
     * @param enableDebugLog Whether to enable debug logging.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> postAsync(event: T, enableDebugLog: Boolean? = false): T {
        return processEvent(event, EventProcessingType.Async, enableDebugLog)
    }

    /**
     * Posts an event synchronously ensuring all handlers are executed on the calling thread.
     *
     * @param T The type of the event.
     * @param event The event to post.
     * @param enableDebugLog Whether to enable debug logging.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> postSynchronous(event: T, enableDebugLog: Boolean? = false): T {
        return processEvent(event, EventProcessingType.Synchronous, enableDebugLog)
    }

    /**
     * Posts an event to be handled after a specified delay by all registered hooks for the event's class.
     *
     * @param T The type of the event.
     * @param event The event to post.
     * @param delay The delay after which the event should be handled.
     * @param timeUnit The time unit of the delay.
     * @param processingType The type of processing (Sync, Async, Synchronous).
     * @param enableDebugLog Whether to enable debug logging.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> postDelayed(event: T, delay: Long, timeUnit: TimeUnit, processingType: EventProcessingType, enableDebugLog: Boolean? = false): T {
        asyncExecutor.schedule({
            processEvent(event, processingType, enableDebugLog)
        }, delay, timeUnit)
        return event
    }

    /**
     * Posts an event to be handled within a specified timeout by all registered hooks for the event's class.
     *
     * @param T The type of the event.
     * @param event The event to post.
     * @param timeout The timeout within which the event should be handled.
     * @param timeUnit The time unit of the timeout.
     * @param processingType The type of processing (Sync, Async, Synchronous).
     * @param enableDebugLog Whether to enable debug logging.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> postWithTimeout(event: T, timeout: Long, timeUnit: TimeUnit, processingType: EventProcessingType, enableDebugLog: Boolean? = false): T {
        val future = asyncExecutor.submit<T> {
            processEvent(event, processingType, enableDebugLog)
            event
        }

        runCatching {
            future.get(timeout, timeUnit)
        }.onFailure {
            logger.error("Exception while executing handler with timeout: ${it.message}", it)
        }

        return event
    }

    /**
     * Posts an event to be handled asynchronously, with a callback executed upon completion.
     *
     * @param T The type of the event.
     * @param event The event to post.
     * @param callback The callback to execute upon completion.
     * @param delay Optional delay before executing the callback.
     * @param processingType The type of processing (Sync, Async, Synchronous).
     * @param enableDebugLog Whether to enable debug logging.
     * @return The event after processing.
     */
    @JvmStatic
    fun <T : Event> postWithCallback(event: T, callback: (T) -> Unit, delay: Long?, processingType: EventProcessingType, enableDebugLog: Boolean? = false): T {
        if (delay != null) {
            asyncExecutor.schedule({
                val processedEvent = processEvent(event, processingType, enableDebugLog)
                callback(processedEvent)
            }, delay, TimeUnit.MILLISECONDS)
        } else {
            val processedEvent = processEvent(event, processingType, enableDebugLog)
            callback(processedEvent)
        }

        return event
    }

    /**
     * Posts an event to be handled synchronously by all registered hooks for the event's class and returns the result.
     *
     * @param T The type of the event.
     * @param R The type of the return value.
     * @param event The event to post.
     * @param processingType The type of processing (Sync, Async, Synchronous).
     * @param enableDebugLog Whether to enable debug logging.
     * @return The result of the event after processing.
     */
    @JvmStatic
    fun <T : ReturnableEvent<R>, R> postReturnable(event: T, processingType: EventProcessingType, enableDebugLog: Boolean? = false): R? {
        if (enableDebugLog == true) {
            logger.info("Calling returnable event: ${event::class.simpleName}")
        } else {
            logger.debug("Calling returnable event: ${event::class.simpleName}")
        }
        val handlersMap = returnableRegistry[event::class.java] ?: return null
        val target = handlersMap.values

        for (eventHook in target) {
            if (!eventHook.ignoresCondition && !eventHook.handlerClass.handleEvents()) {
                continue
            }

            if (eventHook.condition?.invoke() == false) {
                continue
            } else {
                when (processingType) {
                    EventProcessingType.Sync -> {
                        val future = asyncExecutor.submit<R> { (eventHook as ReturnableEventHook<T, R>).handler(event) }
                        runCatching {
                            val result = if (eventHook.timeout != null) {
                                future.get(eventHook.timeout, TimeUnit.MILLISECONDS)
                            } else {
                                future.get()
                            }
                            event.setResult(result)
                        }.onFailure {
                            logger.error("Exception while executing handler: ${it.message}", it)
                        }
                    }
                    EventProcessingType.Async -> {
                        asyncExecutor.submit {
                            runCatching {
                                val result = (eventHook as ReturnableEventHook<T, R>).handler(event)
                                event.setResult(result)
                            }.onFailure {
                                logger.error("Exception while executing handler: ${it.message}", it)
                            }
                        }
                    }
                    EventProcessingType.Synchronous -> {
                        runCatching {
                            val result = (eventHook as ReturnableEventHook<T, R>).handler(event)
                            event.setResult(result)
                        }.onFailure {
                            logger.error("Exception while executing handler: ${it.message}", it)
                        }
                    }
                }

                if (enableDebugLog == true) {
                    logger.info("Handled returnable event: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                } else {
                    logger.debug("Handled returnable event: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                }
            }
        }
        return event.result
    }

    /**
     * Initializes the EventBus, setting up the asynchronous executor service.
     * Also registers a shutdown hook to cleanly shut down the executor service on application exit.
     */
    @JvmStatic
    fun initialize(packageName: String) {
        asyncExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
        Runtime.getRuntime().addShutdownHook(Thread {
            shutdown()
        })
        initializeEventHandlers(packageName)
        logger.info("EventBus initialized")
    }

    private fun initializeEventHandlers(packageName: String) {
        try {
            val reflections = Reflections(
                ConfigurationBuilder()
                    .forPackage(packageName)
                    .addScanners(Scanners.SubTypes)
            )

            val subTypes = reflections.getSubTypesOf(IEventHandler::class.java)

            for (subType in subTypes) {
                try {
                    val kClass = subType.kotlin
                    if (kClass.isSubclassOf(IEventHandler::class)) {
                        kClass.createInstance()
                    }
                } catch (e: Exception) {
                    logger.error("Failed to initialize event handler: ${subType.name}", e)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to scan package: $packageName", e)
        }
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
