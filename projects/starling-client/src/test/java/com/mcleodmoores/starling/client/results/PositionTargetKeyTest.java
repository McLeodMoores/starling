/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link PositionTargetKey}.
 */
@Test(groups = TestGroup.UNIT)
public class PositionTargetKeyTest {

  /**
   * Tests the behaviour when a null correlation id is supplied.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOf() {
    PositionTargetKey.of(null);
  }

  /**
   * Tests the creation of a key.
   */
  @Test
  public void testOf() {
    Assert.assertNotNull(PositionTargetKey.of(ExternalId.of("A", "B")));
  }

  /**
   * Tests that the correct correlation id is returned.
   */
  @Test
  public void testGetCorrelationId() {
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("A", "B")).getCorrelationId(), ExternalId.of("A", "B"));
  }

  /**
   * Tests the hashcode.
   */
  @Test
  public void testHashCode() {
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("A", "B")).hashCode(), PositionTargetKey.of(ExternalId.of("A", "B")).hashCode());
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("C", "D")).hashCode(), PositionTargetKey.of(ExternalId.of("C", "D")).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final PositionTargetKey ab = PositionTargetKey.of(ExternalId.of("A", "B"));
    Assert.assertEquals(ab, ab);
    Assert.assertEquals(ab, PositionTargetKey.of(ExternalId.of("A", "B")));
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("C", "D")), PositionTargetKey.of(ExternalId.of("C", "D")));
    Assert.assertNotEquals(ab, PositionTargetKey.of(ExternalId.of("C", "D")));
    Assert.assertNotEquals(ab, PositionTargetKey.of(ExternalId.of("A", "D")));
    Assert.assertNotEquals(ab, PositionTargetKey.of(ExternalId.of("C", "B")));
    Assert.assertNotEquals(null, ab);
    Assert.assertNotEquals(new Object(), ab);
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("A", "B")).toString(), "PositionTargetKey[correlationId=A~B]");
  }
}