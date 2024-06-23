@file:Suppress("unused")

package net.rk4z.beacon

typealias Handler<T> = (T) -> Unit

class EventHook<T : Event> (
    val handlerClass: Listener,
    val handler: Handler<T>,
    val ignoresCondition: Boolean,
    var priority: Priority = Priority.NORMAL,
    val condition: (() -> Boolean)? = null,
    val timeout: Long? = null
)

interface Listener {
    fun handleEvents(): Boolean = parent()?.handleEvents() ?: true

    fun parent(): Listener? = null

    fun children(): List<Listener> = emptyList()

    fun unregister() {

        for (child in children()) {
            child.unregister()
        }
    }
}

inline fun <reified T: Event> Listener.handler(
    noinline condition: () -> Boolean,
    ignoresCondition: Boolean = false,
    priority: Priority = Priority.NORMAL,
    noinline handler: Handler<T>,
) {
    if (this in EventBus.listeners) {
        EventBus.registerEventHook(T::class, EventHook(this, handler, ignoresCondition, priority, condition))
    } else {
        throw IllegalStateException("This listener is not registered: ${this::class.simpleName}")
    }
}