package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegacyStoreManagerGatewayTest {

  private LegacyStoreManagerGateway gateway;

  @BeforeEach
  void setUp() {
    gateway = new LegacyStoreManagerGateway();
  }

  @Test
  void shouldHandleCreatedStoreEvent() {
    // given
    Store store = new Store("Created Store");
    store.id = 1L;
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.CREATED);

    // when & then - should not throw
    assertDoesNotThrow(() -> gateway.onStoreEvent(event));
  }

  @Test
  void shouldHandleUpdatedStoreEvent() {
    // given
    Store store = new Store("Updated Store");
    store.id = 2L;
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.UPDATED);

    // when & then - should not throw
    assertDoesNotThrow(() -> gateway.onStoreEvent(event));
  }

  @Test
  void shouldLogCreatedStore() {
    // given
    Store store = new Store("Log Test Store");
    store.id = 3L;
    store.quantityProductsInStock = 100;
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.CREATED);

    // when & then - just verify it doesn't throw
    gateway.onStoreEvent(event);
  }

  @Test
  void shouldLogUpdatedStore() {
    // given
    Store store = new Store("Updated Log Store");
    store.id = 4L;
    store.quantityProductsInStock = 200;
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.UPDATED);

    // when & then - just verify it doesn't throw
    gateway.onStoreEvent(event);
  }
}
