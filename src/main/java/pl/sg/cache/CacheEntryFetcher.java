package pl.sg.cache;

import java.util.Optional;

public interface CacheEntryFetcher {
    Optional<String> fetch(String key);
}
