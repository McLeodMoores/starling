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
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.SimpleImmutableFxMatrix;
import com.opengamma.analytics.financial.model.SimpleMutableFxMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pairs;

/**
 * Unit tests for {@link AmericanVanillaFxOptionPricingMethod}.
 */
public class AmericanVanillaFxOptionPricingMethodTest {
  /** The valuation date of the trade */
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2016, 7, 1);
  /** The domestic currency */
  private static final Currency DOMESTIC = Currency.USD;
  /** The foreign currency */
  private static final Currency FOREIGN = Currency.JPY;
  /** The spot */
  private static final double SPOT = 100;
  /** A domestic curve */
  private static final YieldAndDiscountCurve ZERO_DOMESTIC_CURVE = new YieldCurve("USD", ConstantDoublesCurve.from(0.0));
  /** A foreign curve */
  private static final YieldAndDiscountCurve ZERO_FOREIGN_CURVE = new YieldCurve("JPY", ConstantDoublesCurve.from(0.0));
  /** A domestic curve */
  private static final YieldAndDiscountCurve DOMESTIC_CURVE = new YieldCurve("USD", ConstantDoublesCurve.from(0.01));
  /** A foreign curve */
  private static final YieldAndDiscountCurve FOREIGN_CURVE = new YieldCurve("JPY", ConstantDoublesCurve.from(0.02));
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
  private static final SimpleImmutableFxMatrix FX_MATRIX;
  static {
    ZERO_RATE_DISCOUNTING_CURVES.put(DOMESTIC, ZERO_DOMESTIC_CURVE);
    ZERO_RATE_DISCOUNTING_CURVES.put(FOREIGN, ZERO_FOREIGN_CURVE);
    DISCOUNTING_CURVES.put(DOMESTIC, DOMESTIC_CURVE);
    DISCOUNTING_CURVES.put(FOREIGN, FOREIGN_CURVE);
    final SimpleMutableFxMatrix fx = SimpleMutableFxMatrix.of();
    fx.addCurrency(DOMESTIC, FOREIGN, SPOT);
    FX_MATRIX = SimpleImmutableFxMatrix.of(fx);
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
  private static final BlackForexSmileProviderInterface ZERO_VOL_ZERO_RATES = new BlackForexSmileProviderDiscount(ZERO_CURVES, ZERO_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
  /** Market data with a flat volatility surface and zero rates */
  private static final BlackForexSmileProviderInterface FLAT_VOL_ZERO_RATES = new BlackForexSmileProviderDiscount(ZERO_CURVES, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
  /** Market data with a flat volatility surface */
  private static final BlackForexSmileProviderInterface FLAT_VOL = new BlackForexSmileProviderDiscount(CURVES, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
  /** The accuracy */
  private static final double EPS = 1e-12;

  /**
   * Tests that the instrument cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPvMethodNullOption() {
    AmericanVanillaFxOptionPricingMethod.presentValue(null, ZERO_VOL_ZERO_RATES);
  }

  /**
   * Tests that the market data cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPvMethodNullMarketData() {
    final ForexDefinition underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), 1, 100);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), null);
  }

  /**
   * Tests the values of OTM and ITM options if the volatility is close to zero. Interest rates are zero so ITM calls and puts
   * should be worth (strike - diff).
   */
  @Test
  public void testZeroVolAndInterestRate() {
    final double diff = 0.5;
    // OTM call
    ForexDefinition underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), 1, SPOT + diff);
    ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    MultipleCurrencyAmount pv = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), ZERO_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(FOREIGN), 0, EPS);
    // ITM call
    underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), 1, SPOT - diff);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    pv = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), ZERO_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(FOREIGN), diff, EPS);
    // OTM put
    underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), 1, SPOT - diff);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), false, true);
    pv = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), ZERO_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(FOREIGN), 0, EPS);
    // ITM put
    underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), 1, SPOT + diff);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), false, true);
    pv = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), ZERO_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(FOREIGN), diff, EPS);
  }

  /**
   * Tests the values with a flat volatility surface. Interest rates are zero so calls should be equal to European options.
   */
  @Test
  public void testAmericanEuropeanEquivalence() {
    final double amount = 10000000;
    // OTM call
    ForexDefinition underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), amount, SPOT + 0.5);
    ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    MultipleCurrencyAmount pv = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(FOREIGN), ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES).getAmount(FOREIGN), EPS);
    // ITM call
    underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), amount, SPOT - 0.5);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    pv = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(FOREIGN), ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES).getAmount(FOREIGN), EPS);
    // OTM put
    underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), amount, SPOT - 0.5);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), false, true);
    pv = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(FOREIGN), ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES).getAmount(FOREIGN), EPS);
    // ITM call
    underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), amount, SPOT + 0.5);
    option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), false, true);
    pv = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pv.size(), 1);
    assertEquals(pv.getAmount(FOREIGN), ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES).getAmount(FOREIGN), EPS);
  }

  /**
   * Tests a call vs a put with zero rates. The options should be the same as European and so follow call-put parity.
   */
  @Test
  public void testPresentValueCallPut() {
    final ForexDefinition callUnderlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), 1, SPOT);
    final ForexDefinition putUnderlying = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), -1 * SPOT, 1 / SPOT);
    final ForexOptionVanillaDefinition call = new ForexOptionVanillaDefinition(callUnderlying, VALUATION_DATE.plusMonths(3), true, true);
    final ForexOptionVanillaDefinition put = new ForexOptionVanillaDefinition(putUnderlying, VALUATION_DATE.plusMonths(3), false, true);
    final MultipleCurrencyAmount pvCall = AmericanVanillaFxOptionPricingMethod.presentValue(call.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    final MultipleCurrencyAmount pvPut = AmericanVanillaFxOptionPricingMethod.presentValue(put.toDerivative(VALUATION_DATE), FLAT_VOL_ZERO_RATES);
    assertEquals(pvCall.getAmount(FOREIGN) / SPOT, pvPut.getAmount(DOMESTIC), EPS);
  }

  /**
   * Tests a long vs short.
   */
  @Test
  public void testPresentValueLongShort() {
    final ForexDefinition underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), 1, SPOT);
    final ForexOptionVanillaDefinition longOption = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final ForexOptionVanillaDefinition shortOption = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, false);
    final MultipleCurrencyAmount pvLong = AmericanVanillaFxOptionPricingMethod.presentValue(longOption.toDerivative(VALUATION_DATE), FLAT_VOL);
    final MultipleCurrencyAmount pvShort = AmericanVanillaFxOptionPricingMethod.presentValue(shortOption.toDerivative(VALUATION_DATE), FLAT_VOL);
    assertEquals(pvLong.getAmount(FOREIGN), -pvShort.getAmount(FOREIGN), EPS);
  }

  /**
   * Tests the currency exposure against the present value.
   */
  //@Test
  public void testCurrencyExposureVsPresentValue() {
    final ForexDefinition underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), 1, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final MultipleCurrencyAmount pv = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), FLAT_VOL);
    final MultipleCurrencyAmount ce = AmericanVanillaFxOptionPricingMethod.currencyExposure(option.toDerivative(VALUATION_DATE), FLAT_VOL);
    assertEquals(ce.getAmount(FOREIGN) + ce.getAmount(DOMESTIC) * SPOT, pv.getAmount(FOREIGN), EPS);
  }

  @Test
  public void testSpotDelta() {
    final double eps = 0.0001;
    final ForexDefinition underlying = new ForexDefinition(DOMESTIC, FOREIGN, VALUATION_DATE.plusMonths(3), 1, SPOT + 0.5);
    final ForexOptionVanillaDefinition option = new ForexOptionVanillaDefinition(underlying, VALUATION_DATE.plusMonths(3), true, true);
    final SimpleMutableFxMatrix fx = SimpleMutableFxMatrix.of();
    fx.addCurrency(DOMESTIC, FOREIGN, SPOT);
    final MulticurveProviderDiscount spotCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), SimpleImmutableFxMatrix.of(fx));
    final BlackForexSmileProviderInterface spot = new BlackForexSmileProviderDiscount(spotCurves, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
    final SimpleMutableFxMatrix fxUp = SimpleMutableFxMatrix.of();
    fxUp.addCurrency(DOMESTIC, FOREIGN, SPOT + eps);
    final MulticurveProviderDiscount spotUpCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), SimpleImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface spotUp = new BlackForexSmileProviderDiscount(spotUpCurves, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
    final SimpleMutableFxMatrix fxDown = SimpleMutableFxMatrix.of();
    fxDown.addCurrency(DOMESTIC, FOREIGN, SPOT - eps);
    final MulticurveProviderDiscount spotDownCurves = new MulticurveProviderDiscount(DISCOUNTING_CURVES, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), SimpleImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface spotDown = new BlackForexSmileProviderDiscount(spotDownCurves, FLAT_VOL_SURFACE, Pairs.of(DOMESTIC, FOREIGN));
    final double spotDelta = AmericanVanillaFxOptionPricingMethod.spotDelta(option.toDerivative(VALUATION_DATE), spot);
    final double pvUp = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), spotUp).getAmount(FOREIGN);
    final double pvDown = AmericanVanillaFxOptionPricingMethod.presentValue(option.toDerivative(VALUATION_DATE), spotDown).getAmount(FOREIGN);
    assertEquals(spotDelta, (pvUp - pvDown) / 2 / eps, eps * eps);
  }
}

