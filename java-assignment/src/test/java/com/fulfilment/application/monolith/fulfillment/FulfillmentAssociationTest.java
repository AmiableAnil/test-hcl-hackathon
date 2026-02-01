package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FulfillmentAssociationTest {

  @Test
  void shouldCreateAssociationWithDefaultConstructor() {
    // when
    FulfillmentAssociation association = new FulfillmentAssociation();

    // then
    assertNull(association.productId);
    assertNull(association.warehouseCode);
    assertNull(association.storeId);
  }

  @Test
  void shouldCreateAssociationWithAllFields() {
    // given
    Long productId = 1L;
    String warehouseCode = "MWH.001";
    Long storeId = 2L;

    // when
    FulfillmentAssociation association = new FulfillmentAssociation(productId, warehouseCode, storeId);

    // then
    assertEquals(productId, association.productId);
    assertEquals(warehouseCode, association.warehouseCode);
    assertEquals(storeId, association.storeId);
  }

  @Test
  void shouldAllowSettingFields() {
    // given
    FulfillmentAssociation association = new FulfillmentAssociation();

    // when
    association.productId = 5L;
    association.warehouseCode = "MWH.TEST";
    association.storeId = 10L;

    // then
    assertEquals(5L, association.productId);
    assertEquals("MWH.TEST", association.warehouseCode);
    assertEquals(10L, association.storeId);
  }
}
