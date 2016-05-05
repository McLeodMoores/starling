/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
/**
 * Unit tests for {@link ScalarMarketDataMetaData}.
 */
@Test(groups = TestGroup.UNIT)
public class ScalarMarketDataMetaDataTest {

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(ScalarMarketDataMetaData.INSTANCE.getType(), Double.class);
    assertEquals(ScalarMarketDataMetaData.INSTANCE.toString(), "ScalarMarketDataMetaData[Double]");
  }
}
