package pl.sg.cache.service;

import org.junit.jupiter.api.Test;
import pl.sg.cache.CacheEntryRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentCacheUsageTest {

    private static CacheEntryRepository inMemory;

    @Test
    public void init() throws InterruptedException {
        CacheRequestHandler handler = new CacheRequestHandler(
                new CacheRequestHandler() {
                    Map<String, String> cache = new HashMap<>();

                    @Override
                    public Optional<String> getValue(String key) {
                        return Optional.ofNullable(cache.get(key));
                    }

                    @Override
                    public void setValue(String key, String value) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        cache.put(key, value);
                    }
                }
        );

        handler.setValue("a", "1");
        assertEquals("1", handler.getValue("a").orElseThrow());
        new Thread(() -> handler.setValue("a", "2")).start();
        Thread.sleep(500);
        assertEquals("2", handler.getValue("a").orElseThrow());
    }

}