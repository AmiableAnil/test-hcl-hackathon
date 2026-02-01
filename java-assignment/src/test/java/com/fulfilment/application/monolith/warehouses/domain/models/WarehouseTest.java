package com.fulfilment.application.monolith.warehouses.domain.models;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class WarehouseTest {

  @Test
  void shouldCreateWarehouseWithDefaultValues() {
    // when
    Warehouse warehouse = new Warehouse();

    // then
    assertNull(warehouse.businessUnitCode);
    assertNull(warehouse.location);
    assertNull(warehouse.capacity);
    assertNull(warehouse.stock);
    assertNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  void shouldAllowSettingAllFields() {
    // given
    Warehouse warehouse = new Warehouse();
    LocalDateTime now = LocalDateTime.now();

    // when
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = now;
    warehouse.archivedAt = now.plusDays(30);

    // then
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("AMSTERDAM-001", warehouse.location);
    assertEquals(100, warehouse.capacity);
    assertEquals(50, warehouse.stock);
    assertEquals(now, warehouse.createdAt);
    assertEquals(now.plusDays(30), warehouse.archivedAt);
  }

  @Test
  void shouldAllowNullValuesForOptionalFields() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.002";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    // then
    assertNotNull(warehouse.businessUnitCode);
    assertNotNull(warehouse.location);
    assertNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }
}
