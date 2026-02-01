package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DbWarehouseTest {

  @Test
  void shouldCreateDbWarehouseWithDefaultConstructor() {
    // when
    DbWarehouse dbWarehouse = new DbWarehouse();

    // then
    assertNull(dbWarehouse.id);
    assertNull(dbWarehouse.businessUnitCode);
    assertNull(dbWarehouse.location);
    assertNull(dbWarehouse.capacity);
    assertNull(dbWarehouse.stock);
    assertNull(dbWarehouse.createdAt);
    assertNull(dbWarehouse.archivedAt);
  }

  @Test
  void shouldConvertToWarehouse() {
    // given
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.id = 1L;
    dbWarehouse.businessUnitCode = "MWH.001";
    dbWarehouse.location = "AMSTERDAM-001";
    dbWarehouse.capacity = 100;
    dbWarehouse.stock = 50;
    dbWarehouse.createdAt = LocalDateTime.now();
    dbWarehouse.archivedAt = null;

    // when
    Warehouse warehouse = dbWarehouse.toWarehouse();

    // then
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("AMSTERDAM-001", warehouse.location);
    assertEquals(100, warehouse.capacity);
    assertEquals(50, warehouse.stock);
    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  void shouldConvertArchivedWarehouse() {
    // given
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = "MWH.ARCHIVED";
    dbWarehouse.location = "ZWOLLE-001";
    dbWarehouse.capacity = 30;
    dbWarehouse.stock = 10;
    dbWarehouse.createdAt = LocalDateTime.now().minusDays(30);
    dbWarehouse.archivedAt = LocalDateTime.now();

    // when
    Warehouse warehouse = dbWarehouse.toWarehouse();

    // then
    assertEquals("MWH.ARCHIVED", warehouse.businessUnitCode);
    assertNotNull(warehouse.archivedAt);
  }

  @Test
  void shouldAllowSettingAllFields() {
    // given
    DbWarehouse dbWarehouse = new DbWarehouse();
    LocalDateTime now = LocalDateTime.now();

    // when
    dbWarehouse.id = 5L;
    dbWarehouse.businessUnitCode = "MWH.TEST";
    dbWarehouse.location = "TILBURG-001";
    dbWarehouse.capacity = 40;
    dbWarehouse.stock = 20;
    dbWarehouse.createdAt = now;
    dbWarehouse.archivedAt = now.plusDays(1);

    // then
    assertEquals(5L, dbWarehouse.id);
    assertEquals("MWH.TEST", dbWarehouse.businessUnitCode);
    assertEquals("TILBURG-001", dbWarehouse.location);
    assertEquals(40, dbWarehouse.capacity);
    assertEquals(20, dbWarehouse.stock);
    assertEquals(now, dbWarehouse.createdAt);
    assertEquals(now.plusDays(1), dbWarehouse.archivedAt);
  }
}
