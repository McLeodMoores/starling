/**
 *
 */
package com.opengamma.analytics.financial.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Unit tests for {@link SimpleMutableFxMatrix}.
 */
public class SimpleMutableFxMatrixTest {
  /** The tolerance */
  private static final double EPS = 2e-16;

  /**
   * Tests that the getCurrencies() method of the superclass fails.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetCurrencies() {
    SimpleMutableFxMatrix.of().getCurrencies();
  }

  /**
   * Tests that the getRates() method of the superclass fails.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetRates() {
    SimpleMutableFxMatrix.of().getRates();
  }

  /**
   * Tests that existing data for an FX pair cannot be overwritten. In this case, the pairs that are being added
   * are in the same order i.e. EUR/USD and EUR/USD.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testAddExistingPair1() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.1);
  }

  /**
   * Tests that existing data for an FX pair cannot be overwritten. In this case, the pairs that are being added
   * are inverted i.e. EUR/USD and USD/EUR.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testAddExistingPair2() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.USD, Currency.EUR, 0.83);
  }

  /**
   * Tests that negative FX rates cannot be added to the matrix.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNegativeRate() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, -1.2);
  }

  /**
   * Tests that zero FX rates cannot be added to the matrix.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddZeroRate() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 0);
  }

  /**
   * Tests that the matrix cannot be updated with a negative rate.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpdateNegativeRate() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.updateRates(Currency.EUR, Currency.USD, -1.2);
  }

  /**
   * Tests that the matrix cannot be updated with a zero rate.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpdateZeroRate() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.EUR, Currency.USD, 0);
  }

  /**
   * Tests that only rates that are present in the matrix can be updated.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpdateNonExistentPair1() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.updateRates(Currency.JPY, Currency.USD, 100);
  }

  /**
   * Tests that only rates that are present in the matrix can be updated.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpdateNonExistentPair2() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.updateRates(Currency.USD, Currency.JPY, 0.01);
  }

  /**
   * Tests that there is a failure when the numerator currency is not in the matrix.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNumeratorNotInMatrix() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.getFxRate(Currency.JPY, Currency.USD);
  }

  /**
   * Tests that there is a failure when the denominator currency is not in the matrix.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDenominatorNotInMatrix() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.getFxRate(Currency.USD, Currency.JPY);
  }

  /**
   * Tests the containsPair() method.
   */
  @Test
  public void testContainsPairMethod() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 1.3);
    matrix.addCurrency(Currency.CHF, Currency.AUD, 2.3);
    assertTrue(matrix.containsPair(Currency.EUR, Currency.USD));
    assertTrue(matrix.containsPair(Currency.USD, Currency.EUR));
    // note that crosses aren't filled in, so this pair is not in the matrix
    assertFalse(matrix.containsPair(Currency.EUR, Currency.CHF));
    assertFalse(matrix.containsPair(Currency.EUR, Currency.NZD));
    assertFalse(matrix.containsPair(Currency.NZD, Currency.EUR));
    assertFalse(matrix.containsPair(Currency.NZD, Currency.BRL));
  }

  /**
   * Tests that rates for CCY/CCY are 1.
   */
  @Test
  public void testDiagonal() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 1.3);
    matrix.addCurrency(Currency.CHF, Currency.AUD, 2.3);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.EUR), 1, EPS);
    // note this also works for currencies not in the matrix
    assertEquals(matrix.getFxRate(Currency.NZD, Currency.NZD), 1, EPS);
  }

  /**
   * Tests that only rates that have been set can be retrieved from a sparsely-populated matrix.
   */
  @Test
  public void testRequestNonExistentRate() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 1.3);
    matrix.addCurrency(Currency.CHF, Currency.AUD, 2.3);
    try {
      matrix.getFxRate(Currency.GBP, Currency.USD);
      fail("Should not be able to get rate for GBP/USD");
    } catch (final IllegalStateException e) {
      // expected
    }
    try {
      matrix.getFxRate(Currency.AUD, Currency.USD);
      fail("Should not be able to get rate for AUD/USD");
    } catch (final IllegalStateException e) {
      // expected
    }
    try {
      matrix.getFxRate(Currency.CHF, Currency.EUR);
      fail("Should not be able to get rate for CHF/EUR");
    } catch (final IllegalStateException e) {
      // expected
    }
    try {
      matrix.getFxRate(Currency.AUD, Currency.EUR);
      fail("Should not be able to get rate for AUD/EUR");
    } catch (final IllegalStateException e) {
      // expected
    }
    try {
      matrix.getFxRate(Currency.EUR, Currency.CHF);
      fail("Should not be able to get rate for EUR/CHF");
    } catch (final IllegalStateException e) {
      // expected
    }
    try {
      matrix.getFxRate(Currency.GBP, Currency.CHF);
      fail("Should not be able to get rate for GBP/CHF");
    } catch (final IllegalStateException e) {
      // expected
    }
    try {
      matrix.getFxRate(Currency.USD, Currency.GBP);
      fail("Should not be able to get rate for USD/GBP");
    } catch (final IllegalStateException e) {
      // expected
    }
    try {
      matrix.getFxRate(Currency.CHF, Currency.GBP);
      fail("Should not be able to get rate for CHF/GBP");
    } catch (final IllegalStateException e) {
      // expected
    }
    try {
      matrix.getFxRate(Currency.AUD, Currency.GBP);
      fail("Should not be able to get rate for AUD/GBP");
    } catch (final IllegalStateException e) {
      // expected
    }
  }

  /**
   * Tests the addition of EUR/USD rate to an empty matrix. The matrix should be:
   *
   *      USD       EUR
   *  USD   -        #
   *  EUR   -        -
   */
  @Test
  public void testAdd1() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    final double[][] rates = matrix.getFxRates();
    final double[][] expected = new double[][] {
        new double[] {1.2},         // USD
        new double[0]               // EUR
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR));
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), 1.2, EPS);
    // inverse
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), 1 / 1.2, EPS);
    assertMatrixEquals(rates, expected, EPS);
  }

  /**
   * Tests the addition of an EUR/USD rate and CHF/GBP rate. The matrix should be:
   *
   *      USD     EUR     GBP     CHF
   *  USD   -      #       -1     -1
   *  EUR   -      -       -1     -1
   *  GBP   -      -       -      #
   *  CHF   -      -       -      -
   */
  @Test
  public void testAdd2() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.GBP, 1.7);
    final double[][] rates = matrix.getFxRates();
    final double[][] expected = new double[][] {
        new double[] {1.2, -1, -1},     // USD
        new double[] {-1, -1},          // EUR
        new double[] {1.7},             // GBP
        new double[0]                   // CHF
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.GBP, Currency.CHF));
    assertMatrixEquals(rates, expected, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.GBP), 1.7, EPS);
    // inverse
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), 1 / 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.GBP, Currency.CHF), 1 / 1.7, EPS);
  }

  /**
   * Tests the addition of two rates with a common denominator currency. The matrix should be:
   *
   *      USD     EUR     CHF
   *  USD   -      #       #
   *  EUR   -      -       -1
   *  CHF   -      -       -
   */
  @Test
  public void testAdd3() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    final double[][] rates = matrix.getFxRates();
    final double[][] expected = new double[][] {
        new double[] {1.2, 0.5},        // USD
        new double[] {-1},              // EUR
        new double[0]                   // CHF
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.CHF));
    assertMatrixEquals(rates, expected, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.USD), 0.5, EPS);
    // inverse
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), 1 / 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.CHF), 1 / 0.5, EPS);
  }

  /**
   * Tests the addition of two rates with a common currency, one in the denominator and the other in the numerator. The matrix should be:
   *
   *      USD     EUR     CHF
   *  USD   -      #       -1
   *  EUR   -      -       #
   *  CHF   -      -       -
   */
  @Test
  public void testAdd4() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.EUR, 1.5);
    final double[][] rates = matrix.getFxRates();
    final double[][] expected = new double[][] {
        new double[] {1.2, -1},         // USD
        new double[] {1.5},             // EUR
        new double[0]                   // CHF
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.CHF));
    assertMatrixEquals(rates, expected, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.EUR), 1.5, EPS);
    // inverse
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), 1 / 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.CHF), 1 / 1.5, EPS);
  }

  /**
   * Tests the addition of EUR/USD, CHF/USD and GBP/EUR rates to a matrix. The matrix should be:
   *
   *      USD     EUR     CHF     GBP
   *  USD   -      #       #      -1
   *  EUR   -      -       -1      #
   *  GBP   -      -       -      -1
   *  CHF   -      -       -      -
   */
  @Test
  public void testAdd5() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 0.7);
    final double[][] rates = matrix.getFxRates();
    final double[][] expected = new double[][] {
        new double[] {1.2, 0.5, -1},      // USD
        new double[] {-1, 0.7},           // EUR
        new double[] {-1},                // CHF
        new double[0]                     // GBP
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.CHF, Currency.GBP));
    assertMatrixEquals(rates, expected, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.USD), 0.5, EPS);
    assertEquals(matrix.getFxRate(Currency.GBP, Currency.EUR), 0.7, EPS);
    // inverse
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), 1 / 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.CHF), 1 / 0.5, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.GBP), 1 / 0.7, EPS);
  }

  /**
   * Tests the addition of EUR/USD, CHF/USD and EUR/GBP to a matrix. The matrix should be:
   *
   *      USD     EUR     CHF     GBP
   *  USD   -      #       #      -1
   *  EUR   -      -       -1      1 / #
   *  GBP   -      -       -      -1
   *  CHF   -      -       -      -
   *
   *  Note that the EUR/GBP rate is inverted, as a EUR rate is already present in the matrix when the EUR/GBP rate is added.
   */
  @Test
  public void testAdd6() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.EUR, Currency.GBP, 1.3);
    final double[][] rates = matrix.getFxRates();
    final double[][] expected = new double[][] {
        new double[] {1.2, 0.5, -1},        // USD
        new double[] {-1, 1 / 1.3},         // EUR
        new double[] {-1},                  // CHF
        new double[0]                       // GBP
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.CHF, Currency.GBP));
    assertMatrixEquals(rates, expected, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.USD), 0.5, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.GBP), 1.3, EPS);
    // inverse
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), 1 / 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.CHF), 1 / 0.5, EPS);
    assertEquals(matrix.getFxRate(Currency.GBP, Currency.EUR), 1 / 1.3, EPS);
  }

  /**
   * Tests the addition of EUR/USD, CHF/USD, GBP/EUR and CHF/AUD rates to a matrix. The matrix should be:
   *
   *      USD     EUR     CHF     GBP     AUD
   *  USD   -      #       #      -1      -1
   *  EUR   -      -      -1      #       -1
   *  CHF   -      -       -      -1      1 / #
   *  GBP   -      -       -      -       -1
   *  AUD   -      -       -      -       -
   */
  @Test
  public void testAdd7() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 1.3);
    matrix.addCurrency(Currency.CHF, Currency.AUD, 2.3);
    final double[][] rates = matrix.getFxRates();
    final double[][] expected = new double[][] {
        new double[] {1.2, 0.5, -1, -1},      // USD
        new double[] {-1, 1.3, -1},           // EUR
        new double[] {-1, 1 / 2.3},           // CHF
        new double[] {-1},                    // GBP
        new double[0]                         // AUD
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.CHF, Currency.GBP, Currency.AUD));
    assertMatrixEquals(rates, expected, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.USD), 0.5, EPS);
    assertEquals(matrix.getFxRate(Currency.GBP, Currency.EUR), 1.3, EPS);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.AUD), 2.3, EPS);
    // inverse
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), 1 / 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.CHF), 1 / 0.5, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.GBP), 1 / 1.3, EPS);
    assertEquals(matrix.getFxRate(Currency.AUD, Currency.CHF), 1 / 2.3, EPS);
  }

  /**
   * Tests the addition of EUR/USD, CHF/USD, NZD/JPY, GBP/EUR, CHF/AUD, JPY/USD, USD/NZD to a matrix. The matrix should be:
   *
   *      USD     EUR     CHF     JPY     NZD     GBP     AUD
   *  USD   -      #       #       #       1 / #   -1      -1
   *  EUR   -      -       -1      -1      -1      #       -1
   *  CHF   -      -       -       -1      -1      -1      1 / #
   *  JPY   -      -       -       -       #       -1      -1
   *  NZD   -      -       -       -       -       -1      -1
   *  GBP   -      -       -       -       -       -       -1
   *  AUD   -      -       -       -       -       -       -
   */
  @Test
  public void testAdd8() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.NZD, Currency.JPY, 0.015);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 1.3);
    matrix.addCurrency(Currency.CHF, Currency.AUD, 2.3);
    matrix.addCurrency(Currency.JPY, Currency.USD, 100);
    matrix.addCurrency(Currency.USD, Currency.NZD, 0.9);
    final double[][] rates = matrix.getFxRates();
    final double[][] expected = new double[][] {
      new double[] {1.2, 0.5, 100, 1 / 0.9, -1, -1},          // USD
      new double[] {-1, -1, -1, 1.3, -1},                     // EUR
      new double[] {-1, -1, -1, 1 / 2.3},                     // CHF
      new double[] {0.015, -1, -1},                           // JPY
      new double[] {-1, -1},                                  // NZD
      new double[] {-1},                                      // GBP
      new double[0]                                           // AUD
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.CHF, Currency.JPY, Currency.NZD, Currency.GBP, Currency.AUD));
    assertMatrixEquals(rates, expected, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.USD), 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.USD), 0.5, EPS);
    assertEquals(matrix.getFxRate(Currency.NZD, Currency.JPY), 0.015, EPS);
    assertEquals(matrix.getFxRate(Currency.GBP, Currency.EUR), 1.3, EPS);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.AUD), 2.3, EPS);
    assertEquals(matrix.getFxRate(Currency.JPY, Currency.USD), 100, EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.NZD), 0.9, EPS);
    // inverse
    assertEquals(matrix.getFxRate(Currency.USD, Currency.EUR), 1 / 1.2, EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.CHF), 1 / 0.5, EPS);
    assertEquals(matrix.getFxRate(Currency.JPY, Currency.NZD), 1 / 0.015, EPS);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.GBP), 1 / 1.3, EPS);
    assertEquals(matrix.getFxRate(Currency.AUD, Currency.CHF), 1 / 2.3, EPS);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.JPY), 1 / 100., EPS);
    assertEquals(matrix.getFxRate(Currency.NZD, Currency.USD), 1 / 0.9, EPS);
  }

  /**
   * Tests the creation of a full-populated matrix. No checks are performed on the data so it is possible to enter
   * inconsistent values e.g. (GBP/USD) / EUR/USD != GBP/EUR
   */
  @Test
  public void testFullMatrix() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.USD, 1.3);
    matrix.addCurrency(Currency.CHF, Currency.EUR, 2.3);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 23);
    matrix.addCurrency(Currency.GBP, Currency.CHF, 1.4);
    final double[][] rates = matrix.getFxRates();
    final double[][] expected = new double[][] {
      new double[] {1.2, 0.5, 1.3},
      new double[] {2.3, 23},
      new double[] {1.4},
      new double[0]
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.CHF, Currency.GBP));
    assertMatrixEquals(rates, expected, EPS);
  }

  /**
   * Tests that rates are updated correctly.
   */
  @Test
  public void testUpdateRate() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.USD, 1.3);
    matrix.addCurrency(Currency.CHF, Currency.EUR, 2.3);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 23);
    matrix.addCurrency(Currency.GBP, Currency.CHF, 1.4);
    matrix.updateRates(Currency.GBP, Currency.USD, 0.8);
    double[][] rates = matrix.getFxRates();
    double[][] expected = new double[][] {
      new double[] {1.2, 0.5, 0.8},
      new double[] {2.3, 23},
      new double[] {1.4},
      new double[0]
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.CHF, Currency.GBP));
    assertMatrixEquals(rates, expected, EPS);
    // update an inverse rate
    matrix.updateRates(Currency.USD, Currency.GBP, 0.8);
    rates = matrix.getFxRates();
    expected = new double[][] {
      new double[] {1.2, 0.5, 1 / 0.8},
      new double[] {2.3, 23},
      new double[] {1.4},
      new double[0]
    };
    assertEquals(matrix.getCurrencyList(), Arrays.asList(Currency.USD, Currency.EUR, Currency.CHF, Currency.GBP));
    assertMatrixEquals(rates, expected, EPS);
  }

  /**
   * Tests the conversion of the elements of a MultipleCurrencyAmount.
   */
  @Test
  public void testConvert() {
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.USD, 1.3);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(new Currency[] {Currency.EUR, Currency.GBP, Currency.CHF, Currency.EUR, Currency.USD}, new double[] {1000, 10000, 100000, 1000000, 10000000});
    final CurrencyAmount expected = CurrencyAmount.of(Currency.USD, 1000 / 1.2 + 10000 / 1.3 + 100000 / 0.5 + 1000000 / 1.2 + 10000000);
    assertEquals(expected, matrix.convert(mca, Currency.USD));
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertNotEquals(new FXMatrix(), SimpleMutableFxMatrix.of());
    final SimpleMutableFxMatrix matrix = SimpleMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    matrix.addCurrency(Currency.CHF, Currency.USD, 0.5);
    matrix.addCurrency(Currency.GBP, Currency.USD, 1.3);
    matrix.addCurrency(Currency.CHF, Currency.EUR, 2.3);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 23);
    matrix.addCurrency(Currency.GBP, Currency.CHF, 1.4);
    assertEquals(matrix.getNumberOfCurrencies(), 4);
    SimpleMutableFxMatrix other = SimpleMutableFxMatrix.of();
    other.addCurrency(Currency.EUR, Currency.USD, 1.2);
    other.addCurrency(Currency.CHF, Currency.USD, 0.5);
    other.addCurrency(Currency.GBP, Currency.USD, 1.3);
    other.addCurrency(Currency.CHF, Currency.EUR, 2.3);
    other.addCurrency(Currency.GBP, Currency.EUR, 23);
    other.addCurrency(Currency.GBP, Currency.CHF, 1.4);
    assertEquals(matrix, matrix);
    assertNotEquals(null, matrix);
    assertEquals(matrix, other);
    assertEquals(matrix.hashCode(), other.hashCode());
    // different currencies
    other = SimpleMutableFxMatrix.of();
    other.addCurrency(Currency.EUR, Currency.USD, 1.2);
    other.addCurrency(Currency.CHF, Currency.USD, 0.5);
    other.addCurrency(Currency.GBP, Currency.USD, 1.3);
    other.addCurrency(Currency.NZD, Currency.EUR, 2.3);
    other.addCurrency(Currency.GBP, Currency.EUR, 23);
    other.addCurrency(Currency.GBP, Currency.NZD, 1.4);
    assertNotEquals(matrix, other);
    // different values
    other = SimpleMutableFxMatrix.of();
    other.addCurrency(Currency.EUR, Currency.USD, 1.2);
    other.addCurrency(Currency.CHF, Currency.USD, 0.5);
    other.addCurrency(Currency.GBP, Currency.USD, 1.3);
    other.addCurrency(Currency.CHF, Currency.EUR, 2.3);
    other.addCurrency(Currency.GBP, Currency.EUR, 23);
    other.addCurrency(Currency.GBP, Currency.CHF, 1.5);
    assertNotEquals(matrix, other);
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
}
