package net.rk4z.beacon.javaExtension;

import net.rk4z.beacon.Event;
import net.rk4z.beacon.EventBus;
import net.rk4z.beacon.EventProcessingType;
import net.rk4z.beacon.ReturnableEvent;
import org.jetbrains.annotations.NotNull;

public class JEventBus {
    /**
     * Posts an event synchronously.
     *
     * @param <T> The type of the event.
     * @param event The event to post.
     * @param enableDebugLog Whether to enable debug logging.
     */
    public static <T extends Event> void postHandlerSync(T event, Boolean enableDebugLog) {
        EventBus.postHandlerSync(event, enableDebugLog);
    }
    public static <T extends Event> void postHandlerSync(T event) {
        postHandlerSync(event, false);
    }

    /**
     * Posts an event asynchronously.
     *
     * @param <T> The type of the event.
     * @param event The event to post.
     * @param enableDebugLog Whether to enable debug logging.
     */
    public static <T extends Event> void postAsync(T event, Boolean enableDebugLog) {
        EventBus.postAsync(event, enableDebugLog);
    }
    public static <T extends Event> void postAsync(T event) {
        postAsync(event, false);
    }

    /**
     * Posts a returnable event synchronously.
     *
     * @param <T> The type of the event.
     * @param event The event to post.
     * @param enableDebugLog Whether to enable debug logging.
     */
    public static <T extends Event> void postFullSync(T event, Boolean enableDebugLog) {
        EventBus.postFullSync(event, enableDebugLog);
    }
    public static <T extends Event> void postFullSync(T event) {
        postFullSync(event, false);
    }

    /**
     * Posts a returnable event asynchronously.
     *
     * @param <T> The type of the event.
     * @param event The event to post.
     * @param pt The event processing type.
     * @param enableDebugLog Whether to enable debug logging.
     * @return The return value of the event.
     * @param <R> The type of the return value.
     */
    public static <T extends ReturnableEvent<R>, R> R postReturnable(T event, EventProcessingType pt,Boolean enableDebugLog) {
        return EventBus.postReturnable(event, pt, enableDebugLog);
    }
    public static <T extends ReturnableEvent<R>, R> R postReturnable(T event, EventProcessingType pt) {
        return postReturnable(event, pt, false);
    }
}
