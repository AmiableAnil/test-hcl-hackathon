package com.fulfilment.application.monolith.warehouses.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class WarehouseValidationExceptionTest {

  @Test
  void shouldCreateExceptionWithMessage() {
    // given
    String message = "Warehouse validation failed";

    // when
    WarehouseValidationException exception = new WarehouseValidationException(message);

    // then
    assertEquals(message, exception.getMessage());
  }

  @Test
  void shouldBeRuntimeException() {
    // given
    WarehouseValidationException exception = new WarehouseValidationException("test");

    // then
    assertTrue(exception instanceof RuntimeException);
  }
}
