package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StoreEventTest {

  @Test
  void shouldCreateCreatedEvent() {
    // given
    Store store = new Store("Test Store");
    store.id = 1L;

    // when
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.CREATED);

    // then
    assertEquals(store, event.getStore());
    assertEquals(StoreEvent.Type.CREATED, event.getType());
  }

  @Test
  void shouldCreateUpdatedEvent() {
    // given
    Store store = new Store("Updated Store");
    store.id = 2L;

    // when
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.UPDATED);

    // then
    assertEquals(store, event.getStore());
    assertEquals(StoreEvent.Type.UPDATED, event.getType());
  }

  @Test
  void shouldPreserveStoreReference() {
    // given
    Store store = new Store("Reference Store");
    store.id = 3L;
    store.quantityProductsInStock = 100;

    // when
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.CREATED);

    // then
    assertSame(store, event.getStore());
    assertEquals("Reference Store", event.getStore().name);
    assertEquals(100, event.getStore().quantityProductsInStock);
  }
}
