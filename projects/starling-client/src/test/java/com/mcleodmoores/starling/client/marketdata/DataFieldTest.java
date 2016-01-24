/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link DataField} and {@link DataFieldFactory}.
 */
@Test(groups = TestGroup.UNIT)
public class DataFieldTest {

  /**
   * Tests the behaviour when the field is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    DataField.of(null);
  }

  /**
   * Tests the behaviour with a newly created field.
   */
  public void testNormalOperation() {
    Assert.assertNotNull(DataField.of("PRICE"));
    Assert.assertEquals(DataField.of("PRICE").getName(), "PRICE");
  }

  /**
   * Tests that instances are cached in the factory.
   */
  public void testPooling() {
    Assert.assertTrue(DataField.of("PRICE") == DataField.of("PRICE"));
    Assert.assertFalse(DataField.of("PRICE") == DataField.of("LAST_PRICE"));
  }

  /**
   * Tests the equals method.
   */
  public void testEquals() {
    Assert.assertEquals(DataField.of("PRICE"), DataField.of("PRICE"));
    Assert.assertEquals(DataField.of(""), DataField.of(""));
    Assert.assertEquals(DataField.of("PRICE2"), DataField.of("PRICE2"));
    Assert.assertNotEquals(DataField.of("PRICE"), DataField.of("PRICE2"));
    Assert.assertNotEquals(DataField.of("PRICE2"), DataField.of("PRICE"));
    Assert.assertNotEquals(null, DataField.of("PRICE2"));
    Assert.assertNotEquals(new Object(), DataField.of("PRICE2"));
  }

  /**
   * Tests the hashcode.
   */
  public void testHashcode() {
    Assert.assertEquals(DataField.of("PRICE").hashCode(), DataField.of("PRICE").hashCode());
    Assert.assertEquals(DataField.of("PRICE2").hashCode(), DataField.of("PRICE2").hashCode());
  }

  /**
   * Tests the toString() method.
   */
  public void testToString() {
    Assert.assertEquals(DataField.of("ICPL").toString(), "ICPL");
  }

  /**
   * Tests the behaviour when a null name is passed into the factory.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryNull() {
    DataFieldFactory.INSTANCE.of(null);
  }

  /**
   * Tests the factory.
   */
  public void testFactory() {
    Assert.assertEquals(DataField.of("PRICE"), DataFieldFactory.INSTANCE.of("PRICE"));
    Assert.assertEquals(DataFieldFactory.INSTANCE.of("PRICE"), DataField.of("PRICE"));
  }

}