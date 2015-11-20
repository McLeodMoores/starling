package com.mcleodmoores.starling.client.results;

import com.opengamma.engine.ComputationTargetSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jim on 19/06/15.
 */
public class MarketDataTargetKeyTest {

  @Test
  public void testInstance() throws Exception {
    Assert.assertNotNull(MarketDataTargetKey.instance());
  }

  @Test
  public void testHashCode() throws Exception {
    Assert.assertEquals(MarketDataTargetKey.instance().hashCode(), MarketDataTargetKey.instance().hashCode());
  }

  @Test
  public void testEquals() throws Exception {
    Assert.assertEquals(MarketDataTargetKey.instance(), MarketDataTargetKey.instance());
    Assert.assertNotEquals(MarketDataTargetKey.instance(), LegacyTargetKey.of(ComputationTargetSpecification.NULL));
    Assert.assertNotEquals(MarketDataTargetKey.instance(), null);
  }
}