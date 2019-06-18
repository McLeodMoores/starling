/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.EquityDerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.DoubleQuadraticInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VarianceSwapRatesSensitivityTest {

  private static final EquityDerivativeSensitivityCalculator DELTA_CAL =
      new EquityDerivativeSensitivityCalculator(VarianceSwapPresentValueCalculator.getInstance());
  // Tests ------------------------------------------

  /*
   * List of tests:
   * EQUITY FWD
   *  - Test zero if vols are flat
   * Rates
   *  - For two scalar risks, compare to presentValue calc
   *  - calcDiscountRateSensitivity = 10000 * calcPV01
   *  - For bucketedDelta, compare Sum to calcDiscountRateSensitivity
   * Vega
   * - Parallel Vega should be equal to 2 * sigma * Z(0,T)
   * - Test sum of individuals = One big shift by manually creating another vol surface
   * - Test that we don't get sensitivity on unexpected expiries
   */

  private static final double TOLERATED = 1.0E-8;

  /**
   * Forward sensitivity comes only from volatility skew. Let's check
   * Note the vol surface is flat at 0.5, 1 and 10 years, but skewed at 5 years.
   */
  @Test
  public void testForwardSensitivity() {

    final double relShift = 0.01;

    final double deltaSkew = DELTA_CAL.calcForwardSensitivity(SWAP_5Y, MARKET, relShift);
    final double deltaFlatLong = DELTA_CAL.calcForwardSensitivity(SWAP_10Y, MARKET, relShift);
    final double deltaFlatShort = DELTA_CAL.calcForwardSensitivity(SWAP_1Y, MARKET, relShift);

    assertTrue(Math.abs(deltaSkew) > Math.abs(deltaFlatShort));
    assertTrue(Math.abs(deltaSkew) > Math.abs(deltaFlatLong));
    assertEquals(deltaFlatLong, deltaFlatShort, TOLERATED);
    assertEquals(0.0, deltaFlatShort, TOLERATED);
  }

  /**
   * If the smile/skew translates with the forward, we always expect zero forward sensitivity.
   */
  @Test
  public void testForwardSensitivityForDeltaStrikeParameterisation() {

    final InterpolatedDoublesSurface deltaSurface =
        new InterpolatedDoublesSurface(EXPIRIES, CALLDELTAS, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_LINEAR, INTERPOLATOR_1D_DBLQUAD));
    final BlackVolatilitySurfaceDelta deltaVolSurface = new BlackVolatilitySurfaceDelta(deltaSurface, FORWARD_CURVE);
    final StaticReplicationDataBundle deltaMarket = new StaticReplicationDataBundle(deltaVolSurface, FUNDING, FORWARD_CURVE);

    final double relShift = 0.1;

    final double deltaSkew = DELTA_CAL.calcForwardSensitivity(SWAP_5Y, deltaMarket, relShift);
    final double deltaFlatLong = DELTA_CAL.calcForwardSensitivity(SWAP_10Y, deltaMarket, relShift);
    final double deltaFlatShort = DELTA_CAL.calcForwardSensitivity(SWAP_1Y, deltaMarket, relShift);

    assertEquals(0.0, deltaSkew, TOLERATED);
    assertEquals(0.0, deltaFlatLong, TOLERATED);
    assertEquals(0.0, deltaFlatShort, TOLERATED);
  }

  @Test
  public void testTotalRateSensitivity() {

    final double relShift = 0.01;

    final double delta = DELTA_CAL.calcForwardSensitivity(SWAP_5Y, MARKET, relShift);
    final double pv = PRICER_WITHOUT_CUTOFF.presentValue(SWAP_5Y, MARKET);
    final double settlement = SWAP_5Y.getTimeToSettlement();

    final double totalRateSens = DELTA_CAL.calcDiscountRateSensitivity(SWAP_5Y, MARKET, relShift);
    final double fwd = FORWARD_CURVE.getForward(settlement);

    assertEquals(totalRateSens, settlement * (delta * fwd - pv), TOLERATED);

  }

  @Test
  public void testDiscountRateSensitivityWithNoSkew() {

    final double rateSens = DELTA_CAL.calcDiscountRateSensitivity(SWAP_10Y, MARKET);
    final double pv = PRICER_WITHOUT_CUTOFF.presentValue(SWAP_10Y, MARKET);
    final double settlement = SWAP_10Y.getTimeToSettlement();

    assertEquals(-settlement * pv, rateSens, TOLERATED);
  }

  @Test
  public void testPV01() {

    final double rateSens = DELTA_CAL.calcDiscountRateSensitivity(SWAP_STARTS_NOW, MARKET);
    final double pv01 = DELTA_CAL.calcPV01(SWAP_STARTS_NOW, MARKET);

    assertEquals(pv01 * 10000, rateSens, TOLERATED);
  }

  @Test
  public void testBucketedDeltaVsPV01() {

    final double rateSens = DELTA_CAL.calcDiscountRateSensitivity(SWAP_STARTS_NOW, MARKET);
    final DoubleMatrix1D deltaBuckets = DELTA_CAL.calcDeltaBucketed(SWAP_STARTS_NOW, MARKET);
    final int nDeltas = deltaBuckets.getNumberOfElements();
    final int nYieldNodes = ((YieldCurve) MARKET.getDiscountCurve()).getCurve().size();
    assertEquals(nDeltas, nYieldNodes, TOLERATED);

    double bucketSum = 0.0;
    for (int i = 0; i < nDeltas; i++) {
      bucketSum += deltaBuckets.getEntry(i);
    }
    assertEquals(rateSens, bucketSum, TOLERATED);
  }

  @Test
  public void testBlackVegaParallel() {

    final double expiry = 0.5;
    final double sigma = SURFACE.getZValue(expiry, 100.0);
    final VarianceSwap swap = new VarianceSwap(T_PLUS_ONE, expiry, expiry, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR,
        ANNUALIZATION, N_OBS_EXPECTED, N_OBS_DISRUPTED, NO_OBSERVATIONS, NO_OBS_WEIGHTS);
    final double zcb = MARKET.getDiscountCurve().getDiscountFactor(swap.getTimeToSettlement());

    final Double vegaParallel = DELTA_CAL.calcBlackVegaParallel(swap, MARKET);
    final double theoreticalVega = 2 * sigma * zcb * swap.getVarNotional();

    assertEquals(theoreticalVega, vegaParallel, 0.01);
  }

  @Test
  public void testBlackVegaForEntireSurface() {

    // Compute the surface
    final NodalDoublesSurface vegaSurface = DELTA_CAL.calcBlackVegaForEntireSurface(SWAP_STARTS_NOW, MARKET);
    // Sum up each constituent
    final double[] vegaBuckets = vegaSurface.getZDataAsPrimitive();
    double sumVegaBuckets = 0.0;
    for (int i = 0; i < vegaSurface.size(); i++) {
      sumVegaBuckets += vegaBuckets[i];
    }

    // Compute parallel vega, ie to a true parallel shift
    final Double parallelVega = DELTA_CAL.calcBlackVegaParallel(SWAP_STARTS_NOW, MARKET);

    assertEquals(parallelVega, sumVegaBuckets, 0.01);
  }

  /**
   * Test BlackVolatilityDeltaSurface.
   * sum of vega buckets = 4583.92106434809
   * parallelVega = 4583.95175875458
   */
  @Test
  public void testBlackVegaForDeltaSurface() {

    final InterpolatedDoublesSurface deltaSurface =
        new InterpolatedDoublesSurface(EXPIRIES, CALLDELTAS, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_LINEAR, INTERPOLATOR_1D_DBLQUAD));
    final BlackVolatilitySurfaceDelta deltaVolSurface = new BlackVolatilitySurfaceDelta(deltaSurface, FORWARD_CURVE);
    final StaticReplicationDataBundle deltaMarket = new StaticReplicationDataBundle(deltaVolSurface, FUNDING, FORWARD_CURVE);

    // Compute the surface
    final NodalDoublesSurface vegaSurface = DELTA_CAL.calcBlackVegaForEntireSurface(SWAP_STARTS_NOW, deltaMarket);
    // Sum up each constituent
    final double[] vegaBuckets = vegaSurface.getZDataAsPrimitive();
    double sumVegaBuckets = 0.0;
    for (int i = 0; i < vegaSurface.size(); i++) {
      sumVegaBuckets += vegaBuckets[i];
    }

    // Compute parallel vega, ie to a true parallel shift
    final Double parallelVega = DELTA_CAL.calcBlackVegaParallel(SWAP_STARTS_NOW, deltaMarket);

    assertEquals(parallelVega, sumVegaBuckets, 0.033);
  }

  // Setup ------------------------------------------

  // The pricing method
  //  final VarianceSwapStaticReplication pricer_default_w_cutoff = new VarianceSwapStaticReplication(StrikeParameterization.STRIKE);
  private static final VarianceSwapStaticReplication PRICER_WITHOUT_CUTOFF = new VarianceSwapStaticReplication();

  // Market data
  private static final double SPOT = 80;
  private static final double DRIFT = 0.03;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
  // private static final double FORWARD = 100;

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0 };
  private static final double[] STRIKES = new double[] {40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120 };
  private static final double[] CALLDELTAS = new double[] {0.9, 0.75, 0.5, 0.25, 0.9, 0.75, 0.5, 0.25, 0.9, 0.75, 0.5, 0.25, 0.9, 0.75, 0.5, 0.25 };

  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28, 0.25, 0.25, 0.25, 0.25, 0.26, 0.24, 0.23, 0.25, 0.20, 0.20, 0.20, 0.20 };

  private static final Interpolator1D INTERPOLATOR_1D_DBLQUAD = NamedInterpolator1dFactory.of(DoubleQuadraticInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);

  private static final Interpolator1D INTERPOLATOR_1D_LINEAR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME);

  private static final InterpolatedDoublesSurface SURFACE =
      new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_LINEAR, INTERPOLATOR_1D_DBLQUAD));
  private static final BlackVolatilitySurfaceStrike VOL_SURFACE = new BlackVolatilitySurfaceStrike(SURFACE);

  private static final double[] MATURITIES = {0.5, 1.0, 5.0, 10.0, 20.0 };
  private static final double[] RATES = {0.02, 0.03, 0.05, 0.05, 0.04 };
  private static final YieldCurve FUNDING = YieldCurve.from(new InterpolatedDoublesCurve(MATURITIES, RATES, INTERPOLATOR_1D_DBLQUAD, true));

  private static final StaticReplicationDataBundle MARKET = new StaticReplicationDataBundle(VOL_SURFACE, FUNDING, FORWARD_CURVE);

  // The derivative
  private static final double VAR_STRIKE = 0.05;
  private static final double VAR_NOTIONAL = 10000; // A notional of 10000 means PV is in bp
  private static final double NOW = 0;
  private static final double EXPIRY_1 = 1;
  private static final double EXPIRY_2 = 2;
  private static final double EXPIRY_5 = 5;
  private static final double EXPIRY_10 = 10;
  private static final int N_OBS_EXPECTED = 750;
  private static final int N_OBS_DISRUPTED = 0;
  private static final double ANNUALIZATION = 252;

  private static final ZonedDateTime TODAY = ZonedDateTime.now();
  private static final ZonedDateTime TOMORROW = TODAY.plusDays(1);
  private static final double T_PLUS_ONE = TimeCalculator.getTimeBetween(TODAY, TOMORROW);

  private static final double[] NO_OBSERVATIONS = {};
  private static final double[] NO_OBS_WEIGHTS = {};
  private static final double[] SINGLE_OBS_NO_RETURN = {80 };

  private static final VarianceSwap SWAP_STARTS_NOW =
      new VarianceSwap(NOW, EXPIRY_2, EXPIRY_2, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS_EXPECTED,
          N_OBS_DISRUPTED, SINGLE_OBS_NO_RETURN, NO_OBS_WEIGHTS);

  //private static final VarianceSwap swapStartsTomorrow = new VarianceSwap(tPlusOne, expiry2, expiry2, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);

  private static final VarianceSwap SWAP_10Y = new VarianceSwap(T_PLUS_ONE, EXPIRY_10, EXPIRY_10, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS_EXPECTED, N_OBS_DISRUPTED, NO_OBSERVATIONS, NO_OBS_WEIGHTS);
  private static final VarianceSwap SWAP_5Y = new VarianceSwap(T_PLUS_ONE, EXPIRY_5, EXPIRY_5, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS_EXPECTED, N_OBS_DISRUPTED, NO_OBSERVATIONS, NO_OBS_WEIGHTS);
  private static final VarianceSwap SWAP_1Y = new VarianceSwap(T_PLUS_ONE, EXPIRY_1, EXPIRY_1, VAR_STRIKE, VAR_NOTIONAL, Currency.EUR, ANNUALIZATION, N_OBS_EXPECTED, N_OBS_DISRUPTED, NO_OBSERVATIONS, NO_OBS_WEIGHTS);

}
