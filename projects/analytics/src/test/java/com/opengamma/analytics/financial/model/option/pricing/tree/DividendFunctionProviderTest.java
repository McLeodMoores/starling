/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test class for {@link DividendFunctionProvider} and its subclasses, {@link CashDividendFunctionProvider} and {@link ProportionalDividendFunctionProvider}
 */
@Test(groups = TestGroup.UNIT)
public class DividendFunctionProviderTest {
  private static final double[] TIMES = new double[] {1., 2., 3. };
  private static final double[] DIVIDENDS = new double[] {0.2, 0.1, 0.2 };

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTimesTest() {
    new CashDividendFunctionProvider(null, DIVIDENDS);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDividendsTest() {
    new CashDividendFunctionProvider(TIMES, null);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeTest() {
    final double[] times = new double[] {-1., 2., 3. };
    new CashDividendFunctionProvider(times, DIVIDENDS);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeDividendTest() {
    final double[] dividends = new double[] {0.2, -0.1, 0.2 };
    new CashDividendFunctionProvider(TIMES, dividends);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infiniteTimeTest() {
    final double[] times = new double[] {1., 2., Double.POSITIVE_INFINITY };
    new CashDividendFunctionProvider(times, DIVIDENDS);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infiniteDividendTest() {
    final double[] dividends = new double[] {0.2, 0.1, Double.POSITIVE_INFINITY };
    new CashDividendFunctionProvider(TIMES, dividends);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notInOrderTimeTest() {
    final double[] times = new double[] {1., 2., 1.5 };
    new CashDividendFunctionProvider(times, DIVIDENDS);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDataLengthTest() {
    final double[] times = new double[] {1., 2. };
    new CashDividendFunctionProvider(times, DIVIDENDS);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tooBigCashDividendTest() {
    final double[] dividends = new double[] {1., 2., 300. };
    final DividendFunctionProvider div = new CashDividendFunctionProvider(TIMES, dividends);
    div.spotModifier(100, 0.05);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dividendAfterExpiryPriceTest() {
    final double[] times = new double[] {1., 2., 45. };
    final DividendFunctionProvider div = new CashDividendFunctionProvider(times, DIVIDENDS);
    (new BinomialTreeOptionPricingModel()).getPrice(new TianLatticeSpecification(), new EuropeanVanillaOptionFunctionProvider(100, 10., 101, true), 100., 0.2, 0.05, div);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dividendAfterExpiryGreeksTest() {
    final double[] times = new double[] {1., 2., 45. };
    final DividendFunctionProvider div = new CashDividendFunctionProvider(times, DIVIDENDS);
    (new BinomialTreeOptionPricingModel()).getGreeks(new TianLatticeSpecification(), new EuropeanVanillaOptionFunctionProvider(100, 10., 101, true), 100., 0.2, 0.05, div);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsCashTest() {
    final double[] times = new double[] {1., 2., 4. };
    final double[] dividends = new double[] {0.2, 0.1, 0.3 };

    final DividendFunctionProvider ref = new CashDividendFunctionProvider(TIMES, DIVIDENDS);
    final DividendFunctionProvider[] function = new DividendFunctionProvider[] {ref, new CashDividendFunctionProvider(TIMES, DIVIDENDS),
        new CashDividendFunctionProvider(times, DIVIDENDS), new CashDividendFunctionProvider(TIMES, dividends),
        new ProportionalDividendFunctionProvider(TIMES, DIVIDENDS), new ProportionalDividendFunctionProvider(TIMES, dividends), null };
    final int len = function.length;
    for (int i = 0; i < len; ++i) {
      if (ref.equals(function[i])) {
        assertTrue(ref.hashCode() == function[i].hashCode());
      }
    }
    for (int i = 0; i < len - 1; ++i) {
      assertTrue(function[i].equals(ref) == ref.equals(function[i]));
    }
    assertFalse(ref.equals(new EuropeanSpreadOptionFunctionProvider(100., 1., 53, true)));
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsPropTest() {
    final double[] times = new double[] {1., 2., 4. };
    final double[] dividends = new double[] {0.2, 0.1, 0.3 };

    final DividendFunctionProvider ref = new ProportionalDividendFunctionProvider(TIMES, DIVIDENDS);
    final DividendFunctionProvider[] function = new DividendFunctionProvider[] {ref, new ProportionalDividendFunctionProvider(TIMES, DIVIDENDS),
        new ProportionalDividendFunctionProvider(times, DIVIDENDS), new ProportionalDividendFunctionProvider(TIMES, dividends),
        new CashDividendFunctionProvider(TIMES, DIVIDENDS), new CashDividendFunctionProvider(TIMES, dividends), null };
    final int len = function.length;
    for (int i = 0; i < len; ++i) {
      if (ref.equals(function[i])) {
        assertTrue(ref.hashCode() == function[i].hashCode());
      }
    }
    for (int i = 0; i < len - 1; ++i) {
      assertTrue(function[i].equals(ref) == ref.equals(function[i]));
    }
    assertFalse(ref.equals(new EuropeanSpreadOptionFunctionProvider(100., 1., 53, true)));
  }
}
