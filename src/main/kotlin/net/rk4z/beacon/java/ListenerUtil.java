package net.rk4z.beacon.java;

import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ListenerUtil {

    public static <T extends Event> void handler(
            Listener listener,
            Supplier<Boolean> condition,
            boolean ignoresCondition,
            Priority priority,
            Handler<T> handler
    ) {
        if (EventBus.listeners.contains(listener)) {
            EventBus.registerEventHook(
                    (Class<T>) Event.class,
                    new EventHook<>(listener, handler, ignoresCondition, priority, condition)
            );
        } else {
            throw new IllegalStateException("This listener is not registered: " + listener.getClass().getSimpleName());
        }
    }

    public static <T extends Event> void handler(
            Listener listener,
            Handler<T> handler
    ) {
        handler(listener, () -> false, false, Priority.NORMAL, handler);
    }
}
