package pl.sg;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
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
class IncorrectRequestsCacheApplicationTest {

    private final static String key = "first";
    private final static String initialValue = "1";
    private final static String newValue = "new value";

    @Autowired
    CacheEntryRepository cacheEntryRepository;

    @Autowired
    @Qualifier("inMemory")
    Cache cache;

    private static RequestSpecification cacheEndpointSpecification;

    @BeforeAll
    public static void init() {
        cacheEndpointSpecification = given()
                .basePath("/cache")
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
    public void test404WhenNoGetPathVariable() {
        given().spec(cacheEndpointSpecification)
                .when().get()
                .then().statusCode(404)
                .extract().body().asString();
    }

    @Test
    public void test404WhenNoSetPathVariable() {
        given().spec(cacheEndpointSpecification).body("value")
                .when().put()
                .then().statusCode(404)
                .extract().body().asString();
    }

    @Test
    public void test400WhenNoSetBody() {
        given().spec(cacheEndpointSpecification)
                .when().put("key")
                .then().statusCode(400)
                .extract().body().asString();
    }

    @Test
    public void test404WhenNoDeletePathVariable() {
        given().spec(cacheEndpointSpecification)
                .when().delete()
                .then().statusCode(404)
                .extract().body().asString();
    }

    @Test
    public void test404WhenDeletingNotExistingKey() {
        given().spec(cacheEndpointSpecification)
                .when().delete("not existing key")
                .then().statusCode(404)
                .extract().body().asString();
    }

}
