package pl.sg.cache.implementations;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheEntryFetcher;

class CacheFallbackTest {

    private final static String key = "key";
    private final static String value = "value";

    @Test
    public void mustNotInteractWithValueFetcherWhenSettingValue() {

        //given
        CacheEntryFetcher cacheEntryFetcher = Mockito.mock(CacheEntryFetcher.class);
        Cache cache = new CacheFallback(cacheEntryFetcher);

        //when
        cache.setValue(key, value);

        //then
        Mockito.verifyNoInteractions(cacheEntryFetcher);
    }

}