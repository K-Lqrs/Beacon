/*
 * Copyright (c) 2024 Ruxy
 * Released under the MIT license
 * https://opensource.org/license/mit
 */

package net.rk4z.beacon.javaExtension;

/**
 * This is a functional interface that represents a handler for a specific type of event.
 * It contains a single method, handle, that takes an event of type T and returns void.
 * @param <T> The type of the event that this handler can handle.
 */
@FunctionalInterface
public interface Handler<T> {
    /**
     * Handles an event of type T.
     * This method is meant to be implemented by the classes that use this interface.
     * @param event The event to handle.
     */
    void handle(T event);
}