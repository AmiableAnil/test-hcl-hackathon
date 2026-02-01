package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FulfillmentServiceTest {

  @Mock private FulfillmentRepository fulfillmentRepository;

  @Mock private WarehouseRepository warehouseRepository;

  @Mock private ProductRepository productRepository;

  @InjectMocks private FulfillmentService fulfillmentService;

  @Test
  void shouldThrowExceptionWhenProductDoesNotExist() {
    // given
    Long productId = 999L;
    String warehouseCode = "MWH.001";
    Long storeId = 1L;

    when(productRepository.findById(productId)).thenReturn(null);

    // when & then
    FulfillmentValidationException exception =
        assertThrows(
            FulfillmentValidationException.class,
            () -> fulfillmentService.createAssociation(productId, warehouseCode, storeId));

    assertTrue(exception.getMessage().contains("Product with id " + productId + " does not exist"));
    verify(fulfillmentRepository, never()).persist((FulfillmentAssociation) any());
  }

  @Test
  void shouldThrowExceptionWhenWarehouseDoesNotExist() {
    // given
    Long productId = 1L;
    String warehouseCode = "MWH.NONEXISTENT";
    Long storeId = 1L;

    Product product = new Product();
    product.id = productId;

    when(productRepository.findById(productId)).thenReturn(product);
    when(warehouseRepository.findByBusinessUnitCode(warehouseCode)).thenReturn(null);

    // when & then
    FulfillmentValidationException exception =
        assertThrows(
            FulfillmentValidationException.class,
            () -> fulfillmentService.createAssociation(productId, warehouseCode, storeId));

    assertTrue(exception.getMessage().contains("Warehouse with code"));
    verify(fulfillmentRepository, never()).persist((FulfillmentAssociation) any());
  }

  @Test
  void shouldDeleteAssociationSuccessfully() {
    // given
    Long associationId = 1L;
    FulfillmentAssociation association = new FulfillmentAssociation(1L, "MWH.001", 1L);
    association.id = associationId;

    when(fulfillmentRepository.findById(associationId)).thenReturn(association);

    // when
    fulfillmentService.deleteAssociation(associationId);

    // then
    verify(fulfillmentRepository).delete(association);
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistentAssociation() {
    // given
    Long associationId = 999L;

    when(fulfillmentRepository.findById(associationId)).thenReturn(null);

    // when & then
    FulfillmentValidationException exception =
        assertThrows(
            FulfillmentValidationException.class,
            () -> fulfillmentService.deleteAssociation(associationId));

    assertTrue(exception.getMessage().contains("not found"));
    verify(fulfillmentRepository, never()).delete((FulfillmentAssociation) any());
  }

  @Test
  void shouldGetAllAssociations() {
    // given
    FulfillmentAssociation association1 = new FulfillmentAssociation(1L, "MWH.001", 1L);
    FulfillmentAssociation association2 = new FulfillmentAssociation(2L, "MWH.002", 1L);

    when(fulfillmentRepository.listAll()).thenReturn(Arrays.asList(association1, association2));

    // when
    var result = fulfillmentService.getAll();

    // then
    assertEquals(2, result.size());
    verify(fulfillmentRepository).listAll();
  }

  @Test
  void shouldGetEmptyListWhenNoAssociations() {
    // given
    when(fulfillmentRepository.listAll()).thenReturn(Collections.emptyList());

    // when
    var result = fulfillmentService.getAll();

    // then
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldGetAssociationsByProduct() {
    // given
    Long productId = 1L;
    FulfillmentAssociation association = new FulfillmentAssociation(productId, "MWH.001", 1L);

    when(fulfillmentRepository.findByProduct(productId))
        .thenReturn(Collections.singletonList(association));

    // when
    var result = fulfillmentService.getByProduct(productId);

    // then
    assertEquals(1, result.size());
    assertEquals(productId, result.get(0).productId);
    verify(fulfillmentRepository).findByProduct(productId);
  }

  @Test
  void shouldGetAssociationsByWarehouse() {
    // given
    String warehouseCode = "MWH.001";
    FulfillmentAssociation association = new FulfillmentAssociation(1L, warehouseCode, 1L);

    when(fulfillmentRepository.findByWarehouse(warehouseCode))
        .thenReturn(Collections.singletonList(association));

    // when
    var result = fulfillmentService.getByWarehouse(warehouseCode);

    // then
    assertEquals(1, result.size());
    assertEquals(warehouseCode, result.get(0).warehouseCode);
    verify(fulfillmentRepository).findByWarehouse(warehouseCode);
  }

  @Test
  void shouldGetAssociationsByStore() {
    // given
    Long storeId = 1L;
    FulfillmentAssociation association = new FulfillmentAssociation(1L, "MWH.001", storeId);

    when(fulfillmentRepository.findByStore(storeId))
        .thenReturn(Collections.singletonList(association));

    // when
    var result = fulfillmentService.getByStore(storeId);

    // then
    assertEquals(1, result.size());
    assertEquals(storeId, result.get(0).storeId);
    verify(fulfillmentRepository).findByStore(storeId);
  }
}
