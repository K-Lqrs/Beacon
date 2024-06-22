@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.rk4z.beacon

open class Event

/**
 * A cancellable event
 * @property isCancelled whether the event is canceled.
 */
open class CancellableEvent : Event() {
    var isCancelled: Boolean = false
        private set

    fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }
}

enum class Priority(val value: Int) {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4)
}