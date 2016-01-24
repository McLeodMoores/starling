/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link PortfolioNodeTargetKey}.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioNodeTargetKeyTest {

  /**
   * Tests the behaviour when the node path is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfNull() {
    PortfolioNodeTargetKey.of(null);
  }

  /**
   * Tests creation of the key.
   */
  @Test
  public void testOf() {
    Assert.assertNotNull(PortfolioNodeTargetKey.of(""));
    Assert.assertNotNull(PortfolioNodeTargetKey.of("A"));
    Assert.assertNotNull(PortfolioNodeTargetKey.of("A/B"));
    Assert.assertNotNull(PortfolioNodeTargetKey.of("A/B/C"));
  }

  /**
   * Tests getting the node name.
   */
  @Test
  public void testGetNodeName() {
    Assert.assertEquals(PortfolioNodeTargetKey.of("").getNodePath(), "");
    Assert.assertEquals(PortfolioNodeTargetKey.of("A").getNodePath(), "A");
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B").getNodePath(), "A/B");
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B/C").getNodePath(), "A/B/C");
  }

  /**
   * Tests the hashcode.
   */
  @Test
  public void testHashCode() {
    Assert.assertEquals(PortfolioNodeTargetKey.of("").hashCode(), PortfolioNodeTargetKey.of("").hashCode());
    Assert.assertEquals(PortfolioNodeTargetKey.of("A").hashCode(), PortfolioNodeTargetKey.of("A").hashCode());
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B").hashCode(), PortfolioNodeTargetKey.of("A/B").hashCode());
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B/C").hashCode(), PortfolioNodeTargetKey.of("A/B/C").hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final PortfolioNodeTargetKey rootTargetKey = PortfolioNodeTargetKey.of("");
    Assert.assertEquals(rootTargetKey, rootTargetKey);
    Assert.assertEquals(rootTargetKey, PortfolioNodeTargetKey.of(""));
    Assert.assertEquals(PortfolioNodeTargetKey.of("A"), PortfolioNodeTargetKey.of("A"));
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B"), PortfolioNodeTargetKey.of("A/B"));
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B/C"), PortfolioNodeTargetKey.of("A/B/C"));
    Assert.assertNotEquals(PortfolioNodeTargetKey.of("A"), PortfolioNodeTargetKey.of("B"));
    Assert.assertNotEquals(null, PortfolioNodeTargetKey.of("A"));
    Assert.assertNotEquals(new Object(), PortfolioNodeTargetKey.of("A"));
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    Assert.assertEquals(PortfolioNodeTargetKey.of("").toString(), "PortfolioNodeTargetKey[]");
    Assert.assertEquals(PortfolioNodeTargetKey.of("A").toString(), "PortfolioNodeTargetKey[A]");
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B").toString(), "PortfolioNodeTargetKey[A/B]");
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B/C").toString(), "PortfolioNodeTargetKey[A/B/C]");
  }
}