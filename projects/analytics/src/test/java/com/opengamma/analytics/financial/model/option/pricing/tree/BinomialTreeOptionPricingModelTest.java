/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests below hit argumentCheckers in {@link BinomialTreeOptionPricingModel}. 
 * Successful tests are given in test classes of respective subclasses of {@link OptionFunctionProvider1D} or {@link OptionFunctionProvider2D} 
 */
@Test(groups = TestGroup.UNIT)
public class BinomialTreeOptionPricingModelTest {

  private static final BinomialTreeOptionPricingModel MODEL = new BinomialTreeOptionPricingModel();
  private static final LatticeSpecification LATTICE = new CoxRossRubinsteinLatticeSpecification();
  private static final int STEPS = 85;
  private static final OptionFunctionProvider1D F_1D = new EuropeanVanillaOptionFunctionProvider(105.1, 4.2, STEPS, false);
  private static final OptionFunctionProvider2D F_2D = new EuropeanSpreadOptionFunctionProvider(105.1, 4.2, STEPS, false);
  private static final double SPOT = 105.;
  private static final double INTEREST = 0.05;
  private static final double VOL = 0.3;
  private static final double DIVIDEND = 0.02;

  private static final double[] INTERESTS = new double[STEPS];
  private static final double[] VOLS = new double[STEPS];
  private static final double[] DIVIDENDS = new double[STEPS];
  static {
    for (int i = 0; i < STEPS; ++i) {
      INTERESTS[i] = INTEREST + 0.01 * Math.sin(i);
      VOLS[i] = VOL + 0.05 * Math.sin(i);
      DIVIDENDS[i] = DIVIDEND;
    }
  }

  private static final double[] CASH_DIVIDENDS = new double[] {.1, .3, .2 };
  private static final double[] DIVIDEND_TIMES = new double[] {4.2 / 6., 4.2 / 3., 4.2 / 2. };
  private static final DividendFunctionProvider CASH_DIVIDEND = new CashDividendFunctionProvider(DIVIDEND_TIMES, CASH_DIVIDENDS);

