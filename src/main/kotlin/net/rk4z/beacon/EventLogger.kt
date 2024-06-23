package net.rk4z.beacon

/**
 * Singleton object that logs events.
 * It provides methods to log an event and get the event log.
 */
object EventLogger {
    // List to store the event log
    private val eventLog: MutableList<String> = mutableListOf()

    /**
     * Logs an event.
     * The event's class name and the current time in milliseconds are added to the event log.
     * @param event The event to log.
     */
    @JvmStatic
    fun logEvent(event: Event) {
        eventLog.add("Event: ${event::class.simpleName} at ${System.currentTimeMillis()}")
    }

    /**
     * Returns the event log.
     * @return A list of strings representing the event log.
     */
    @JvmStatic
    fun getEventLog(): List<String> {
        return eventLog.toList()
    }
}