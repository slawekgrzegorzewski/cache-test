package pl.sg.db;

import org.springframework.stereotype.Component;
import pl.sg.cache.CacheEntryFetcher;

import java.util.Optional;

@Component
public class ReadCacheEntryFromTable implements CacheEntryFetcher {

    private final CacheEntryRepository cacheEntryRepository;

    public ReadCacheEntryFromTable(CacheEntryRepository cacheEntryRepository) {
        this.cacheEntryRepository = cacheEntryRepository;
    }

    @Override
    public Optional<String> fetch(String key) {
        return cacheEntryRepository.findById(key).map(CacheEntry::getValue);
    }
}
