package net.rk4z.beacon.test.java;

import net.rk4z.beacon.ReturnableEvent;

public class MyReturnableEventJava extends ReturnableEvent<String> {
    private final String message;

    public MyReturnableEventJava(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
