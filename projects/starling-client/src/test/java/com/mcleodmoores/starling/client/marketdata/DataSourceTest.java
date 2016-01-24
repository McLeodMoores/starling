/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link DataSource} and {@link DataSourceFactory}.
 */
@Test(groups = TestGroup.UNIT)
public class DataSourceTest {

  /**
   * Tests the behaviour when the data provider name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    DataField.of(null);
  }

  /**
   * Tests the behaviour with a newly created data source.
   */
  public void testNormalOperation() {
    Assert.assertNotNull(DataSource.of("QUANDL"));
    Assert.assertEquals(DataSource.of("QUANDL").getName(), "QUANDL");
  }

  /**
   * Tests that instances are cached in the factory.
   */
  public void testPooling() {
    Assert.assertTrue(DataSource.of("QUANDL") == DataSource.of("QUANDL"));
    Assert.assertFalse(DataSource.of("QUANDL") == DataSource.of("LAST_QUANDL"));
  }

  /**
   * Tests the equals method.
   */
  public void testEquals() {
    Assert.assertEquals(DataSource.of("QUANDL"), DataSource.of("QUANDL"));
    Assert.assertEquals(DataSource.of(""), DataSource.of(""));
    Assert.assertEquals(DataSource.of("BLOOMBERG"), DataSource.of("BLOOMBERG"));
    Assert.assertNotEquals(DataSource.of("QUANDL"), DataSource.of("BLOOMBERG"));
    Assert.assertNotEquals(DataSource.of("BLOOMBERG"), DataSource.of("QUANDL"));
    Assert.assertNotEquals(null, DataSource.of("BLOOMBERG"));
    Assert.assertNotEquals(new Object(), DataSource.of("BLOOMBERG"));
  }

  /**
   * Tests the hashcode.
   */
  public void testHashcode() {
    Assert.assertEquals(DataSource.of("QUANDL").hashCode(), DataSource.of("QUANDL").hashCode());
    Assert.assertEquals(DataSource.of("BLOOMBERG").hashCode(), DataSource.of("BLOOMBERG").hashCode());
  }

  /**
   * Tests the toString() method.
   */
  public void testToString() {
    Assert.assertEquals(DataSource.of("QUANDL").toString(), "QUANDL");
  }

  /**
   * Tests the behaviour when a null name is passed into the factory.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryNull() {
    DataSourceFactory.INSTANCE.of(null);
  }

  /**
   * Tests the factory.
   */
  public void testFactory() {
    Assert.assertEquals(DataSource.of("QUANDL"), DataSourceFactory.INSTANCE.of("QUANDL"));
    Assert.assertEquals(DataSourceFactory.INSTANCE.of("QUANDL"), DataSource.of("QUANDL"));
  }

}