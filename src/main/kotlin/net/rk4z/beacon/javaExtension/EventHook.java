package net.rk4z.beacon.javaExtension;

import net.rk4z.beacon.Event;
import net.rk4z.beacon.Priority;

import java.util.function.Supplier;

/**
 * This class represents an event hook, which is a handler for a specific type of event.
 * @param <T> The type of the event that this event hook can handle.
 */
@SuppressWarnings("unused")
public class EventHook<T extends Event> {
    private final Listener handlerClass;
    private final Handler<T> handler;
    private final boolean ignoresCondition;
    private Priority priority;
    private final Supplier<Boolean> condition;
    private final Long timeout;

    /**
     * Constructs an EventHook with the given parameters.
     * @param handlerClass The listener that this event hook belongs to.
     * @param handler The function that handles the event.
     * @param ignoresCondition A boolean that indicates whether this event hook ignores its condition.
     * @param priority The priority of this event hook.
     * @param condition A supplier that returns a boolean, which is the condition for this event hook.
     * @param timeout The timeout for this event hook, in milliseconds.
     */
    public EventHook(Listener handlerClass, Handler<T> handler, boolean ignoresCondition, Priority priority, Supplier<Boolean> condition, Long timeout) {
        this.handlerClass = handlerClass;
        this.handler = handler;
        this.ignoresCondition = ignoresCondition;
        this.priority = priority;
        this.condition = condition;
        this.timeout = timeout;
    }

    /**
     * Constructs an EventHook with the given parameters and a null timeout.
     * @param handlerClass The listener that this event hook belongs to.
     * @param handler The function that handles the event.
     * @param ignoresCondition A boolean that indicates whether this event hook ignores its condition.
     * @param priority The priority of this event hook.
     * @param condition A supplier that returns a boolean, which is the condition for this event hook.
     */
    public EventHook(Listener handlerClass, Handler<T> handler, boolean ignoresCondition, Priority priority, Supplier<Boolean> condition) {
        this(handlerClass, handler, ignoresCondition, priority, condition, null);
    }

    /**
     * Returns the listener that this event hook belongs to.
     * @return The listener that this event hook belongs to.
     */
    public Listener getHandlerClass() {
        return handlerClass;
    }

    /**
     * Returns the function that handles the event.
     * @return The function that handles the event.
     */
    public Handler<T> getHandler() {
        return handler;
    }

    /**
     * Returns whether this event hook ignores its condition.
     * @return A boolean that indicates whether this event hook ignores its condition.
     */
    public boolean isIgnoresCondition() {
        return ignoresCondition;
    }

    /**
     * Returns the priority of this event hook.
     * @return The priority of this event hook.
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Returns the condition for this event hook.
     * @return A supplier that returns a boolean, which is the condition for this event hook.
     */
    public Supplier<Boolean> getCondition() {
        return condition;
    }

    /**
     * Returns the timeout for this event hook, in milliseconds.
     * @return The timeout for this event hook, in milliseconds.
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * Sets the priority of this event hook.
     * @param priority The new priority of this event hook.
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}