package net.rk4z.beacon.javaExtension;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import net.rk4z.beacon.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "unused", "RedundantCast"})
public class HandlerUtil {

    /**
     * Registers a handler for events of type T.
     *
     * @param <T> the type of the event to be handled
     * @param instance the event handler instance
     * @param condition a supplier providing the condition to be checked
     * @param ignoresCondition whether the condition should be ignored
     * @param priority the priority of the event handler
     * @param handler the handler to process the event
     * @param timeout the timeout for the event handler
     * @throws IllegalStateException if the listener is not registered
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

        if (IEventHandler.class.isAssignableFrom(clazz)) {
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
     * Registers a handler for events of type T with default settings.
     *
     * @param <T> the type of the event to be handled
     * @param instance the event handler instance
     * @param handler the handler to process the event
     */
    public static <T extends Event> void handler(
            IEventHandler instance,
            Handler<T> handler
    ) {
        handler(instance, () -> false, false, Priority.NORMAL, handler, null);
    }

    /**
     * Registers a handler for returnable events of type T.
     *
     * @param <T> the type of the event to be handled
     * @param <R> the type of the return value
     * @param instance the event handler instance
     * @param condition a supplier providing the condition to be checked
     * @param ignoresCondition whether the condition should be ignored
     * @param priority the priority of the event handler
     * @param handler the handler to process the event
     * @param timeout the timeout for the event handler
     * @throws IllegalStateException if the listener is not registered
     */
    public static <T extends ReturnableEvent<R>, R> void returnableHandler(
            @NotNull IEventHandler instance,
            Supplier<Boolean> condition,
            boolean ignoresCondition,
            Priority priority,
            ReturnableHandler<T, R> handler,
            Long timeout
    ) {
        Class<?> clazz = instance.getClass();

        if (IEventHandler.class.isAssignableFrom(clazz)) {
            EventBus.registerReturnableEventHook(
                    (Class<T>) (Class<?>) ReturnableEvent.class,
                    new ReturnableEventHook<>(
                            instance,
                            (Function1<? super T, R>) handler,
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
     * Registers a handler for returnable events of type T with default settings.
     *
     * @param <T> the type of the event to be handled
     * @param <R> the type of the return value
     * @param instance the event handler instance
     * @param handler the handler to process the event
     */
    public static <T extends ReturnableEvent<R>, R> void returnableHandler(
            IEventHandler instance,
            ReturnableHandler<T, R> handler
    ) {
        returnableHandler(instance, () -> false, false, Priority.NORMAL, handler, null);
    }
}