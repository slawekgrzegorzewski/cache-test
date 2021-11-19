package pl.sg.cache.implementations.examples;

import org.junit.jupiter.api.Test;
import pl.sg.cache.Cache;
import pl.sg.cache.implementations.examples.CappedInMemoryCache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CappedInMemoryCacheTest {

    @Test
    public void exceedingSizeShouldEvictTheOldestEntry() {
        //given
        String firstKey = "a";
        String firstValue = "1";
        String secondKey = "b";
        String secondValue = "2";
        String thirdKey = "c";
        String thirdValue = "3";

        Cache cache = new CappedInMemoryCache();

        cache.setValue(firstKey, firstValue);
        cache.setValue(secondKey, secondValue);

        assertEquals(firstValue, cache.getValue(firstKey).orElseThrow());
        assertEquals(secondValue, cache.getValue(secondKey).orElseThrow());

        //when
        cache.setValue(thirdKey, thirdValue);

        //then
        //capacity of the cache is two so first element should no longer exist on cache
        assertTrue(cache.getValue(firstKey).isEmpty());
        assertEquals(secondValue, cache.getValue(secondKey).orElseThrow());
        assertEquals(thirdValue, cache.getValue(thirdKey).orElseThrow());
    }
}