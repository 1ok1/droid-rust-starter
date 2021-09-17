package com.lok1.rustndkexample;

public class Greetings {
    private static native String greeting(final String pattern);

    public String sayHello(String to) {
        return greeting(to);
    }
}
