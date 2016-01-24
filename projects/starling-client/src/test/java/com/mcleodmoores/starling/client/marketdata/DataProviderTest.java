/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link DataProvider} and {@link DataProviderFactory}.
 */
@Test(groups = TestGroup.UNIT)
public class DataProviderTest {

  /**
   * Tests the behaviour when the data provider name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    DataField.of(null);
  }

  /**
   * Tests the behaviour with a newly created data provider.
   */
  public void testNormalOperation() {
    Assert.assertNotNull(DataProvider.of("ICPL"));
    Assert.assertEquals(DataProvider.of("ICPL").getName(), "ICPL");
  }

  /**
   * Tests that instances are cached in the factory.
   */
  public void testPooling() {
    Assert.assertTrue(DataProvider.of("ICPL") == DataProvider.of("ICPL"));
    Assert.assertFalse(DataProvider.of("ICPL") == DataProvider.of("LAST_ICPL"));
  }

  /**
   * Tests the equals method.
   */
  public void testEquals() {
    Assert.assertEquals(DataProvider.of("ICPL"), DataProvider.of("ICPL"));
    Assert.assertEquals(DataProvider.of(""), DataProvider.of(""));
    Assert.assertEquals(DataProvider.of("BCAL"), DataProvider.of("BCAL"));
    Assert.assertNotEquals(DataProvider.of("ICPL"), DataProvider.of("BCAL"));
    Assert.assertNotEquals(DataProvider.of("BCAL"), DataProvider.of("ICPL"));
    Assert.assertNotEquals(null, DataProvider.of("BCAL"));
    Assert.assertNotEquals(new Object(), DataProvider.of("BCAL"));
  }

  /**
   * Tests the hashcode.
   */
  public void testHashcode() {
    Assert.assertEquals(DataProvider.of("ICPL").hashCode(), DataProvider.of("ICPL").hashCode());
    Assert.assertEquals(DataProvider.of("BCAL").hashCode(), DataProvider.of("BCAL").hashCode());
  }

  /**
   * Tests the toString() method.
   */
  public void testToString() {
    Assert.assertEquals(DataProvider.of("ICPL").toString(), "ICPL");
  }

  /**
   * Tests the behaviour when a null name is passed into the factory.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryNull() {
    DataProviderFactory.INSTANCE.of(null);
  }

  /**
   * Tests the factory.
   */
  public void testFactory() {
    Assert.assertEquals(DataProvider.of("ICPL"), DataProviderFactory.INSTANCE.of("ICPL"));
    Assert.assertEquals(DataProviderFactory.INSTANCE.of("ICPL"), DataProvider.of("ICPL"));
  }

}