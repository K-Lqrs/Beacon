package net.rk4z.beacon.java;

import java.util.List;

public interface Listener {

    default boolean handleEvents() {
        Listener parent = parent();
        return parent == null || parent.handleEvents();
    }

    default Listener parent() {
        return null;
    }

    default List<Listener> children() {
        return List.of();
    }

    default void unregister() {
        for (Listener child : children()) {
            child.unregister();
        }
    }
}
