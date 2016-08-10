/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Unit tests for {@link ImmutableFxMatrix}.
 */
public class ImmutableFxMatrixTest {
  /** The tolerance */
  private static final double EPS = 2e-16;
  /** Unchecked FX data */
  private static final UncheckedMutableFxMatrix UNCHECKED_MATRIX = UncheckedMutableFxMatrix.of();
  /** Checked FX data */
  private static final CheckedMutableFxMatrix CHECKED_MATRIX = CheckedMutableFxMatrix.of();
  static {
    UNCHECKED_MATRIX.addCurrency(Currency.EUR, Currency.USD, 1.2);
    UNCHECKED_MATRIX.addCurrency(Currency.CHF, Currency.USD, 0.5);
    UNCHECKED_MATRIX.addCurrency(Currency.GBP, Currency.USD, 1.1);
    UNCHECKED_MATRIX.addCurrency(Currency.CHF, Currency.EUR, 2.3);
    UNCHECKED_MATRIX.addCurrency(Currency.GBP, Currency.EUR, 23);
    UNCHECKED_MATRIX.addCurrency(Currency.GBP, Currency.CHF, 1.4);
    CHECKED_MATRIX.addCurrency(Currency.EUR, Currency.USD, 1.2);
    CHECKED_MATRIX.addCurrency(Currency.CHF, Currency.USD, 0.5);
    CHECKED_MATRIX.addCurrency(Currency.GBP, Currency.USD, 1.1);
    CHECKED_MATRIX.addCurrency(Currency.CHF, Currency.EUR, 0.5 / 1.2);
    CHECKED_MATRIX.addCurrency(Currency.GBP, Currency.EUR, 1.1 / 1.2);
    CHECKED_MATRIX.addCurrency(Currency.GBP, Currency.CHF, 1.1 / 0.5);
  }

  /**
   * Tests that the FX matrix cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying1() {
    ImmutableFxMatrix.of((UncheckedMutableFxMatrix) null);
  }

  /**
   * Tests that the FX matrix cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying2() {
    ImmutableFxMatrix.of((CheckedMutableFxMatrix) null);
  }

  /**
   * Tests that the FX matrix cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying3() {
    ImmutableFxMatrix.of((FXMatrix) null);
  }

  /**
   * Tests the deprecated getCurrencies() method.
   */
  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testDeprecatedGetCurrencies() {
    UNCHECKED_MATRIX.asImmutable().getCurrencies();
  }

  /**
   * Tests the deprecated getRates() method.
   */
  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testDeprecatedGetRates() {
    UNCHECKED_MATRIX.asImmutable().getRates();
  }

