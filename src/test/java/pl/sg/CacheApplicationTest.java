package pl.sg;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    public void setInitialState() {
        cacheEntryRepository.deleteAll();
        cacheEntryRepository.save(new CacheEntry(key, initialValue));
        cache.reset();
    }

    @Test
    public void shouldReturnValue() {
        readFromApiAndAssertEquals(cacheEndpointSpecification, initialValue);
    }

    @Test
    public void shouldNotHitDatabaseIfValueExistsInMemory() {

        //get value in order to fetch it from db
        readFromApiAndAssertEquals(cacheEndpointSpecification, initialValue);

        //change value in db
        cacheEntryRepository.save(new CacheEntry(key, newValue));

        //get the same key and make sure it didn't change, what means cache hasn't polled value from DB
        readFromApiAndAssertEquals(cacheEndpointSpecification, initialValue);

        //when value is not present it will be read from db again
        cache.removeValue(key);
        readFromApiAndAssertEquals(cacheEndpointSpecification, newValue);

    }

    @Test
    public void shouldAcceptValue() {
        updateValueUsingCacheApi(initialValue, cacheEndpointSpecification);
    }

    @Test
    public void allEndpointUsingTheSameCacheShouldBeSynchronized() {

        readFromApiAndAssertEquals(cacheEndpointSpecification, initialValue);

        readFromApiAndAssertEquals(testCacheEndpointSpecification, initialValue);

        updateValueUsingCacheApi(newValue, cacheEndpointSpecification);

        readFromApiAndAssertEquals(cacheEndpointSpecification, newValue);

        readFromApiAndAssertEquals(testCacheEndpointSpecification, newValue);
    }

    @Test
    public void removingValueShouldNotDeleteItFromDB() {

        readFromApiAndAssertEquals(cacheEndpointSpecification, initialValue);

        removeFromCacheApi(key, cacheEndpointSpecification, 204);

        Assertions.assertTrue(cacheEntryRepository.findById(key).isPresent());
    }

    @Test
    public void removingNotExistingKeyShouldReturnOK() {
        removeFromCacheApi("not existing key", cacheEndpointSpecification, 404);
    }


    private void readFromApiAndAssertEquals(RequestSpecification endpointSpecification, String expectedValue) {
        String response = readFromCacheApi(endpointSpecification);
        Assertions.assertEquals(expectedValue, response);
    }

    private String readFromCacheApi(RequestSpecification endpointSpecification) {
        return given().spec(endpointSpecification)
                .when().get(key)
                .then().contentType(ContentType.TEXT).statusCode(200)
                .extract().body().asString();
    }

    private void updateValueUsingCacheApi(String value, RequestSpecification endpointSpecification) {
        given().spec(endpointSpecification).body(value)
                .when().put(key)
                .then().statusCode(204);
    }

    private void removeFromCacheApi(String key, RequestSpecification endpointSpecification, int expectedStatusCode) {
        given().spec(endpointSpecification)
                .when().delete(key)
                .then().statusCode(expectedStatusCode);
    }

}
