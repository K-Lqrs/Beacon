package net.rk4z.beacon.java;

import java.util.function.Supplier;

public class EventHook<T extends Event> {
    private final Listener handlerClass;
    private final Handler<T> handler;
    private final boolean ignoresCondition;
    private Priority priority;
    private final Supplier<Boolean> condition;
    private final Long timeout;

    public EventHook(Listener handlerClass, Handler<T> handler, boolean ignoresCondition, Priority priority, Supplier<Boolean> condition, Long timeout) {
        this.handlerClass = handlerClass;
        this.handler = handler;
        this.ignoresCondition = ignoresCondition;
        this.priority = priority;
        this.condition = condition;
        this.timeout = timeout;
    }

    public EventHook(Listener handlerClass, Handler<T> handler, boolean ignoresCondition, Priority priority, Supplier<Boolean> condition) {
        this(handlerClass, handler, ignoresCondition, priority, condition, null);
    }

    // Getters
    public Listener getHandlerClass() {
        return handlerClass;
    }

    public Handler<T> getHandler() {
        return handler;
    }

    public boolean isIgnoresCondition() {
        return !ignoresCondition;
    }

    public Priority getPriority() {
        return priority;
    }

    public Supplier<Boolean> getCondition() {
        return condition;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}