  /**
   * Tests that a rate cannot be added.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testAddRateFails() {
    UNCHECKED_MATRIX.asImmutable().addCurrency(Currency.BRL, Currency.CZK, 100);
  }

  /**
   * Tests that a rate cannot be updated.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUpdateRateFails() {
    UNCHECKED_MATRIX.asImmutable().updateRates(Currency.EUR, Currency.USD, 1.4);
  }

  /**
   * Tests that the currency list cannot be changed.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testImmutableCurrencyList() {
    final List<Currency> currencies = UNCHECKED_MATRIX.asImmutable().getCurrencyList();
    currencies.add(Currency.BRL);
  }

  /**
   * Tests that the mutable matrix is mutable, and the immutable matrix is immutable.
   */
  @Test
  public void testImmutability() {
    final UncheckedMutableFxMatrix matrix = UncheckedMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.USD, 1.3);
    matrix.addCurrency(Currency.CHF, Currency.EUR, 2.3);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 23);
    matrix.addCurrency(Currency.GBP, Currency.CHF, 1.4);
    final ImmutableFxMatrix immutable = matrix.asImmutable();
    final List<Currency> originalCurrencies = matrix.getCurrencyList();
    final List<Currency> immutableCurrencies = immutable.getCurrencyList();
    final double[][] originalRates = matrix.getFxRates();
    final double[][] immutableRates = immutable.getFxRates();
    assertEquals(immutableCurrencies, originalCurrencies);
    assertDeepEquals(immutableRates, originalRates, EPS);
    // change the values
    matrix.updateRates(Currency.EUR, Currency.USD, 1.25);
    assertMatrixNotEquals(immutableRates, originalRates, EPS);
    assertDeepEquals(originalRates, matrix.getFxRates(), EPS);
    // change the currencies
    matrix.updateRates(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.BRL, Currency.USD, 7);
    assertNotEquals(immutableCurrencies, originalCurrencies);
    assertEquals(originalCurrencies, matrix.getCurrencyList());
  }

  /**
   * Tests that this matrix delegates to the underlying matrix when getting rate data, converting, etc.
   */
  @Test
  public void testDelegation() {
    final ImmutableFxMatrix immutable = UNCHECKED_MATRIX.asImmutable();
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(new Currency[] {Currency.EUR, Currency.USD}, new double[] {100, 200});
    assertEquals(immutable.containsPair(Currency.EUR, Currency.CHF), UNCHECKED_MATRIX.containsPair(Currency.EUR, Currency.CHF));
    assertEquals(immutable.convert(mca, Currency.CHF), UNCHECKED_MATRIX.convert(mca, Currency.CHF));
    assertEquals(immutable.getNumberOfCurrencies(), UNCHECKED_MATRIX.getNumberOfCurrencies());
    for (final Currency currency : UNCHECKED_MATRIX.getCurrencyList()) {
      assertEquals(immutable.getFxRate(currency, Currency.USD), UNCHECKED_MATRIX.getFxRate(currency, Currency.USD), EPS);
    }
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertNotEquals(new FXMatrix(), ImmutableFxMatrix.of(UncheckedMutableFxMatrix.of()));
    assertNotEquals(new FXMatrix(), ImmutableFxMatrix.of(CheckedMutableFxMatrix.of()));
    assertNotEquals(new FXMatrix(), ImmutableFxMatrix.of(new FXMatrix()));
    final ImmutableFxMatrix matrix = UNCHECKED_MATRIX.asImmutable();
    assertEquals(matrix.getCurrencyList(), UNCHECKED_MATRIX.getCurrencyList());
    assertDeepEquals(matrix.getFxRates(), UNCHECKED_MATRIX.getFxRates(), EPS);
    assertEquals(CHECKED_MATRIX.getCurrencyList(), CHECKED_MATRIX.asImmutable().getCurrencyList());
    assertDeepEquals(CHECKED_MATRIX.getFxRates(), CHECKED_MATRIX.asImmutable().getFxRates(), EPS);
    final ImmutableFxMatrix other = UNCHECKED_MATRIX.asImmutable();
    assertEquals(matrix, matrix);
    assertNotEquals(null, matrix);
    assertEquals(matrix, other);
    assertEquals(matrix.hashCode(), other.hashCode());
    assertNotEquals(matrix, ImmutableFxMatrix.of(UncheckedMutableFxMatrix.of()));
    assertFalse(other == UNCHECKED_MATRIX.asImmutable());
    final FXMatrix oldMatrix = new FXMatrix(Currency.EUR, Currency.USD, 1.1);
    oldMatrix.addCurrency(Currency.GBP, Currency.USD, 0.9);
    final CheckedMutableFxMatrix checked = CheckedMutableFxMatrix.of();
    checked.addCurrency(Currency.EUR, Currency.USD, 1.1);
    checked.addCurrency(Currency.GBP, Currency.USD, 0.9);
    assertEquals(ImmutableFxMatrix.of(oldMatrix), checked.asImmutable());
  }

  /**
   * Checks that double[][] arrays are equal to within a tolerance.
   * @param actual  the actual matrix
   * @param expected  the expected matrix
   * @param eps  the tolerance
   */
  private static void assertDeepEquals(final double[][] actual, final double[][] expected, final double eps) {
    assertEquals(actual.length, expected.length);
    for (int i = 0; i < actual.length; i++) {
      assertEquals(actual[i].length, expected[i].length);
      for (int j = 0; j < actual[i].length; j++) {
        assertEquals(actual[i][j], expected[i][j], eps, "Different elements at " + i + ", " + j);
      }
    }
  }

  /**
   * Checks that double[][] arrays are not equal to within a tolerance.
   * @param actual  the actual matrix
   * @param expected  the expected matrix
   * @param eps  the tolerance
   */
  private static void assertMatrixNotEquals(final double[][] actual, final double[][] expected, final double eps) {
    assertEquals(actual.length, expected.length);
    boolean diff = false;
    for (int i = 0; i < actual.length; i++) {
      assertEquals(actual[i].length, expected[i].length);
      for (int j = 0; j < actual[i].length; j++) {
        if (!(Math.abs(expected[i][j] - actual[i][j]) <= eps)) {
          diff = true;
          break;
        }
      }
    }
    assertTrue(diff);
  }
}
