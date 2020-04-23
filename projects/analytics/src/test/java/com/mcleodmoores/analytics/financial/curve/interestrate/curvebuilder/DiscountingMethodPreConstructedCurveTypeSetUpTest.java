/**
 * Copyright (C) 2020 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class DiscountingMethodPreConstructedCurveTypeSetUpTest {
  private static final UniqueIdentifiable CCY = Currency.GBP;
  private static final IborTypeIndex[] IBOR = new IborTypeIndex[] {
      new IborTypeIndex("3M", Currency.GBP, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, false),
      new IborTypeIndex("6M", Currency.GBP, Tenor.SIX_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, false) };
  private static final OvernightIndex[] OVERNIGHT = new OvernightIndex[] { new OvernightIndex("SONIA", Currency.GBP, DayCounts.ACT_360, 1) };
  private static final DiscountingMethodCurveSetUp SETUP = new DiscountingMethodCurveSetUp();

  /**
   * Tests that the discounting id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDiscountingId() {
    new DiscountingMethodPreConstructedCurveTypeSetUp(SETUP).forDiscounting(null);
  }

  /**
   * Tests that the ibor indices cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIborIndices() {
    new DiscountingMethodPreConstructedCurveTypeSetUp(SETUP).forIndex((IborTypeIndex[]) null);
  }

  /**
   * Tests that the overnight indices cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOvernightIndices() {
    new DiscountingMethodPreConstructedCurveTypeSetUp(SETUP).forIndex((OvernightIndex[]) null);
  }

  /**
   * Test the getters.
   */
  @Test
  public void testGetters() {
    final DiscountingMethodPreConstructedCurveTypeSetUp setup = new DiscountingMethodPreConstructedCurveTypeSetUp(SETUP);
    setup.forDiscounting(CCY);
    setup.forIndex(IBOR);
    setup.forIndex(OVERNIGHT);
    assertEquals(CCY, setup.getDiscountingCurveId());
    assertEquals(IBOR, setup.getIborCurveIndices().toArray());
    assertEquals(OVERNIGHT, setup.getOvernightCurveIndices().toArray());
  }

  /**
   * Tests equals and hashCode.
   */
  @Test
  public void testEqualsHashCode() {
    final DiscountingMethodPreConstructedCurveTypeSetUp setup = new DiscountingMethodPreConstructedCurveTypeSetUp(SETUP);
    setup.forDiscounting(CCY);
    setup.forIndex(IBOR);
    setup.forIndex(OVERNIGHT);
    DiscountingMethodPreConstructedCurveTypeSetUp other = new DiscountingMethodPreConstructedCurveTypeSetUp(SETUP);
    other.forDiscounting(CCY);
    other.forIndex(IBOR);
    other.forIndex(OVERNIGHT);
    assertEquals(setup, other);
    assertEquals(setup.hashCode(), other.hashCode());
    other = new DiscountingMethodPreConstructedCurveTypeSetUp(SETUP);
    other.forIndex(IBOR);
    other.forIndex(OVERNIGHT);
    assertNotEquals(other, setup);
    other = new DiscountingMethodPreConstructedCurveTypeSetUp(SETUP);
    other.forDiscounting(CCY);
    other.forIndex(OVERNIGHT);
    assertNotEquals(other, setup);
    other = new DiscountingMethodPreConstructedCurveTypeSetUp(SETUP);
    other.forDiscounting(CCY);
    other.forIndex(IBOR);
    assertNotEquals(other, setup);
  }
}
