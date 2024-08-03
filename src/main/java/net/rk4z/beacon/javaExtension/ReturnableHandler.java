package net.rk4z.beacon.javaExtension;

import net.rk4z.beacon.ReturnableEvent;

/**
 * This is a functional interface that represents a handler for a specific type of returnable event.
 * It contains a single method, handle, that takes an event of type T and returns a result of type R.
 * @param <T> The type of the event that this handler can handle.
 * @param <R> The type of the result that this handler returns.
 */
@FunctionalInterface
public interface ReturnableHandler<T extends ReturnableEvent<R>, R> {
    /**
     * Handles an event of type T and returns a result of type R.
     * @param event The event to handle.
     * @return The result of handling the event.
     */
    R handle(T event);
}