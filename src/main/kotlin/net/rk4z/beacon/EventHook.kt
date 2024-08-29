@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.rk4z.beacon

import kotlin.reflect.full.isSubclassOf

/**
 * Type alias for a handler function that takes an event of type T and returns Unit.
 */
typealias Handler<T> = (T) -> Unit

/**
 * Type alias for a handler function that takes an event of type T and returns a result of type R.
 */
typealias ReturnableHandler<T, R> = (T) -> R

/**
 * Represents a hook for an event.
 *
 * @param T The type of event.
 * @property handlerClass The class of the event handler.
 * @property handler The handler function for the event.
 * @property ignoresCondition Whether the condition should be ignored.
 * @property priority The priority of the event hook.
 * @property condition An optional condition that must be met for the handler to be executed.
 * @property timeout An optional timeout for the event hook.
 */
class EventHook<T : Event>(
    val handlerClass: IEventHandler,
    val handler: Handler<T>,
    val ignoresCondition: Boolean,
    val priority: Priority = Priority.NORMAL,
    val condition: (() -> Boolean)? = null,
    val timeout: Long? = null
)

/**
 * Represents a hook for a returnable event.
 *
 * @param T The type of event.
 * @param R The type of the result returned by the handler.
 * @property handlerClass The class of the event handler.
 * @property handler The handler function for the event.
 * @property ignoresCondition Whether the condition should be ignored.
 * @property priority The priority of the event hook.
 * @property condition An optional condition that must be met for the handler to be executed.
 * @property timeout An optional timeout for the event hook.
 */
class ReturnableEventHook<T : ReturnableEvent<R>, R>(
    val handlerClass: IEventHandler,
    val handler: ReturnableHandler<T, R>,
    val ignoresCondition: Boolean,
    val priority: Priority = Priority.NORMAL,
    val condition: (() -> Boolean)? = null,
    val timeout: Long? = null
)

/**
 * Registers an event handler for a specific event type.
 *
 * @param T The type of event.
 * @param condition An optional condition that must be met for the handler to be executed.
 * @param ignoresCondition Whether the condition should be ignored.
 * @param priority The priority of the event hook.
 * @param timeout An optional timeout for the event hook.
 * @param handler The handler function for the event.
 * @throws IllegalStateException If the class does not implement IEventHandler.
 */
inline fun <reified T : Event> IEventHandler.handler(
    noinline condition: () -> Boolean = { true },
    ignoresCondition: Boolean = false,
    priority: Priority = Priority.NORMAL,
    timeout: Long? = null,
    noinline handler: Handler<T>
) {
    val clazz = this::class

    if (clazz.isSubclassOf(IEventHandler::class)) {
        EventBus.registerEventHook(
            T::class.java,
            EventHook(
                this,
                handler,
                ignoresCondition,
                priority,
                condition,
                timeout
            )
        )
    } else {
        throw IllegalStateException("Classes containing event handlers need to be implemented with IEventHandler: ${clazz.simpleName}")
    }
}

/**
 * Registers a returnable event handler for a specific event type.
 *
 * @param T The type of event.
 * @param R The type of the result returned by the handler.
 * @param condition An optional condition that must be met for the handler to be executed.
 * @param ignoresCondition Whether the condition should be ignored.
 * @param priority The priority of the event hook.
 * @param timeout An optional timeout for the event hook.
 * @param handler The handler function for the event.
 * @throws IllegalStateException If the class does not implement IEventHandler.
 */
inline fun <reified T : ReturnableEvent<R>, R> IEventHandler.returnableHandler(
    noinline condition: () -> Boolean = { true },
    ignoresCondition: Boolean = false,
    priority: Priority = Priority.NORMAL,
    timeout: Long? = null,
    noinline handler: ReturnableHandler<T, R>
) {
    val clazz = this::class

    if (clazz.isSubclassOf(IEventHandler::class)) {
        EventBus.registerReturnableEventHook(
            T::class.java,
            ReturnableEventHook(
                this,
                handler,
                ignoresCondition,
                priority,
                condition,
                timeout)
        )
    } else {
        throw IllegalStateException("Classes containing event handlers need to be implemented with IEventHandler: ${clazz.simpleName}")
    }
}

/**
 * Represents the type of event processing.
 */
sealed class EventProcessingType {
    /**
     * Synchronous event processing.
     */
    data object Sync : EventProcessingType()

    /**
     * Asynchronous event processing.
     */
    data object Async : EventProcessingType()

    /**
     * Synchronous event processing (alias for Sync).
     */
    data object Synchronous : EventProcessingType()

    companion object {
        fun fromString(type: String): EventProcessingType {
            return when (type.uppercase()) {
                "SYNC" -> Sync
                "ASYNC" -> Async
                "SYNCHRONOUS" -> Synchronous
                else -> throw IllegalArgumentException("Unknown EventProcessingType: $type")
            }
        }
    }
}
