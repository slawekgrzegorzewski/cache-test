package pl.sg;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pl.sg.cache.Cache;
import pl.sg.db.CacheEntry;
import pl.sg.db.CacheEntryRepository;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class BaseCacheApplicationTest {

    protected final static String key = "first";
    protected final static String initialValue = "1";
    protected final static String newValue = "new value";

    protected final CacheEntryRepository cacheEntryRepository;
    protected final Cache cache;

    protected static RequestSpecification cacheEndpointSpecification;
    protected static RequestSpecification testCacheEndpointSpecification;

    protected BaseCacheApplicationTest(CacheEntryRepository cacheEntryRepository, Cache cache) {
        this.cacheEntryRepository = cacheEntryRepository;
        this.cache = cache;
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setInitialState() {
        cacheEndpointSpecification = given()
                .port(port)
                .basePath("/cache")
                .contentType(ContentType.TEXT)
                .filter(new OpenApiValidationFilter("api.yml"))
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());

        testCacheEndpointSpecification = given()
                .port(port)
                .basePath("/cache-test")
                .contentType(ContentType.TEXT)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());

        cacheEntryRepository.deleteAll();
        cacheEntryRepository.save(new CacheEntry(key, initialValue));
        cache.reset();
    }

    @Test
    public void shouldReturnValue() {
        readFromApiAndAssertEquals(cacheEndpointSpecification, key, initialValue);
    }

    @Test
    public void shouldNotHitDatabaseIfValueExistsInMemory() {

        //get value in order to fetch it from db
        readFromApiAndAssertEquals(cacheEndpointSpecification, key, initialValue);

        //change value in db
        cacheEntryRepository.save(new CacheEntry(key, newValue));

        //get the same key and make sure it didn't change, what means cache hasn't polled value from DB
        readFromApiAndAssertEquals(cacheEndpointSpecification, key, initialValue);

        //when value is not present it will be read from db again
        cache.removeValue(key);
        readFromApiAndAssertEquals(cacheEndpointSpecification, key, newValue);

    }

    @Test
    public void shouldAcceptValue() {
        updateValueUsingCacheApi(cacheEndpointSpecification, key, initialValue);
    }

    @Test
    public void allEndpointUsingTheSameCacheShouldBeSynchronized() {

        readFromApiAndAssertEquals(cacheEndpointSpecification, key, initialValue);

        readFromApiAndAssertEquals(testCacheEndpointSpecification, key, initialValue);

        updateValueUsingCacheApi(cacheEndpointSpecification, key, newValue);

        readFromApiAndAssertEquals(cacheEndpointSpecification, key, newValue);

        readFromApiAndAssertEquals(testCacheEndpointSpecification, key, newValue);
    }

    @Test
    public void removingValueShouldNotDeleteItFromDB() {

        readFromApiAndAssertEquals(cacheEndpointSpecification, key, initialValue);

        removeFromCacheApi(key, cacheEndpointSpecification, 204);

        Assertions.assertTrue(cacheEntryRepository.findById(key).isPresent());
    }

    @Test
    public void removingNotExistingKeyShouldReturnOK() {
        removeFromCacheApi("not existing key", cacheEndpointSpecification, 404);
    }


    protected void readFromApiAndAssertEquals(RequestSpecification endpointSpecification, String key, String expectedValue) {
        String response = readFromCacheApi(endpointSpecification, key);
        Assertions.assertEquals(expectedValue, response);
    }

    protected String readFromCacheApi(RequestSpecification endpointSpecification, String key) {
        return given().spec(endpointSpecification)
                .when().get(key)
                .then().contentType(ContentType.TEXT).statusCode(200)
                .extract().body().asString();
    }

    protected void updateValueUsingCacheApi(RequestSpecification endpointSpecification, String key, String value) {
        given().spec(endpointSpecification).body(value)
                .when().put(key)
                .then().statusCode(204);
    }

    protected void removeFromCacheApi(String key, RequestSpecification endpointSpecification, int expectedStatusCode) {
        given().spec(endpointSpecification)
                .when().delete(key)
                .then().statusCode(expectedStatusCode);
    }

}
