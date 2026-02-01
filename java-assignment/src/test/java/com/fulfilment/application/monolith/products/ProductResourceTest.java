package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductResourceTest {

  private String uniqueName() {
    return "Product-" + UUID.randomUUID().toString().substring(0, 8);
  }

  @Test
  void shouldListAllProducts() {
    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(0));
  }

  @Test
  void shouldCreateAndGetProduct() {
    String name = uniqueName();
    String productJson = "{\"name\": \"" + name + "\", \"description\": \"Test\", \"price\": 29.99, \"stock\": 50}";

    Long id = given()
        .contentType(ContentType.JSON)
        .body(productJson)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .body("name", is(name))
        .body("stock", is(50))
        .extract()
        .jsonPath()
        .getLong("id");

    given().when().get("/product/" + id).then().statusCode(200).body("name", is(name));
  }

  @Test
  void shouldReturn404ForNonExistentProduct() {
    given().when().get("/product/999999").then().statusCode(404);
  }

  @Test
  void shouldUpdateProduct() {
    String name = uniqueName();
    Long id = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + name + "\", \"stock\": 50}")
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    String updatedName = uniqueName();
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + updatedName + "\", \"description\": \"Updated\", \"price\": 39.99, \"stock\": 100}")
        .when()
        .put("/product/" + id)
        .then()
        .statusCode(200)
        .body("name", is(updatedName))
        .body("stock", is(100));
  }

  @Test
  void shouldFailUpdateWithoutName() {
    String name = uniqueName();
    Long id = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + name + "\", \"stock\": 50}")
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    given()
        .contentType(ContentType.JSON)
        .body("{\"description\": \"No name\", \"stock\": 100}")
        .when()
        .put("/product/" + id)
        .then()
        .statusCode(422);
  }

  @Test
  void shouldFailUpdateNonExistentProduct() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"Non Existent\", \"stock\": 100}")
        .when()
        .put("/product/999998")
        .then()
        .statusCode(404);
  }

  @Test
  void shouldDeleteProduct() {
    String name = uniqueName();
    Long id = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + name + "\", \"stock\": 50}")
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    given().when().delete("/product/" + id).then().statusCode(204);
  }

  @Test
  void shouldFailDeleteNonExistentProduct() {
    given().when().delete("/product/999997").then().statusCode(404);
  }

  @Test
  void shouldFailCreateProductWithId() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"id\": 1, \"name\": \"Invalid Product\", \"stock\": 10}")
        .when()
        .post("/product")
        .then()
        .statusCode(422);
  }
}
