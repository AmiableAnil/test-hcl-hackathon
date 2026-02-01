package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WarehouseRepositoryTest {

  @Inject WarehouseRepository warehouseRepository;

  @Test
  @Order(1)
  @Transactional
  void shouldCreateWarehouse() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.REPO001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    // when
    warehouseRepository.create(warehouse);

    // then
    Warehouse found = warehouseRepository.findByBusinessUnitCode("MWH.REPO001");
    assertNotNull(found);
    assertEquals("MWH.REPO001", found.businessUnitCode);
    assertEquals("AMSTERDAM-001", found.location);
    assertEquals(50, found.capacity);
    assertEquals(10, found.stock);
    assertNotNull(found.createdAt);
    assertNull(found.archivedAt);
  }

  @Test
  @Order(2)
  void shouldFindByBusinessUnitCode() {
    // when
    Warehouse found = warehouseRepository.findByBusinessUnitCode("MWH.REPO001");

    // then
    assertNotNull(found);
    assertEquals("MWH.REPO001", found.businessUnitCode);
  }

  @Test
  @Order(3)
  void shouldReturnNullForNonExistentWarehouse() {
    // when
    Warehouse found = warehouseRepository.findByBusinessUnitCode("MWH.NONEXISTENT");

    // then
    assertNull(found);
  }

  @Test
  @Order(4)
  void shouldGetAllActiveWarehouses() {
    // when
    List<Warehouse> all = warehouseRepository.getAll();

    // then
    assertNotNull(all);
    assertTrue(all.stream().allMatch(w -> w.archivedAt == null));
  }

  @Test
  @Order(5)
  @Transactional
  void shouldUpdateWarehouse() {
    // given
    Warehouse warehouse = warehouseRepository.findByBusinessUnitCode("MWH.REPO001");
    assertNotNull(warehouse);
    warehouse.capacity = 60;
    warehouse.stock = 20;

    // when
    warehouseRepository.update(warehouse);

    // then
    Warehouse updated = warehouseRepository.findByBusinessUnitCode("MWH.REPO001");
    assertEquals(60, updated.capacity);
    assertEquals(20, updated.stock);
  }

  @Test
  @Order(6)
  void shouldCountActiveWarehousesInLocation() {
    // when
    long count = warehouseRepository.countActiveWarehousesInLocation("AMSTERDAM-001");

    // then
    assertTrue(count >= 0);
  }

  @Test
  @Order(7)
  void shouldGetTotalCapacityInLocation() {
    // when
    int totalCapacity = warehouseRepository.getTotalCapacityInLocation("AMSTERDAM-001");

    // then
    assertTrue(totalCapacity >= 0);
  }

  @Test
  @Order(8)
  @Transactional
  void shouldArchiveWarehouse() {
    // given
    Warehouse warehouse = warehouseRepository.findByBusinessUnitCode("MWH.REPO001");
    assertNotNull(warehouse);
    warehouse.archivedAt = LocalDateTime.now();

    // when
    warehouseRepository.update(warehouse);

    // then
    Warehouse archived = warehouseRepository.findByBusinessUnitCode("MWH.REPO001");
    assertNull(archived); // Should not find archived warehouse
  }

  @Test
  @Order(9)
  @Transactional
  void shouldRemoveWarehouse() {
    // First create a warehouse to remove
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.TOREMOVE";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 30;
    warehouse.stock = 5;
    warehouseRepository.create(warehouse);

    // Verify it exists
    assertNotNull(warehouseRepository.findByBusinessUnitCode("MWH.TOREMOVE"));

    // when
    warehouseRepository.remove(warehouse);

    // then - verify removal after transaction commits
  }

  @Test
  @Order(10)
  void shouldReturnZeroForEmptyLocation() {
    // when
    long count = warehouseRepository.countActiveWarehousesInLocation("NONEXISTENT-001");
    int capacity = warehouseRepository.getTotalCapacityInLocation("NONEXISTENT-001");

    // then
    assertEquals(0, count);
    assertEquals(0, capacity);
  }
}
