/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve;

import org.testng.annotations.Test;

/**
 * Base class for curve building tests.
 */
public abstract class CurveBuildingTests {

  /**
   * Tests that the inverse Jacobians are the expected size.
   */
  @Test
  public abstract void testJacobianSize();

  /**
   * Tests that all instruments used to construct the curve(s) price to zero.
   */
  @Test
  public abstract void testInstrumentsInCurvePriceToZero();

  /**
   * Compares the sensitivities to input market data compared to the entries in the inverse Jacobian,
   * which are (dYield)/(dQuote), where the yield is the value read from the curve and the quote is
   * the market data quote used to construct the curve.
   */
  @Test
  public abstract void testFiniteDifferenceSensitivities();

  /**
   * If relevant, tests that curves that are constructed in different ways but that contain the same market
   * data, interpolators, etc. are equal.
   */
  @Test
  public abstract void testSameCurvesDifferentMethods();

}
