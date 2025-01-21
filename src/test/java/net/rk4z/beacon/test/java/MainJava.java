package net.rk4z.beacon.test.java;

import kotlin.Unit;
import net.rk4z.beacon.EventBus;
import net.rk4z.beacon.EventProcessingType;
import net.rk4z.beacon.IEventHandler;

import static net.rk4z.beacon.javaExtension.HandlerUtil.handler;
import static net.rk4z.beacon.javaExtension.HandlerUtil.returnableHandler;

public class MainJava implements IEventHandler {
    @Override
    public void initHandlers() {
        handler(this, MyEventJava.class, event -> {
            System.out.println("This is a handler for MyEventJava: " + event.getMessage());
            System.out.println("Thread: " + Thread.currentThread().getName());
        });

        returnableHandler(this, MyReturnableEventJava.class, event -> {
            System.out.println("This is a handler for MyReturnableEventJava: " + event.getMessage());
            System.out.println("Thread: " + Thread.currentThread().getName());
            return "Hello from returnable handler!";
        });

        //...or

//        final Unit onMyEvent = handler(this, MyEventJava.class, event -> {
//            System.out.println("This is a handler for MyEventJava: " + event.getMessage());
//            System.out.println("Thread: " + Thread.currentThread().getName());
//        });
//
//        final Unit onMyReturnableEvent = returnableHandler(this, MyReturnableEventJava.class, event -> {
//            System.out.println("This is a handler for MyReturnableEventJava: " + event.getMessage());
//            System.out.println("Thread: " + Thread.currentThread().getName());
//            return "Hello from returnable handler!";
//        });
    }

    public static void main(String[] args) {
        String[] packageNames = {"net.rk4z.beacon.test.java"};

        // Initialize the EventBus
        EventBus.initialize(packageNames);
        EventBus.postAsync(new MyEventJava("Hello world!"));
        final String result = EventBus.postReturnable(new MyReturnableEventJava("Hello, world!"), EventProcessingType.HANDLER_ASYNC);
        System.out.println(result);
    }
}
