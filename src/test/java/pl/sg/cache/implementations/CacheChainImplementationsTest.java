package pl.sg.cache.implementations;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import pl.sg.cache.Cache;
import pl.sg.cache.implementations.examples.CappedInMemoryCache;
import pl.sg.cache.implementations.examples.SynchronizedReadWriteCache;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.mockito.Mockito.inOrder;

class CacheChainImplementationsTest {

    private final static String key = "a";
    private final static String initialValue = "1";

    @ParameterizedTest
    @MethodSource("cacheProviders")
    public void shouldForwardGetRequestIfValueNotPresent(Function<Cache, Cache> testCacheProvider) {

        //given
        Cache nextCacheInChain = Mockito.mock(Cache.class);
        Cache cache = testCacheProvider.apply(nextCacheInChain);

        //when
        cache.getValue(key);

        //then
        inOrder(nextCacheInChain).verify(nextCacheInChain, Mockito.calls(1)).getValue(key);
    }

    @ParameterizedTest
    @MethodSource("cacheProviders")
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

    @ParameterizedTest
    @MethodSource("cacheProviders")
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
    @MethodSource("cacheProviders")
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
    @MethodSource("cacheProviders")
    public void shouldAlwaysForwardRemoveRequests(Function<Cache, Cache> testCacheProvider) {
        //given
        Cache nextCacheInChain = Mockito.mock(Cache.class);
        Cache cache = testCacheProvider.apply(nextCacheInChain);

        //when
        cache.removeValue(key);

        //then
        inOrder(nextCacheInChain).verify(nextCacheInChain, Mockito.calls(1)).removeValue(key);
    }

    static Stream<Function<Cache, Cache>> cacheProviders() {
        return Stream.of(
                InMemoryCache::new,
                CappedInMemoryCache::new,
                cache -> new SynchronizedReadWriteCache(new InMemoryCache(cache))
        );
    }
}