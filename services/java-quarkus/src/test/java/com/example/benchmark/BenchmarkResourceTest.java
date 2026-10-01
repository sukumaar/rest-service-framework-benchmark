package com.example.benchmark;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BenchmarkResourceTest {
    @Test
    void healthReturnsOk() {
        given()
                .when().get("/health")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("ok"));
    }

    @Test
    void jsonReturnsGreeting() {
        given()
                .when().get("/json")
                .then().statusCode(200)
                .body("message", equalTo("Hello, World!"));
    }

    @Test
    void echoReturnsRequestBody() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"test\",\"values\":[1,2]}")
                .when().post("/echo")
                .then().statusCode(200)
                .body("name", equalTo("test"))
                .body("values", equalTo(java.util.List.of(1, 2)));
    }

    @Test
    void itemsUsesDefaultCount() {
        given()
                .when().get("/items")
                .then().statusCode(200)
                .body("$", hasSize(100));
    }

    @Test
    void itemsUsesRequestedCountAndDeterministicValues() {
        given()
                .queryParam("count", 2)
                .when().get("/items")
                .then().statusCode(200)
                .body("$", hasSize(2))
                .body("[0].id", equalTo(0))
                .body("[0].name", equalTo("Item 0"))
                .body("[1].id", equalTo(1))
                .body("[1].name", equalTo("Item 1"));
    }

    @Test
    void itemsRejectsInvalidCount() {
        given()
                .queryParam("count", -1)
                .when().get("/items")
                .then().statusCode(400);
    }
}
