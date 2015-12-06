/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link PriceIndex}.
 */
public class PriceIndexTest {
  /** The index name */
  private static final String NAME = "UK RPI";
  /** The index currency */
  private static final Currency CURRENCY = Currency.GBP;

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new PriceIndex(null, CURRENCY);
  }

  /**
   * Tests the behaviour when the currency is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new PriceIndex(NAME, null);
  }

  /**
   * Tests the object.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testObject() {
    final PriceIndex index = new PriceIndex(NAME, CURRENCY);
    assertEquals(index.getName(), NAME);
    assertEquals(index.getCurrency(), CURRENCY);
    assertEquals(index.toString(), "PriceIndex[UK RPI, currency=GBP]");
    assertEquals(index, index);
    assertNotEquals(new IndexPrice(NAME, CURRENCY), index);
    PriceIndex other = new PriceIndex(NAME, CURRENCY);
    assertEquals(index, other);
    assertEquals(index.hashCode(), other.hashCode());
    other = new PriceIndex("TEST", CURRENCY);
    assertNotEquals(index, other);
    other = new PriceIndex(NAME, Currency.AUD);
    assertNotEquals(index, other);
  }
}
