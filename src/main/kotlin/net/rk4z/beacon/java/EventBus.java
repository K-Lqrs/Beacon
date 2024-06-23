package net.rk4z.beacon.java;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@SuppressWarnings({"unchecked", "unused"})
public class EventBus {
    private static final Map<Class<? extends Event>, CopyOnWriteArrayList<EventHook<? super Event>>> registry = new ConcurrentHashMap<>();
    private static ExecutorService asyncExecutor;
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    public static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public static <T extends Event> void registerEventHook(Class<T> eventClass, EventHook<T> eventHook) {
        CopyOnWriteArrayList<EventHook<?>> handlers = (CopyOnWriteArrayList<EventHook<?>>) registry.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>());

        if (!handlers.contains(eventHook)) {
            handlers.add(eventHook);
            handlers.sort(Comparator.comparingInt(h -> ((EventHook<?>) h).getPriority().getValue()).reversed());
            logger.info("Registered event hook for {} with priority {}", eventClass.getSimpleName(), eventHook.getPriority());
        }
    }


    public static <T extends Event> void unregisterEventHook(Class<T> eventClass, EventHook<T> eventHook) {
        CopyOnWriteArrayList<EventHook<? super Event>> handlers = registry.get(eventClass);
        if (handlers != null) {
            handlers.remove(eventHook);
        }
    }

    public static <T extends Event> T callEvent(T event) {
        logger.debug("Calling event: {}", event.getClass().getSimpleName());
        EventLogger.logEvent(event);
        CopyOnWriteArrayList<EventHook<? super Event>> target = registry.get(event.getClass());

        if (target != null) {
            for (EventHook<? super Event> eventHook : target) {
                if (event instanceof CancellableEvent && ((CancellableEvent) event).isCancelled()) {
                    break;
                }

                if (eventHook.isIgnoresCondition() && !eventHook.getHandlerClass().handleEvents()) {
                    continue;
                }

                if (eventHook.getCondition() != null && !eventHook.getCondition().get()) {
                    continue;
                }

                try {
                    Future<?> future = asyncExecutor.submit(() -> eventHook.getHandler().handle(event));
                    if (eventHook.getTimeout() != null) {
                        future.get(eventHook.getTimeout(), TimeUnit.MILLISECONDS);
                    } else {
                        future.get();
                    }
                    logger.debug("Handled event: {} with {}", event.getClass().getSimpleName(), eventHook.getHandlerClass().getClass().getSimpleName());
                } catch (Exception e) {
                    logger.error("Exception while executing handler: {}", e.getMessage(), e);
                }
            }
        }

        return event;
    }

    public static <T extends Event> T callEventAsync(T event) {
        logger.debug("Calling event asynchronously: {}", event.getClass().getSimpleName());
        CopyOnWriteArrayList<EventHook<? super Event>> target = registry.get(event.getClass());

        if (target != null) {
            for (EventHook<? super Event> eventHook : target) {
                if (event instanceof CancellableEvent && ((CancellableEvent) event).isCancelled()) {
                    break;
                }

                if (eventHook.isIgnoresCondition() && !eventHook.getHandlerClass().handleEvents()) {
                    continue;
                }

                if (eventHook.getCondition() != null && !eventHook.getCondition().get()) {
                    continue;
                }

                asyncExecutor.submit(() -> {
                    try {
                        eventHook.getHandler().handle(event);
                        logger.debug("Handled event asynchronously: {} with {}", event.getClass().getSimpleName(), eventHook.getHandlerClass().getClass().getSimpleName());
                    } catch (Exception e) {
                        logger.error("Exception while executing handler asynchronously: {}", e.getMessage(), e);
                    }
                });
            }
        }

        return event;
    }

    public static void registerAllListeners(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends Listener>> listenerClasses = reflections.getSubTypesOf(Listener.class);

        listenerClasses.forEach(listenerClass -> {
            try {
                Listener instance = listenerClass.getDeclaredConstructor().newInstance();
                registerListener(instance);
                logger.info("Registered listener: {}", listenerClass.getSimpleName());
            } catch (Exception e) {
                logger.error("Failed to register listener: {}", listenerClass.getName(), e);
            }
        });
    }

    private static void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public static void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public static <T extends Event> void changeEventHookPriority(Class<T> eventClass, EventHook<T> eventHook, Priority newPriority) {
        CopyOnWriteArrayList<EventHook<?>> handlers = (CopyOnWriteArrayList<EventHook<?>>) registry.get(eventClass);
        if (handlers != null) {
            EventHook<?> hookToChange = handlers.stream().filter(h -> h == eventHook).findFirst().orElse(null);
            if (hookToChange != null) {
                hookToChange.setPriority(newPriority);
                handlers.sort(Comparator.comparingInt(h -> ((EventHook<?>) h).getPriority().getValue()).reversed());
                logger.debug("Changed priority for {} to {}", eventClass.getSimpleName(), newPriority);
            }
        }
    }


    public static void initialize() {
        asyncExecutor = Executors.newCachedThreadPool();
        logger.info("EventManager initialized");
    }

    public static void shutdown() {
        asyncExecutor.shutdown();
        logger.info("EventManager shutdown");
    }
}
