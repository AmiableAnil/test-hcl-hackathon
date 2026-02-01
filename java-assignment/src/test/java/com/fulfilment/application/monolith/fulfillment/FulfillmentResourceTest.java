package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfillmentResourceTest {

  private String uniqueCode() {
    return "MWH." + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private String uniqueName() {
    return "Name-" + UUID.randomUUID().toString().substring(0, 8);
  }

  @Test
  void shouldListAllFulfillments() {
    given()
        .when()
        .get("/fulfillment")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(0));
  }

  @Test
  void shouldCreateAndGetFulfillmentAssociation() {
    // Create test data
    String productName = uniqueName();
    Long productId = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + productName + "\", \"description\": \"Test\"}")
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    String storeName = uniqueName();
    Long storeId = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + storeName + "\", \"quantityProductsInStock\": 100}")
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    String warehouseCode = uniqueCode();
    given()
        .contentType(ContentType.JSON)
        .body("{\"businessUnitCode\": \"" + warehouseCode + "\", \"location\": \"AMSTERDAM-002\", \"capacity\": 20, \"stock\": 5}")
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    // Create fulfillment association
    Long associationId = given()
        .contentType(ContentType.JSON)
        .body("{\"productId\": " + productId + ", \"warehouseCode\": \"" + warehouseCode + "\", \"storeId\": " + storeId + "}")
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201)
        .body("productId", is(productId.intValue()))
        .body("warehouseCode", is(warehouseCode))
        .body("storeId", is(storeId.intValue()))
        .extract()
        .jsonPath()
        .getLong("id");

    // Test get by filters
    given()
        .queryParam("productId", productId)
        .when()
        .get("/fulfillment")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));

    given()
        .queryParam("warehouseCode", warehouseCode)
        .when()
        .get("/fulfillment")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));

    given()
        .queryParam("storeId", storeId)
        .when()
        .get("/fulfillment")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));

    // Delete association
    given().when().delete("/fulfillment/" + associationId).then().statusCode(204);
  }

  @Test
  void shouldFailToCreateWithNonExistentProduct() {
    String storeName = uniqueName();
    Long storeId = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + storeName + "\", \"quantityProductsInStock\": 100}")
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    String warehouseCode = uniqueCode();
    given()
        .contentType(ContentType.JSON)
        .body("{\"businessUnitCode\": \"" + warehouseCode + "\", \"location\": \"AMSTERDAM-001\", \"capacity\": 15, \"stock\": 5}")
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\": 999999, \"warehouseCode\": \"" + warehouseCode + "\", \"storeId\": " + storeId + "}")
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(400);
  }

  @Test
  void shouldFailToCreateWithNonExistentWarehouse() {
    String productName = uniqueName();
    Long productId = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + productName + "\"}")
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    String storeName = uniqueName();
    Long storeId = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + storeName + "\", \"quantityProductsInStock\": 100}")
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\": " + productId + ", \"warehouseCode\": \"MWH.NOTEXIST999\", \"storeId\": " + storeId + "}")
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(400);
  }

  @Test
  void shouldFailToCreateWithNonExistentStore() {
    String productName = uniqueName();
    Long productId = given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"" + productName + "\"}")
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    String warehouseCode = uniqueCode();
    given()
        .contentType(ContentType.JSON)
        .body("{\"businessUnitCode\": \"" + warehouseCode + "\", \"location\": \"AMSTERDAM-001\", \"capacity\": 15, \"stock\": 5}")
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\": " + productId + ", \"warehouseCode\": \"" + warehouseCode + "\", \"storeId\": 999999}")
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(400);
  }

  @Test
  void shouldFailToDeleteNonExistentAssociation() {
    given().when().delete("/fulfillment/999999").then().statusCode(404);
  }
}
