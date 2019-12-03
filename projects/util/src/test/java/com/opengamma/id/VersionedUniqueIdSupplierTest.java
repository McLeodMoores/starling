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
 * Test {@link VersionedUniqueIdSupplier}.
 */
@Test(groups = TestGroup.UNIT)
public class VersionedUniqueIdSupplierTest {

  /**
   * Tests construction from an object id.
   */
  @Test
  public void testConstructorObjectId() {
    final VersionedUniqueIdSupplier supplier = new VersionedUniqueIdSupplier(ObjectId.of("A", "B"));
    final UniqueId test1 = supplier.get();
    final UniqueId test2 = supplier.get();
    final UniqueId test3 = supplier.get();
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test1.equals(test3));
    assertEquals(ObjectId.of("A", "B"), test1.getObjectId());
    assertEquals(ObjectId.of("A", "B"), test2.getObjectId());
    assertEquals(ObjectId.of("A", "B"), test3.getObjectId());
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorObjectIdNull() {
    new VersionedUniqueIdSupplier((ObjectId) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests construction from strings.
   */
  @Test
  public void testConstructorStringString() {
    final VersionedUniqueIdSupplier supplier = new VersionedUniqueIdSupplier("A", "B");
    final UniqueId test1 = supplier.get();
    final UniqueId test2 = supplier.get();
    final UniqueId test3 = supplier.get();
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test1.equals(test3));
    assertEquals(ObjectId.of("A", "B"), test1.getObjectId());
    assertEquals(ObjectId.of("A", "B"), test2.getObjectId());
    assertEquals(ObjectId.of("A", "B"), test3.getObjectId());
  }

  /**
   * Tests that the scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorStringStringNullScheme() {
    new VersionedUniqueIdSupplier(null, "B");
  }

  /**
   * Tests that the value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorStringStringNullValue() {
    new VersionedUniqueIdSupplier("A", null);
  }

}
