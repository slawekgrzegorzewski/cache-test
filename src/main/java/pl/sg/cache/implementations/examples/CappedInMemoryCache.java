package pl.sg.cache.implementations.examples;

import lombok.NonNull;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheChain;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class CappedInMemoryCache extends CacheChain {
    private static final int MAX_CAPACITY = 2;

    private final HashMap<String, String> inMemoryCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > MAX_CAPACITY;
        }
    };

    public CappedInMemoryCache() {
        super(null);
    }

    public CappedInMemoryCache(Cache nextHandler) {
        super(nextHandler);
    }

    @Override
    public Optional<String> getValue(@NonNull String key) {
        return ofNullable(inMemoryCache.get(key))
                .or(() -> super.getValue(key));
    }

    @Override
    public void setValue(@NonNull String key, @NonNull String value) {
        super.setValue(key, value);
        this.inMemoryCache.put(key, value);
    }

    @Override
    public RemovalStatus removeValue(@NonNull String key) {
        super.removeValue(key);
        return this.inMemoryCache.remove(key) == null ? RemovalStatus.NOT_FOUND : RemovalStatus.REMOVED;
    }

    @Override
    public void reset() {
        super.reset();
        this.inMemoryCache.clear();
    }
}
