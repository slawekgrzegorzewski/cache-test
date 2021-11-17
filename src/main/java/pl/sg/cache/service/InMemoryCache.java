package pl.sg.cache.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class InMemoryCache extends CacheRequestHandler {
    private final Map<String, String> inMemoryCache = new HashMap<>();

    public InMemoryCache() {
        super(null);
    }

    public InMemoryCache(CacheRequestHandler nextHandler) {
        super(nextHandler);
    }

    @Override
    public Optional<String> getValue(String key) {
        String result = inMemoryCache.computeIfAbsent(
                key,
                //TODO check thread safety
                lackingKey -> super.getValue(lackingKey).orElse(null)
        );
        return ofNullable(result);
    }

    @Override
    public void setValue(String key, String value) {
        this.inMemoryCache.put(key, value);
        super.setValue(key, value);
    }
}
