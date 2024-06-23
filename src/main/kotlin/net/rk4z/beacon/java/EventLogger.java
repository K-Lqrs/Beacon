package net.rk4z.beacon.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventLogger {
    private static final List<String> eventLog = Collections.synchronizedList(new ArrayList<>());

    public static void logEvent(Event event) {
        eventLog.add("Event: " + event.getClass().getSimpleName() + " at " + System.currentTimeMillis());
    }

    public static List<String> getEventLog() {
        synchronized (eventLog) {
            return new ArrayList<>(eventLog);
        }
    }
}
