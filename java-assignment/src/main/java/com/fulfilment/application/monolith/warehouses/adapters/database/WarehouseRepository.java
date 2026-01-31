package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt IS NULL").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = LocalDateTime.now();
    dbWarehouse.archivedAt = null;
    persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse =
        find("businessUnitCode = ?1 AND archivedAt IS NULL", warehouse.businessUnitCode)
            .firstResult();
    if (dbWarehouse != null) {
      dbWarehouse.location = warehouse.location;
      dbWarehouse.capacity = warehouse.capacity;
      dbWarehouse.stock = warehouse.stock;
      dbWarehouse.archivedAt = warehouse.archivedAt;
      persist(dbWarehouse);
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
    delete("businessUnitCode = ?1", warehouse.businessUnitCode);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse =
        find("businessUnitCode = ?1 AND archivedAt IS NULL", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  public long countActiveWarehousesInLocation(String location) {
    return count("location = ?1 AND archivedAt IS NULL", location);
  }

  public int getTotalCapacityInLocation(String location) {
    List<DbWarehouse> warehouses = find("location = ?1 AND archivedAt IS NULL", location).list();
    return warehouses.stream().mapToInt(w -> w.capacity != null ? w.capacity : 0).sum();
  }
}
