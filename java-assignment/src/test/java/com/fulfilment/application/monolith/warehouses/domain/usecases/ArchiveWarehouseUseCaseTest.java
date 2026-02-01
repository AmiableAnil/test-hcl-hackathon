package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArchiveWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;

  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void shouldArchiveWarehouseSuccessfully() {
    // given
    Warehouse existingWarehouse = createWarehouse("MWH.001", "AMSTERDAM-001", 100, 50);
    Warehouse warehouseToArchive = createWarehouse("MWH.001", null, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);

    // when
    useCase.archive(warehouseToArchive);

    // then
    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseStore).update(captor.capture());

    Warehouse updatedWarehouse = captor.getValue();
    assertNotNull(updatedWarehouse.archivedAt);
    assertEquals("MWH.001", updatedWarehouse.businessUnitCode);
  }

  @Test
  void shouldThrowExceptionWhenWarehouseDoesNotExist() {
    // given
    Warehouse warehouseToArchive = createWarehouse("MWH.NONEXISTENT", null, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.NONEXISTENT")).thenReturn(null);

    // when & then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.archive(warehouseToArchive));

    assertTrue(exception.getMessage().contains("does not exist"));
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void shouldSetArchivedAtTimestamp() {
    // given
    Warehouse existingWarehouse = createWarehouse("MWH.002", "ZWOLLE-001", 50, 20);
    assertNull(existingWarehouse.archivedAt); // Verify initially null

    Warehouse warehouseToArchive = createWarehouse("MWH.002", null, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.002")).thenReturn(existingWarehouse);

    // when
    useCase.archive(warehouseToArchive);

    // then
    assertNotNull(existingWarehouse.archivedAt);
    verify(warehouseStore).update(existingWarehouse);
  }

  @Test
  void shouldCallUpdateOnWarehouseStore() {
    // given
    Warehouse existingWarehouse = createWarehouse("MWH.003", "TILBURG-001", 30, 10);
    Warehouse warehouseToArchive = createWarehouse("MWH.003", null, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.003")).thenReturn(existingWarehouse);

    // when
    useCase.archive(warehouseToArchive);

    // then
    verify(warehouseStore, times(1)).findByBusinessUnitCode("MWH.003");
    verify(warehouseStore, times(1)).update(existingWarehouse);
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
