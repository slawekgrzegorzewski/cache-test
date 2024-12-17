package pl.sg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import pl.sg.cache.Cache;
import pl.sg.db.CacheEntry;
import pl.sg.db.CacheEntryRepository;

@ActiveProfiles(value = {"test", "cappedInMemoryCache"})
class CappedInMemoryCacheApplicationTest extends BaseCacheApplicationTest {

    protected CappedInMemoryCacheApplicationTest(
            @Autowired CacheEntryRepository cacheEntryRepository,
            @Autowired Cache cache) {
        super(cacheEntryRepository, cache);
    }

    @Test
    @Override
    public void shouldNotHitDatabaseIfValueExistsInMemory() {
        //this is handled differently in CappedInMemoryCache - default values from DB are never stored under a key

        //get value in order to fetch it from db
        readFromApiAndAssertEquals(cacheEndpointSpecification, key, initialValue);

        //change value in db
        cacheEntryRepository.save(new CacheEntry(key, newValue));

        //get the same key and make sure it didn't change, what means cache hasn't polled value from DB
        readFromApiAndAssertEquals(cacheEndpointSpecification, key, newValue);

        //when value is not present it will be read from db again
        cache.removeValue(key);
        readFromApiAndAssertEquals(cacheEndpointSpecification, key, newValue);

    }

    @Test
    @Override
    public void removingValueShouldNotDeleteItFromDB() {

        readFromApiAndAssertEquals(cacheEndpointSpecification, key, initialValue);

        //this key was never loaded to a cache so this call should return 404
        removeFromCacheApi(key, cacheEndpointSpecification, 404);

        Assertions.assertTrue(cacheEntryRepository.findById(key).isPresent());
    }

    @Test
    public void shouldRemoveTheOldestEntryWhenLimitReached() {
        cacheEntryRepository.deleteAll();
        cache.reset();
        cacheEntryRepository.save(new CacheEntry("1", "one"));
        cacheEntryRepository.save(new CacheEntry("2", "two"));
        cacheEntryRepository.save(new CacheEntry("3", "three"));

        readFromApiAndAssertEquals(cacheEndpointSpecification, "1", "one");
        readFromApiAndAssertEquals(cacheEndpointSpecification, "2", "two");
        readFromApiAndAssertEquals(cacheEndpointSpecification, "3", "three");

        updateValueUsingCacheApi(cacheEndpointSpecification, "1", "jeden");

        readFromApiAndAssertEquals(cacheEndpointSpecification, "1", "jeden");
        readFromApiAndAssertEquals(cacheEndpointSpecification, "2", "two");
        readFromApiAndAssertEquals(cacheEndpointSpecification, "3", "three");

        updateValueUsingCacheApi(cacheEndpointSpecification, "2", "dwa");

        readFromApiAndAssertEquals(cacheEndpointSpecification, "1", "jeden");
        readFromApiAndAssertEquals(cacheEndpointSpecification, "2", "dwa");
        readFromApiAndAssertEquals(cacheEndpointSpecification, "3", "three");

        updateValueUsingCacheApi(cacheEndpointSpecification, "3", "trzy");

        //value for key = 1 is 'one' again meaning the override from this test was removed and cache fell back to DB
        readFromApiAndAssertEquals(cacheEndpointSpecification, "1", "one");
        readFromApiAndAssertEquals(cacheEndpointSpecification, "2", "dwa");
        readFromApiAndAssertEquals(cacheEndpointSpecification, "3", "trzy");
    }
}
