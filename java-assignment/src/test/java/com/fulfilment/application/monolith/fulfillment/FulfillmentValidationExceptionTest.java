package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FulfillmentValidationExceptionTest {

  @Test
  void shouldCreateExceptionWithMessage() {
    // given
    String message = "Fulfillment validation failed";

    // when
    FulfillmentValidationException exception = new FulfillmentValidationException(message);

    // then
    assertEquals(message, exception.getMessage());
  }

  @Test
  void shouldBeRuntimeException() {
    // given
    FulfillmentValidationException exception = new FulfillmentValidationException("test");

    // then
    assertTrue(exception instanceof RuntimeException);
  }
}
