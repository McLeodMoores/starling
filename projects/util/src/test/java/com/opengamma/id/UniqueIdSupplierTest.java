/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link UniqueIdSupplier}.
 */
@Test(groups = TestGroup.UNIT)
public class UniqueIdSupplierTest {

  /**
   * Tests unique id generation.
   */
  @Test
  public void testBasics() {
    final UniqueIdSupplier test = new UniqueIdSupplier("Scheme");
    assertEquals(UniqueId.parse("Scheme~1"), test.get());
    assertEquals(UniqueId.parse("Scheme~2"), test.get());
    assertEquals(UniqueId.parse("Scheme~3"), test.get());
  }

  /**
   * Tests that a null scheme is not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullScheme() {
    new UniqueIdSupplier((String) null);
  }

  /**
   * Tests that an empty schme is not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorEmptyScheme() {
    new UniqueIdSupplier("");
  }

  /**
   * Tests adding a prefix to the id value.
   */
  @Test
  public void testPrefix() {
    final UniqueIdSupplier test = new UniqueIdSupplier("Prefixing");
    assertEquals(UniqueId.parse("Prefixing~A-1"), test.getWithValuePrefix("A-"));
    assertEquals(UniqueId.parse("Prefixing~A-2"), test.getWithValuePrefix("A-"));
    assertEquals(UniqueId.parse("Prefixing~B-3"), test.getWithValuePrefix("B-"));
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    final UniqueIdSupplier test = new UniqueIdSupplier("Prefixing");
    assertEquals(true, test.toString().contains("Prefixing"));
  }

}
