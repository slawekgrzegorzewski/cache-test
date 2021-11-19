package pl.sg.utils;

import pl.sg.cache.CacheEntryFetcher;

import java.util.Optional;

public class BasicCacheEntryFetcher implements CacheEntryFetcher {
    private String value;

    public BasicCacheEntryFetcher(String value) {
        this.value = value;
    }

    @Override
    public Optional<String> fetch(String key) {
        return Optional.of(value);
    }

    public void setValue(String value) {
        this.value = value;
    }
}
