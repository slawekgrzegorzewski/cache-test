package pl.sg.cache.service;

import java.util.Optional;

public class CacheRequestHandler {

    private final CacheRequestHandler nextHandler;

    public CacheRequestHandler() {
        this(null);
    }

    public CacheRequestHandler(CacheRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public Optional<String> getValue(String key) {
        return Optional.ofNullable(nextHandler).flatMap(nh -> nh.getValue(key));
    }

    public void setValue(String key, String value) {
        Optional.ofNullable(nextHandler).ifPresent(nh -> nh.setValue(key, value));
    }
}
