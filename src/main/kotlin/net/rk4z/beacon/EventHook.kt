/*
 * Copyright (c) 2024 Ruxy
 * Released under the MIT license
 * https://opensource.org/license/mit
 */

package net.rk4z.beacon

import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

// Defines a type alias for a generic event handler function that takes an event of type T and returns Unit (void).
typealias Handler<T> = (T) -> Unit

// Defines a type alias for a generic returnable event handler function that takes an event of type T and returns R.
typealias ReturnableHandler<T, R> = (T) -> R

/**
 * Represents a hook for a specific type of event within the event system.
 *
 * @param T The type of the event this hook can handle.
 * @property handlerClass The listener instance that this event hook is associated with.
 * @property handler The function that will be called when the event occurs.
 * @property ignoresCondition A flag indicating whether the event hook should ignore its condition and always execute.
 * @property priority The priority of the event hook, used to determine the order of execution among multiple hooks.
 * @property condition An optional condition that determines whether the event hook should be executed.
 * @property timeout An optional timeout in milliseconds after which the event hook will not be executed.
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
 * Represents a hook for a specific type of returnable event within the event system.
 *
 * @param T The type of the event this hook can handle.
 * @param R The type of the result that this hook returns.
 * @property handlerClass The listener instance that this event hook is associated with.
 * @property handler The function that will be called when the event occurs.
 * @property ignoresCondition A flag indicating whether the event hook should ignore its condition and always execute.
 * @property priority The priority of the event hook, used to determine the order of execution among multiple hooks.
 * @property condition An optional condition that determines whether the event hook should be executed.
 * @property timeout An optional timeout in milliseconds after which the event hook will not be executed.
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
 * Extension function for Listener to easily register an event handler with custom parameters.
 *
 * @param T The type of the event to handle.
 * @param condition A lambda expression that returns a Boolean indicating whether the event should be handled.
 * @param ignoresCondition A Boolean indicating whether the condition should be ignored.
 * @param priority The priority of the event handler.
 * @param handler A lambda expression that defines how the event is handled.
 * @throws IllegalStateException if the class containing the event handler is not annotated with [IEventHandler].
 */
inline fun <reified T : Event> IEventHandler.handler(
    noinline condition: () -> Boolean = { true },
    ignoresCondition: Boolean = false,
    priority: Priority = Priority.NORMAL,
    noinline handler: Handler<T>,
    timeout: Long?
) {
    val clazz = this::class

    // Check if the class is annotated with @EventHandler and implements IEventHandler
    if (clazz.findAnnotation<EventHandler>() != null && clazz.isSubclassOf(IEventHandler::class)) {
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
        throw IllegalStateException("Classes containing event handlers need to be annotated with EventHandler and implement IEventHandler: ${clazz.simpleName}")
    }
}

/**
 * Extension function for Listener to easily register a returnable event handler with custom parameters.
 *
 * @param T The type of the returnable event to handle.
 * @param R The type of the result that the handler returns.
 * @param condition A lambda expression that returns a Boolean indicating whether the event should be handled.
 * @param ignoresCondition A Boolean indicating whether the condition should be ignored.
 * @param priority The priority of the event handler.
 * @param handler A lambda expression that defines how the event is handled and returns a result.
 * @throws IllegalStateException if the class containing the event handler is not annotated with [IEventHandler].
 */
inline fun <reified T : ReturnableEvent<R>, R> IEventHandler.returnableHandler(
    noinline condition: () -> Boolean = { true },
    ignoresCondition: Boolean = false,
    priority: Priority = Priority.NORMAL,
    noinline handler: ReturnableHandler<T, R>,
    timeout: Long?
) {
    val clazz = this::class

    // Check if the class is annotated with @EventHandler and implements IEventHandler
    if (clazz.findAnnotation<EventHandler>() != null && clazz.isSubclassOf(IEventHandler::class)) {
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
        throw IllegalStateException("Classes containing event handlers need to be annotated with EventHandler and implement IEventHandler: ${clazz.simpleName}")
    }
}