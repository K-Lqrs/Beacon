package net.rk4z.beacon

import org.reflections.Reflections
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createInstance

@Suppress("unused", "UNCHECKED_CAST", "MemberVisibilityCanBePrivate", "LoggingSimilarMessage")
object EventBus {
    private val logger: Logger = LoggerFactory.getLogger(EventBus::class.java)
    private val registry: MutableMap<Class<out Event>, CopyOnWriteArrayList<EventHook<in Event>>> = mutableMapOf()
    private lateinit var asyncExecutor: ExecutorService
    @JvmField
    val listeners: MutableList<Listener> = mutableListOf()

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

    @JvmStatic
    fun <T : Event> unregisterEventHook(eventClass: Class<T>, eventHook: EventHook<T>) {
        registry[eventClass]?.remove(eventHook as EventHook<in Event>)
    }

    @JvmStatic
    private fun registerAllListeners(packageName: String?) {
        if (packageName == null) {
            logger.warn("Package name is null, cannot register listeners")
            return
        }

        val reflection = Reflections(packageName)
        val listenerClasses = reflection.getSubTypesOf(Listener::class.java)

        for (listenerClass in listenerClasses) {
            try {
                val instance = when {
                    listenerClass.kotlin.companionObject != null -> {
                        listenerClass.kotlin.companionObjectInstance ?: listenerClass.kotlin.companionObject?.createInstance()
                    }
                    listenerClass.kotlin.isData -> {
                        listenerClass.kotlin.createInstance()
                    }
                    else -> {
                        listenerClass.getDeclaredConstructor().newInstance()
                    }
                }
                registerListener(instance as Listener)
            } catch (e: Exception) {
                logger.error("Failed to register listener: ${listenerClass.name}", e)
            }
        }
    }

    @JvmStatic
    fun registerListener(listener: Listener) {
        listeners.add(listener)
        logger.info("Registered listener: ${listener::class.java.name}")
    }

    @JvmStatic
    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
        logger.info("Unregistered listener: ${listener::class.java.name}")
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
            }

            if (event is DelayableEvent) {
                logger.debug("Event ${event::class.simpleName} is delayed by ${event.delay} milliseconds")
                Thread.sleep(event.delay)
            }

            if (event is RoopableEvent) {
                for (i in 1..event.roop) {
                    logger.debug("Handling event: ${event::class.simpleName}, loop $i of ${event.roop}")
                    runCatching {
                        eventHook.handler(event)
                        logger.debug("Handled event: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                    }.onFailure {
                        logger.error("Exception while executing handler: ${it.message}", it)
                    }
                }
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
            }

            if (event is DelayableEvent) {
                logger.debug("Event ${event::class.simpleName} is delayed by ${event.delay} milliseconds")
                Thread.sleep(event.delay)
            }

            if (event is RoopableEvent) {
                for (i in 1..event.roop) {
                    logger.debug("Handling event asynchronously: ${event::class.simpleName}, loop $i of ${event.roop}")
                    asyncExecutor.submit {
                        runCatching {
                            eventHook.handler(event)
                            logger.debug("Handled event asynchronously: ${event::class.simpleName} with ${eventHook.handlerClass::class.simpleName}")
                        }.onFailure {
                            logger.error("Exception while executing handler asynchronously: ${it.message}", it)
                        }
                    }
                }
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
    fun initialize(packageName: String?) {
        asyncExecutor = Executors.newCachedThreadPool()
        registerAllListeners(packageName)
        logger.info("EventBus initialized with package: $packageName")
    }

    @JvmStatic
    fun shutdown() {
        asyncExecutor.shutdown()
        logger.info("EventBus shutdown")
    }
}
