package pl.sg.cache.service;

import pl.sg.cache.CacheEntry;
import pl.sg.cache.CacheEntryRepository;

import java.util.Optional;

public class DBCache extends CacheRequestHandler {
    private final CacheEntryRepository cacheEntryRepository;

    public DBCache(CacheEntryRepository cacheEntryRepository) {
        super(null);
        this.cacheEntryRepository = cacheEntryRepository;
    }

    public DBCache(CacheRequestHandler nextHandler, CacheEntryRepository cacheEntryRepository) {
        super(nextHandler);
        this.cacheEntryRepository = cacheEntryRepository;
    }

    @Override
    public Optional<String> getValue(String key) {
        return cacheEntryRepository.findById(key)
                .map(CacheEntry::getValue).or(() -> super.getValue(key));
    }

    @Override
    public void setValue(String key, String value) {
        //don't save value to a database
        super.setValue(key, value);
    }
}
