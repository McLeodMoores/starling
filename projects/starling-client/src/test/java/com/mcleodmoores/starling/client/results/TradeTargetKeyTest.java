/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link TradeTargetKey}.
 */
@Test(groups = TestGroup.UNIT)
public class TradeTargetKeyTest {

  /**
   * Tests the behaviour when a null correlation id is supplied.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOf()  {
    TradeTargetKey.of(null);
  }

  /**
   * Tests creation of the key.
   */
  @Test
  public void testOf()  {
    Assert.assertNotNull(TradeTargetKey.of(ExternalId.of("A", "B")));
  }

  /**
   * Tests that the correct correlation id is returned.
   */
  @Test
  public void testGetCorrelationId()  {
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("A", "B")).getCorrelationId(), ExternalId.of("A", "B"));
  }

  /**
   * Tests the hashcode.
   */
  @Test
  public void testHashCode()  {
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("A", "B")).hashCode(), TradeTargetKey.of(ExternalId.of("A", "B")).hashCode());
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("C", "D")).hashCode(), TradeTargetKey.of(ExternalId.of("C", "D")).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals()  {
    final TradeTargetKey ab = TradeTargetKey.of(ExternalId.of("A", "B"));
    Assert.assertEquals(ab, ab);
    Assert.assertEquals(ab, TradeTargetKey.of(ExternalId.of("A", "B")));
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("C", "D")), TradeTargetKey.of(ExternalId.of("C", "D")));
    Assert.assertNotEquals(ab, TradeTargetKey.of(ExternalId.of("C", "D")));
    Assert.assertNotEquals(ab, TradeTargetKey.of(ExternalId.of("A", "D")));
    Assert.assertNotEquals(ab, TradeTargetKey.of(ExternalId.of("C", "B")));
    Assert.assertNotEquals(new Object(), ab);
    Assert.assertNotEquals(null, ab);
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    Assert.assertEquals(TradeTargetKey.of(ExternalId.of("A", "B")).toString(), "TradeTargetKey[correlationId=A~B]");
  }
}