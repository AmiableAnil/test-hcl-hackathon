package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseRepository warehouseRepository;
  private final LocationResolver locationResolver;

  @Inject
  public CreateWarehouseUseCase(
      WarehouseRepository warehouseRepository, LocationResolver locationResolver) {
    this.warehouseRepository = warehouseRepository;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    // 1. Business Unit Code Verification - ensure it doesn't already exist
    Warehouse existing = warehouseRepository.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null) {
      throw new WarehouseValidationException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' already exists");
    }

    // 2. Location Validation - confirm the location is valid
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WarehouseValidationException(
          "Location '" + warehouse.location + "' does not exist");
    }

    // 3. Warehouse Creation Feasibility - check max number of warehouses in location
    long currentWarehouseCount =
        warehouseRepository.countActiveWarehousesInLocation(warehouse.location);
    if (currentWarehouseCount >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses ("
              + location.maxNumberOfWarehouses
              + ") reached for location '"
              + warehouse.location
              + "'");
    }

    // 4. Capacity Validation - ensure capacity doesn't exceed location max capacity
    int currentTotalCapacity =
        warehouseRepository.getTotalCapacityInLocation(warehouse.location);
    int newCapacity = warehouse.capacity != null ? warehouse.capacity : 0;
    if (currentTotalCapacity + newCapacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Adding warehouse with capacity "
              + newCapacity
              + " would exceed location max capacity of "
              + location.maxCapacity
              + " (current total: "
              + currentTotalCapacity
              + ")");
    }

    // 5. Stock Validation - ensure stock doesn't exceed warehouse capacity
    int stock = warehouse.stock != null ? warehouse.stock : 0;
    if (stock > newCapacity) {
      throw new WarehouseValidationException(
          "Stock (" + stock + ") cannot exceed warehouse capacity (" + newCapacity + ")");
    }

    // All validations passed, create the warehouse
    warehouseRepository.create(warehouse);
  }
}
