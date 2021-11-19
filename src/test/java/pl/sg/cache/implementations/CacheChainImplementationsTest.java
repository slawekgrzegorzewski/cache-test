package pl.sg.cache.implementations;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheEntryFetcher;
import pl.sg.cache.implementations.examples.CappedInMemoryCache;
import pl.sg.cache.implementations.examples.SynchronizedReadWriteCache;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.mockito.Mockito.inOrder;

class CacheChainImplementationsTest {

    private final static String key = "a";
    private final static String initialValue = "1";

    @ParameterizedTest
    @MethodSource("cacheProvidersForGetRequestNotExistingValue")
    public void shouldForwardGetRequestIfValueNotPresent(Function<Cache, Cache> testCacheProvider) {

        //given
        Cache nextCacheInChain = Mockito.mock(Cache.class);
        Cache cache = testCacheProvider.apply(nextCacheInChain);

        //when
        cache.getValue(key);

        //then
        inOrder(nextCacheInChain).verify(nextCacheInChain, Mockito.calls(1)).getValue(key);
    }

    static Stream<Function<Cache, Cache>> cacheProvidersForGetRequestNotExistingValue() {
        return cacheProvidersForGetRequest(noValue());
    }

    @ParameterizedTest
    @MethodSource("cacheProvidersForGetRequestAndExistingValue")
    public void shouldNotForwardGetRequestIfValuePresent(Function<Cache, Cache> testCacheProvider) {
        //given
        Cache nextCacheInChain = Mockito.mock(Cache.class);
        Cache cache = testCacheProvider.apply(nextCacheInChain);
        cache.setValue(key, initialValue);

        //when
        cache.getValue(key);

        //then
        inOrder(nextCacheInChain).verify(nextCacheInChain, Mockito.never()).getValue(key);
    }

    static Stream<Function<Cache, Cache>> cacheProvidersForGetRequestAndExistingValue() {
        return cacheProvidersForGetRequest(withValue());
    }

    @ParameterizedTest
    @MethodSource("cacheProvidersForOtherRequests")
    public void shouldAlwaysForwardSetRequest(Function<Cache, Cache> testCacheProvider) {
        //given
        Cache nextCacheInChain = Mockito.mock(Cache.class);
        Cache cache = testCacheProvider.apply(nextCacheInChain);

        //when
        cache.setValue(key, initialValue);

        //then
        inOrder(nextCacheInChain).verify(nextCacheInChain, Mockito.calls(1)).setValue(key, initialValue);
    }

    @ParameterizedTest
    @MethodSource("cacheProvidersForOtherRequests")
    public void shouldAlwaysForwardResetRequests(Function<Cache, Cache> testCacheProvider) {
        //given
        Cache nextCacheInChain = Mockito.mock(Cache.class);
        Cache cache = testCacheProvider.apply(nextCacheInChain);

        //when
        cache.reset();

        //then
        inOrder(nextCacheInChain).verify(nextCacheInChain, Mockito.calls(1)).reset();
    }

    @ParameterizedTest
    @MethodSource("cacheProvidersForOtherRequests")
    public void shouldAlwaysForwardRemoveRequests(Function<Cache, Cache> testCacheProvider) {
        //given
        Cache nextCacheInChain = Mockito.mock(Cache.class);
        Cache cache = testCacheProvider.apply(nextCacheInChain);

        //when
        cache.removeValue(key);

        //then
        inOrder(nextCacheInChain).verify(nextCacheInChain, Mockito.calls(1)).removeValue(key);
    }

    static Stream<Function<Cache, Cache>> cacheProvidersForOtherRequests() {
        return Stream.concat(cacheProvidersForOtherRequests(noValue()), cacheProvidersForOtherRequests(withValue()));
    }

    private static Stream<Function<Cache, Cache>> cacheProvidersForGetRequest(CacheEntryFetcher fetcher) {
        return Stream.of(
                InMemoryCache::new,
                cache -> new CacheFallback(cache, fetcher),
                CappedInMemoryCache::new
        );
    }

    private static Stream<Function<Cache, Cache>> cacheProvidersForOtherRequests(CacheEntryFetcher fetcher) {
        return Stream.of(
                InMemoryCache::new,
                cache -> new CacheFallback(cache, fetcher),
                CappedInMemoryCache::new,
                SynchronizedReadWriteCache::new
        );
    }

    private static CacheEntryFetcher noValue() {
        CacheEntryFetcher fetcher = Mockito.mock(CacheEntryFetcher.class);
        Mockito.when(fetcher.fetch(key)).thenReturn(Optional.empty());
        return fetcher;
    }

    private static CacheEntryFetcher withValue() {
        CacheEntryFetcher fetcher = Mockito.mock(CacheEntryFetcher.class);
        Mockito.when(fetcher.fetch(key)).thenReturn(Optional.of(initialValue));
        return fetcher;
    }
}