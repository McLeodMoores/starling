/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ValueSnapshot}.
 */
@Test(groups = TestGroup.UNIT)
public class ValueSnapshotTest extends AbstractFudgeBuilderTestCase {
  private static final Object MARKET_VALUE = 4.;
  private static final Object OVERRIDE_VALUE = 10.;

  /**
   * Tests construction of a value snapshot.
   */
  @Test
  public void testConstruction() {
    assertEquals(ValueSnapshot.of(MARKET_VALUE), ValueSnapshot.of(MARKET_VALUE, null));
    assertEquals(ValueSnapshot.of(((Double) MARKET_VALUE).doubleValue()), ValueSnapshot.of(MARKET_VALUE));
  }

  /**
   * Tests the hashCode method.
   */
  @Test
  public void testHashCode() {
    assertEquals(ValueSnapshot.of(MARKET_VALUE, OVERRIDE_VALUE).hashCode(), ValueSnapshot.of(MARKET_VALUE, OVERRIDE_VALUE).hashCode());
    assertEquals(ValueSnapshot.of(MARKET_VALUE).hashCode(), ValueSnapshot.of(MARKET_VALUE).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final ValueSnapshot value = ValueSnapshot.of(MARKET_VALUE, OVERRIDE_VALUE);
    assertEquals(value, value);
    assertNotEquals(null, value);
    assertNotEquals(value, MARKET_VALUE);
    ValueSnapshot other = ValueSnapshot.of(MARKET_VALUE, OVERRIDE_VALUE);
    assertEquals(value, other);
    other = ValueSnapshot.of(OVERRIDE_VALUE, OVERRIDE_VALUE);
    assertNotEquals(value, other);
    other = ValueSnapshot.of(MARKET_VALUE, MARKET_VALUE);
    assertNotEquals(value, other);
    other = ValueSnapshot.of(MARKET_VALUE);
    assertNotEquals(value, other);
  }

  /**
   * Tests a message cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(ValueSnapshot.class, ValueSnapshot.of(MARKET_VALUE, OVERRIDE_VALUE));
    assertEncodeDecodeCycle(ValueSnapshot.class, ValueSnapshot.of(MARKET_VALUE));
    assertEncodeDecodeCycle(ValueSnapshot.class, ValueSnapshot.of(null, null));
  }

  /**
   * Tests Fudge builder.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testFudgeBuilder() {
    final ValueSnapshot value = ValueSnapshot.of(MARKET_VALUE, OVERRIDE_VALUE);
    final MutableFudgeMsg msg = ValueSnapshotFudgeBuilder.INSTANCE.buildMessage(getFudgeSerializer(), value);
    assertEquals(value.toFudgeMsg(getFudgeSerializer()), msg);
    assertEquals(ValueSnapshot.fromFudgeMsg(getFudgeDeserializer(), msg), value);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ValueSnapshot value = ValueSnapshot.of(MARKET_VALUE, OVERRIDE_VALUE);
    assertNotNull(value.metaBean());
    assertNotNull(value.metaBean().marketValue());
    assertNotNull(value.metaBean().overrideValue());
    assertEquals(value.metaBean().marketValue().get(value), MARKET_VALUE);
    assertEquals(value.metaBean().overrideValue().get(value), OVERRIDE_VALUE);
    assertEquals(value.property("marketValue").get(), MARKET_VALUE);
    assertEquals(value.property("overrideValue").get(), OVERRIDE_VALUE);
  }
}
