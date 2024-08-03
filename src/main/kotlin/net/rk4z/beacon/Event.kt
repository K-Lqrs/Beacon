/*
 * Copyright (c) 2024 Ruxy
 * Released under the MIT license
 * https://opensource.org/license/mit
 */

package net.rk4z.beacon

/**
 * This class represents a generic event.
 */
abstract class Event

/**
 * This class represents a cancellable event, which is a type of event that can be cancelled.
 * @property isCancelled A boolean that indicates whether this event is cancelled.
 */
abstract class CancellableEvent : Event() {
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
 * This class represents a returnable event.
 */
abstract class ReturnableEvent<T> : Event() {
    private var result: T? = null

    /**
     * Sets the result of the event.
     * @param result The result of the event.
     */
    fun setResult(result: T) {
        this.result = result
    }

    /**
     * Returns the result of the event.
     * @return The result of the event.
     */
    fun getResult(): T? {
        return result
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