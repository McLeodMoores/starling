/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link OvernightIndex}.
 */
public class OvernightIndexTest {
  /** The name */
  private static final String NAME = "USD OVERNIGHT";
  /** The currency */
  private static final Currency CURRENCY = Currency.USD;
  /** The day count */
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  /** The publication lag */
  private static final int PUBLICATION_LAG = 1;

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new OvernightIndex(null, CURRENCY, DAY_COUNT, PUBLICATION_LAG);
  }

  /**
   * Tests the behaviour when the currency is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new OvernightIndex(NAME, null, DAY_COUNT, PUBLICATION_LAG);
  }

  /**
   * Tests the behaviour when the day count is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new OvernightIndex(NAME, CURRENCY, null, PUBLICATION_LAG);
  }

  /**
   * Tests the behaviour when the publication lag is neither 0 nor 1.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongPublicationLag() {
    new OvernightIndex(NAME, CURRENCY, DAY_COUNT, 2);
  }

  /**
   * Tests the object.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testObject() {
    final OvernightIndex index = new OvernightIndex(NAME, CURRENCY, DAY_COUNT, PUBLICATION_LAG);
    assertEquals(index.getName(), NAME);
    assertEquals(index.getCurrency(), CURRENCY);
    assertEquals(index.getDayCount(), DAY_COUNT);
    assertEquals(index.getPublicationLag(), PUBLICATION_LAG);
    assertEquals(index.toString(), "OvernightIndex[USD OVERNIGHT, currency=USD, day count=Actual/360, publication lag=1]");
    assertEquals(index, index);
    assertNotEquals(new IndexON(NAME, CURRENCY, DAY_COUNT, PUBLICATION_LAG), index);
    OvernightIndex other = new OvernightIndex(NAME, CURRENCY, DAY_COUNT, PUBLICATION_LAG);
    assertEquals(other, index);
    assertEquals(other.hashCode(), index.hashCode());
    other = new OvernightIndex("TEST", CURRENCY, DAY_COUNT, PUBLICATION_LAG);
    assertNotEquals(index, other);
    other = new OvernightIndex(NAME, Currency.AUD, DAY_COUNT, PUBLICATION_LAG);
    assertNotEquals(index, other);
    other = new OvernightIndex(NAME, CURRENCY, DayCounts.ACT_365, PUBLICATION_LAG);
    assertNotEquals(index, other);
    other = new OvernightIndex(NAME, CURRENCY, DAY_COUNT, 0);
    assertNotEquals(index, other);
  }
}
