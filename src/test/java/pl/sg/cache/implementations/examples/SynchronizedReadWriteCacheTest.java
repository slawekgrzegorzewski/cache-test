package pl.sg.cache.implementations.examples;

import org.junit.jupiter.api.Test;
import pl.sg.cache.Cache;
import pl.sg.cache.implementations.InMemoryCache;
import pl.sg.cache.implementations.examples.SynchronizedReadWriteCache;
import pl.sg.utils.DelayedSetCache;

import java.util.Map;

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
                        new DelayedSetCache(Map.of(key, initialValue))
                ));

        //when
        new Thread(() -> cache.setValue(key, newValue)).start();
        Thread.sleep(500);
        String valueFromCache = cache.getValue(key).orElseThrow();

        //then
        assertEquals(newValue, valueFromCache);
    }

}