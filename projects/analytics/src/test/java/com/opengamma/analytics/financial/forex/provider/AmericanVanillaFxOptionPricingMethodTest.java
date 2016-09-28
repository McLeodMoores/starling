/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.forex.provider;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.EmptyWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.ImmutableFxMatrix;
import com.opengamma.analytics.financial.model.UncheckedMutableFxMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.calculator.blackforex.BjerksundStenslandFxCurveSensitivityCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.BjerksundStenslandFxOptionPvCalculator;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.blackforex.ParameterSensitivityForexBlackSmileDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pairs;

/**
 * Unit tests for {@link BjerksundStenslandVanillaFxOptionCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class AmericanVanillaFxOptionPricingMethodTest {
  /** The valuation date of the trade */
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2016, 7, 1);
  /** The domestic currency */
  private static final Currency DOMESTIC = Currency.USD;
  /** The foreign currency */
  private static final Currency FOREIGN = Currency.JPY;
  /** The spot */
  private static final double SPOT = 100;
  /** The interpolator used in the curves */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME);
  /** A domestic curve */
  private static final YieldAndDiscountCurve ZERO_DOMESTIC_CURVE = new YieldCurve("USD Curve", InterpolatedDoublesCurve.from(new double[] {0, 1, 2, 3}, new double[] {0, 0, 0, 0}, INTERPOLATOR));
  /** A foreign curve */
  private static final YieldAndDiscountCurve ZERO_FOREIGN_CURVE = new YieldCurve("JPY Curve", InterpolatedDoublesCurve.from(new double[] {0, 1, 2, 3}, new double[] {0, 0, 0, 0}, INTERPOLATOR));
  /** The domestic interest rate */
  private static final double DOMESTIC_RATE = 0.03;
  /** The foreign interest rate */
  private static final double FOREIGN_RATE = 0.04;
  /** A domestic curve */
  private static final YieldAndDiscountCurve DOMESTIC_CURVE = new YieldCurve("USD Curve",
      InterpolatedDoublesCurve.from(new double[] {0, 1, 2, 3}, new double[] {DOMESTIC_RATE, DOMESTIC_RATE, DOMESTIC_RATE, DOMESTIC_RATE}, INTERPOLATOR));
  /** A foreign curve */
  private static final YieldAndDiscountCurve FOREIGN_CURVE = new YieldCurve("JPY Curve",
      InterpolatedDoublesCurve.from(new double[] {0, 1, 2, 3}, new double[] {FOREIGN_RATE, FOREIGN_RATE, FOREIGN_RATE, FOREIGN_RATE}, INTERPOLATOR));
  /** Curves with zero rates and FX data */
  private static final MulticurveProviderDiscount ZERO_CURVES;
  /** Curves and FX data */
  private static final MulticurveProviderDiscount CURVES;
  /** A zero volatility surface */
  private static final SmileDeltaTermStructureParametersStrikeInterpolation ZERO_VOL_SURFACE;
  /** A flat volatility surface */
  private static final SmileDeltaTermStructureParametersStrikeInterpolation FLAT_VOL_SURFACE;
  /** Discounting curves where the rates are zero */
  private static final Map<Currency, YieldAndDiscountCurve> ZERO_RATE_DISCOUNTING_CURVES = new HashMap<>();
  /** Discounting curves */
  private static final Map<Currency, YieldAndDiscountCurve> DISCOUNTING_CURVES = new HashMap<>();
  /** The FX matrix */
  private static final ImmutableFxMatrix FX_MATRIX;
  static {
    ZERO_RATE_DISCOUNTING_CURVES.put(DOMESTIC, ZERO_DOMESTIC_CURVE);
    ZERO_RATE_DISCOUNTING_CURVES.put(FOREIGN, ZERO_FOREIGN_CURVE);
    DISCOUNTING_CURVES.put(DOMESTIC, DOMESTIC_CURVE);
    DISCOUNTING_CURVES.put(FOREIGN, FOREIGN_CURVE);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(FOREIGN, DOMESTIC, SPOT);
    FX_MATRIX = ImmutableFxMatrix.of(fx);
    ZERO_CURVES = new MulticurveProviderDiscount(ZERO_RATE_DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(), Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), FX_MATRIX);
    CURVES = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(), Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), FX_MATRIX);
    final ZonedDateTime[] expiryDate = new ZonedDateTime[5];
    final double[] expiryTime = new double[5];
    for (int i = 0; i < 5; i++) {
      expiryDate[i] = ScheduleCalculator.getAdjustedDate(VALUATION_DATE, Period.ofMonths((i + 1) * 3), BusinessDayConventions.NONE, EmptyWorkingDayCalendar.INSTANCE, true);
      expiryTime[i] = TimeCalculator.getTimeBetween(VALUATION_DATE, expiryDate[i]);
    }
    ZERO_VOL_SURFACE = new SmileDeltaTermStructureParametersStrikeInterpolation(expiryTime,
        new double[] {0.1, 0.25}, new double[] {1e-12, 1e-12, 1e-12, 1e-12, 1e-12}, new double[][] {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}, new double[][] {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}});
    FLAT_VOL_SURFACE = new SmileDeltaTermStructureParametersStrikeInterpolation(expiryTime,
        new double[] {0.1, 0.25}, new double[] {0.2, 0.2, 0.2, 0.2, 0.2}, new double[][] {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}, new double[][] {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}});
  }
  /** Market data with zero volatility surface and zero rates*/
  private static final BlackForexSmileProviderDiscount ZERO_VOL_ZERO_RATES = new BlackForexSmileProviderDiscount(ZERO_CURVES, ZERO_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
  /** Market data with a flat volatility surface and zero rates */
  private static final BlackForexSmileProviderDiscount FLAT_VOL_ZERO_RATES = new BlackForexSmileProviderDiscount(ZERO_CURVES, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
  /** Market data with a flat volatility surface */
  private static final BlackForexSmileProviderDiscount FLAT_VOL = new BlackForexSmileProviderDiscount(CURVES, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
  /** The notional in domestic currency */
  private static final int NOTIONAL = 1234;
  /** The accuracy */
  private static final double EPS = 1e-9;
  /** Calculates the bucketed PV01 */
  private static final ParameterSensitivityParameterCalculator<BlackForexSmileProviderInterface> BUCKETED_PV01_CALCULATOR = new ParameterSensitivityParameterCalculator<>(
      BjerksundStenslandFxCurveSensitivityCalculator.getInstance());
  /** Calculates the bucketed PV01 using finite difference */
  private static final ParameterSensitivityForexBlackSmileDiscountInterpolatedFDCalculator FINITE_DIFFERENCE_BUCKETED_SENSITIVITY_CALCULATOR =
      new ParameterSensitivityForexBlackSmileDiscountInterpolatedFDCalculator(BjerksundStenslandFxOptionPvCalculator.getInstance(), 1e-4);


  /**
   * Tests that the instrument cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPvMethodNullOption() {
    BjerksundStenslandVanillaFxOptionCalculator.presentValue(null, ZERO_VOL_ZERO_RATES);
  }

  /**
   * Tests that the market data cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPvMethodNullMarketData() {
    final ForexDefinition underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), NOTIONAL, 100);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), null);
  }

  /**
   * Tests the values of OTM and ITM options if the volatility is close to zero. Interest rates are zero so ITM calls and puts
   * should be worth (strike - diff).
   */
  @Test
  public void testZeroVolAndInterestRate() {
    final double diff = 0.5;
    // OTM call
    ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + diff);
    ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    MultipleCurrencyAmount pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), ZERO_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(DOMESTIC), 0, EPS);
    // ITM call
    underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT - diff);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), ZERO_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(DOMESTIC), NOTIONAL * diff, EPS);
    // OTM put
    underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT - diff);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), false, true);
    pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), ZERO_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(DOMESTIC), 0, EPS);
    // ITM put
    underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + diff);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), false, true);
    pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), ZERO_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(DOMESTIC), NOTIONAL * diff, EPS);
  }

  /**
   * Tests the values with a flat volatility surface. Interest rates are zero so calls should be equal to European options.
   */
  @Test
  public void testAmericanEuropeanEquivalence() {
    // OTM call
    ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    MultipleCurrencyAmount pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(DOMESTIC), ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES).getAmount(DOMESTIC), EPS);
    // ITM call
    underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT - 0.5);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(DOMESTIC), ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES).getAmount(DOMESTIC), EPS);
    // OTM put
    underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT - 0.5);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), false, true);
    pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(DOMESTIC), ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES).getAmount(DOMESTIC), EPS);
    // ITM call
    underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), false, true);
    pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(DOMESTIC), ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES).getAmount(DOMESTIC), EPS);
  }

  /**
   * Tests a call vs a put with zero rates. The options should be the same as European and so follow call-put parity.
   */
  @Test
  public void testPresentValueCallPut() {
    final ForexDefinition callUnderlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT);
    final ForexDefinition putUnderlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), -NOTIONAL * SPOT, 1 / SPOT);
    final ForexOptionVanillaDefinition call = new ForexOptionVanillaDefinition(callUnderlying, VALUATION_DATE.plusMonths(3), true, true);
    final ForexOptionVanillaDefinition put = new ForexOptionVanillaDefinition(putUnderlying, VALUATION_DATE.plusMonths(3), false, true);
    final MultipleCurrencyAmount pvCall = BjerksundStenslandVanillaFxOptionCalculator.presentValue(call.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    final MultipleCurrencyAmount pvPut = BjerksundStenslandVanillaFxOptionCalculator.presentValue(put.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pvCall.getAmount(DOMESTIC) / SPOT, pvPut.getAmount(FOREIGN), EPS);
  }

  /**
   * Tests a long vs short.
   */
  @Test
  public void testPresentValueLongShort() {
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT);
    final ForexOptionVanillaDefinition longOption = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final ForexOptionVanillaDefinition shortOption = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, false);
    final MultipleCurrencyAmount pvLong = BjerksundStenslandVanillaFxOptionCalculator.presentValue(longOption.toDerivative(VALUATION_DATE), FLAT_VOL);
    final MultipleCurrencyAmount pvShort = BjerksundStenslandVanillaFxOptionCalculator.presentValue(shortOption.toDerivative(VALUATION_DATE), FLAT_VOL);
    assertEquals(pvLong.getAmount(DOMESTIC), -pvShort.getAmount(DOMESTIC), EPS);
  }

  /**
   * Tests the currency exposure against the present value.
   */
  @Test
  public void testCurrencyExposureVsPresentValue() {
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final MultipleCurrencyAmount pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL);
    final MultipleCurrencyAmount ce = BjerksundStenslandVanillaFxOptionCalculator.currencyExposure(option.toDerivative(VALUATION_DATE), FLAT_VOL);
    assertEquals(ce.getAmount(DOMESTIC) + ce.getAmount(FOREIGN) * SPOT, pv.getAmount(DOMESTIC), EPS);
  }

  /**
   * Tests the implied volatility method.
   */
  @Test
  public void testImpliedVolatility() {
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    assertEquals(BjerksundStenslandVanillaFxOptionCalculator.impliedVolatility(option.toDerivative(VALUATION_DATE), FLAT_VOL), 0.2);
  }

  /**
   * Tests the spot delta calculation against an approximation calculated using central finite difference.
   */
  @Test
  public void testSpotDelta() {
    final double eps = 0.0001;
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(FOREIGN, DOMESTIC, SPOT);
    final MulticurveProviderDiscount spotCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fx));
    final BlackForexSmileProviderInterface spot = new BlackForexSmileProviderDiscount(spotCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    fxUp.addCurrency(FOREIGN, DOMESTIC, SPOT + eps);
    final MulticurveProviderDiscount spotUpCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface spotUp = new BlackForexSmileProviderDiscount(spotUpCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    fxDown.addCurrency(FOREIGN, DOMESTIC, SPOT - eps);
    final MulticurveProviderDiscount spotDownCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface spotDown = new BlackForexSmileProviderDiscount(spotDownCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final double spotDelta = BjerksundStenslandVanillaFxOptionCalculator.theoreticalSpotDelta(option.toDerivative(VALUATION_DATE), spot);
    final double delta = BjerksundStenslandVanillaFxOptionCalculator.delta(option.toDerivative(VALUATION_DATE), spot, true).getAmount();
    final double pvUp = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotUp).getAmount(DOMESTIC);
    final double pvDown = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotDown).getAmount(DOMESTIC);
    assertEquals(spotDelta, (pvUp - pvDown) / 2 / eps / NOTIONAL, eps * eps * 10);
    assertEquals(delta, spotDelta * NOTIONAL, eps);
  }

  /**
   * Tests the forward delta calculation against an approximation calculated using central finite difference.
   */
  @Test
  public void testForwardDelta() {
    final double eps = 0.0001;
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(FOREIGN, DOMESTIC, SPOT);
    final MulticurveProviderDiscount spotCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fx));
    final double domesticDf = spotCurves.getDiscountFactor(DOMESTIC, option.toDerivative(VALUATION_DATE).getUnderlyingForex().getPaymentTime());
    final double foreignDf = spotCurves.getDiscountFactor(FOREIGN, option.toDerivative(VALUATION_DATE).getUnderlyingForex().getPaymentTime());
    final BlackForexSmileProviderInterface spot = new BlackForexSmileProviderDiscount(spotCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    final double forward = SPOT * foreignDf / domesticDf;
    final double spotUp = (forward + eps) * domesticDf / foreignDf;
    fxUp.addCurrency(FOREIGN, DOMESTIC, spotUp);
    final MulticurveProviderDiscount forwardUpCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface forwardUp = new BlackForexSmileProviderDiscount(forwardUpCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    final double spotDown = (forward - eps) * domesticDf / foreignDf;
    fxDown.addCurrency(FOREIGN, DOMESTIC, spotDown);
    final MulticurveProviderDiscount forwardDownCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface forwardDown = new BlackForexSmileProviderDiscount(forwardDownCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final double forwardDelta = BjerksundStenslandVanillaFxOptionCalculator.theoreticalForwardDelta(option.toDerivative(VALUATION_DATE), spot);
    final double pvUp = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), forwardUp).getAmount(DOMESTIC);
    final double pvDown = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), forwardDown).getAmount(DOMESTIC);
    assertEquals(forwardDelta, (pvUp - pvDown) / 2 / eps / NOTIONAL, eps * eps * 10);
  }

  /**
   * Tests the direct relative delta calculation against an approximation calculated using central finite difference.
   */
  @Test
  public void testDirectRelativeDelta() {
    final double eps = 0.0001;
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(FOREIGN, DOMESTIC, SPOT);
    final MulticurveProviderDiscount spotCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fx));
    final BlackForexSmileProviderInterface spot = new BlackForexSmileProviderDiscount(spotCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    fxUp.addCurrency(FOREIGN, DOMESTIC, SPOT + eps);
    final MulticurveProviderDiscount spotUpCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface spotUp = new BlackForexSmileProviderDiscount(spotUpCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    fxDown.addCurrency(FOREIGN, DOMESTIC, SPOT - eps);
    final MulticurveProviderDiscount spotDownCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface spotDown = new BlackForexSmileProviderDiscount(spotDownCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final double relativeDelta = BjerksundStenslandVanillaFxOptionCalculator.theoreticalRelativeDelta(option.toDerivative(VALUATION_DATE), spot, true);
    final double relativeDeltaSpot = BjerksundStenslandVanillaFxOptionCalculator.theoreticalRelativeDeltaSpot(option.toDerivative(VALUATION_DATE), spot, true);
    final double pvUp = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotUp).getAmount(DOMESTIC);
    final double pvDown = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotDown).getAmount(DOMESTIC);
    assertEquals(relativeDelta, (pvUp - pvDown) / 2 / eps / NOTIONAL, eps * eps * 10);
    assertEquals(relativeDeltaSpot, SPOT * (pvUp - pvDown) / 2 / eps / NOTIONAL, eps * eps * 10 * SPOT);
  }

  /**
   * Tests the indirect relative delta calculation against an approximation calculated using central finite difference.
   */
  @Test
  public void testIndirectRelativeDelta() {
    final double eps = 0.0001;
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(DOMESTIC, FOREIGN, 1 / SPOT);
    final MulticurveProviderDiscount spotCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fx));
    final BlackForexSmileProviderInterface spot = new BlackForexSmileProviderDiscount(spotCurves, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    fxUp.addCurrency(DOMESTIC, FOREIGN, 1 / (SPOT + eps));
    final MulticurveProviderDiscount spotUpCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface spotUp = new BlackForexSmileProviderDiscount(spotUpCurves, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    fxDown.addCurrency(DOMESTIC, FOREIGN, 1 / (SPOT - eps));
    final MulticurveProviderDiscount spotDownCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface spotDown = new BlackForexSmileProviderDiscount(spotDownCurves, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
    final double relativeDelta = BjerksundStenslandVanillaFxOptionCalculator.theoreticalRelativeDelta(option.toDerivative(VALUATION_DATE), spot, false);
    final double relativeDeltaSpot = BjerksundStenslandVanillaFxOptionCalculator.theoreticalRelativeDeltaSpot(option.toDerivative(VALUATION_DATE), spot, false);
    final double pvUp = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotUp).getAmount(DOMESTIC);
    final double pvDown = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotDown).getAmount(DOMESTIC);
    assertEquals(relativeDelta, -SPOT * SPOT * (pvUp - pvDown) / 2 / eps / NOTIONAL, eps * eps * 10 * SPOT * SPOT);
    assertEquals(relativeDeltaSpot, -SPOT * (pvUp - pvDown) / 2 / eps / NOTIONAL, eps * eps * NOTIONAL);
  }

  /**
   * Tests the spot gamma calculation against approximations calculated using central finite difference.
   */
  @Test
  public void testSpotGamma() {
    final double eps = 0.0001;
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(FOREIGN, DOMESTIC, SPOT);
    final MulticurveProviderDiscount spotCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fx));
    final BlackForexSmileProviderInterface spot = new BlackForexSmileProviderDiscount(spotCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    fxUp.addCurrency(FOREIGN, DOMESTIC, SPOT + eps);
    final MulticurveProviderDiscount spotUpCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface spotUp = new BlackForexSmileProviderDiscount(spotUpCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    fxDown.addCurrency(FOREIGN, DOMESTIC, SPOT - eps);
    final MulticurveProviderDiscount spotDownCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface spotDown = new BlackForexSmileProviderDiscount(spotDownCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final double spotGamma = BjerksundStenslandVanillaFxOptionCalculator.theoreticalSpotGamma(option.toDerivative(VALUATION_DATE), spot);
    final double gamma = BjerksundStenslandVanillaFxOptionCalculator.gamma(option.toDerivative(VALUATION_DATE), spot, true).getAmount();
    final double pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spot).getAmount(DOMESTIC);
    final double pvUp = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotUp).getAmount(DOMESTIC);
    final double pvDown = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotDown).getAmount(DOMESTIC);
    final double deltaUp = BjerksundStenslandVanillaFxOptionCalculator.theoreticalSpotDelta(option.toDerivative(VALUATION_DATE), spotUp);
    final double deltaDown = BjerksundStenslandVanillaFxOptionCalculator.theoreticalSpotDelta(option.toDerivative(VALUATION_DATE), spotDown);
    assertEquals(spotGamma, (pvUp + pvDown - 2 * pv) / eps / eps / NOTIONAL, eps / 10);
    assertEquals(spotGamma, (deltaUp - deltaDown) / 2 / eps, eps * eps);
    assertEquals(gamma, spotGamma * NOTIONAL, eps);
  }

  /**
   * Tests the forward gamma calculation against approximations calculated using central finite difference.
   */
  @Test
  public void testForwardGamma() {
    final double eps = 0.0001;
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(FOREIGN, DOMESTIC, SPOT);
    final MulticurveProviderDiscount spotCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fx));
    final double domesticDf = spotCurves.getDiscountFactor(DOMESTIC, option.toDerivative(VALUATION_DATE).getUnderlyingForex().getPaymentTime());
    final double foreignDf = spotCurves.getDiscountFactor(FOREIGN, option.toDerivative(VALUATION_DATE).getUnderlyingForex().getPaymentTime());
    final BlackForexSmileProviderInterface spot = new BlackForexSmileProviderDiscount(spotCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    final double forward = SPOT * foreignDf / domesticDf;
    final double spotUp = (forward + eps) * domesticDf / foreignDf;
    fxUp.addCurrency(FOREIGN, DOMESTIC, spotUp);
    final MulticurveProviderDiscount forwardUpCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface forwardUp = new BlackForexSmileProviderDiscount(forwardUpCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    final double spotDown = (forward - eps) * domesticDf / foreignDf;
    fxDown.addCurrency(FOREIGN, DOMESTIC, spotDown);
    final MulticurveProviderDiscount forwardDownCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface forwardDown = new BlackForexSmileProviderDiscount(forwardDownCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final double forwardGamma = BjerksundStenslandVanillaFxOptionCalculator.theoreticalForwardGamma(option.toDerivative(VALUATION_DATE), spot);
    final double pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spot).getAmount(DOMESTIC);
    final double pvUp = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), forwardUp).getAmount(DOMESTIC);
    final double pvDown = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), forwardDown).getAmount(DOMESTIC);
    final double deltaUp = BjerksundStenslandVanillaFxOptionCalculator.theoreticalForwardDelta(option.toDerivative(VALUATION_DATE), forwardUp);
    final double deltaDown = BjerksundStenslandVanillaFxOptionCalculator.theoreticalForwardDelta(option.toDerivative(VALUATION_DATE), forwardDown);
    assertEquals(forwardGamma, (pvUp + pvDown - 2 * pv) / eps / eps / NOTIONAL, eps / 10);
    assertEquals(forwardGamma, (deltaUp - deltaDown) / 2 / eps, eps * eps);
  }

  /**
   * Tests the direct relative gamma calculation against an approximation calculated using central finite difference.
   */
  @Test
  public void testDirectRelativeGamma() {
    final double eps = 0.0001;
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(FOREIGN, DOMESTIC, SPOT);
    final MulticurveProviderDiscount spotCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fx));
    final BlackForexSmileProviderInterface spot = new BlackForexSmileProviderDiscount(spotCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    fxUp.addCurrency(FOREIGN, DOMESTIC, SPOT + eps);
    final MulticurveProviderDiscount spotUpCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface spotUp = new BlackForexSmileProviderDiscount(spotUpCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    fxDown.addCurrency(FOREIGN, DOMESTIC, SPOT - eps);
    final MulticurveProviderDiscount spotDownCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface spotDown = new BlackForexSmileProviderDiscount(spotDownCurves, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final double relativeGamma = BjerksundStenslandVanillaFxOptionCalculator.theoreticalRelativeGamma(option.toDerivative(VALUATION_DATE), spot, true);
    final double relativeGammaSpot = BjerksundStenslandVanillaFxOptionCalculator.theoreticalRelativeGammaSpot(option.toDerivative(VALUATION_DATE), spot, true);
    final double pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spot).getAmount(DOMESTIC);
    final double pvUp = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotUp).getAmount(DOMESTIC);
    final double pvDown = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotDown).getAmount(DOMESTIC);
    assertEquals(relativeGamma, (pvUp + pvDown - 2 * pv) / eps / eps / NOTIONAL, eps / 10);
    assertEquals(relativeGammaSpot, SPOT * (pvUp + pvDown - 2 * pv) / eps / eps / NOTIONAL, eps * 10);
  }

  /**
   * Tests the indirect relative gamma calculation against an approximation calculated using central finite difference.
   */
  @Test
  public void testIndirectRelativeGamma() {
    final double eps = 0.0001;
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(DOMESTIC, FOREIGN, 1 / SPOT);
    final MulticurveProviderDiscount spotCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fx));
    final BlackForexSmileProviderInterface spot = new BlackForexSmileProviderDiscount(spotCurves, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    fxUp.addCurrency(DOMESTIC, FOREIGN, 1 / (SPOT + eps));
    final MulticurveProviderDiscount spotUpCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface spotUp = new BlackForexSmileProviderDiscount(spotUpCurves, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    fxDown.addCurrency(DOMESTIC, FOREIGN, 1 / (SPOT - eps));
    final MulticurveProviderDiscount spotDownCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface spotDown = new BlackForexSmileProviderDiscount(spotDownCurves, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
    final double relativeGamma = BjerksundStenslandVanillaFxOptionCalculator.theoreticalRelativeGamma(option.toDerivative(VALUATION_DATE), spot, false);
    final double relativeGammaSpot = BjerksundStenslandVanillaFxOptionCalculator.theoreticalRelativeGammaSpot(option.toDerivative(VALUATION_DATE), spot, false);
    final double pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spot).getAmount(DOMESTIC);
    final double pvUp = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotUp).getAmount(DOMESTIC);
    final double pvDown = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), spotDown).getAmount(DOMESTIC);
    final double fdGamma = (pvUp + pvDown - 2 * pv) / eps / eps / NOTIONAL;
    final double fdDelta = (pvUp - pvDown) / 2 / eps / NOTIONAL;
    assertEquals(relativeGamma, SPOT * SPOT * SPOT * (SPOT * fdGamma + 2 * fdDelta), NOTIONAL);
    assertEquals(relativeGammaSpot, SPOT * SPOT * (SPOT * fdGamma + 2 * fdDelta), NOTIONAL / 10);
  }

  /**
   * Tests the theta calculation against an approximation calculated by rolling the date of the option expiry forward by one day.
   */
  @Test
  public void testTheta() {
    final double eps = 1 / 365.;
    final double paymentTime = 0.25;
    final double paymentTimeForward = 0.25 - eps;
    final PaymentFixed payment1 = new PaymentFixed(FOREIGN, paymentTime, -(SPOT + 0.5));
    final PaymentFixed payment2 = new PaymentFixed(DOMESTIC, paymentTime, 1);
    final Forex underlying = new Forex(payment1, payment2);
    final ForexOptionVanilla option = new ForexOptionVanilla(underlying, paymentTime, true, true);
    final PaymentFixed paymentForward1 = new PaymentFixed(FOREIGN, paymentTimeForward, -(SPOT + 0.5));
    final PaymentFixed paymentForward2 = new PaymentFixed(DOMESTIC, paymentTimeForward, 1);
    final Forex underlyingForward = new Forex(paymentForward1, paymentForward2);
    final ForexOptionVanilla optionForward = new ForexOptionVanilla(underlyingForward, paymentTimeForward, true, true);
    final double theta = BjerksundStenslandVanillaFxOptionCalculator.theoreticalTheta(option, FLAT_VOL);
    final double pv = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option, FLAT_VOL).getAmount(DOMESTIC);
    final double pvForwardInTime = BjerksundStenslandVanillaFxOptionCalculator.presentValue(optionForward, FLAT_VOL).getAmount(DOMESTIC);
    assertEquals(theta, (pvForwardInTime - pv) / eps / 365, 1e-3);
  }

  /**
   * Tests the vega calculation against an approximation calculated by central finite difference.
   */
  @Test
  public void testVega() {
    final double eps = 0.0001;
    final SmileDeltaParameters[] volTermStructure = FLAT_VOL_SURFACE.getVolatilityTerm();
    final SmileDeltaParameters[] volUpTermStructure = new SmileDeltaParameters[volTermStructure.length];
    final SmileDeltaParameters[] volDownTermStructure = new SmileDeltaParameters[volTermStructure.length];
    for (int i = 0; i < volTermStructure.length; i++) {
      final SmileDeltaParameters strip = volTermStructure[i];
      final double[] vols = strip.getVolatility();
      final double[] volsUp = new double[vols.length];
      final double[] volsDown = new double[vols.length];
      for (int j = 0; j < vols.length; j++) {
        volsUp[j] = vols[j] + eps;
        volsDown[j] = vols[j] - eps;
      }
      volUpTermStructure[i] = new SmileDeltaParameters(strip.getTimeToExpiry(), strip.getDelta(), volsUp);
      volDownTermStructure[i] = new SmileDeltaParameters(strip.getTimeToExpiry(), strip.getDelta(), volsDown);
    }
    final SmileDeltaTermStructureParametersStrikeInterpolation volUpSurface = new SmileDeltaTermStructureParametersStrikeInterpolation(volUpTermStructure);
    final SmileDeltaTermStructureParametersStrikeInterpolation volDownSurface = new SmileDeltaTermStructureParametersStrikeInterpolation(volDownTermStructure);
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final BlackForexSmileProviderInterface vol = new BlackForexSmileProviderDiscount(ZERO_CURVES, FLAT_VOL_SURFACE, Pairs.of(FOREIGN, DOMESTIC));
    final BlackForexSmileProviderInterface volUp = new BlackForexSmileProviderDiscount(ZERO_CURVES, volUpSurface, Pairs.of(FOREIGN, DOMESTIC));
    final BlackForexSmileProviderInterface volDown = new BlackForexSmileProviderDiscount(ZERO_CURVES, volDownSurface, Pairs.of(FOREIGN, DOMESTIC));
    final double theoreticalVega = BjerksundStenslandVanillaFxOptionCalculator.theoreticalVega(option.toDerivative(VALUATION_DATE), vol);
    final PresentValueForexBlackVolatilitySensitivity vega = BjerksundStenslandVanillaFxOptionCalculator.vega(option.toDerivative(VALUATION_DATE), vol);
    final double pvUp = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), volUp).getAmount(DOMESTIC);
    final double pvDown = BjerksundStenslandVanillaFxOptionCalculator.presentValue(option.toDerivative(VALUATION_DATE), volDown).getAmount(DOMESTIC);
    assertEquals(theoreticalVega, (pvUp - pvDown) / 2 / eps / NOTIONAL, eps * eps);
    assertEquals(vega.getCurrencyPair(), Pairs.of(FOREIGN, DOMESTIC));
    assertEquals(vega.getVega().getMap().size(), 1);
    assertEquals(vega.getVega().toSingleValue(), (pvUp - pvDown) / 2 / eps, eps);
  }

  /**
   * Tests the bucketed PV01 calculation against a finite difference approximation (i.e. bumping each node point in the curves
   * in turn).
   */
  @Test
  public void testBucketedPv01s() {
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanilla option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true).toDerivative(VALUATION_DATE);
    final MultipleCurrencyParameterSensitivity bucketedSensitivities = BUCKETED_PV01_CALCULATOR.calculateSensitivity(option, FLAT_VOL);
    final MultipleCurrencyParameterSensitivity finiteDifference = FINITE_DIFFERENCE_BUCKETED_SENSITIVITY_CALCULATOR.calculateSensitivity(option, FLAT_VOL);
    AssertSensitivityObjects.assertEquals("AmericanVanillaFxOptionPricingMethodTest: bucketed PV01 ", bucketedSensitivities, finiteDifference, 1e-2);
  }

  /**
   * Tests the bucketed vega calculation.
   */
  @Test
  public void testBucketedVega() {
    // note the expiry and payment do not fall on a surface node
    final ForexDefinition underlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3).minusDays(5), NOTIONAL, SPOT + 0.5);
    final ForexOptionVanilla option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3).minusDays(5), true, true).toDerivative(VALUATION_DATE);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle bucketedVega = BjerksundStenslandVanillaFxOptionCalculator.bucketedVega(option, FLAT_VOL);
    assertEquals(bucketedVega.getCurrencyPair(), Pairs.of(FOREIGN, DOMESTIC));
    assertEquals(bucketedVega.getVega().getNumberOfElements(), 25);
    double totalVega = 0;
    int nonZeroTotal = 0;
    for (int i = 0; i < bucketedVega.getVega().getNumberOfRows(); i++) {
      for (int j = 0; j < bucketedVega.getVega().getNumberOfColumns(); j++) {
        final Double value = bucketedVega.getVega().getEntry(i, j);
        if (Double.compare(value, 0) != 0) {
          nonZeroTotal++;
        }
        totalVega += value;
      }
    }
    assertEquals(nonZeroTotal, 2);
    assertEquals(totalVega, BjerksundStenslandVanillaFxOptionCalculator.theoreticalVega(option, FLAT_VOL) * NOTIONAL, EPS);
  }
}

