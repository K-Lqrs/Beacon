package net.rk4z.beacon.javaExtension;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import net.rk4z.beacon.*;
import net.rk4z.beacon.EventHook;

import java.util.function.Supplier;

/**
 * This class provides utility methods for listeners in Java.
 * It provides methods to register a handler for a specific type of event.
 */
@SuppressWarnings("unchecked")
public class ListenerUtil {

    /**
     * Registers a handler for a specific type of event.
     * The handler is registered as an event hook in the EventBus.
     * If the listener is not registered in the EventBus, an exception is thrown.
     * @param listener The listener that the handler belongs to.
     * @param condition A supplier that returns a boolean, which is the condition for the event hook.
     * @param ignoresCondition A boolean that indicates whether the event hook ignores its condition.
     * @param priority The priority of the event hook.
     * @param handler The function that handles the event.
     */
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
                    new EventHook<>(
                            listener,
                            (Function1<? super T, Unit>) handler,
                            ignoresCondition,
                            priority,
                            (Function0<Boolean>) condition,
                            null)
            );
        } else {
            throw new IllegalStateException("This listener is not registered: " + listener.getClass().getSimpleName());
        }
    }

    /**
     * Registers a handler for a specific type of event with default parameters.
     * The handler is registered as an event hook in the EventBus with a condition that always returns false,
     * does not ignore its condition, and has normal priority.
     * If the listener is not registered in the EventBus, an exception is thrown.
     * @param listener The listener that the handler belongs to.
     * @param handler The function that handles the event.
     */
    public static <T extends Event> void handler(
            Listener listener,
            Handler<T> handler
    ) {
        handler(listener, () -> false, false, Priority.NORMAL, handler);
    }
}