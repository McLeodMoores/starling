/**
 *
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
 * Unit tests for {@link UncheckedImmutableFxMatrix}.
 */
public class UncheckedImmutableFxMatrixTest {
  /** The tolerance */
  private static final double EPS = 2e-16;
  /** The FX data */
  private static final UncheckedMutableFxMatrix MATRIX = UncheckedMutableFxMatrix.of();
  static {
    MATRIX.addCurrency(Currency.EUR, Currency.USD, 1.2);
    MATRIX.addCurrency(Currency.CHF, Currency.USD, 0.5);
    MATRIX.addCurrency(Currency.GBP, Currency.USD, 1.3);
    MATRIX.addCurrency(Currency.CHF, Currency.EUR, 2.3);
    MATRIX.addCurrency(Currency.GBP, Currency.EUR, 23);
    MATRIX.addCurrency(Currency.GBP, Currency.CHF, 1.4);
  }

  /**
   * Tests that the FX matrix cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    UncheckedImmutableFxMatrix.of(null);
  }

  /**
   * Tests the deprecated getCurrencies() method.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testDeprecatedGetCurrencies() {
    MATRIX.asImmutable().getCurrencies();
  }

  /**
   * Tests the deprecated getRates() method.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testDeprecatedGetRates() {
    MATRIX.asImmutable().getRates();
  }

  /**
   * Tests that a rate cannot be added.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testAddRateFails() {
    MATRIX.asImmutable().addCurrency(Currency.BRL, Currency.CZK, 100);
  }

  /**
   * Tests that a rate cannot be updated.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUpdateRateFails() {
    MATRIX.asImmutable().updateRates(Currency.EUR, Currency.USD, 1.4);
  }

  /**
   * Tests that the currency list cannot be changed.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testImmutableCurrencyList() {
    final List<Currency> currencies = MATRIX.asImmutable().getCurrencyList();
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
    final UncheckedImmutableFxMatrix immutable = matrix.asImmutable();
    final List<Currency> originalCurrencies = matrix.getCurrencyList();
    final List<Currency> immutableCurrencies = immutable.getCurrencyList();
    final double[][] originalRates = matrix.getFxRates();
    final double[][] immutableRates = immutable.getFxRates();
    assertEquals(immutableCurrencies, originalCurrencies);
    assertMatrixEquals(immutableRates, originalRates, EPS);
    // change the values
    matrix.updateRates(Currency.EUR, Currency.USD, 1.25);
    assertMatrixNotEquals(immutableRates, originalRates, EPS);
    assertMatrixEquals(originalRates, matrix.getFxRates(), EPS);
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
    final UncheckedImmutableFxMatrix immutable = MATRIX.asImmutable();
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(new Currency[] {Currency.EUR, Currency.USD}, new double[] {100, 200});
    assertEquals(immutable.containsPair(Currency.EUR, Currency.CHF), MATRIX.containsPair(Currency.EUR, Currency.CHF));
    assertEquals(immutable.convert(mca, Currency.CHF), MATRIX.convert(mca, Currency.CHF));
    assertEquals(immutable.getNumberOfCurrencies(), MATRIX.getNumberOfCurrencies());
    for (final Currency currency : MATRIX.getCurrencyList()) {
      assertEquals(immutable.getFxRate(currency, Currency.USD), MATRIX.getFxRate(currency, Currency.USD), EPS);
    }
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertNotEquals(new FXMatrix(), UncheckedImmutableFxMatrix.of(UncheckedMutableFxMatrix.of()));
    final UncheckedImmutableFxMatrix matrix = MATRIX.asImmutable();
    assertEquals(matrix.getCurrencyList(), MATRIX.getCurrencyList());
    assertMatrixEquals(matrix.getFxRates(), MATRIX.getFxRates(), EPS);
    final UncheckedImmutableFxMatrix other = MATRIX.asImmutable();
    assertEquals(matrix, matrix);
    assertNotEquals(null, matrix);
    assertEquals(matrix, other);
    assertEquals(matrix.hashCode(), other.hashCode());
    assertNotEquals(matrix, UncheckedImmutableFxMatrix.of(UncheckedMutableFxMatrix.of()));
    assertFalse(other == MATRIX.asImmutable());
  }

  /**
   * Checks that double[][] arrays are equal to within a tolerance.
   * @param actual  the actual matrix
   * @param expected  the expected matrix
   * @param eps  the tolerance
   */
  private static void assertMatrixEquals(final double[][] actual, final double[][] expected, final double eps) {
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
