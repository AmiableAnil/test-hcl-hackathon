package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseRepository warehouseRepository;
  private final LocationResolver locationResolver;

  @Inject
  public ReplaceWarehouseUseCase(
      WarehouseRepository warehouseRepository, LocationResolver locationResolver) {
    this.warehouseRepository = warehouseRepository;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // 1. Find existing warehouse by business unit code
    Warehouse existingWarehouse =
        warehouseRepository.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existingWarehouse == null) {
      throw new WarehouseValidationException(
          "Warehouse with business unit code '"
              + newWarehouse.businessUnitCode
              + "' does not exist");
    }

    // 2. Location Validation - confirm the new location is valid
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new WarehouseValidationException(
          "Location '" + newWarehouse.location + "' does not exist");
    }

    // 3. Capacity Accommodation - new warehouse capacity must accommodate old warehouse's stock
    int oldStock = existingWarehouse.stock != null ? existingWarehouse.stock : 0;
    int newCapacity = newWarehouse.capacity != null ? newWarehouse.capacity : 0;
    if (newCapacity < oldStock) {
      throw new WarehouseValidationException(
          "New warehouse capacity ("
              + newCapacity
              + ") cannot accommodate the existing stock ("
              + oldStock
              + ")");
    }

    // 4. Stock Matching - new warehouse stock must match old warehouse stock
    int newStock = newWarehouse.stock != null ? newWarehouse.stock : 0;
    if (newStock != oldStock) {
      throw new WarehouseValidationException(
          "New warehouse stock ("
              + newStock
              + ") must match the existing warehouse stock ("
              + oldStock
              + ")");
    }

    // 5. Capacity validation for location (accounting for removed old capacity)
    int currentTotalCapacity =
        warehouseRepository.getTotalCapacityInLocation(newWarehouse.location);
    int oldCapacity = existingWarehouse.capacity != null ? existingWarehouse.capacity : 0;

    // If same location, subtract old capacity; otherwise just check new location
    int effectiveCurrentCapacity;
    if (existingWarehouse.location.equals(newWarehouse.location)) {
      effectiveCurrentCapacity = currentTotalCapacity - oldCapacity;
    } else {
      effectiveCurrentCapacity = currentTotalCapacity;
    }

    if (effectiveCurrentCapacity + newCapacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "New warehouse capacity would exceed location max capacity of " + location.maxCapacity);
    }

    // All validations passed
    // Archive the old warehouse
    existingWarehouse.archivedAt = LocalDateTime.now();
    warehouseRepository.update(existingWarehouse);

    // Create the new warehouse with the same business unit code
    warehouseRepository.create(newWarehouse);
  }
}
