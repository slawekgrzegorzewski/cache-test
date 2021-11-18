package pl.sg.cache.implementations.examples;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import lombok.NonNull;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheChain;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public class CappedInMemoryCache extends CacheChain {
    //used because of atomicity of put operations family, but performance should be checked
    private final ConcurrentLinkedHashMap<String, String> inMemoryCache = new ConcurrentLinkedHashMap.Builder<String, String>()
            .maximumWeightedCapacity(2)
            .build();

    public CappedInMemoryCache() {
        super(null);
    }

    public CappedInMemoryCache(Cache nextHandler) {
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
