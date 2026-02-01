package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StoreTest {

  @Test
  void shouldCreateStoreWithDefaultConstructor() {
    // when
    Store store = new Store();

    // then
    assertNull(store.name);
    assertEquals(0, store.quantityProductsInStock);
  }

  @Test
  void shouldCreateStoreWithName() {
    // when
    Store store = new Store("My Store");

    // then
    assertEquals("My Store", store.name);
    assertEquals(0, store.quantityProductsInStock);
  }

  @Test
  void shouldAllowSettingFields() {
    // given
    Store store = new Store();

    // when
    store.name = "Test Store";
    store.quantityProductsInStock = 50;
    store.id = 1L;

    // then
    assertEquals("Test Store", store.name);
    assertEquals(50, store.quantityProductsInStock);
    assertEquals(1L, store.id);
  }
}
