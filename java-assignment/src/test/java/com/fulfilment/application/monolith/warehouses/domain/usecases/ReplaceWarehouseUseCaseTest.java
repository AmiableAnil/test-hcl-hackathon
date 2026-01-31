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
class ReplaceWarehouseUseCaseTest {

  @Mock private WarehouseRepository warehouseRepository;

  @Mock private LocationResolver locationResolver;

  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ReplaceWarehouseUseCase(warehouseRepository, locationResolver);
  }

  @Test
  void shouldReplaceWarehouseSuccessfully() {
    // given
    Warehouse existingWarehouse = createWarehouse("MWH.001", "AMSTERDAM-001", 100, 50);
    Warehouse newWarehouse = createWarehouse("MWH.001", "AMSTERDAM-001", 120, 50);
    Location location = new Location("AMSTERDAM-001", 5, 200);

    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.getTotalCapacityInLocation("AMSTERDAM-001")).thenReturn(100);

    // when
    useCase.replace(newWarehouse);

    // then
    verify(warehouseRepository).update(existingWarehouse);
    assertNotNull(existingWarehouse.archivedAt);
    verify(warehouseRepository).create(newWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenWarehouseDoesNotExist() {
    // given
    Warehouse newWarehouse = createWarehouse("MWH.NONEXISTENT", "AMSTERDAM-001", 100, 50);

    when(warehouseRepository.findByBusinessUnitCode("MWH.NONEXISTENT")).thenReturn(null);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));

    assertTrue(exception.getMessage().contains("does not exist"));
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldThrowExceptionWhenLocationDoesNotExist() {
    // given
    Warehouse existingWarehouse = createWarehouse("MWH.001", "AMSTERDAM-001", 100, 50);
    Warehouse newWarehouse = createWarehouse("MWH.001", "INVALID-LOCATION", 100, 50);

    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);
    when(locationResolver.resolveByIdentifier("INVALID-LOCATION")).thenReturn(null);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));

    assertTrue(exception.getMessage().contains("does not exist"));
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldThrowExceptionWhenNewCapacityCannotAccommodateOldStock() {
    // given
    Warehouse existingWarehouse = createWarehouse("MWH.001", "AMSTERDAM-001", 100, 80);
    Warehouse newWarehouse = createWarehouse("MWH.001", "AMSTERDAM-001", 50, 80); // capacity < stock
    Location location = new Location("AMSTERDAM-001", 5, 200);

    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));

    assertTrue(exception.getMessage().contains("cannot accommodate"));
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldThrowExceptionWhenStockDoesNotMatch() {
    // given
    Warehouse existingWarehouse = createWarehouse("MWH.001", "AMSTERDAM-001", 100, 50);
    Warehouse newWarehouse = createWarehouse("MWH.001", "AMSTERDAM-001", 100, 60); // different stock
    Location location = new Location("AMSTERDAM-001", 5, 200);

    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));

    assertTrue(exception.getMessage().contains("must match"));
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldThrowExceptionWhenNewCapacityExceedsLocationMax() {
    // given
    Warehouse existingWarehouse = createWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse newWarehouse = createWarehouse("MWH.001", "ZWOLLE-001", 50, 10);
    Location location = new Location("ZWOLLE-001", 2, 40); // max capacity 40

    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseRepository.getTotalCapacityInLocation("ZWOLLE-001")).thenReturn(30);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));

    assertTrue(exception.getMessage().contains("exceed location max capacity"));
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
