package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceTest {

  private String uniqueName() {
    return "Store-" + UUID.randomUUID().toString().substring(0, 8);
  }

  @Test
  void shouldListAllStores() {
    given()
        .when()
        .get("/store")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(0));
  }

  @Test
  void shouldCreateAndGetStore() {
    String name = uniqueName();
    String storeJson = "{\"name\": \"" + name + "\", \"quantityProductsInStock\": 100}";

    Long id = given()
        .contentType(ContentType.JSON)
        .body(storeJson)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .body("name", is(name))
        .body("quantityProductsInStock", is(100))
        .extract()
        .jsonPath()
        .getLong("id");

    given().when().get("/store/" + id).then().statusCode(200).body("name", is(name));
  }

  @Test
  void shouldReturn404ForNonExistentStore() {
    given().when().get("/store/999999").then().statusCode(404);
  }

  @Test
  void shouldUpdateStore() {
    String name = uniqueName();
    Long id = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + name + "\", \"quantityProductsInStock\": 100}")
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    String updatedName = uniqueName();
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + updatedName + "\", \"quantityProductsInStock\": 200}")
        .when()
        .put("/store/" + id)
        .then()
        .statusCode(200)
        .body("name", is(updatedName))
        .body("quantityProductsInStock", is(200));
  }

  @Test
  void shouldFailUpdateWithoutName() {
    String name = uniqueName();
    Long id = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + name + "\", \"quantityProductsInStock\": 100}")
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    given()
        .contentType(ContentType.JSON)
        .body("{\"quantityProductsInStock\": 300}")
        .when()
        .put("/store/" + id)
        .then()
        .statusCode(422);
  }

  @Test
  void shouldFailUpdateNonExistentStore() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"Non Existent\", \"quantityProductsInStock\": 100}")
        .when()
        .put("/store/999998")
        .then()
        .statusCode(404);
  }

  @Test
  void shouldPatchStore() {
    String name = uniqueName();
    Long id = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + name + "\", \"quantityProductsInStock\": 100}")
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    String patchedName = uniqueName();
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + patchedName + "\", \"quantityProductsInStock\": 150}")
        .when()
        .patch("/store/" + id)
        .then()
        .statusCode(200)
        .body("name", is(patchedName));
  }

  @Test
  void shouldFailPatchWithoutName() {
    String name = uniqueName();
    Long id = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + name + "\", \"quantityProductsInStock\": 100}")
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    given()
        .contentType(ContentType.JSON)
        .body("{\"quantityProductsInStock\": 300}")
        .when()
        .patch("/store/" + id)
        .then()
        .statusCode(422);
  }

  @Test
  void shouldFailPatchNonExistentStore() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"Non Existent\", \"quantityProductsInStock\": 100}")
        .when()
        .patch("/store/999997")
        .then()
        .statusCode(404);
  }

  @Test
  void shouldDeleteStore() {
    String name = uniqueName();
    Long id = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + name + "\", \"quantityProductsInStock\": 100}")
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    given().when().delete("/store/" + id).then().statusCode(204);
  }

  @Test
  void shouldFailDeleteNonExistentStore() {
    given().when().delete("/store/999996").then().statusCode(404);
  }

  @Test
  void shouldFailCreateStoreWithId() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"id\": 1, \"name\": \"Invalid Store\", \"quantityProductsInStock\": 100}")
        .when()
        .post("/store")
        .then()
        .statusCode(422);
  }
}
