@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.rk4z.beacon

/**
 * Represents a generic event.
 */
abstract class Event {
    val metadata: MutableMap<String, Any?> = mutableMapOf()

    fun setMeta(key: String, value: Any) = metadata.put(key, value)
    fun getMeta(key: String): Any? = metadata[key]
    fun <T> getMetaOrDefault(key: String, default: T): T = metadata[key] as? T ?: default
}

/**
 * Represents an event that can be cancelled.
 */
abstract class CancellableEvent : Event() {
    /**
     * Indicates whether the event is cancelled.
     */
    var isCancelled: Boolean = false
        private set

    /**
     * Cancels the event.
     */
    fun cancel() {
        isCancelled = true
    }

    /**
     * Uncancels the event.
     */
    fun uncancel() {
        isCancelled = false
    }
}

/**
 * Represents an event that can return a result.
 * @param T The type of the result.
 */
abstract class ReturnableEvent<T> : Event() {
    /**
     * The result of the event.
     */
    var result: T? = null
        private set

    /**
     * Sets the result of the event.
     * @param result The result to set.
     */
    fun setResult(result: T) {
        this.result = result
    }

    /**
     * Gets the result of the event or a default value if the result is null.
     * @param default The default value to return if the result is null.
     * @return The result of the event or the default value.
     */
    fun getResultOrDefault(default: T): T = result ?: default
}

/**
 * Represents the priority of an event.
 * @param level The integer value of the priority.
 */
enum class Priority(val level: Int) {
    LOWEST(0),
    LOW(1),
    MID_LOW(2),
    NORMAL(3),
    MID_HIGH(4),
    HIGH(5),
    HIGHEST(6)
}