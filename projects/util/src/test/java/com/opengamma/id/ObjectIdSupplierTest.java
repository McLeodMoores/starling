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
 * Test {@link ObjectIdSupplier}.
 */
@Test(groups = TestGroup.UNIT)
public class ObjectIdSupplierTest {

  /**
   * Tests object id generation.
   */
  @Test
  public void testBasics() {
    final String scheme = "Scheme";
    final ObjectIdSupplier test = new ObjectIdSupplier(scheme);
    assertEquals(test.getScheme(), scheme);
    assertEquals(ObjectId.parse(scheme + "~1"), test.get());
    assertEquals(ObjectId.parse(scheme + "~2"), test.get());
    assertEquals(ObjectId.parse(scheme + "~3"), test.get());
  }

  /**
   * Tests that a null scheme is not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullScheme() {
    new ObjectIdSupplier((String) null);
  }

  /**
   * Tests that an empty scheme is not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorEmptyScheme() {
    new ObjectIdSupplier("");
  }

  /**
   * Tests adding a prefix to the id value.
   */
  @Test
  public void testPrefix() {
    final ObjectIdSupplier test = new ObjectIdSupplier("Prefixing");
    assertEquals(ObjectId.parse("Prefixing~A-1"), test.getWithValuePrefix("A-"));
    assertEquals(ObjectId.parse("Prefixing~A-2"), test.getWithValuePrefix("A-"));
    assertEquals(ObjectId.parse("Prefixing~B-3"), test.getWithValuePrefix("B-"));
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    final ObjectIdSupplier test = new ObjectIdSupplier("Prefixing");
    assertEquals(true, test.toString().contains("Prefixing"));
  }

}
