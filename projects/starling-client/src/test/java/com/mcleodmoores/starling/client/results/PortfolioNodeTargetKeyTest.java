package com.mcleodmoores.starling.client.results;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jim on 19/06/15.
 */
public class PortfolioNodeTargetKeyTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfNull() throws Exception {
    PortfolioNodeTargetKey.of(null);
  }

  @Test
  public void testOf() throws Exception {
    Assert.assertNotNull(PortfolioNodeTargetKey.of(""));
    Assert.assertNotNull(PortfolioNodeTargetKey.of("A"));
    Assert.assertNotNull(PortfolioNodeTargetKey.of("A/B"));
    Assert.assertNotNull(PortfolioNodeTargetKey.of("A/B/C"));
  }

  @Test
  public void testGetNodeName() throws Exception {
    Assert.assertEquals(PortfolioNodeTargetKey.of("").getNodePath(), "");
    Assert.assertEquals(PortfolioNodeTargetKey.of("A").getNodePath(), "A");
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B").getNodePath(), "A/B");
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B/C").getNodePath(), "A/B/C");
  }

  @Test
  public void testHashCode() throws Exception {
    Assert.assertEquals(PortfolioNodeTargetKey.of("").hashCode(), PortfolioNodeTargetKey.of("").hashCode());
    Assert.assertEquals(PortfolioNodeTargetKey.of("A").hashCode(), PortfolioNodeTargetKey.of("A").hashCode());
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B").hashCode(), PortfolioNodeTargetKey.of("A/B").hashCode());
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B/C").hashCode(), PortfolioNodeTargetKey.of("A/B/C").hashCode());
  }

  @Test
  public void testEquals() throws Exception {
    Assert.assertEquals(PortfolioNodeTargetKey.of(""), PortfolioNodeTargetKey.of(""));
    Assert.assertEquals(PortfolioNodeTargetKey.of("A"), PortfolioNodeTargetKey.of("A"));
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B"), PortfolioNodeTargetKey.of("A/B"));
    Assert.assertEquals(PortfolioNodeTargetKey.of("A/B/C"), PortfolioNodeTargetKey.of("A/B/C"));
    Assert.assertNotEquals(PortfolioNodeTargetKey.of("A"), PortfolioNodeTargetKey.of("B"));
    Assert.assertNotEquals(PortfolioNodeTargetKey.of("A"), new Object());
    Assert.assertNotEquals(PortfolioNodeTargetKey.of("A"), null);
  }
}