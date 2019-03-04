/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.RealizedVariance;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.factory.DoubleQuadraticInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1d;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

import cern.jet.random.engine.MersenneTwister64;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VarianceSwapPresentValueTest {

  // Setup ------------------------------------------

  // Market data
  private static final double SPOT = 80;
  private static final double DRIFT = 0.05;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
  // private static final double FORWARD = 100;

  // The pricing method
  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication();

  private static final YieldAndDiscountCurve DISCOUNT = new YieldCurve("Discount", ConstantDoublesCurve.from(0.05));

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0 };
  private static final double[] STRIKES = new double[] {40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120 };
  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28, 0.25, 0.25, 0.25, 0.25, 0.26, 0.24, 0.23, 0.25, 0.20, 0.20, 0.20, 0.20 };

  private static final NamedInterpolator1d INTERPOLATOR_1D_STRIKE = NamedInterpolator1dFactory.of(DoubleQuadraticInterpolator1dAdapter.NAME,
      LinearExtrapolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);

  final static NamedInterpolator1d INTERPOLATOR_1D_EXPIRY = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME);

  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, INTERPOLATOR_1D_STRIKE));
  private static final BlackVolatilitySurfaceStrike VOL_SURFACE = new BlackVolatilitySurfaceStrike(SURFACE);
  private static final StaticReplicationDataBundle MARKET = new StaticReplicationDataBundle(VOL_SURFACE, DISCOUNT, FORWARD_CURVE);

  // The derivative
  private static final double VAR_STRIKE = 0.05;
  private static final double VAR_NOTIONAL = 10000; // A notional of 10000 means PV is in bp
  private static final double NOW = 0;
  private static final double EXPIRY_1 = 1;
  //private static final double expiry2 = 2;
  private static final double EXPIRY_5 = 5;
  //private static final double expiry10 = 10;
  private static final int N_OBS_EXPECTED = 750;
  private static final int NO_OBS_DISRUPTED = 0;
  private static final double ANNUALIZATION = 252;

  private static final double[] NO_OBSERVATIONS = {};
  private static final double[] NO_OBS_WEIGHTS = {};

  private static final double[] SINGLE_OBS_NO_RETURN = {80 };
  private static final VarianceSwap SWAP_STARTS_NOW =
      new VarianceSwap(NOW, EXPIRY_5, EXPIRY_5, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS_EXPECTED, NO_OBS_DISRUPTED, SINGLE_OBS_NO_RETURN, NO_OBS_WEIGHTS);

  private static final ZonedDateTime TODAY = ZonedDateTime.now();
  private static final ZonedDateTime TOMORROW = TODAY.plusDays(1);
  private static final double T_PLUS_ONE = TimeCalculator.getTimeBetween(TODAY, TOMORROW);

  // Tests ------------------------------------------

  private static double TOLERATED = 1.0E-9;

  @Test
  /**
   * Compare presentValue with impliedVariance, ensuring that spot starting varianceSwaps equal that coming only from implied part <p>
   * Ensure we handle the one underlying observation correctly. i.e. no *returns* yet
   *
   */
  public void onFirstObsDateWithOneObs() {

    final double pv = PRICER.presentValue(SWAP_STARTS_NOW, MARKET);
    final double variance = PRICER.expectedVariance(SWAP_STARTS_NOW, MARKET);
    final double pvOfHedge = SWAP_STARTS_NOW.getVarNotional() * (variance - SWAP_STARTS_NOW.getVarStrike()) * MARKET.getDiscountCurve().getDiscountFactor(EXPIRY_5);
    assertEquals(pv, pvOfHedge, TOLERATED);
  }

  @Test
  /**
   * Variance is additive, hence a forward starting VarianceSwap may be decomposed into the difference of two spot starting ones.
   */
  public void swapForwardStarting() {

    // First, create a swap which starts in 1 year and observes for a further four
    final VarianceSwap swapForwardStarting1to5 = new VarianceSwap(EXPIRY_1, EXPIRY_5, EXPIRY_5, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS_EXPECTED, NO_OBS_DISRUPTED, SINGLE_OBS_NO_RETURN,
        NO_OBS_WEIGHTS);

    final double pvFowardStart = PRICER.presentValue(swapForwardStarting1to5, MARKET);

    // Second, create two spot starting swaps. One that expires at the end of observations, one expiring at the beginnning
    final VarianceSwap swapSpotStarting1 = new VarianceSwap(NOW, EXPIRY_1, EXPIRY_5, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS_EXPECTED, NO_OBS_DISRUPTED, SINGLE_OBS_NO_RETURN, NO_OBS_WEIGHTS);
    final VarianceSwap swapSpotStarting5 = new VarianceSwap(NOW, EXPIRY_5, EXPIRY_5, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS_EXPECTED, NO_OBS_DISRUPTED, SINGLE_OBS_NO_RETURN, NO_OBS_WEIGHTS);

    final double pvSpot1 = PRICER.presentValue(swapSpotStarting1, MARKET);
    final double pvSpot5 = PRICER.presentValue(swapSpotStarting5, MARKET);

    final double pvDiffOfTwoSpotStarts = (5.0 * pvSpot5 - 1.0 * pvSpot1) / 4.0;

    assertEquals(pvFowardStart, pvDiffOfTwoSpotStarts, TOLERATED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void onFirstObsWithoutObs() {

    final VarianceSwap swapOnFirstObsWithoutObs = new VarianceSwap(NOW, EXPIRY_5, EXPIRY_5, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS_EXPECTED, NO_OBS_DISRUPTED, NO_OBSERVATIONS,
        NO_OBS_WEIGHTS);
    @SuppressWarnings("unused")
    final double pv = PRICER.presentValue(swapOnFirstObsWithoutObs, MARKET);
  }

  final static double VOL_ANNUAL = 0.28;
  final static double VOL_DAILY = VOL_ANNUAL / Math.sqrt(ANNUALIZATION);
  final static double STD_DEV_DAILY = Math.sqrt(0.5) * VOL_DAILY;
  //final static ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, stdDevDaily);
  final static ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, STD_DEV_DAILY, new MersenneTwister64(99));

  final static int N_OBS = 252 * 5;
  final static double[] OBS_WEIGHT = {1.0 };
  static double AVG_RETURN = 0;
  static double AVG_SQUARE_RETURN = 0;
  static double[] OBS = new double[N_OBS];

  static {
    for (int i = 0; i < N_OBS; i++) {
      OBS[i] = Math.exp(NORMAL.nextRandom());
      if (i > 0) {
        AVG_RETURN += Math.log(OBS[i] / OBS[i - 1]);
        AVG_SQUARE_RETURN += Math.pow(Math.log(OBS[i] / OBS[i - 1]), 2);
      }
    }
    AVG_RETURN /= N_OBS - 1;
    AVG_SQUARE_RETURN /= N_OBS - 1;
  }

  @Test
  /**
   * Simply test the machinery: the average of squared log returns of a lognormal distribution
   * will return the standard deviation used to generate the random observations
   */
  public void testAvgSquareReturn() {
    final double sampleDailyVariance = AVG_SQUARE_RETURN - FunctionUtils.square(AVG_RETURN);
    final double sampleAnnualVariance = sampleDailyVariance * ANNUALIZATION;
    final double annualVolatilityEstimate = Math.sqrt(sampleAnnualVariance);
    assertEquals(VOL_ANNUAL, annualVolatilityEstimate, 0.01);
    assertEquals(0.27710025417636447, annualVolatilityEstimate, TOLERATED);
  }

  @Test
  /**
   * After lastObs but before settlement date, presentValue == RealizedVar
   */
  public void swapObservationsCompleted() {
    final VarianceSwap swapPaysTomorrow = new VarianceSwap(-1., -T_PLUS_ONE, T_PLUS_ONE, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS - 1, 0, OBS, OBS_WEIGHT);

    final double pv = PRICER.presentValue(swapPaysTomorrow, MARKET);
    final double variance = new RealizedVariance().evaluate(swapPaysTomorrow);
    final double pvOfHedge = SWAP_STARTS_NOW.getVarNotional() * (variance - SWAP_STARTS_NOW.getVarStrike()) * MARKET.getDiscountCurve().getDiscountFactor(T_PLUS_ONE);
    assertEquals(pvOfHedge, pv, 0.01);
  }

  @Test
  /**
   * After settlement, presentValue == 0.0
   */
  public void swapAfterSettlement() {
    final VarianceSwap swapEnded = new VarianceSwap(-1.0, -1.0 / 365, -1.0 / 365, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS, 0, OBS, OBS_WEIGHT);
    final double pv = PRICER.presentValue(swapEnded, MARKET);
    assertEquals(0.0, pv, TOLERATED);
  }

}
