# Implementation Documentation

This document describes the implementation details for the Fulfilment Application code assignment.

## Table of Contents

- [Tasks Completed](#tasks-completed)
- [Architecture Overview](#architecture-overview)
- [API Documentation](#api-documentation)
- [Business Rules](#business-rules)
- [Testing](#testing)
- [Code Coverage](#code-coverage)

---

## Tasks Completed

| Task | Status | Description |
|------|--------|-------------|
| 1. Location | Done | Implemented `LocationGateway.resolveByIdentifier()` |
| 2. Store | Done | Store events fired after transaction commit via CDI events |
| 3. Warehouse Create | Done | With business unit code, location, and capacity validations |
| 3. Warehouse Replace | Done | Archives old warehouse, creates new with stock validation |
| 3. Warehouse Archive | Done | Soft delete with archived date |
| Bonus: Fulfillment | Done | Product-Warehouse-Store associations |
| Tests | Done | 94% code coverage achieved |
| OpenAPI Documentation | Done | Full API spec with Swagger UI |

---

## Architecture Overview

### Package Structure

```
com.fulfilment.application.monolith
├── fulfillment/          # Fulfillment associations (Product-Warehouse-Store)
│   ├── FulfillmentAssociation.java      # JPA Entity
│   ├── FulfillmentRepository.java       # Panache Repository
│   ├── FulfillmentResource.java         # REST Resource
│   ├── FulfillmentService.java          # Business logic
│   └── FulfillmentValidationException.java
│
├── location/             # Location validation
│   └── LocationGateway.java             # Resolves valid locations
│
├── products/             # Product catalog
│   ├── Product.java                     # JPA Entity
│   ├── ProductRepository.java           # Panache Repository
│   └── ProductResource.java             # REST Resource
│
├── stores/               # Store management
│   ├── Store.java                       # JPA Entity (Panache)
│   ├── StoreEvent.java                  # CDI Event for legacy sync
│   ├── StoreResource.java               # REST Resource
│   └── LegacyStoreManagerGateway.java   # Legacy system integration
│
└── warehouses/           # Hexagonal Architecture
    ├── adapters/
    │   ├── database/
    │   │   ├── DbWarehouse.java         # JPA Entity
    │   │   └── WarehouseRepository.java # Repository implementation
    │   └── restapi/
    │       └── WarehouseResourceImpl.java # REST Resource
    └── domain/
        ├── models/
        │   ├── Location.java            # Domain model
        │   └── Warehouse.java           # Domain model
        ├── ports/
        │   ├── LocationResolver.java    # Port interface
        │   └── WarehouseStore.java      # Port interface
        └── usecases/
            ├── CreateWarehouseUseCase.java
            ├── ReplaceWarehouseUseCase.java
            └── ArchiveWarehouseUseCase.java
```

### Design Patterns

1. **Hexagonal Architecture** (Warehouses)
   - Domain layer isolated from infrastructure
   - Ports define interfaces, adapters implement them
   - Use cases encapsulate business logic

2. **Repository Pattern**
   - `WarehouseRepository` implements `WarehouseStore` port
   - `FulfillmentRepository` for association persistence

3. **Event-Driven Architecture** (Stores)
   - `StoreEvent` CDI events fired after transaction commit
   - `LegacyStoreManagerGateway` observes events with `TransactionPhase.AFTER_SUCCESS`

---

## API Documentation

### Endpoints Summary

#### Warehouse API (`/warehouse`)

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/warehouse` | List all warehouses | 200 |
| POST | `/warehouse` | Create warehouse | 200, 400 |
| GET | `/warehouse/{id}` | Get by business unit code | 200, 404 |
| DELETE | `/warehouse/{id}` | Archive warehouse | 204, 404 |
| POST | `/warehouse/{id}/replacement` | Replace warehouse | 200, 400, 404 |

#### Product API (`/product`)

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/product` | List all products | 200 |
| POST | `/product` | Create product | 201, 422 |
| GET | `/product/{id}` | Get product | 200, 404 |
| PUT | `/product/{id}` | Update product | 200, 404, 422 |
| DELETE | `/product/{id}` | Delete product | 204, 404 |

#### Store API (`/store`)

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/store` | List all stores | 200 |
| POST | `/store` | Create store | 201, 422 |
| GET | `/store/{id}` | Get store | 200, 404 |
| PUT | `/store/{id}` | Full update | 200, 404, 422 |
| PATCH | `/store/{id}` | Partial update | 200, 404, 422 |
| DELETE | `/store/{id}` | Delete store | 204, 404 |

#### Fulfillment API (`/fulfillment`)

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/fulfillment` | List associations | 200 |
| GET | `/fulfillment?productId=X` | Filter by product | 200 |
| GET | `/fulfillment?warehouseCode=X` | Filter by warehouse | 200 |
| GET | `/fulfillment?storeId=X` | Filter by store | 200 |
| POST | `/fulfillment` | Create association | 201, 400 |
| DELETE | `/fulfillment/{id}` | Delete association | 204, 404 |

### Interactive Documentation

When running the application:

- **Swagger UI**: http://localhost:8080/swagger-ui
- **OpenAPI JSON**: http://localhost:8080/q/openapi
- **OpenAPI YAML**: http://localhost:8080/q/openapi?format=yaml

### OpenAPI Specification

Full OpenAPI 3.0 specification available at:
- `src/main/resources/openapi/fulfilment-api-openapi.yaml`

---

## Business Rules

### Warehouse Validations

#### Create Warehouse

1. **Business Unit Code Uniqueness**
   - Code must not already exist in the system
   - Error: "Warehouse with business unit code {code} already exists"

2. **Location Validation**
   - Location must be a valid identifier from the location registry
   - Error: "Location '{location}' is not valid"

3. **Maximum Warehouses per Location**
   - Each location has a maximum number of allowed warehouses
   - Error: "Maximum number of warehouses ({max}) reached for location '{location}'"

4. **Capacity Validation**
   - New capacity cannot exceed location's maximum capacity
   - Error: "Adding warehouse with capacity {cap} would exceed location max capacity of {max}"

5. **Stock Validation**
   - Stock cannot exceed warehouse capacity
   - Error: "Stock ({stock}) cannot exceed capacity ({capacity})"

#### Replace Warehouse

1. **Existing Warehouse Required**
   - Original warehouse must exist
   - Error: 404 Not Found

2. **Stock Accommodation**
   - New capacity must be >= existing stock
   - Error: "New warehouse capacity ({cap}) cannot be less than current stock ({stock})"

3. **Capacity Limits**
   - New capacity cannot exceed location maximum
   - Error: "New warehouse capacity would exceed location max capacity of {max}"

#### Archive Warehouse

1. **Existing Warehouse Required**
   - Warehouse must exist to be archived
   - Error: 404 Not Found

### Valid Locations

| Location | Max Warehouses | Max Capacity |
|----------|----------------|--------------|
| AMSTERDAM-001 | 5 | 100 |
| AMSTERDAM-002 | 3 | 75 |
| ZWOLLE-001 | 1 | 40 |
| ZWOLLE-002 | 2 | 50 |
| TILBURG-001 | 1 | 40 |
| HELMOND-001 | 1 | 45 |
| EINDHOVEN-001 | 2 | 70 |
| VETSBY-001 | 1 | 90 |

### Fulfillment Validations

1. **Product Must Exist**
   - Error: "Product with id {id} does not exist"

2. **Warehouse Must Exist**
   - Error: "Warehouse with code {code} does not exist"

3. **Store Must Exist**
   - Error: "Store with id {id} does not exist"

---

## Testing

### Test Classes

| Test Class | Type | Coverage |
|------------|------|----------|
| `WarehouseResourceImplTest` | Integration | Warehouse REST API |
| `StoreResourceTest` | Integration | Store REST API |
| `FulfillmentResourceTest` | Integration | Fulfillment REST API |
| `CreateWarehouseUseCaseTest` | Unit | Create warehouse logic |
| `ReplaceWarehouseUseCaseTest` | Unit | Replace warehouse logic |
| `ArchiveWarehouseUseCaseTest` | Unit | Archive warehouse logic |
| `LocationGatewayTest` | Unit | Location resolution |
| `FulfillmentAssociationTest` | Unit | Entity tests |
| `DbWarehouseTest` | Unit | Entity mapping |

### Running Tests

```sh
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw clean verify

# Run specific test class
./mvnw test -Dtest=WarehouseResourceImplTest
```

### Test Configuration

Tests use H2 in-memory database configured in `src/test/resources/application.properties`:

```properties
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
quarkus.hibernate-orm.database.generation=drop-and-create
```

---

## Code Coverage

### Overall Coverage: 94%

The project uses JaCoCo integrated with Quarkus for accurate coverage measurement.

### Coverage Reports

After running `./mvnw clean verify`:

| Report | Location |
|--------|----------|
| HTML Report | `target/site/jacoco/index.html` |
| CSV Report | `target/site/jacoco/jacoco.csv` |
| XML Report | `target/site/jacoco/jacoco.xml` |

### Coverage by Component

| Component | Coverage |
|-----------|----------|
| WarehouseResourceImpl | 100% |
| FulfillmentResource | 100% |
| StoreResource | 100% |
| ProductResource | 100% |
| WarehouseRepository | 99% |
| FulfillmentRepository | 92% |
| CreateWarehouseUseCase | 84% |
| ReplaceWarehouseUseCase | 81% |
| FulfillmentService | 82% |
| LocationGateway | 100% |

### JaCoCo Configuration

Coverage merges unit tests and Quarkus integration tests:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>merge-results</id>
            <phase>verify</phase>
            <goals><goal>merge</goal></goals>
            <configuration>
                <fileSets>
                    <fileSet>
                        <directory>${project.build.directory}</directory>
                        <includes>
                            <include>jacoco.exec</include>
                            <include>jacoco-quarkus.exec</include>
                        </includes>
                    </fileSet>
                </fileSets>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## How to Run

### Prerequisites

- JDK 17+
- PostgreSQL (or Docker)

### Database Setup

```sh
docker run -it --rm=true --name quarkus_test \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=quarkus_test \
  -p 5432:5432 postgres:13.3
```

### Development Mode

```sh
./mvnw quarkus:dev
```

Application available at http://localhost:8080

### Production Build

```sh
./mvnw package
java -jar ./target/quarkus-app/quarkus-run.jar
```
