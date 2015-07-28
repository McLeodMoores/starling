/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link CurrencyLabelledMatrix1D}.
 */
public class CurrencyLabelledMatrix1DTest {
  /** The currencies */
  private static final Currency[] CURRENCIES = { Currency.AUD, Currency.BRL, Currency.CAD};
  /** The values */
  private static final double[] VALUES = {100, 1000, 10000};
  /** The matrix */
  private static final CurrencyLabelledMatrix1D MATRIX = new CurrencyLabelledMatrix1D(CURRENCIES, VALUES);

  /**
   * Tests the behaviour when the currency is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyGetValueForCurrency() {
    MATRIX.getValueForCurrency(null);
  }

  /**
   * Tests the behaviour when the currency is not present in the matrix.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoValueForCurrency() {
    MATRIX.getValueForCurrency(Currency.CHF);
  }

  /**
   * Tests that the correct value for a currency is obtained from the matrix.
   */
  @Test
  public void testGetValueForCurrency() {
    assertEquals(MATRIX.getValueForCurrency(Currency.AUD), 100.);
    assertEquals(MATRIX.getValueForCurrency(Currency.BRL), 1000.);
    assertEquals(MATRIX.getValueForCurrency(Currency.CAD), 10000.);
  }
}
