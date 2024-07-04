package net.rk4z.beacon

/**
 * This class represents a generic event.
 */
open class Event

/**
 * This class represents a cancellable event, which is a type of event that can be cancelled.
 * @property isCancelled A boolean that indicates whether this event is cancelled.
 */
open class CancellableEvent : Event() {
    var isCancelled: Boolean = false
        private set

    /**
     * Sets the cancelled status of this event.
     * @param v The new cancelled status of this event.
     */
    fun setCansel(v: Boolean) {
        isCancelled = v
    }
}

/**
 * This enum represents the priority of an event.
 * @property v The value of the priority.
 */
enum class Priority(val v: Int) {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4)
}