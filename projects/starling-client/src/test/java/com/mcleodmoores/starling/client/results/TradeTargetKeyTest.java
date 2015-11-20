package com.mcleodmoores.starling.client.results;

import com.opengamma.id.ExternalId;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by jim on 19/06/15.
 */
public class TradeTargetKeyTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOf() throws Exception {
    TradeTargetKey.of(null);
  }
  @Test
  public void testOf() throws Exception {
    Assert.assertNotNull(TradeTargetKey.of(ExternalId.of("A", "B")));
  }

  @Test
  public void testGetCorrelationId() throws Exception {
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("A", "B")).getCorrelationId(), ExternalId.of("A", "B"));
  }

  @Test
  public void testHashCode() throws Exception {
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("A", "B")).hashCode(), TradeTargetKey.of(ExternalId.of("A", "B")).hashCode());
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("C", "D")).hashCode(), TradeTargetKey.of(ExternalId.of("C", "D")).hashCode());
  }

  @Test
  public void testEquals() throws Exception {
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("A", "B")), TradeTargetKey.of(ExternalId.of("A", "B")));
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("C", "D")), TradeTargetKey.of(ExternalId.of("C", "D")));
    Assert.assertNotEquals(TradeTargetKey.of(ExternalId.of("A", "B")), TradeTargetKey.of(ExternalId.of("C", "D")));
    Assert.assertNotEquals(TradeTargetKey.of(ExternalId.of("A", "B")), TradeTargetKey.of(ExternalId.of("A", "D")));
    Assert.assertNotEquals(TradeTargetKey.of(ExternalId.of("A", "B")), TradeTargetKey.of(ExternalId.of("C", "B")));
    Assert.assertNotEquals(TradeTargetKey.of(ExternalId.of("A", "B")), new Object());
    Assert.assertNotEquals(TradeTargetKey.of(ExternalId.of("A", "B")), null);
  }
}