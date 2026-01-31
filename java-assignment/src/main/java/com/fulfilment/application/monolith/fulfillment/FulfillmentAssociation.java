package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "fulfillment_association",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"productId", "warehouseCode", "storeId"})
    })
public class FulfillmentAssociation extends PanacheEntity {

  public Long productId;

  public String warehouseCode;

  public Long storeId;

  public FulfillmentAssociation() {}

  public FulfillmentAssociation(Long productId, String warehouseCode, Long storeId) {
    this.productId = productId;
    this.warehouseCode = warehouseCode;
    this.storeId = storeId;
  }
}
