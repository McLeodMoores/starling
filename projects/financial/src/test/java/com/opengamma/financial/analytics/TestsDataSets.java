/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.test.TestGroup;
//CSOFF

/**
 * Sets of market data used in tests.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class TestsDataSets {

  /**
   * Linear interpolator. Used for SABR parameters interpolation.
   */
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Expiry is between 0 and 10 years, maturity between 0 and 10 years.
   * Beta is 0.5.  Alpha 0.05 at 1Y and 0.06 at 10Y. Rho 0.50 at 1Y and 0.30 at 10Y. Nu -0.25 at 1Y and 0.00 at 10Y.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2,
        5, 10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
            0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(LINEAR, LINEAR));
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
            0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25,
            -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, new GridInterpolator2D(LINEAR, LINEAR));
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50,
            0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(LINEAR, LINEAR));
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1() {
    return createSABR1(new SABRHaganVolatilityFunction());
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double shift) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift,
            0.05 + shift, 0.05 + shift, 0.05 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(
            LINEAR, LINEAR));
    //    final VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final double shift = 0.0001;
    return createSABR1AlphaBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped(final double shift) {
    return createSABR1AlphaBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped() {
    return createSABR1AlphaBumped(new SABRHaganVolatilityFunction());
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Rho data is bumped by the shift with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double shift) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(
            LINEAR, LINEAR));
    //    final VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift,
            -0.25 + shift, -0.25 + shift, -0.25 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(
            LINEAR, LINEAR));
    //    final VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Rho data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final double shift = 0.0001;
    return createSABR1RhoBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped(final double shift) {
    return createSABR1RhoBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped() {
    final double shift = 0.0001;
    return createSABR1RhoBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Nu data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double shift) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(
            LINEAR, LINEAR));
    //    final VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift,
            0.50 + shift, 0.50 + shift, 0.50 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Nu data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final double shift = 0.0001;
    return createSABR1NuBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Nu data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped(final double shift) {
    return createSABR1NuBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Nu data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped() {
    final double shift = 0.0001;
    return createSABR1NuBumped(new SABRHaganVolatilityFunction(), shift);
  }

}
