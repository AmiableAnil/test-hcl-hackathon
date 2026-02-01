package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductTest {

  @Test
  void shouldCreateProductWithDefaultConstructor() {
    // when
    Product product = new Product();

    // then
    assertNull(product.id);
    assertNull(product.name);
    assertNull(product.description);
    assertNull(product.price);
    assertEquals(0, product.stock);
  }

  @Test
  void shouldCreateProductWithName() {
    // when
    Product product = new Product("Test Product");

    // then
    assertEquals("Test Product", product.name);
    assertEquals(0, product.stock);
  }

  @Test
  void shouldAllowSettingAllFields() {
    // given
    Product product = new Product();

    // when
    product.id = 1L;
    product.name = "Full Product";
    product.description = "A product description";
    product.price = new BigDecimal("19.99");
    product.stock = 100;

    // then
    assertEquals(1L, product.id);
    assertEquals("Full Product", product.name);
    assertEquals("A product description", product.description);
    assertEquals(new BigDecimal("19.99"), product.price);
    assertEquals(100, product.stock);
  }

  @Test
  void shouldAllowNullOptionalFields() {
    // given
    Product product = new Product("Simple Product");

    // then
    assertNull(product.description);
    assertNull(product.price);
  }
}
