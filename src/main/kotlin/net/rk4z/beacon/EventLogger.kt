package net.rk4z.beacon

object EventLogger {
    private val eventLog: MutableList<String> = mutableListOf()

    fun logEvent(event: Event) {
        eventLog.add("Event: ${event::class.simpleName} at ${System.currentTimeMillis()}")
    }

    fun getEventLog(): List<String> {
        return eventLog.toList()
    }
}
