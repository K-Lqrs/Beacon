@file:Suppress("unused")

package net.rk4z.beacon

import net.rk4z.beacon.common.ListenerBase

/**
 * A type alias for a function that takes an event of type T and returns Unit.
 */
typealias Handler<T> = (T) -> Unit

/**
 * This class represents an event hook, which is a handler for a specific type of event.
 * @property handlerClass The listener that this event hook belongs to.
 * @property handler The function that handles the event.
 * @property ignoresCondition A boolean that indicates whether this event hook ignores its condition.
 * @property priority The priority of this event hook.
 * @property condition A function that returns a boolean, which is the condition for this event hook.
 * @property timeout The timeout for this event hook, in milliseconds.
 */
class EventHook<T : Event> (
    val handlerClass: Listener,
    val handler: Handler<T>,
    val ignoresCondition: Boolean,
    var priority: Priority = Priority.NORMAL,
    val condition: (() -> Boolean)? = null,
    val timeout: Long? = null
)

/**
 * This interface represents a listener, which is an object that can handle events.
 * A listener can have a parent and children, and it can handle events if its parent can.
 */
interface Listener : ListenerBase {
    /**
     * Returns whether this listener can handle events.
     * If this listener has a parent, it returns whether the parent can handle events.
     * Otherwise, it returns true.
     */
    override fun handleEvents(): Boolean = parent()?.handleEvents() ?: true

    /**
     * Returns the parent of this listener, or null if it has no parent.
     */
    fun parent(): Listener? = null

    /**
     * Returns the children of this listener.
     * By default, a listener has no children.
     */
    fun children(): List<Listener> = emptyList()

    /**
     * Unregisters this listener and all its children.
     */
    override fun unregister() {
        for (child in children()) {
            child.unregister()
        }
    }
}

/**
 * This function allows a listener to register a handler for a specific type of event.
 * The handler is registered as an event hook in the EventBus.
 * If the listener is not registered in the EventBus, an exception is thrown.
 * @param condition A function that returns a boolean, which is the condition for the event hook.
 * @param ignoresCondition A boolean that indicates whether the event hook ignores its condition.
 * @param priority The priority of the event hook.
 * @param handler The function that handles the event.
 */
inline fun <reified T: Event> Listener.handler(
    noinline condition: () -> Boolean = { false },
    ignoresCondition: Boolean = false,
    priority: Priority = Priority.NORMAL,
    noinline handler: Handler<T>,
) {
    if (this in EventBus.listeners) {
        EventBus.registerEventHook(T::class.java, EventHook(this, handler, ignoresCondition, priority, condition))
    } else {
        throw IllegalStateException("This listener is not registered: ${this::class.simpleName}")
    }
}