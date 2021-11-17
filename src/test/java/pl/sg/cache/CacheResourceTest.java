package pl.sg.cache;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import pl.sg.cache.service.CacheRequestHandler;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CacheResourceTest {

    @Autowired CacheEntryRepository cacheEntryRepository;
    @Autowired
    CacheRequestHandler cache;
    private static RequestSpecification defaultSpecification;

    @BeforeAll
    public static void beforeAll(@LocalServerPort int localPort) {
        System.out.println("localPort = " + localPort);
        defaultSpecification = given()
                .basePath("/cache")
                .contentType(ContentType.TEXT)
                .port(localPort)
                .filter(new OpenApiValidationFilter("api.yml"))
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
    }

    @Test
    public void shouldReturnValue() {
        String response = given().spec(defaultSpecification)
                .when().get("first")
                .then().contentType(ContentType.TEXT).statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("1", response);
    }

    @Test
    public void shouldNotHitDatabaseIfValueExistsInMemory() {
        String response = given().spec(defaultSpecification)
                .when().get("first")
                .then().contentType(ContentType.TEXT).statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("1", response);
        cacheEntryRepository.save(new CacheEntry("first", "other value"));
        response = given().spec(defaultSpecification)
                .when().get("first")
                .then().contentType(ContentType.TEXT).statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("1", response);
        cache.setValue("first", null);
        response = given().spec(defaultSpecification)
                .when().get("first")
                .then().contentType(ContentType.TEXT).statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("other value", response);

    }

    @Test
    public void shouldAcceptValue() {
        given().spec(defaultSpecification).body("new value")
                .when().post("a")
                .then().statusCode(200);
    }
}