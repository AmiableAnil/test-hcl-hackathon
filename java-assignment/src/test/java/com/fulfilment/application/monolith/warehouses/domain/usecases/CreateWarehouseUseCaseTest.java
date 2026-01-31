package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateWarehouseUseCaseTest {

  @Mock private WarehouseRepository warehouseRepository;

  @Mock private LocationResolver locationResolver;

  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CreateWarehouseUseCase(warehouseRepository, locationResolver);
  }

  @Test
  void shouldCreateWarehouseSuccessfully() {
    // given
    Warehouse warehouse = createWarehouse("MWH.NEW", "AMSTERDAM-001", 50, 10);
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesInLocation("AMSTERDAM-001")).thenReturn(0L);
    when(warehouseRepository.getTotalCapacityInLocation("AMSTERDAM-001")).thenReturn(0);

    // when
    useCase.create(warehouse);

    // then
    verify(warehouseRepository).create(warehouse);
  }

  @Test
  void shouldThrowExceptionWhenBusinessUnitCodeAlreadyExists() {
    // given
    Warehouse warehouse = createWarehouse("MWH.001", "AMSTERDAM-001", 50, 10);
    Warehouse existing = createWarehouse("MWH.001", "AMSTERDAM-001", 100, 20);

    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("already exists"));
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldThrowExceptionWhenLocationDoesNotExist() {
    // given
    Warehouse warehouse = createWarehouse("MWH.NEW", "INVALID-LOCATION", 50, 10);

    when(warehouseRepository.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID-LOCATION")).thenReturn(null);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("does not exist"));
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldThrowExceptionWhenMaxWarehousesReached() {
    // given
    Warehouse warehouse = createWarehouse("MWH.NEW", "ZWOLLE-001", 30, 5);
    Location location = new Location("ZWOLLE-001", 1, 40); // max 1 warehouse

    when(warehouseRepository.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesInLocation("ZWOLLE-001")).thenReturn(1L);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("Maximum number of warehouses"));
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldThrowExceptionWhenCapacityExceedsLocationMax() {
    // given
    Warehouse warehouse = createWarehouse("MWH.NEW", "ZWOLLE-001", 50, 10);
    Location location = new Location("ZWOLLE-001", 2, 40); // max capacity 40

    when(warehouseRepository.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesInLocation("ZWOLLE-001")).thenReturn(0L);
    when(warehouseRepository.getTotalCapacityInLocation("ZWOLLE-001")).thenReturn(0);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("exceed location max capacity"));
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldThrowExceptionWhenStockExceedsCapacity() {
    // given
    Warehouse warehouse = createWarehouse("MWH.NEW", "AMSTERDAM-001", 50, 60); // stock > capacity
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesInLocation("AMSTERDAM-001")).thenReturn(0L);
    when(warehouseRepository.getTotalCapacityInLocation("AMSTERDAM-001")).thenReturn(0);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("cannot exceed warehouse capacity"));
    verify(warehouseRepository, never()).create(any());
  }

  private Warehouse createWarehouse(
      String businessUnitCode, String location, Integer capacity, Integer stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
