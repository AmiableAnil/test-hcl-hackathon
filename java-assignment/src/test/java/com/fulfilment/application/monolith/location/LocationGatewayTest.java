package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  void shouldResolveExistingLocationZwolle001() {
    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  void shouldResolveExistingLocationZwolle002() {
    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-002");

    // then
    assertNotNull(location);
    assertEquals("ZWOLLE-002", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(50, location.maxCapacity);
  }

  @Test
  void shouldResolveExistingLocationAmsterdam001() {
    // when
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");

    // then
    assertNotNull(location);
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  void shouldResolveExistingLocationAmsterdam002() {
    // when
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-002");

    // then
    assertNotNull(location);
    assertEquals("AMSTERDAM-002", location.identification);
    assertEquals(3, location.maxNumberOfWarehouses);
    assertEquals(75, location.maxCapacity);
  }

  @Test
  void shouldResolveExistingLocationTilburg001() {
    // when
    Location location = locationGateway.resolveByIdentifier("TILBURG-001");

    // then
    assertNotNull(location);
    assertEquals("TILBURG-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  void shouldResolveExistingLocationHelmond001() {
    // when
    Location location = locationGateway.resolveByIdentifier("HELMOND-001");

    // then
    assertNotNull(location);
    assertEquals("HELMOND-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(45, location.maxCapacity);
  }

  @Test
  void shouldResolveExistingLocationEindhoven001() {
    // when
    Location location = locationGateway.resolveByIdentifier("EINDHOVEN-001");

    // then
    assertNotNull(location);
    assertEquals("EINDHOVEN-001", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(70, location.maxCapacity);
  }

  @Test
  void shouldResolveExistingLocationVetsby001() {
    // when
    Location location = locationGateway.resolveByIdentifier("VETSBY-001");

    // then
    assertNotNull(location);
    assertEquals("VETSBY-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(90, location.maxCapacity);
  }

  @Test
  void shouldReturnNullWhenLocationDoesNotExist() {
    // when
    Location location = locationGateway.resolveByIdentifier("NON-EXISTENT");

    // then
    assertNull(location);
  }

  @Test
  void shouldReturnNullForEmptyIdentifier() {
    // when
    Location location = locationGateway.resolveByIdentifier("");

    // then
    assertNull(location);
  }

  @Test
  void shouldBeCaseSensitive() {
    // when
    Location location = locationGateway.resolveByIdentifier("zwolle-001");

    // then
    assertNull(location); // Should not match because of case sensitivity
  }

  @Test
  void shouldReturnNullForPartialMatch() {
    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE");

    // then
    assertNull(location); // Should not match partial identifier
  }
}
