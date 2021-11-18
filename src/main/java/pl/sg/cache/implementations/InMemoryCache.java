package pl.sg.cache.implementations;

import lombok.NonNull;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheChain;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

public class InMemoryCache extends CacheChain {
    //used because of atomicity of put operations family, but performance should be checked
    private final Map<String, String> inMemoryCache = new ConcurrentHashMap<>();

    public InMemoryCache() {
        super(null);
    }

    public InMemoryCache(Cache nextHandler) {
        super(nextHandler);
    }

    @Override
    public Optional<String> getValue(@NonNull String key) {
        String result = inMemoryCache.computeIfAbsent(
                key,
                lackingKey -> super.getValue(lackingKey).orElse(null)
        );
        return ofNullable(result);
    }

    @Override
    public void setValue(@NonNull String key, @NonNull String value) {
        super.setValue(key, value);
        this.inMemoryCache.put(key, value);
    }

    @Override
    public void removeValue(@NonNull String key) {
        super.removeValue(key);
        this.inMemoryCache.remove(key);
    }

    @Override
    public void reset() {
        super.reset();
        this.inMemoryCache.clear();
    }
}
