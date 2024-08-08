package net.rk4z.beacon.javaExtension;

import net.rk4z.beacon.ReturnableEvent;

/**
 * Functional interface representing a handler for returnable events of type T.
 *
 * @param <T> the type of the returnable event to be handled
 * @param <R> the type of the return value
 */
@FunctionalInterface
public interface ReturnableHandler<T extends ReturnableEvent<R>, R> {
    /**
     * Handles the given returnable event and returns a result.
     *
     * @param event the returnable event to be handled
     * @return the result of handling the event
     */
    R handle(T event);
}