/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.model;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the the behaviour of {@link FXMatrix} and {@link CheckedMutableFxMatrix} and {@link UncheckedMutableFxMatrix} are compatible,
 * i.e. that the latter are drop-in replacements for the former.
 */
@Test(groups = TestGroup.UNIT)
public class FxMatrixCompatibilityTest {
  private static final double EUR_PER_USD = 0.8;
  private static final double JPY_PER_USD = 100;
  private static final double NZD_PER_USD = 0.6;
  private static final FXMatrix FX_MATRIX;
  private static final double EPS = 1e-13;

  static {
    FX_MATRIX = new FXMatrix();
    FX_MATRIX.addCurrency(Currency.EUR, Currency.USD, EUR_PER_USD);
    FX_MATRIX.addCurrency(Currency.JPY, Currency.USD, JPY_PER_USD);
    FX_MATRIX.addCurrency(Currency.NZD, Currency.USD, NZD_PER_USD);
  }

  @Test
  public void testOriginalMatrixRates() {
    assertEquals(FX_MATRIX.getFxRate(Currency.EUR, Currency.USD), EUR_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.JPY, Currency.USD), JPY_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.NZD, Currency.USD), NZD_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.USD, Currency.EUR), 1 / EUR_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.USD, Currency.JPY), 1 / JPY_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.USD, Currency.NZD), 1 / NZD_PER_USD, EPS);
  }

  @Test
  public void testOriginalMatrixCrosses() {
    assertEquals(FX_MATRIX.getFxRate(Currency.EUR, Currency.JPY), EUR_PER_USD / JPY_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.EUR, Currency.NZD), EUR_PER_USD / NZD_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.JPY, Currency.NZD), JPY_PER_USD / NZD_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.JPY, Currency.EUR), JPY_PER_USD / EUR_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.NZD, Currency.EUR), NZD_PER_USD / EUR_PER_USD, EPS);
    assertEquals(FX_MATRIX.getFxRate(Currency.NZD, Currency.JPY), NZD_PER_USD / JPY_PER_USD, EPS);
  }

  @Test
  public void testUncheckedAddedInSameOrder() {
    final UncheckedMutableFxMatrix matrix = UncheckedMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, EUR_PER_USD);
    matrix.addCurrency(Currency.JPY, Currency.USD, JPY_PER_USD);
    matrix.addCurrency(Currency.NZD, Currency.USD, NZD_PER_USD);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), FX_MATRIX.getFxRate(Currency.EUR, Currency.USD), EPS);
    assertEquals(matrix.getFxRate(Currency.JPY, Currency.USD), FX_MATRIX.getFxRate(Currency.JPY, Currency.USD), EPS);
    assertEquals(matrix.getFxRate(Currency.NZD, Currency.USD), FX_MATRIX.getFxRate(Currency.NZD, Currency.USD), EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), FX_MATRIX.getFxRate(Currency.USD, Currency.EUR), EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.JPY), FX_MATRIX.getFxRate(Currency.USD, Currency.JPY), EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.NZD), FX_MATRIX.getFxRate(Currency.USD, Currency.NZD), EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.JPY), FX_MATRIX.getFxRate(Currency.EUR, Currency.JPY), EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.NZD), FX_MATRIX.getFxRate(Currency.EUR, Currency.NZD), EPS);
    assertEquals(matrix.getFxRate(Currency.JPY, Currency.NZD), FX_MATRIX.getFxRate(Currency.JPY, Currency.NZD), EPS);
    assertEquals(matrix.getFxRate(Currency.JPY, Currency.EUR), FX_MATRIX.getFxRate(Currency.JPY, Currency.EUR), EPS);
    assertEquals(matrix.getFxRate(Currency.NZD, Currency.EUR), FX_MATRIX.getFxRate(Currency.NZD, Currency.EUR), EPS);
    assertEquals(matrix.getFxRate(Currency.NZD, Currency.JPY), FX_MATRIX.getFxRate(Currency.NZD, Currency.JPY), EPS);
  }

  @Test
  public void testCheckedAddedInSameOrder() {
    final CheckedMutableFxMatrix matrix = CheckedMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, EUR_PER_USD);
    matrix.addCurrency(Currency.JPY, Currency.USD, JPY_PER_USD);
    matrix.addCurrency(Currency.NZD, Currency.USD, NZD_PER_USD);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), FX_MATRIX.getFxRate(Currency.EUR, Currency.USD), EPS);
    assertEquals(matrix.getFxRate(Currency.JPY, Currency.USD), FX_MATRIX.getFxRate(Currency.JPY, Currency.USD), EPS);
    assertEquals(matrix.getFxRate(Currency.NZD, Currency.USD), FX_MATRIX.getFxRate(Currency.NZD, Currency.USD), EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), FX_MATRIX.getFxRate(Currency.USD, Currency.EUR), EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.JPY), FX_MATRIX.getFxRate(Currency.USD, Currency.JPY), EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.NZD), FX_MATRIX.getFxRate(Currency.USD, Currency.NZD), EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.JPY), FX_MATRIX.getFxRate(Currency.EUR, Currency.JPY), EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.NZD), FX_MATRIX.getFxRate(Currency.EUR, Currency.NZD), EPS);
    assertEquals(matrix.getFxRate(Currency.JPY, Currency.NZD), FX_MATRIX.getFxRate(Currency.JPY, Currency.NZD), EPS);
    assertEquals(matrix.getFxRate(Currency.JPY, Currency.EUR), FX_MATRIX.getFxRate(Currency.JPY, Currency.EUR), EPS);
    assertEquals(matrix.getFxRate(Currency.NZD, Currency.EUR), FX_MATRIX.getFxRate(Currency.NZD, Currency.EUR), EPS);
    assertEquals(matrix.getFxRate(Currency.NZD, Currency.JPY), FX_MATRIX.getFxRate(Currency.NZD, Currency.JPY), EPS);
  }
}
