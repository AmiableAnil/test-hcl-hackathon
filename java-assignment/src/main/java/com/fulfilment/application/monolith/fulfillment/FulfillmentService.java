package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class FulfillmentService {

  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  @Inject FulfillmentRepository fulfillmentRepository;

  @Inject WarehouseRepository warehouseRepository;

  @Inject ProductRepository productRepository;

  /**
   * Creates a new fulfillment association between a product, warehouse, and store.
   * Validates all constraints before creating.
   */
  public FulfillmentAssociation createAssociation(
      Long productId, String warehouseCode, Long storeId) {

    // Validate that product exists
    Product product = productRepository.findById(productId);
    if (product == null) {
      throw new FulfillmentValidationException("Product with id " + productId + " does not exist");
    }

    // Validate that warehouse exists
    var warehouse = warehouseRepository.findByBusinessUnitCode(warehouseCode);
    if (warehouse == null) {
      throw new FulfillmentValidationException(
          "Warehouse with code '" + warehouseCode + "' does not exist");
    }

    // Validate that store exists
    Store store = Store.findById(storeId);
    if (store == null) {
      throw new FulfillmentValidationException("Store with id " + storeId + " does not exist");
    }

    // Check if association already exists
    if (fulfillmentRepository.associationExists(productId, warehouseCode, storeId)) {
      throw new FulfillmentValidationException(
          "Association already exists for product "
              + productId
              + ", warehouse "
              + warehouseCode
              + ", store "
              + storeId);
    }

    // Constraint 1: Each Product can be fulfilled by max 2 different Warehouses per Store
    long warehousesForProductInStore =
        fulfillmentRepository.countWarehousesForProductInStore(productId, storeId);
    if (warehousesForProductInStore >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new FulfillmentValidationException(
          "Product "
              + productId
              + " is already fulfilled by "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses for store "
              + storeId);
    }

    // Constraint 2: Each Store can be fulfilled by max 3 different Warehouses
    // Only check if this warehouse is not already associated with this store
    if (!fulfillmentRepository.warehouseAlreadyAssociatedWithStore(warehouseCode, storeId)) {
      long distinctWarehousesForStore =
          fulfillmentRepository.countDistinctWarehousesForStore(storeId);
      if (distinctWarehousesForStore >= MAX_WAREHOUSES_PER_STORE) {
        throw new FulfillmentValidationException(
            "Store "
                + storeId
                + " is already fulfilled by "
                + MAX_WAREHOUSES_PER_STORE
                + " different warehouses");
      }
    }

    // Constraint 3: Each Warehouse can store max 5 types of Products
    // Only check if this product is not already in this warehouse
    if (!fulfillmentRepository.productAlreadyInWarehouse(productId, warehouseCode)) {
      long distinctProductsInWarehouse =
          fulfillmentRepository.countDistinctProductsInWarehouse(warehouseCode);
      if (distinctProductsInWarehouse >= MAX_PRODUCTS_PER_WAREHOUSE) {
        throw new FulfillmentValidationException(
            "Warehouse "
                + warehouseCode
                + " already stores "
                + MAX_PRODUCTS_PER_WAREHOUSE
                + " different product types");
      }
    }

    // All validations passed, create the association
    FulfillmentAssociation association =
        new FulfillmentAssociation(productId, warehouseCode, storeId);
    fulfillmentRepository.persist(association);
    return association;
  }

  /**
   * Deletes a fulfillment association.
   */
  public void deleteAssociation(Long id) {
    FulfillmentAssociation association = fulfillmentRepository.findById(id);
    if (association == null) {
      throw new FulfillmentValidationException(
          "Fulfillment association with id " + id + " not found");
    }
    fulfillmentRepository.delete(association);
  }

  /**
   * Get all fulfillment associations.
   */
  public List<FulfillmentAssociation> getAll() {
    return fulfillmentRepository.listAll();
  }

  /**
   * Get associations by product.
   */
  public List<FulfillmentAssociation> getByProduct(Long productId) {
    return fulfillmentRepository.findByProduct(productId);
  }

  /**
   * Get associations by warehouse.
   */
  public List<FulfillmentAssociation> getByWarehouse(String warehouseCode) {
    return fulfillmentRepository.findByWarehouse(warehouseCode);
  }

  /**
   * Get associations by store.
   */
  public List<FulfillmentAssociation> getByStore(Long storeId) {
    return fulfillmentRepository.findByStore(storeId);
  }
}
