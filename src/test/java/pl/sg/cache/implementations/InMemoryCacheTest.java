package pl.sg.cache.implementations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.sg.cache.Cache;
import pl.sg.utils.BasicCacheEntryFetcher;
import pl.sg.utils.DelayedSetCache;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryCacheTest {

    private final static String key = "a";
    private final static String initialValue = "1";
    private final static String newValue = "2";

    @Test
    public void getsValueFromFallbackWhenValueNotPresent() {
        //given
        BasicCacheEntryFetcher cacheEntryFetcher = new BasicCacheEntryFetcher(initialValue);
        Cache cache = new InMemoryCache(new CacheFallback(cacheEntryFetcher));

        //when
        String valueFromCache = cache.getValue(key).orElseThrow();

        //then
        assertEquals(initialValue, valueFromCache);
    }

    @Test
    public void dontGetValueFromFallbackWhenValuePresent() {
        //given;
        BasicCacheEntryFetcher cacheEntryFetcher = new BasicCacheEntryFetcher(initialValue);
        Cache cache = new InMemoryCache(new CacheFallback(cacheEntryFetcher));
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

    @Test
    public void throwsNullPointerExceptionWhenCallingAnyMethodWithNullArguments() {

        //given
        Cache cache = new InMemoryCache(new DelayedSetCache(Map.of(key, initialValue)));

        //when
        //then
        Assertions.assertThrows(NullPointerException.class, ()-> cache.getValue(null));
        Assertions.assertThrows(NullPointerException.class, ()-> cache.setValue(null, ""));
        Assertions.assertThrows(NullPointerException.class, ()-> cache.setValue("", null));
        Assertions.assertThrows(NullPointerException.class, ()-> cache.removeValue(null));
    }
}