package net.rk4z.beacon.javaExtension;

import net.rk4z.beacon.Event;
import net.rk4z.beacon.EventBus;
import org.jetbrains.annotations.NotNull;

public class JEventBus {
    /**
     * Posts an event synchronously.
     *
     * @param <T> The type of the event.
     * @param event The event to post.
     * @param enableDebugLog Whether to enable debug logging.
     * @return The event after processing.
     */
    @NotNull
    public static <T extends Event> T postHandlerSync(T event, Boolean enableDebugLog) {
        return EventBus.postHandlerSync(event, enableDebugLog);
    }
    @NotNull
    public static <T extends Event> T postHandlerSync(T event) {
        return postHandlerSync(event, false);
    }

    /**
     * Posts an event asynchronously.
     *
     * @param <T> The type of the event.
     * @param event The event to post.
     * @param enableDebugLog Whether to enable debug logging.
     * @return The event after processing.
     */
    @NotNull
    public static <T extends Event> T postAsync(T event, Boolean enableDebugLog) {
        return EventBus.postAsync(event, enableDebugLog);
    }
    @NotNull
    public static <T extends Event> T postAsync(T event) {
        return postAsync(event, false);
    }


}
