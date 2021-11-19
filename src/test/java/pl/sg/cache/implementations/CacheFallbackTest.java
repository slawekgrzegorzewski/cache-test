package pl.sg.cache.implementations;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheEntryFetcher;
import pl.sg.utils.BasicCacheEntryFetcher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheFallbackTest {

    private final static String key = "key";
    private final static String value = "value";

    @Test
    public void doesNotCacheValueFromFetcher() {

        //given
        String firstValue = "A";
        String secondValue = "B";
        BasicCacheEntryFetcher cacheEntryFetcher = new BasicCacheEntryFetcher(firstValue);
        Cache cache = new CacheFallback(cacheEntryFetcher);
        assertEquals(firstValue, cache.getValue(key).orElseThrow());

        //when
        cacheEntryFetcher.setValue(secondValue);
        String secondGetResult = cache.getValue(key).orElseThrow();

        //then
        assertEquals(secondValue, secondGetResult);
    }
}