package net.rk4z.beacon.java;

@FunctionalInterface
public interface Handler<T> {
    void handle(T event);
}
