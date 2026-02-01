package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseResourceImplTest {

  private String uniqueCode() {
    return "MWH." + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  @Test
  void shouldListAllWarehouses() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(0));
  }

  @Test
  void shouldCreateWarehouse() {
    String code = uniqueCode();
    String warehouseJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "AMSTERDAM-002",
          "capacity": 20,
          "stock": 5
        }
        """,
            code);

    given()
        .contentType(ContentType.JSON)
        .body(warehouseJson)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200)
        .body("businessUnitCode", is(code))
        .body("location", is("AMSTERDAM-002"))
        .body("capacity", is(20))
        .body("stock", is(5));
  }

  @Test
  void shouldGetWarehouseById() {
    String code = uniqueCode();
    String warehouseJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "VETSBY-001",
          "capacity": 30,
          "stock": 10
        }
        """,
            code);

    given().contentType(ContentType.JSON).body(warehouseJson).when().post("/warehouse").then().statusCode(200);

    given().when().get("/warehouse/" + code).then().statusCode(200).body("businessUnitCode", is(code));
  }

  @Test
  void shouldReturn404ForNonExistentWarehouse() {
    given().when().get("/warehouse/MWH.NOTEXIST999").then().statusCode(404);
  }

  @Test
  void shouldReplaceWarehouse() {
    String code = uniqueCode();
    String createJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "ZWOLLE-002",
          "capacity": 15,
          "stock": 10
        }
        """,
            code);

    given().contentType(ContentType.JSON).body(createJson).when().post("/warehouse").then().statusCode(200);

    String replacementJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "ZWOLLE-002",
          "capacity": 18,
          "stock": 10
        }
        """,
            code);

    given()
        .contentType(ContentType.JSON)
        .body(replacementJson)
        .when()
        .post("/warehouse/" + code + "/replacement")
        .then()
        .statusCode(200)
        .body("capacity", is(18));
  }

  @Test
  void shouldFailToReplaceNonExistentWarehouse() {
    given()
        .contentType(ContentType.JSON)
        .body("""
        {
          "businessUnitCode": "MWH.NOTEXIST888",
          "location": "AMSTERDAM-001",
          "capacity": 60,
          "stock": 10
        }
        """)
        .when()
        .post("/warehouse/MWH.NOTEXIST888/replacement")
        .then()
        .statusCode(404);
  }

  @Test
  void shouldArchiveWarehouse() {
    String code = uniqueCode();
    String warehouseJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "EINDHOVEN-001",
          "capacity": 20,
          "stock": 5
        }
        """,
            code);

    given().contentType(ContentType.JSON).body(warehouseJson).when().post("/warehouse").then().statusCode(200);
    given().when().delete("/warehouse/" + code).then().statusCode(204);
  }

  @Test
  void shouldFailToArchiveNonExistentWarehouse() {
    given().when().delete("/warehouse/MWH.NOTEXIST777").then().statusCode(404);
  }

  @Test
  void shouldFailToCreateWarehouseWithDuplicateCode() {
    String code = uniqueCode();
    String warehouseJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "AMSTERDAM-001",
          "capacity": 15,
          "stock": 5
        }
        """,
            code);

    given().contentType(ContentType.JSON).body(warehouseJson).when().post("/warehouse").then().statusCode(200);
    given().contentType(ContentType.JSON).body(warehouseJson).when().post("/warehouse").then().statusCode(400);
  }

  @Test
  void shouldFailToCreateWarehouseWithInvalidLocation() {
    String code = uniqueCode();
    String warehouseJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "INVALID-LOCATION",
          "capacity": 30,
          "stock": 5
        }
        """,
            code);

    given().contentType(ContentType.JSON).body(warehouseJson).when().post("/warehouse").then().statusCode(400);
  }

  @Test
  void shouldFailToCreateWarehouseWithCapacityExceedingMax() {
    String code = uniqueCode();
    String warehouseJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "ZWOLLE-001",
          "capacity": 100,
          "stock": 5
        }
        """,
            code);

    given().contentType(ContentType.JSON).body(warehouseJson).when().post("/warehouse").then().statusCode(400);
  }

  @Test
  void shouldFailToReplaceWithCapacityLessThanStock() {
    String code = uniqueCode();
    String createJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "ZWOLLE-002",
          "capacity": 30,
          "stock": 25
        }
        """,
            code);

    given().contentType(ContentType.JSON).body(createJson).when().post("/warehouse").then().statusCode(200);

    String replaceJson =
        String.format(
            """
        {
          "businessUnitCode": "%s",
          "location": "ZWOLLE-002",
          "capacity": 20,
          "stock": 25
        }
        """,
            code);

    given()
        .contentType(ContentType.JSON)
        .body(replaceJson)
        .when()
        .post("/warehouse/" + code + "/replacement")
        .then()
        .statusCode(400);
  }
}
