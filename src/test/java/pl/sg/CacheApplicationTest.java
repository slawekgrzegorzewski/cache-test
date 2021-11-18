package pl.sg;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.sg.cache.Cache;
import pl.sg.db.CacheEntry;
import pl.sg.db.CacheEntryRepository;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class CacheApplicationTest {

    private final static String key = "first";
    private final static String initialValue = "1";
    private final static String newValue = "new value";

    @Autowired
    CacheEntryRepository cacheEntryRepository;

    @Autowired
    @Qualifier("inMemory")
    Cache cache;

    private static RequestSpecification cacheEndpointSpecification;
    private static RequestSpecification testCacheEndpointSpecification;

    @BeforeAll
    public static void init() {
        cacheEndpointSpecification = given()
                .basePath("/cache")
                .contentType(ContentType.TEXT)
                .filter(new OpenApiValidationFilter("api.yml"))
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());

        testCacheEndpointSpecification = given()
                .basePath("/cache-test")
                .contentType(ContentType.TEXT)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
    }

    @AfterEach
    public void restoreOriginalState() {
        cacheEntryRepository.deleteAll();
        cacheEntryRepository.save(new CacheEntry(key, initialValue));
        cache.reset();
    }

    @Test
    public void shouldReturnValue() {
        String response = readFromCacheApi(cacheEndpointSpecification);
        Assertions.assertEquals(initialValue, response);
    }

    @Test
    public void shouldNotHitDatabaseIfValueExistsInMemory() {

        //get value in order to fetch it from db
        String response = readFromCacheApi(cacheEndpointSpecification);
        Assertions.assertEquals(initialValue, response);

        //change value in db
        cacheEntryRepository.save(new CacheEntry(key, newValue));

        //get the same key and make sure it didn't change, what means cache hasn't polled value from DB
        response = readFromCacheApi(cacheEndpointSpecification);
        Assertions.assertEquals(initialValue, response);

        //when value is not present it will be read from db again
        cache.removeValue(key);
        response = readFromCacheApi(cacheEndpointSpecification);
        Assertions.assertEquals(newValue, response);

    }

    @Test
    public void shouldAcceptValue() {
        updateValueUsingCacheApi(initialValue);
    }

    @Test
    public void allEndpointUsingTheSameCacheShouldBeSynchronized() {

        String response = readFromCacheApi(cacheEndpointSpecification);
        Assertions.assertEquals(initialValue, response);

        updateValueUsingCacheApi(newValue);

        response = readFromCacheApi(testCacheEndpointSpecification);
        Assertions.assertEquals(newValue, response);

    }

    @Test
    public void removingValueShouldNotDeleteItFromDB() {

        String response = readFromCacheApi(cacheEndpointSpecification);
        Assertions.assertEquals(initialValue, response);

        removeFromCacheApi(key);

        cacheEntryRepository.save(new CacheEntry(key, newValue));
        response = readFromCacheApi(testCacheEndpointSpecification);
        Assertions.assertEquals(newValue, response);
        Assertions.assertTrue(cacheEntryRepository.findById(key).isPresent());
    }

    @Test
    public void removingNotExistingKeyShouldReturnOK() {
        removeFromCacheApi("not existing key");
    }

    private String readFromCacheApi(RequestSpecification endpointSpecification) {
        return given().spec(endpointSpecification)
                .when().get(key)
                .then().contentType(ContentType.TEXT).statusCode(200)
                .extract().body().asString();
    }

    private void updateValueUsingCacheApi(String value) {
        given().spec(cacheEndpointSpecification).body(value)
                .when().post(key)
                .then().statusCode(200);
    }

    private void removeFromCacheApi(String key) {
        given().spec(cacheEndpointSpecification)
                .when().delete(key)
                .then().statusCode(200);
    }

}
