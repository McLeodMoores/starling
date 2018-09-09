/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link VolatilityPoint}.
 */
public class VolatilityPointTest extends AbstractFudgeBuilderTestCase {
  private static final Tenor SWAP_TENOR = Tenor.TEN_YEARS;
  private static final Tenor OPTION_EXPIRY = Tenor.FIVE_YEARS;
  private static final double RELATIVE_STRIKE = 30;

  /**
   * Tests the hashCode method.
   */
  @Test
  public void testHashCode() {
    assertEquals(new VolatilityPoint(SWAP_TENOR, OPTION_EXPIRY, RELATIVE_STRIKE).hashCode(),
        new VolatilityPoint(SWAP_TENOR, OPTION_EXPIRY, RELATIVE_STRIKE).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final VolatilityPoint point = new VolatilityPoint(SWAP_TENOR, OPTION_EXPIRY, RELATIVE_STRIKE);
    assertEquals(point, point);
    assertNotEquals(null, point);
    assertNotEquals(point, SWAP_TENOR);
    VolatilityPoint other = new VolatilityPoint(SWAP_TENOR, OPTION_EXPIRY, RELATIVE_STRIKE);
    assertEquals(point, other);
    other = new VolatilityPoint(OPTION_EXPIRY, OPTION_EXPIRY, RELATIVE_STRIKE);
    assertNotEquals(point, other);
    other = new VolatilityPoint(SWAP_TENOR, SWAP_TENOR, RELATIVE_STRIKE);
    assertNotEquals(point, other);
    other = new VolatilityPoint(SWAP_TENOR, OPTION_EXPIRY, RELATIVE_STRIKE + 10);
    assertNotEquals(point, other);
  }

  /**
   * Tests a message cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(VolatilityPoint.class, new VolatilityPoint(SWAP_TENOR, OPTION_EXPIRY, RELATIVE_STRIKE));
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final VolatilityPoint point = new VolatilityPoint(SWAP_TENOR, OPTION_EXPIRY, RELATIVE_STRIKE);
    assertNotNull(point.metaBean());
    assertNotNull(point.metaBean().swapTenor());
    assertNotNull(point.metaBean().optionExpiry());
    assertNotNull(point.metaBean().relativeStrike());
    assertEquals(point.metaBean().swapTenor().get(point), SWAP_TENOR);
    assertEquals(point.metaBean().optionExpiry().get(point), OPTION_EXPIRY);
    assertEquals(point.metaBean().relativeStrike().get(point), RELATIVE_STRIKE);
    assertEquals(point.property("swapTenor").get(), SWAP_TENOR);
    assertEquals(point.property("optionExpiry").get(), OPTION_EXPIRY);
    assertEquals(point.property("relativeStrike").get(), RELATIVE_STRIKE);
  }

}
