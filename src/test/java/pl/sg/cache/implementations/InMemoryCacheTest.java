package pl.sg.cache.implementations;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.sg.cache.Cache;
import pl.sg.utils.DelayedSetCache;
import pl.sg.utils.ImmutableSetCache;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;

class InMemoryCacheTest {

    private final static String key = "a";
    private final static String initialValue = "1";
    private final static String newValue = "2";

    @Test
    public void getsValueFromFallbackWhenValueNotPresent() {
        //given
        Cache cache = new InMemoryCache(new ImmutableSetCache(Map.of(key, initialValue)));

        //when
        String valueFromCache = cache.getValue(key).orElseThrow();

        //then
        assertEquals(initialValue, valueFromCache);
    }

    @Test
    public void dontGetValueFromFallbackWhenValuePresent() {
        //given;
        Cache cache = new InMemoryCache(new ImmutableSetCache(Map.of(key, initialValue)));
        cache.setValue(key, newValue);

        //when
        String valueFromCache = cache.getValue(key).orElseThrow();

        //then
        assertEquals(newValue, valueFromCache);
    }

    @Test
    public void setAndGetShouldNotBeSynchronized() throws InterruptedException {

        //given
        Cache cache = new InMemoryCache(new DelayedSetCache(Map.of(key, initialValue)));

        //when
        new Thread(() -> cache.setValue(key, newValue)).start();
        Thread.sleep(500);
        String valueFromCache = cache.getValue(key).orElseThrow();

        //then
        //at this moment value read from a cache should not be changed by set call
        assertEquals(initialValue, valueFromCache);
    }
}