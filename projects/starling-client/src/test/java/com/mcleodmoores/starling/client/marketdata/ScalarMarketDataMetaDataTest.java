/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
/**
 * Unit tests for {@link ScalarMarketDataMetaData}.
 */
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
