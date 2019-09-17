/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.pricing.RealizedVariance;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RealizedVarianceTest {

  // -------------------------------- SETUP ------------------------------------------

  private static final RealizedVariance REALIZED_VARIANCE = new RealizedVariance();

  // The derivative
  private static final double VAR_STRIKE = 0.05;
  private static final double VAR_NOTIONAL = 3150;
  private static final double EXPIRY = 5;
  private static final int N_OBS_EXPECTED = 750;
  private static final int NO_MKT_DISRUPTIONS = 0;
  private static final double ANNUALIZATION_FACTOR = 252;

  private static final double[] NO_OBS = {};
  private static final double[] DEFAULT_WEIGHTS = {};
  private static final VarianceSwap SWAP_NULL =
      new VarianceSwap(0, EXPIRY, EXPIRY, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION_FACTOR, N_OBS_EXPECTED,
          NO_MKT_DISRUPTIONS, NO_OBS, DEFAULT_WEIGHTS);
  private static final double[] ONE_OBS = {100.0 };
  private static final VarianceSwap SWAP_ONE_OBS =
      new VarianceSwap(0, EXPIRY, EXPIRY, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION_FACTOR, N_OBS_EXPECTED,
          NO_MKT_DISRUPTIONS, ONE_OBS, DEFAULT_WEIGHTS);

  private static final double[] TWO_OBS = {100.0, 150.0 };
  private static final VarianceSwap SWAP_TWO_OBS = new VarianceSwap(0, EXPIRY, EXPIRY, VAR_STRIKE, VAR_NOTIONAL,
      Currency.EUR, ANNUALIZATION_FACTOR, 1, NO_MKT_DISRUPTIONS, TWO_OBS, DEFAULT_WEIGHTS);

  private static final double[] THREE_OBS = {100.0, 150.0, 100.0 };
  private static final VarianceSwap SWAP_THREE_OBS = new VarianceSwap(0, EXPIRY, EXPIRY, VAR_STRIKE, VAR_NOTIONAL,
      Currency.EUR, ANNUALIZATION_FACTOR, 2, NO_MKT_DISRUPTIONS, THREE_OBS, DEFAULT_WEIGHTS);

  private static final double[] OBS_WITH_ZERO = {100.0, 150.0, 0.0 };
  private static final VarianceSwap SWAP_WITH_ZERO_OBS = new VarianceSwap(0, EXPIRY, EXPIRY, VAR_STRIKE, VAR_NOTIONAL,
      Currency.EUR, ANNUALIZATION_FACTOR, 2, NO_MKT_DISRUPTIONS, OBS_WITH_ZERO, DEFAULT_WEIGHTS);

  // -------------------------------- TESTS ------------------------------------------

  @Test
  public void testNullObs() {
    assertEquals(REALIZED_VARIANCE.apply(SWAP_NULL), 0.0, 1e-9);
  }

  @Test
  public void testOneObs() {
    assertEquals(REALIZED_VARIANCE.apply(SWAP_ONE_OBS), 0.0, 1e-9);
  }

  @Test
  public void testTwoObs() {
    assertEquals(REALIZED_VARIANCE.apply(SWAP_TWO_OBS), ANNUALIZATION_FACTOR * FunctionUtils.square(Math.log(1.5)), 1e-9);
  }

  @Test
  public void testThreeObs() {
    assertEquals(REALIZED_VARIANCE.apply(SWAP_THREE_OBS), ANNUALIZATION_FACTOR * FunctionUtils.square(Math.log(1.5)), 1e-9);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroInTimeSeries() {
    REALIZED_VARIANCE.apply(SWAP_WITH_ZERO_OBS);
  }
}
