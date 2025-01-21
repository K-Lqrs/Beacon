package net.rk4z.beacon.test.java;

import net.rk4z.beacon.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class MyEventJava extends Event {
    private final String message;

    public MyEventJava(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @NotNull
    @Contract("_ -> new")
    public static MyEventJava get(String message) {
        return new MyEventJava(message);
    }
}
