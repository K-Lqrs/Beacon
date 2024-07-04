package net.rk4z.beacon;


import java.util.List;

@SuppressWarnings("unused")
public interface IEventHandler {

    /**
     * Returns whether this listener can handle events.
     * If this listener has a parent, it returns whether the parent can handle events.
     * Otherwise, it returns true.
     *
     * @return A boolean indicating whether this listener can handle events.
     */
    default boolean handleEvents() {
        IEventHandler parent = parent();
        return parent == null || parent.handleEvents();
    }

    /**
     * Returns the parent of this listener, or null if it has no parent.
     *
     * @return The parent of this listener, or null if it has no parent.
     */
    default IEventHandler parent() {
        return null;
    }

    /**
     * Returns the children of this listener.
     * By default, a listener has no children.
     *
     * @return List of children.
     */
    default List<IEventHandler> children() {
        return List.of();
    }

    /**
     * Unregisters this listener and all its children.
     */
    default void unregister() {
        for (IEventHandler child : children()) {
            child.unregister();
        }
    }
}