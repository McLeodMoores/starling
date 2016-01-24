/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link MarketDataTargetKey}.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataTargetKeyTest {

  /**
   * Tests that the instance created is not null
   */
  @Test
  public void testInstance() {
    Assert.assertNotNull(MarketDataTargetKey.instance());
  }

  /**
   * Tests the hashcode.
   */
  @Test
  public void testHashCode() {
    Assert.assertEquals(MarketDataTargetKey.instance().hashCode(), MarketDataTargetKey.instance().hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final MarketDataTargetKey targetKey = MarketDataTargetKey.instance();
    Assert.assertEquals(targetKey, targetKey);
    Assert.assertEquals(targetKey, MarketDataTargetKey.instance());
    Assert.assertNotEquals(null, targetKey);
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.NULL), targetKey);
    Assert.assertNotEquals(targetKey, null);
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    Assert.assertEquals(MarketDataTargetKey.instance().toString(), "MarketDataTargetKey[SINGLETON]");
  }
}