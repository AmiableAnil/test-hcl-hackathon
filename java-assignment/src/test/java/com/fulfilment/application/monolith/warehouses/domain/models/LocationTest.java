package com.fulfilment.application.monolith.warehouses.domain.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LocationTest {

  @Test
  void shouldCreateLocationWithConstructor() {
    // when
    Location location = new Location("AMSTERDAM-001", 5, 100);

    // then
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  void shouldAllowModifyingFields() {
    // given
    Location location = new Location("ZWOLLE-001", 1, 40);

    // when
    location.identification = "ZWOLLE-002";
    location.maxNumberOfWarehouses = 2;
    location.maxCapacity = 50;

    // then
    assertEquals("ZWOLLE-002", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(50, location.maxCapacity);
  }

  @Test
  void shouldCreateLocationWithZeroValues() {
    // when
    Location location = new Location("EMPTY-001", 0, 0);

    // then
    assertEquals("EMPTY-001", location.identification);
    assertEquals(0, location.maxNumberOfWarehouses);
    assertEquals(0, location.maxCapacity);
  }
}
