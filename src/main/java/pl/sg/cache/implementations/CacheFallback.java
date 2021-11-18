package pl.sg.cache.implementations;

import lombok.NonNull;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheChain;
import pl.sg.cache.CacheEntryFetcher;

import java.util.Optional;

public class CacheFallback extends CacheChain {
    private final CacheEntryFetcher cacheEntryFetcher;

    public CacheFallback(CacheEntryFetcher cacheEntryFetcher) {
        super(null);
        this.cacheEntryFetcher = cacheEntryFetcher;
    }

    public CacheFallback(Cache nextHandler, CacheEntryFetcher cacheEntryFetcher) {
        super(nextHandler);
        this.cacheEntryFetcher = cacheEntryFetcher;
    }

    @Override
    public Optional<String> getValue(@NonNull String key) {
        return cacheEntryFetcher.fetch(key).or(() -> super.getValue(key));
    }
}
