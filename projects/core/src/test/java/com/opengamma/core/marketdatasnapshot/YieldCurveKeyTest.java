/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link YieldCurveKey}.
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveKeyTest extends AbstractFudgeBuilderTestCase {
  private static final Currency CCY = Currency.AUD;
  private static final String NAME = "FOO";

  /**
   * Tests the hashCode method.
   */
  @Test
  public void testHashCode() {
    assertEquals(YieldCurveKey.of(CCY, NAME).hashCode(), YieldCurveKey.of(CCY, NAME).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final YieldCurveKey key = YieldCurveKey.of(CCY, NAME);
    assertEquals(key, key);
    assertNotEquals(null, key);
    assertNotEquals(key, NAME);
    YieldCurveKey other = YieldCurveKey.of(Currency.of("AUD"), "FOO");
    assertEquals(key, other);
    other = YieldCurveKey.of(Currency.BRL, NAME);
    assertNotEquals(key, other);
    other = YieldCurveKey.of(Currency.AUD, "BAR");
    assertNotEquals(key, other);
  }

  /**
   * Tests the compareTo method.
   */
  @Test
  public void testCompareTo() {
    final YieldCurveKey key = YieldCurveKey.of(CCY, NAME);
    assertEquals(key.compareTo(YieldCurveKey.of(CCY, NAME)), 0);
    assertNotEquals(key.compareTo(YieldCurveKey.of(Currency.of("AAA"), NAME)), 0);
    assertNotEquals(key.compareTo(YieldCurveKey.of(Currency.of("ZZZ"), NAME)), 0);
    assertNotEquals(key.compareTo(YieldCurveKey.of(CCY, "z" + NAME)), 0);
    assertNotEquals(key.compareTo(YieldCurveKey.of(CCY, "a" + NAME)), 0);
  }

  /**
   * Tests the visitor.
   */
  @Test
  public void testVisitor() {
    final YieldCurveKey key = YieldCurveKey.of(CCY, NAME);
    assertEquals(key.accept(TestKeyVisitor.INSTANCE), "YieldCurveKey");
  }

  /**
   * Tests a message cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(YieldCurveKey.class, YieldCurveKey.of(CCY, NAME));
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final YieldCurveKey key = YieldCurveKey.of(CCY, NAME);
    assertNotNull(key.metaBean());
    assertNotNull(key.metaBean().currency());
    assertNotNull(key.metaBean().name());
    assertEquals(key.metaBean().currency().get(key), CCY);
    assertEquals(key.metaBean().name().get(key), NAME);
    assertEquals(key.property("currency").get(), CCY);
    assertEquals(key.property("name").get(), NAME);
  }

}
