/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.forex.provider;

import static com.opengamma.util.money.Currency.EUR;
import static com.opengamma.util.money.Currency.GBP;
import static com.opengamma.util.money.Currency.USD;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.util.money.Currency;

/**
 * Sets of market data used in Forex tests.
 */
public class MulticurveProviderDiscountForexDataSets {
  private static final Currency KRW = Currency.of("KRW");
  private static final String DISCOUNTING_EUR = "Discounting EUR";
  private static final String DISCOUNTING_USD = "Discounting USD";
  private static final String DISCOUNTING_GBP = "Discounting GBP";
  private static final String DISCOUNTING_KRW = "Discounting KRW";
  private static final double EUR_USD = 1.40;
  private static final double USD_KRW = 1111.11;
  private static final double GBP_USD = 1.50;
//  private static final ImmutableFxMatrix FX_MATRIX;

//  static {
//    final CheckedMutableFxMatrix matrix = CheckedMutableFxMatrix.of();
//    matrix.addCurrency(EUR, USD, 1 / EUR_USD);
//    matrix.addCurrency(KRW, USD, 1 / USD_KRW);
//    matrix.addCurrency(GBP, USD, GBP_USD);
//    FX_MATRIX = ImmutableFxMatrix.of(matrix);
//  }

  private static final FXMatrix FX_MATRIX;

  static {
    FX_MATRIX = new FXMatrix(EUR, USD, EUR_USD);
    FX_MATRIX.addCurrency(KRW, USD, 1.0 / USD_KRW);
    FX_MATRIX.addCurrency(GBP, USD, GBP_USD);
  }

  private static final Interpolator1D LINEAR_FLAT = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  private static final double[] USD_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0 };
  private static final double[] USD_DSC_RATE = new double[] {0.0100, 0.0120, 0.0120, 0.0140, 0.0140 };
  private static final String USD_DSC_NAME = "USD Dsc";
  private static final YieldAndDiscountCurve USD_DSC = new YieldCurve(USD_DSC_NAME, new InterpolatedDoublesCurve(USD_DSC_TIME, USD_DSC_RATE, LINEAR_FLAT, true, USD_DSC_NAME));

  private static final double[] EUR_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0 };
  private static final double[] EUR_DSC_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150 };
  private static final String EUR_DSC_NAME = "EUR Dsc";
  private static final YieldAndDiscountCurve EUR_DSC = new YieldCurve(EUR_DSC_NAME, new InterpolatedDoublesCurve(EUR_DSC_TIME, EUR_DSC_RATE, LINEAR_FLAT, true, EUR_DSC_NAME));

  private static final double[] GBP_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0 };
  private static final double[] GBP_DSC_RATE = new double[] {0.0160, 0.0135, 0.0160, 0.0185, 0.0160 };
  private static final String GBP_DSC_NAME = "GBP Dsc";
  private static final YieldAndDiscountCurve GBP_DSC = new YieldCurve(GBP_DSC_NAME, new InterpolatedDoublesCurve(GBP_DSC_TIME, GBP_DSC_RATE, LINEAR_FLAT, true, GBP_DSC_NAME));

  private static final double[] KRW_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0 };
  private static final double[] KRW_DSC_RATE = new double[] {0.0350, 0.0325, 0.0350, 0.0375, 0.0350 };
  private static final String KRW_DSC_NAME = "KRW Dsc";
  private static final YieldAndDiscountCurve KRW_DSC = new YieldCurve(KRW_DSC_NAME, new InterpolatedDoublesCurve(KRW_DSC_TIME, KRW_DSC_RATE, LINEAR_FLAT, true, KRW_DSC_NAME));

  /**
   * Create a curve data bundle with discounting curves for USD, EUR, GBP and KRW.
   * @return  the curve data bundle
   */
  public static MulticurveProviderDiscount createMulticurvesForex() {
    final MulticurveProviderDiscount multicurves = new MulticurveProviderDiscount(FX_MATRIX);
    multicurves.setCurve(EUR, EUR_DSC);
    multicurves.setCurve(USD, USD_DSC);
    multicurves.setCurve(GBP, GBP_DSC);
    multicurves.setCurve(KRW, KRW_DSC);
    return multicurves;
  }

  /**
   * Create a curve data bundle with EUR and USD discounting curves.
   * @return  the curve data bundle
   */
  public static MulticurveProviderDiscount createMulticurvesEURUSD() {
    final MulticurveProviderDiscount multicurves = new MulticurveProviderDiscount(FX_MATRIX);
    multicurves.setCurve(EUR, EUR_DSC);
    multicurves.setCurve(USD, USD_DSC);
    return multicurves;
  }

  /**
   * Gets the discounting curve names for USD, EUR, GBP and KRW.
   * @return  the curve names
   */
  public static String[] curveNames() {
    return new String[] {DISCOUNTING_EUR, DISCOUNTING_USD, DISCOUNTING_GBP, DISCOUNTING_KRW };
  }

  /**
   * Gets an immutable FX matrix containing cross-rate values for USD, EUR, GBP and KRW.
   * @return  the FX matrix
   */
  public static FXMatrix fxMatrix() {
    return FX_MATRIX;
  }

}
