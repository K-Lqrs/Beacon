package net.rk4z.beacon.javaExtension;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import net.rk4z.beacon.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@SuppressWarnings({"unused"})
public class HandlerUtil {

    /**
     * Registers a handler for events of type T.
     *
     * @param <T> the type of the event to be handled
     * @param instance the event handler instance
     * @param eventType the class type of the event
     * @param condition a supplier providing the condition to be checked
     * @param ignoresCondition whether the condition should be ignored
     * @param priority the priority of the event handler
     * @param handler the handler to process the event
     * @param timeout the timeout for the event handler
     * @throws IllegalStateException if the listener is not registered
     */
    public static <T extends Event> Unit handler(
            @NotNull IEventHandler instance,
            Class<T> eventType,
            Supplier<Boolean> condition,
            boolean ignoresCondition,
            Priority priority,
            Handler<T> handler,
            Long timeout
    ) {
        Class<?> clazz = instance.getClass();

        Function0<Boolean> kc = toKotlinFunction0(condition);

        EventBus.registerEventHook(
                eventType,
                new EventHook<>(
                        instance,
                        handler,
                        ignoresCondition,
                        priority,
                        kc,
                        timeout
                )
        );

        return Unit.INSTANCE;
    }

    /**
     * Registers a handler for events of type T with default settings.
     *
     * @param <T> the type of the event to be handled
     * @param instance the event handler instance
     * @param handler the handler to process the event
     */
    public static <T extends Event> Unit handler(
            @NotNull IEventHandler instance,
            Class<T> eventType,
            Handler<T> handler
    ) {
        return handler(instance, eventType, () -> true, false, Priority.NORMAL, handler, null);
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
    public static <T extends ReturnableEvent<R>, R> Unit returnableHandler(
            @NotNull IEventHandler instance,
            Class<T> eventType,
            Supplier<Boolean> condition,
            boolean ignoresCondition,
            Priority priority,
            ReturnableHandler<T, R> handler,
            Long timeout
    ) {
        Class<?> clazz = instance.getClass();

        Function0<Boolean> kc = toKotlinFunction0(condition);

        EventBus.registerReturnableEventHook(
                eventType,
                new ReturnableEventHook<>(
                        instance,
                        handler,
                        ignoresCondition,
                        priority,
                        kc,
                        timeout
                )
        );

        return Unit.INSTANCE;
    }

    /**
     * Registers a handler for returnable events of type T with default settings.
     *
     * @param <T> the type of the event to be handled
     * @param <R> the type of the return value
     * @param instance the event handler instance
     * @param handler the handler to process the event
     */
    public static <T extends ReturnableEvent<R>, R> Unit returnableHandler(
            IEventHandler instance,
            Class<T> eventType,
            ReturnableHandler<T, R> handler
    ) {
        return returnableHandler(instance, eventType, () -> true, false, Priority.NORMAL, handler, null);
    }

    @NotNull
    @Contract(pure = true)
    static <T> Function0<T> toKotlinFunction0(@NotNull Supplier<T> supplier) {
        return supplier::get;
    }

    @NotNull
    @Contract(pure = true)
    static <T extends Event> Function1<T, Unit> toKotlinFunction1(Handler<T> handler) {
        return event -> {
            handler.handle(event);
            return Unit.INSTANCE;
        };
    }

    @NotNull
    @Contract(pure = true)
    static <T extends ReturnableEvent<R>, R> Function1<T, R> toKotlinFunction1(@NotNull ReturnableHandler<T, R> handler) {
        return handler::handle;
    }

}