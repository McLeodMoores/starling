/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link SwapIndex}.
 */
public class SwapIndexTest {
  /** The name */
  private static final String NAME = "USD 5Y Swap";
  /** The currency */
  private static final Currency CURRENCY = Currency.USD;
  /** The fixed leg payment tenor */
  private static final Tenor FIXED_LEG_TENOR = Tenor.SIX_MONTHS;
  /** The fixed leg day count */
  private static final DayCount FIXED_LEG_DAYCOUNT = DayCounts.THIRTY_U_360;
  /** The floating leg index */
  private static final IborTypeIndex IBOR_INDEX = new IborTypeIndex("USD 3M LIBOR", CURRENCY, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, false);
  /** The swap tenor */
  private static final Tenor SWAP_TENOR = Tenor.FIVE_YEARS;

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new SwapIndex(null, CURRENCY, FIXED_LEG_TENOR, FIXED_LEG_DAYCOUNT, IBOR_INDEX, SWAP_TENOR);
  }

  /**
   * Tests the behaviour when the currency is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new SwapIndex(NAME, null, FIXED_LEG_TENOR, FIXED_LEG_DAYCOUNT, IBOR_INDEX, SWAP_TENOR);
  }

  /**
   * Tests the behaviour when the fixed leg tenor is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedLegTenor() {
    new SwapIndex(NAME, CURRENCY, null, FIXED_LEG_DAYCOUNT, IBOR_INDEX, SWAP_TENOR);
  }

  /**
   * Tests the behaviour when the fixed leg day count is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new SwapIndex(NAME, CURRENCY, FIXED_LEG_TENOR, null, IBOR_INDEX, SWAP_TENOR);
  }

  /**
   * Tests the behaviour when the ibor index is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIborIndex() {
    new SwapIndex(NAME, CURRENCY, SWAP_TENOR, FIXED_LEG_DAYCOUNT, null, SWAP_TENOR);
  }

  /**
   * Tests the behaviour when the swap tenor is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSwapTenor() {
    new SwapIndex(NAME, CURRENCY, FIXED_LEG_TENOR, FIXED_LEG_DAYCOUNT, IBOR_INDEX, null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final SwapIndex index = new SwapIndex(NAME, CURRENCY, FIXED_LEG_TENOR, FIXED_LEG_DAYCOUNT, IBOR_INDEX, SWAP_TENOR);
    assertEquals(index.getName(), NAME);
    assertEquals(index.getCurrency(), CURRENCY);
    assertEquals(index.getFixedLegPaymentTenor(), FIXED_LEG_TENOR);
    assertEquals(index.getFixedLegDayCount(), FIXED_LEG_DAYCOUNT);
    assertEquals(index.getIborIndex(), IBOR_INDEX);
    assertEquals(index.getSwapTenor(), SWAP_TENOR);
    assertEquals(index.toString(), "SwapIndex[USD 5Y Swap, currency=USD, swap tenor=P5Y, ibor index=IborIndex[USD 3M LIBOR, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Modified Following, spot lag=2]]");
    assertEquals(index, index);
    assertNotEquals(new IndexSwap(FIXED_LEG_TENOR.getPeriod(), FIXED_LEG_DAYCOUNT, new IborIndex(CURRENCY, Tenor.THREE_MONTHS.getPeriod(), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, false, "USD 3M LIBOR"), SWAP_TENOR.getPeriod(), new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)), index);
    SwapIndex other = new SwapIndex(NAME, CURRENCY, FIXED_LEG_TENOR, FIXED_LEG_DAYCOUNT, IBOR_INDEX, SWAP_TENOR);
    assertEquals(index, other);
    assertEquals(index.hashCode(), other.hashCode());
    other = new SwapIndex("TEST", CURRENCY, FIXED_LEG_TENOR, FIXED_LEG_DAYCOUNT, IBOR_INDEX, SWAP_TENOR);
    assertNotEquals(index, other);
    other = new SwapIndex(NAME, Currency.AUD, FIXED_LEG_TENOR, FIXED_LEG_DAYCOUNT, IBOR_INDEX, SWAP_TENOR);
    assertNotEquals(index, other);
    other = new SwapIndex(NAME, CURRENCY, Tenor.THREE_MONTHS, FIXED_LEG_DAYCOUNT, IBOR_INDEX, SWAP_TENOR);
    assertNotEquals(index, other);
    other = new SwapIndex(NAME, CURRENCY, FIXED_LEG_TENOR, DayCounts.ACT_365, IBOR_INDEX, SWAP_TENOR);
    assertNotEquals(index, other);
    other = new SwapIndex(NAME, CURRENCY, FIXED_LEG_TENOR, FIXED_LEG_DAYCOUNT, new IborTypeIndex("USD 6M LIBOR", CURRENCY, Tenor.SIX_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, false), SWAP_TENOR);
    assertNotEquals(index, other);
    other = new SwapIndex(NAME, CURRENCY, FIXED_LEG_TENOR, FIXED_LEG_DAYCOUNT, IBOR_INDEX, Tenor.TEN_YEARS);
    assertNotEquals(index, other);
  }
}
