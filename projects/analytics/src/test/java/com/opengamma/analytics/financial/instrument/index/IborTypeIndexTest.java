/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link IborTypeIndex}.
 */
public class IborTypeIndexTest {
  /** The name */
  private static final String NAME = "3M USD LIBOR";
  /** The currency */
  private static final Currency CURRENCY = Currency.USD;
  /** The spot lag */
  private static final int SPOT_LAG = 2;
  /** The day count convention */
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  /** The business day convention */
  private static final BusinessDayConvention BUSINESS_DAY_CONVENTION = BusinessDayConventions.MODIFIED_FOLLOWING;
  /** EOM rule */
  private static final boolean EOM = true;
  /** The index tenor */
  private static final Tenor TENOR = Tenor.THREE_MONTHS;

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new IborTypeIndex(null, CURRENCY, TENOR, SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM);
  }

  /**
   * Tests the behaviour when the currency is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new IborTypeIndex(NAME, null, TENOR, SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM);
  }

  /**
   * Tests the behaviour when the tenor is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTenor() {
    new IborTypeIndex(NAME, CURRENCY, null, SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM);
  }

  /**
   * Tests the behaviour when the day count is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new IborTypeIndex(NAME, CURRENCY, TENOR, SPOT_LAG, null, BUSINESS_DAY_CONVENTION, EOM);
  }

  /**
   * Tests the behaviour when the business day convention is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDayConvention() {
    new IborTypeIndex(NAME, CURRENCY, TENOR, SPOT_LAG, DAY_COUNT, null, EOM);
  }

  /**
   * Tests the object.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testObject() {
    final IborTypeIndex index = new IborTypeIndex(NAME, CURRENCY, TENOR, SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM);
    assertEquals(index.getName(), NAME);
    assertEquals(index.getCurrency(), CURRENCY);
    assertEquals(index.getTenor(), TENOR);
    assertEquals(index.getSpotLag(), SPOT_LAG);
    assertEquals(index.getDayCount(), DAY_COUNT);
    assertEquals(index.getBusinessDayConvention(), BUSINESS_DAY_CONVENTION);
    assertEquals(index.isEndOfMonth(), EOM);
    assertEquals(index.toString(), "IborIndex[3M USD LIBOR, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Modified Following, spot lag=2, end-of-month]");
    IborTypeIndex other = new IborTypeIndex(NAME, CURRENCY, TENOR, SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM);
    assertEquals(index, other);
    assertEquals(index.hashCode(), other.hashCode());
    assertEquals(index, index);
    assertNotEquals(new IborIndex(CURRENCY, TENOR.getPeriod(), SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM, NAME), index);
    other = new IborTypeIndex("TEST", CURRENCY, TENOR, SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM);
    assertNotEquals(index, other);
    other = new IborTypeIndex(NAME, Currency.GBP, TENOR, SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM);
    assertNotEquals(index, other);
    other = new IborTypeIndex(NAME, CURRENCY, Tenor.SIX_MONTHS, SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM);
    assertNotEquals(index, other);
    other = new IborTypeIndex(NAME, CURRENCY, TENOR, SPOT_LAG - 1, DAY_COUNT, BUSINESS_DAY_CONVENTION, EOM);
    assertNotEquals(index, other);
    other = new IborTypeIndex(NAME, CURRENCY, TENOR, SPOT_LAG, DayCounts.ACT_365, BUSINESS_DAY_CONVENTION, EOM);
    assertNotEquals(index, other);
    other = new IborTypeIndex(NAME, CURRENCY, TENOR, SPOT_LAG, DAY_COUNT, BusinessDayConventions.FOLLOWING, EOM);
    assertNotEquals(index, other);
    other = new IborTypeIndex(NAME, CURRENCY, TENOR, SPOT_LAG, DAY_COUNT, BUSINESS_DAY_CONVENTION, !EOM);
    assertNotEquals(index, other);
  }
}
