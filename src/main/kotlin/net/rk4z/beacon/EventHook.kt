@file:Suppress("unused")

package net.rk4z.beacon

typealias Handler<T> = (T) -> Unit

class EventHook<T : Event>(
    val handlerClass: Listener,
    val handler: Handler<T>,
    val ignoresCondition: Boolean,
    val priority: Priority = Priority.NORMAL,
    val condition: (() -> Boolean)? = null,
    val timeout: Long? = null
)

inline fun <reified T : Event> Listener.handler(
    noinline condition: () -> Boolean = { false },
    ignoresCondition: Boolean = false,
    priority: Priority = Priority.NORMAL,
    noinline handler: Handler<T>
) {
    if (this in EventBus.listeners) {
        EventBus.registerEventHook(T::class.java, EventHook(this, handler, ignoresCondition, priority, condition))
    } else {
        throw IllegalStateException("This listener is not registered: ${this::class.simpleName}")
    }
}