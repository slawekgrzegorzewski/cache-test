package pl.sg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheEntryFetcher;
import pl.sg.cache.implementations.CacheFallback;
import pl.sg.cache.implementations.InMemoryCache;
import pl.sg.cache.implementations.examples.SynchronizedReadWriteCache;

@SpringBootApplication
public class CacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheApplication.class, args);
    }

    @Bean("inMemory")
    public static Cache getCache(CacheFallback cacheFallback) {
        return new InMemoryCache(cacheFallback);
    }

    @Bean("readWriteBlocking")
    public static Cache getCache2(@Qualifier("inMemory") Cache cache) {
        return new SynchronizedReadWriteCache(cache);
    }

    @Bean
    public static CacheFallback getCacheFallback(CacheEntryFetcher cacheEntryFetcher) {
        return new CacheFallback(cacheEntryFetcher);
    }
}
