package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfillmentRepository implements PanacheRepository<FulfillmentAssociation> {

  /**
   * Count how many different warehouses fulfill a specific product for a specific store.
   * Constraint: Each Product can be fulfilled by max 2 different Warehouses per Store
   */
  public long countWarehousesForProductInStore(Long productId, Long storeId) {
    return count("productId = ?1 AND storeId = ?2", productId, storeId);
  }

  /**
   * Count how many different warehouses fulfill a specific store (across all products).
   * Constraint: Each Store can be fulfilled by max 3 different Warehouses
   */
  public long countDistinctWarehousesForStore(Long storeId) {
    return find("storeId = ?1", storeId).stream()
        .map(a -> a.warehouseCode)
        .distinct()
        .count();
  }

  /**
   * Count how many different product types a warehouse stores.
   * Constraint: Each Warehouse can store max 5 types of Products
   */
  public long countDistinctProductsInWarehouse(String warehouseCode) {
    return find("warehouseCode = ?1", warehouseCode).stream()
        .map(a -> a.productId)
        .distinct()
        .count();
  }

  /**
   * Check if a specific association already exists.
   */
  public boolean associationExists(Long productId, String warehouseCode, Long storeId) {
    return count("productId = ?1 AND warehouseCode = ?2 AND storeId = ?3",
        productId, warehouseCode, storeId) > 0;
  }

  /**
   * Get all associations for a product.
   */
  public List<FulfillmentAssociation> findByProduct(Long productId) {
    return list("productId = ?1", productId);
  }

  /**
   * Get all associations for a warehouse.
   */
  public List<FulfillmentAssociation> findByWarehouse(String warehouseCode) {
    return list("warehouseCode = ?1", warehouseCode);
  }

  /**
   * Get all associations for a store.
   */
  public List<FulfillmentAssociation> findByStore(Long storeId) {
    return list("storeId = ?1", storeId);
  }

  /**
   * Check if warehouse is already associated with the store (for any product).
   */
  public boolean warehouseAlreadyAssociatedWithStore(String warehouseCode, Long storeId) {
    return count("warehouseCode = ?1 AND storeId = ?2", warehouseCode, storeId) > 0;
  }

  /**
   * Check if product is already associated with the warehouse (for any store).
   */
  public boolean productAlreadyInWarehouse(Long productId, String warehouseCode) {
    return count("productId = ?1 AND warehouseCode = ?2", productId, warehouseCode) > 0;
  }
}
