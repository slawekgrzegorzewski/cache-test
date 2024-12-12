package pl.sg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheEntryFetcher;
import pl.sg.cache.implementations.CacheFallback;
import pl.sg.cache.implementations.InMemoryCache;
import pl.sg.cache.implementations.examples.CappedInMemoryCache;
import pl.sg.cache.implementations.examples.SynchronizedReadWriteCache;

@SpringBootApplication
public class CacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheApplication.class, args);
    }

    @Bean
    @Profile("inMemory")
    public static Cache getCache(CacheEntryFetcher cacheEntryFetcher) {
        return new InMemoryCache(new CacheFallback(cacheEntryFetcher));
    }

    @Bean
    @Profile("readWriteBlocking")
    public static Cache getCache2(CacheEntryFetcher cacheEntryFetcher) {
        return new SynchronizedReadWriteCache(
                new InMemoryCache(
                        new CacheFallback(cacheEntryFetcher)));
    }

    @Bean
    @Profile("cappedInMemoryCache")
    public static Cache getCache3(CacheEntryFetcher cacheEntryFetcher) {
        return new CappedInMemoryCache(
                new CacheFallback(cacheEntryFetcher));
    }
}
