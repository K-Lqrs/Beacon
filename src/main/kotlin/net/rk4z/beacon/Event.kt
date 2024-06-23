@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.rk4z.beacon

/**
 * This is the base class for all events in the system.
 * Any new event type should extend this class.
 */
open class Event

/**
 * This class represents an event that can be cancelled.
 * @property isCancelled A boolean property that indicates whether the event is cancelled or not.
 */
open class CancellableEvent : Event() {
    /**
     * A boolean property that indicates whether the event is cancelled or not.
     * It is private to set, but publicly readable.
     */
    var isCancelled: Boolean = false
        private set

    /**
     * This function allows canceling or uncancel the event.
     * @param cancel A boolean value indicating whether to cancel the event or not.
     */
    fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }
}

/**
 * This enum represents the priority of an event.
 * It has five levels: LOWEST, LOW, NORMAL, HIGH, HIGHEST.
 * Each level is associated with a numerical value, from 0 (LOWEST) to 4 (HIGHEST).
 */
enum class Priority(val value: Int) {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4)
}