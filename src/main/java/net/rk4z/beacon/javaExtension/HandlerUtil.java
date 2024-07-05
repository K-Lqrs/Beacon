/*
 * Copyright (c) 2024 Ruxy
 * Released under the MIT license
 * https://opensource.org/license/mit
 */

package net.rk4z.beacon.javaExtension;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import net.rk4z.beacon.*;
import net.rk4z.beacon.EventHook;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * This class provides utility methods for listeners in Java.
 * It provides methods to register a handler for a specific type of event.
 */
@SuppressWarnings("unchecked")
public class HandlerUtil {

    /**
     * Registers a handler for a specific type of event.
     * The handler is registered as an event hook in the EventBus.
     * If the listener is not registered in the EventBus, an exception is thrown.
     * @param instance The listener that the handler belongs to.
     * @param condition A supplier that returns a boolean, which is the condition for the event hook.
     * @param ignoresCondition A boolean that indicates whether the event hook ignores its condition.
     * @param priority The priority of the event hook.
     * @param handler The function that handles the event.
     */
    public static <T extends Event> void handler(
            @NotNull IEventHandler instance,
            Supplier<Boolean> condition,
            boolean ignoresCondition,
            Priority priority,
            Handler<T> handler,
            Long timeout
    ) {
        Class<?> clazz = instance.getClass();

        if (clazz.isAnnotationPresent(EventHandler.class) && IEventHandler.class.isAssignableFrom(clazz)) {
            EventBus.registerEventHook(
                    (Class<T>) Event.class,
                    new EventHook<>(
                            instance,
                            (Function1<? super T, Unit>) handler,
                            ignoresCondition,
                            priority,
                            (Function0<Boolean>) condition,
                            timeout
                    )
            );
        } else {
            throw new IllegalStateException("This listener is not registered: " + instance.getClass().getSimpleName());
        }
    }

    /**
     * Registers a handler for a specific type of event with default parameters.
     * The handler is registered as an event hook in the EventBus with a condition that always returns false,
     * does not ignore its condition, and has normal priority.
     * If the listener is not registered in the EventBus, an exception is thrown.
     * @param instance The listener that the handler belongs to.
     * @param handler The function that handles the event.
     */
    public static <T extends Event> void handler(
            IEventHandler instance,
            Handler<T> handler
    ) {
        handler(instance, () -> false, false, Priority.NORMAL, handler, null);
    }
}