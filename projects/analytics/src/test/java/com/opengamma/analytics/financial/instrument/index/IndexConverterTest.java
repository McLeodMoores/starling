/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link IndexConverter}.
 */
public class IndexConverterTest {

  /**
   * Tests that the ibor type index cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIborTypeIndex() {
    IndexConverter.toIborIndex(null);
  }

  /**
   * Tests that the ibor type index cannot have a business day tenor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTenorType() {
    IndexConverter.toIborIndex(new IborTypeIndex("", Currency.USD, Tenor.ON, 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, true));
  }

  /**
   * Tests that the overnight index cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOvernightIndex() {
    IndexConverter.toIndexOn(null);
  }

  /**
   * Tests the ibor type index converter.
   */
  @Test
  public void testIborIndexConverter() {
    final String name = "name";
    final Currency currency = Currency.USD;
    final Tenor tenor = Tenor.THREE_MONTHS;
    final int spotLag = 2;
    final DayCount dc = DayCounts.ACT_360;
    final BusinessDayConvention bdc = BusinessDayConventions.MODIFIED_FOLLOWING;
    final boolean isEom = false;
    assertEquals(IndexConverter.toIborIndex(new IborTypeIndex(name, currency, tenor, spotLag, dc, bdc, isEom)),
        new IborIndex(currency, tenor.getPeriod(), spotLag, dc, bdc, isEom, name));
  }

  /**
   * Tests the overnight index converter.
   */
  @Test
  public void testOvernightIndexConverter() {
    final String name = "name";
    final Currency currency = Currency.EUR;
    final DayCount dc = DayCounts.ACT_365;
    final int publicationLag = 1;
    assertEquals(IndexConverter.toIndexOn(new OvernightIndex(name, currency, dc, publicationLag)),
        new IndexON(name, currency, dc, publicationLag));
  }
}
