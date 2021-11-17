package pl.sg.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.sg.cache.service.CacheRequestHandler;
import pl.sg.cache.service.DBCache;
import pl.sg.cache.service.InMemoryCache;

@SpringBootApplication
public class CacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheApplication.class, args);
    }

    @Bean
    public static CacheRequestHandler getCacheEntryProcessor(CacheEntryRepository cacheEntryRepository) {
        return new InMemoryCache(
                new DBCache(cacheEntryRepository)
        );
    }
}
