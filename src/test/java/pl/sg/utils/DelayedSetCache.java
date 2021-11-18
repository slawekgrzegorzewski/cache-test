package pl.sg.utils;

import org.springframework.lang.NonNull;
import pl.sg.cache.CacheChain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DelayedSetCache extends CacheChain {
    private final Map<String, String> cache;

    public DelayedSetCache(Map<String, String> cache) {
        this.cache = new HashMap<>(cache);
    }

    @Override
    public Optional<String> getValue(@NonNull String key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public void setValue(@NonNull String key, @NonNull String value) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cache.put(key, value);
    }
}
