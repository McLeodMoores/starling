/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueCurveSensitivityForexBlackFlatCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueForexBlackFlatCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.CurrencyExposureDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexFlatProvider;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionVanillaBlackFlatMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountForexDataSets.createMulticurvesForex();

  private static final FXMatrix FX_MATRIX = MULTICURVES.getFxRates();
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final double SPOT = FX_MATRIX.getFxRate(EUR, USD);
  // General
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  // Vol data
  private static final Pair<Currency, Currency> CURRENCY_PAIR = Pairs.of(EUR, USD);
  private static final Period[] EXPIRY_PERIOD = new Period[] { Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1),
      Period.ofYears(2), Period.ofYears(5) };
  private static final double[] VOL = new double[] { 0.20, 0.25, 0.20, 0.15, 0.20 };
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final ZonedDateTime REFERENCE_SPOT = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime[] PAY_DATE = new ZonedDateTime[NB_EXP];
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXP];
  private static final double[] TIME_TO_EXPIRY = new double[NB_EXP];
  static {
    for (int i = 0; i < NB_EXP; i++) {
      PAY_DATE[i] = ScheduleCalculator.getAdjustedDate(REFERENCE_SPOT, EXPIRY_PERIOD[i], BUSINESS_DAY, CALENDAR);
      EXPIRY_DATE[i] = ScheduleCalculator.getAdjustedDate(PAY_DATE[i], -SETTLEMENT_DAYS, CALENDAR);
      TIME_TO_EXPIRY[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRY_DATE[i]);
    }
  }
  private static final Interpolator1D LINEAR_FLAT = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  private static final InterpolatedDoublesCurve TERM_STRUCTURE_VOL = new InterpolatedDoublesCurve(TIME_TO_EXPIRY, VOL, LINEAR_FLAT, true);

  // Methods and curves
  private static final BlackForexTermStructureParameters BLACK_TS_VOL = new BlackForexTermStructureParameters(TERM_STRUCTURE_VOL);

  private static final BlackForexFlatProvider BLACK_MULTICURVES = new BlackForexFlatProvider(MULTICURVES, BLACK_TS_VOL, CURRENCY_PAIR);

  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  private static final ForexOptionVanillaBlackFlatMethod METHOD_BLACK_FLAT = ForexOptionVanillaBlackFlatMethod.getInstance();
  private static final ForexOptionVanillaBlackSmileMethod METHOD_SMILE = ForexOptionVanillaBlackSmileMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final CurrencyExposureDiscountingCalculator CEDC = CurrencyExposureDiscountingCalculator.getInstance();
  private static final PresentValueForexBlackFlatCalculator PVFBFC = PresentValueForexBlackFlatCalculator.getInstance();
  private static final PresentValueCurveSensitivityForexBlackFlatCalculator PVCSFBFC = PresentValueCurveSensitivityForexBlackFlatCalculator
      .getInstance();
  // For comparison
  private static final double[] DELTA = new double[] { 0.10, 0.25 };
  private static final double[][] RISK_REVERSAL_FLAT = new double[][] { { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 },
      { 0.0, 0.0 } };
  private static final double[][] STRANGLE_FLAT = new double[][] { { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 } };
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_FLAT = new SmileDeltaTermStructureParametersStrikeInterpolation(
      TIME_TO_EXPIRY,
      DELTA, VOL, RISK_REVERSAL_FLAT, STRANGLE_FLAT, LINEAR_FLAT, LINEAR_FLAT);
  private static final BlackForexSmileProvider SMILE_FLAT_MULTICURVES = new BlackForexSmileProvider(MULTICURVES, SMILE_TERM_FLAT,
      CURRENCY_PAIR);

  // Some options
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL_EUR = 100000000;
  private static final ZonedDateTime OPT_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY,
      CALENDAR);
  private static final ZonedDateTime OPT_EXP_DATE = ScheduleCalculator.getAdjustedDate(OPT_PAY_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(EUR, USD, OPT_PAY_DATE, NOTIONAL_EUR, STRIKE);
  private static final ForexOptionVanillaDefinition CALL_LONG_DEFINITION = new ForexOptionVanillaDefinition(FX_DEFINITION, OPT_EXP_DATE,
      IS_CALL, IS_LONG);
  private static final ForexOptionVanillaDefinition PUT_SHORT_DEFINITION = new ForexOptionVanillaDefinition(FX_DEFINITION, OPT_EXP_DATE,
      !IS_CALL, !IS_LONG);
  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final ForexOptionVanilla CALL_LONG = CALL_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final ForexOptionVanilla PUT_SHORT = PUT_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_VOL = 1.0E-8;

  /**
   * Tests the present value against an explicit computation.
   */
  @Test
  public void presentValue() {
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, OPT_EXP_DATE);
    final double df = MULTICURVES.getDiscountFactor(USD, TimeCalculator.getTimeBetween(REFERENCE_DATE, OPT_PAY_DATE));
    final double forward = SPOT * MULTICURVES.getDiscountFactor(EUR, TimeCalculator.getTimeBetween(REFERENCE_DATE, OPT_PAY_DATE)) / df;
    final double volatility = TERM_STRUCTURE_VOL.getYValue(timeToExpiry);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(CALL_LONG);
    final double priceExpected = func.evaluate(dataBlack) * NOTIONAL_EUR;
    final MultipleCurrencyAmount priceComputed = METHOD_BLACK_FLAT.presentValue(CALL_LONG, BLACK_MULTICURVES);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(USD), TOLERANCE_PV);
  }

  /**
   * Tests the put/call parity present value.
   */
  @Test
  public void presentValuePutCallParity() {
    final MultipleCurrencyAmount pvCall = METHOD_BLACK_FLAT.presentValue(CALL_LONG, BLACK_MULTICURVES);
    final MultipleCurrencyAmount pvPut = METHOD_BLACK_FLAT.presentValue(PUT_SHORT, BLACK_MULTICURVES);
    final MultipleCurrencyAmount pvForward = FX.accept(PVDC, MULTICURVES);
    assertEquals("Forex vanilla option: present value put/call parity", MULTICURVES.getFxRates().convert(pvForward, EUR).getAmount(),
        MULTICURVES.getFxRates().convert(pvCall.plus(pvPut), EUR).getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests the present value Method versus the Calculator.
   */
  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BLACK_FLAT.presentValue(CALL_LONG, BLACK_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = CALL_LONG.accept(PVFBFC, BLACK_MULTICURVES);
    assertEquals("Forex vanilla option: present value Method vs Calculator", pvMethod.getAmount(USD), pvCalculator.getAmount(USD),
        TOLERANCE_PV);
  }

  /**
   * Tests the present value curve sensitivity Method versus the Calculator.
   */
  @Test
  public void presentValueCurveSensitivity() {
    MultipleCurrencyMulticurveSensitivity pvcsTS = METHOD_BLACK_FLAT.presentValueCurveSensitivity(CALL_LONG, BLACK_MULTICURVES);
    pvcsTS = pvcsTS.cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsSmile = METHOD_SMILE.presentValueCurveSensitivity(CALL_LONG, SMILE_FLAT_MULTICURVES);
    pvcsSmile = pvcsSmile.cleaned();
    AssertSensitivityObjects.assertEquals("Forex vanilla option: present value curve sensitivity vs flat smile", pvcsTS, pvcsSmile,
        TOLERANCE_PV);
  }

  /**
   * Tests the present value curve sensitivity Method versus the Calculator.
   */
  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_BLACK_FLAT.presentValueCurveSensitivity(CALL_LONG, BLACK_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CALL_LONG.accept(PVCSFBFC, BLACK_MULTICURVES);
    AssertSensitivityObjects.assertEquals("Forex vanilla option: present value curve sensitivity Method vs Calculator", pvcsMethod,
        pvcsCalculator, TOLERANCE_PV);
  }

  /**
   * Tests the currency exposure against the present value.
   */
  @Test
  public void currencyExposureVsPresentValue() {
    final MultipleCurrencyAmount pv = METHOD_BLACK_FLAT.presentValue(CALL_LONG, BLACK_MULTICURVES);
    final MultipleCurrencyAmount ce = METHOD_BLACK_FLAT.currencyExposure(CALL_LONG, BLACK_MULTICURVES);
    assertEquals("Forex vanilla option: currency exposure vs present value", ce.getAmount(USD) + ce.getAmount(EUR) * SPOT,
        pv.getAmount(USD), TOLERANCE_PV);
  }

  /**
   * Tests the put/call parity currency exposure.
   */
  @Test
  public void currencyExposurePutCallParity() {
    final MultipleCurrencyAmount currencyExposureCall = METHOD_BLACK_FLAT.currencyExposure(CALL_LONG, BLACK_MULTICURVES);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_BLACK_FLAT.currencyExposure(PUT_SHORT, BLACK_MULTICURVES);
    final MultipleCurrencyAmount currencyExposureForward = FX.accept(CEDC, MULTICURVES);
    assertEquals("Forex vanilla option: currency exposure put/call parity foreign", currencyExposureForward.getAmount(EUR),
        currencyExposureCall.getAmount(EUR)
            + currencyExposurePut.getAmount(EUR),
        TOLERANCE_PV);
    assertEquals("Forex vanilla option: currency exposure put/call parity domestic", currencyExposureForward.getAmount(USD),
        currencyExposureCall.getAmount(USD)
            + currencyExposurePut.getAmount(USD),
        TOLERANCE_PV);
  }

  /**
   * Tests the implied volatility.
   */
  @Test
  public void impliedVolatility() {
    final double ivCall = METHOD_BLACK_FLAT.impliedVolatility(CALL_LONG, BLACK_MULTICURVES);
    final double ivPut = METHOD_BLACK_FLAT.impliedVolatility(PUT_SHORT, BLACK_MULTICURVES);
    final double volExpected = BLACK_TS_VOL.getVolatility(CALL_LONG.getTimeToExpiry());
    assertEquals("Forex vanilla option: implied volatility", ivCall, volExpected, TOLERANCE_VOL);
    assertEquals("Forex vanilla option: implied volatility", ivPut, volExpected, TOLERANCE_VOL);
  }

  // TODO: test delta relative and delta relative spot

}