  /*
   * Tests for getPrice with constant parameters
   */
  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotGetPriceTest() {
    MODEL.getPrice(LATTICE, F_1D, -SPOT, VOL, INTEREST, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolGetPriceTest() {
    MODEL.getPrice(LATTICE, F_1D, SPOT, -VOL, INTEREST, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeProbabilityGetPriceTest() {
    MODEL.getPrice(LATTICE, F_1D, SPOT, 0.01, -10. * INTEREST, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeProbabilityGetPriceTest() {
    MODEL.getPrice(LATTICE, F_1D, SPOT, 0.001, INTEREST, DIVIDEND);
  }

  /*
   * Tests for getPrice with time-varying parameters
   */
  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotGetPriceVaryingTest() {
    MODEL.getPrice(F_1D, -SPOT, VOLS, INTERESTS, DIVIDENDS);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolGetPriceVaryingTest() {
    VOLS[2] = -0.6;
    MODEL.getPrice(F_1D, SPOT, VOLS, INTERESTS, DIVIDENDS);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeProbabilityGetPriceVaryingTest() {
    final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(105.1, 4.2e18, STEPS, false);
    VOLS[2] = 1.e-9;
    MODEL.getPrice(function, SPOT, VOLS, INTERESTS, DIVIDENDS);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongRateLengthVaryingTest() {
    MODEL.getPrice(F_1D, SPOT, VOLS, new double[] {INTEREST }, DIVIDENDS);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDividendLengthVaryingTest() {
    MODEL.getPrice(F_1D, SPOT, VOLS, INTERESTS, new double[] {DIVIDEND });
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongVolsLengthVaryingTest() {
    MODEL.getPrice(F_1D, SPOT, new double[] {VOL }, INTERESTS, DIVIDENDS);
  }

  /*
   * Tests for getPrice with dividend provider
   */
  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotGetPriceDivTest() {
    MODEL.getPrice(LATTICE, F_1D, -SPOT, VOL, INTEREST, CASH_DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolGetPriceDivTest() {
    MODEL.getPrice(LATTICE, F_1D, SPOT, -VOL, INTEREST, CASH_DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeProbabilityGetPriceDivTest() {
    MODEL.getPrice(LATTICE, F_1D, SPOT, 0.01, -10. * INTEREST, CASH_DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeProbabilityGetPriceDivTest() {
    MODEL.getPrice(LATTICE, F_1D, SPOT, 0.001, INTEREST, CASH_DIVIDEND);
  }

  /*
   * Tests for getPrice on two assets
   */
  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpot1GetPriceTwoAssetTest() {
    MODEL.getPrice(F_2D, -SPOT, SPOT, VOL, VOL, 0.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpot2GetPriceTwoAssetTest() {
    MODEL.getPrice(F_2D, SPOT, -SPOT, VOL, VOL, 0.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVol1GetPriceTwoAssetTest() {
    MODEL.getPrice(F_2D, SPOT, SPOT, -VOL, VOL, 0.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVol2GetPriceTwoAssetTest() {
    MODEL.getPrice(F_2D, SPOT, SPOT, VOL, -VOL, 0.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeUUProbability2GetPriceTwoAssetTest() {
    MODEL.getPrice(F_2D, SPOT, SPOT, VOL, VOL, 0.5, 100. * INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeUDProbabilityGetPriceTwoAssetTest() {
    MODEL.getPrice(F_2D, SPOT, SPOT, VOL, VOL, 0.5, INTEREST, 100. * DIVIDEND, -100. * DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeUDProbability2GetPriceTwoAssetTest() {
    MODEL.getPrice(F_2D, SPOT, SPOT, VOL, VOL, 0.5, INTEREST, -100. * DIVIDEND, 200. * DIVIDEND);
  }

  /**
   * Note that the remaining 2 branches are unlikely to be hit
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeDUProbabilityGetPriceTwoAssetTest() {
    final double vol = 10000.;
    final double vol2 = 10000.;
    final OptionFunctionProvider2D function2D = new EuropeanSpreadOptionFunctionProvider(105.1, 10., 10, false);
    MODEL.getPrice(function2D, SPOT, SPOT, vol - 1.1, vol2 - 1, 0., -vol - 0.1, -0.5 * vol * vol, -vol2 - 0.5 * vol2 * vol2);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeDDProbabilityGetPriceTwoAssetTest() {
    final double vol = 10000000.;
    final double vol2 = 10000000.;
    final OptionFunctionProvider2D function2D = new EuropeanSpreadOptionFunctionProvider(105.1, 10., 10, false);
    MODEL.getPrice(function2D, SPOT, SPOT, vol - 1.01, vol2 - 1., 0., -vol + 2.1, -0.5 * vol * vol, -vol2 - 0.5 * vol2 * vol2);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void smallCorrelationGetPriceTwoAssetTest() {
    MODEL.getPrice(F_2D, SPOT, SPOT, VOL, VOL, -21.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeCorrelationGetPriceTwoAssetTest() {
    MODEL.getPrice(F_2D, SPOT, SPOT, VOL, VOL, 11.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /*
   * Tests for getGreeks with constant parameters
   */
  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotGetGreeksTest() {
    MODEL.getGreeks(LATTICE, F_1D, -SPOT, VOL, INTEREST, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolGetGreeksTest() {
    MODEL.getGreeks(LATTICE, F_1D, SPOT, -VOL, INTEREST, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeProbabilityGetGreeksTest() {
    MODEL.getGreeks(LATTICE, F_1D, SPOT, 0.01, -10. * INTEREST, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeProbabilityGetGreeksTest() {
    MODEL.getGreeks(LATTICE, F_1D, SPOT, 0.001, INTEREST, DIVIDEND);
  }

  /*
   * Tests for getGreeks with dividend provider
   */
  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotGetGreeksDivTest() {
    MODEL.getGreeks(LATTICE, F_1D, -SPOT, VOL, INTEREST, CASH_DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolGetGreeksDivTest() {
    MODEL.getGreeks(LATTICE, F_1D, SPOT, -VOL, INTEREST, CASH_DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeProbabilityGetGreeksDivTest() {
    MODEL.getGreeks(LATTICE, F_1D, SPOT, 0.01, -10. * INTEREST, CASH_DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeProbabilityGetGreeksDivTest() {
    MODEL.getGreeks(LATTICE, F_1D, SPOT, 0.001, INTEREST, CASH_DIVIDEND);
  }

  /*
   * Tests for getGreeks with time-varying parameters
   */
  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotGetGreeksVaryingTest() {
    MODEL.getGreeks(F_1D, -SPOT, VOLS, INTERESTS, DIVIDENDS);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolGetGreeksVaryingTest() {
    VOLS[2] = -VOLS[2];
    MODEL.getGreeks(F_1D, SPOT, VOLS, INTERESTS, DIVIDENDS);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeProbabilityGetGreeksVaryingTest() {
    final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(105.1, 4.2e18, STEPS, false);
    VOLS[2] = 1.e-9;
    MODEL.getGreeks(function, SPOT, VOLS, INTERESTS, DIVIDENDS);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongRateLengthGreeksVaryingTest() {
    MODEL.getGreeks(F_1D, SPOT, VOLS, new double[] {INTEREST }, DIVIDENDS);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDividendLengthGreeksVaryingTest() {
    MODEL.getGreeks(F_1D, SPOT, VOLS, INTERESTS, new double[] {DIVIDEND });
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongVolsLengthGreeksVaryingTest() {
    MODEL.getGreeks(F_1D, SPOT, new double[] {VOL }, INTERESTS, DIVIDENDS);
  }

  /*
   * Tests for getGreeks on two assets
   */
  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpot1GetGreeksTwoAssetTest() {
    MODEL.getGreeks(F_2D, -SPOT, SPOT, VOL, VOL, 0.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpot2GetGreeksTwoAssetTest() {
    MODEL.getGreeks(F_2D, SPOT, -SPOT, VOL, VOL, 0.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVol1GetGreeksTwoAssetTest() {
    MODEL.getGreeks(F_2D, SPOT, SPOT, -VOL, VOL, 0.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVol2GetGreeksTwoAssetTest() {
    MODEL.getGreeks(F_2D, SPOT, SPOT, VOL, -VOL, 0.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeUUProbability2GetGreeksTwoAssetTest() {
    MODEL.getGreeks(F_2D, SPOT, SPOT, VOL, VOL, 0.5, 100. * INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeUDProbabilityGetGreeksTwoAssetTest() {
    MODEL.getGreeks(F_2D, SPOT, SPOT, VOL, VOL, 0.5, INTEREST, 100. * DIVIDEND, -100. * DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeUDProbability2GetGreeksTwoAssetTest() {
    MODEL.getGreeks(F_2D, SPOT, SPOT, VOL, VOL, 0.5, INTEREST, -100. * DIVIDEND, 200. * DIVIDEND);
  }

  /**
   * Note that the remaining 2 branches are unlikely to be hit
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeDUProbabilityGetGreeksTwoAssetTest() {
    final double vol = 10000.;
    final double vol2 = 10000.;
    final OptionFunctionProvider2D function2D = new EuropeanSpreadOptionFunctionProvider(105.1, 10., 10, false);
    MODEL.getGreeks(function2D, SPOT, SPOT, vol - 1.1, vol2 - 1, 0., -vol - 0.1, -0.5 * vol * vol, -vol2 - 0.5 * vol2 * vol2);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeDDProbabilityGetGreeksTwoAssetTest() {
    final double vol = 10000000.;
    final double vol2 = 10000000.;
    final OptionFunctionProvider2D function2D = new EuropeanSpreadOptionFunctionProvider(105.1, 10., 10, false);
    MODEL.getGreeks(function2D, SPOT, SPOT, vol - 1.01, vol2 - 1., 0., -vol + 2.1, -0.5 * vol * vol, -vol2 - 0.5 * vol2 * vol2);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void smallCorrelationGetGreeksTwoAssetTest() {
    MODEL.getGreeks(F_2D, SPOT, SPOT, VOL, VOL, -21.5, INTEREST, DIVIDEND, DIVIDEND);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeCorrelationGetGreeksTwoAssetTest() {
    MODEL.getGreeks(F_2D, SPOT, SPOT, VOL, VOL, 11.5, INTEREST, DIVIDEND, DIVIDEND);
  }

}
