package pl.sg.cache.implementations.examples;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheChain;
import pl.sg.cache.implementations.InMemoryCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SynchronizedReadWriteCacheTest {

    @Test
    public void setAndGetShouldBeSynchronized() throws InterruptedException {

        //given
        final String key = "a";
        final String initialValue = "1";
        final String newValue = "2";

        Cache cache = new SynchronizedReadWriteCache(
                new InMemoryCache(
                        delayedCache(key, initialValue)
                ));

        //when
        new Thread(() -> cache.setValue(key, newValue)).start();
        Thread.sleep(500);
        String valueFromCache = cache.getValue(key).orElseThrow();

        //then
        assertEquals(newValue, valueFromCache);
    }

    private static @NotNull CacheChain delayedCache(String key, String initialValue) {
        return new CacheChain() {
            private final Map<String, String> cache = new HashMap<>(Map.of(key, initialValue));

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
        };
    }

}