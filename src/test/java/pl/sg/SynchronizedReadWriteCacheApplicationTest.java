package pl.sg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import pl.sg.cache.Cache;
import pl.sg.db.CacheEntryRepository;

@ActiveProfiles(value = {"test", "readWriteBlocking"})
class SynchronizedReadWriteCacheApplicationTest extends BaseCacheApplicationTest {

    protected SynchronizedReadWriteCacheApplicationTest(
            @Autowired CacheEntryRepository cacheEntryRepository,
            @Autowired Cache cache) {
        super(cacheEntryRepository, cache);
    }
}
