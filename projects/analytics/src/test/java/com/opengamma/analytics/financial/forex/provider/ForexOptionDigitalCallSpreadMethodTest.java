/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.FXDataSets;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.DoubleQuadraticInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests related to the pricing method for digital Forex option transactions with Black function and a volatility provider.
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionDigitalCallSpreadMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountForexDataSets.createMulticurvesForex();

  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);

  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM = ForexSmileProviderDataSets
      .smile5points(REFERENCE_DATE);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_FLAT = ForexSmileProviderDataSets
      .smileFlat(REFERENCE_DATE);
  private static final BlackForexSmileProviderDiscount SMILE_MULTICURVES = new BlackForexSmileProviderDiscount(MULTICURVES, SMILE_TERM,
      Pairs.of(EUR, USD));
  private static final BlackForexSmileProviderDiscount SMILE_FLAT_MULTICURVES = new BlackForexSmileProviderDiscount(MULTICURVES,
      SMILE_TERM_FLAT, Pairs.of(EUR, USD));

  private static final FXMatrix FX_MATRIX = MULTICURVES.getFxRates();
  private static final double SPOT = FX_MATRIX.getFxRate(EUR, USD);
  // General
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;

  private static final ForexOptionVanillaBlackSmileMethod METHOD_VANILLA_BLACK = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexOptionDigitalBlackSmileMethod METHOD_DIGITAL_BLACK = ForexOptionDigitalBlackSmileMethod.getInstance();
  private static final double CALL_SPREAD = 0.0001;
  private static final ForexOptionDigitalCallSpreadBlackSmileMethod METHOD_DIGITAL_SPREAD = new ForexOptionDigitalCallSpreadBlackSmileMethod(
      CALL_SPREAD);

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  // option
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL = 100000000;
  private static final ZonedDateTime OPTION_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY,
      CALENDAR);
  private static final ZonedDateTime OPTION_EXP_DATE = ScheduleCalculator.getAdjustedDate(OPTION_PAY_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(EUR, USD, OPTION_PAY_DATE, NOTIONAL, STRIKE);
  private static final Forex FOREX = FOREX_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final ForexOptionDigitalDefinition FOREX_DIGITAL_CALL_DOM_DEFINITION = new ForexOptionDigitalDefinition(FOREX_DEFINITION,
      OPTION_EXP_DATE, IS_CALL,
      IS_LONG, true);
  private static final ForexOptionDigital FOREX_DIGITAL_CALL_DOM = FOREX_DIGITAL_CALL_DOM_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final ForexOptionDigitalDefinition FOREX_DIGITAL_CALL_FOR_DEFINITION = new ForexOptionDigitalDefinition(FOREX_DEFINITION,
      OPTION_EXP_DATE, IS_CALL,
      IS_LONG, false);
  private static final ForexOptionDigital FOREX_DIGITAL_CALL_FOR = FOREX_DIGITAL_CALL_FOR_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_FLAT = 1.0E+1; // The spread size will create a discrepancy.
  private static final double TOLERANCE_CE_FLAT = 1.0E+2; // The spread size will create a discrepancy.
  private static final double TOLERANCE_DELTA = 1.0E+2; // 0.01 currency unit for 1 bp
  private static final double TOLERANCE_RELATIVE = 1.0E-6;

  /**
   * Tests the present value in a flat smile case.
   */
  @Test
  public void presentValueFlat() {
    final MultipleCurrencyAmount pvSpread = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_FLAT_MULTICURVES);
    final MultipleCurrencyAmount pvBlack = METHOD_DIGITAL_BLACK.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_FLAT_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - present value", pvBlack.getAmount(USD), pvSpread.getAmount(USD),
        TOLERANCE_PV_FLAT);
  }

  /**
   * Tests the present value with an explicit computation.
   */
  @Test
  public void presentValue() {
    final double strikeM = STRIKE * (1 - CALL_SPREAD);
    final double strikeP = STRIKE * (1 + CALL_SPREAD);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final MultipleCurrencyAmount pvP = METHOD_VANILLA_BLACK.presentValue(vanillaP, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvM = METHOD_VANILLA_BLACK.presentValue(vanillaM, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvExpected = pvM.plus(pvP.multipliedBy(-1.0))
        .multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final MultipleCurrencyAmount pvComputed = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - present value", pvExpected.getAmount(USD), pvComputed.getAmount(USD),
        TOLERANCE_PV);
  }

  /**
   * Tests the present value with an explicit computation.
   */
  @Test
  public void presentValueDoubleQuadratic() {
    final Interpolator1D interpolator = NamedInterpolator1dFactory.of(DoubleQuadraticInterpolator1dAdapter.NAME,
        LinearExtrapolator1dAdapter.NAME,
        LinearExtrapolator1dAdapter.NAME);
    final SmileDeltaTermStructureParametersStrikeInterpolation smileTerm = FXDataSets.smile3points(REFERENCE_DATE, interpolator);
    final BlackForexSmileProviderDiscount smile = new BlackForexSmileProviderDiscount(MULTICURVES, smileTerm, Pairs.of(EUR, USD));
    final double strikeM = STRIKE * (1 - CALL_SPREAD);
    final double strikeP = STRIKE * (1 + CALL_SPREAD);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final MultipleCurrencyAmount pvP = METHOD_VANILLA_BLACK.presentValue(vanillaP, smile);
    final MultipleCurrencyAmount pvM = METHOD_VANILLA_BLACK.presentValue(vanillaM, smile);
    final MultipleCurrencyAmount pvExpected = pvM.plus(pvP.multipliedBy(-1.0))
        .multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final MultipleCurrencyAmount pvComputed = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, smile);
    assertEquals("Forex Digital option: call spread method - present value", pvExpected.getAmount(USD), pvComputed.getAmount(USD),
        TOLERANCE_PV);
  }

  /**
   * Tests put call parity.
   */
  @Test
  public void presentValuePutCallParityDomestic() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition callDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall,
        isLong);
    final ForexOptionDigitalDefinition putDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, !isCall,
        isLong);
    final ForexOptionDigital call = callDefinition.toDerivative(REFERENCE_DATE);
    final ForexOptionDigital put = putDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvCall = METHOD_DIGITAL_SPREAD.presentValue(call, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvPut = METHOD_DIGITAL_SPREAD.presentValue(put, SMILE_MULTICURVES);
    final Double pvCash = Math.abs(put.getUnderlyingForex().getPaymentCurrency2().accept(PVDC, MULTICURVES).getAmount(USD));
    assertEquals("Forex Digital option: call spread method - present value", pvCall.getAmount(USD) + pvPut.getAmount(USD), Math.abs(pvCash),
        TOLERANCE_PV_FLAT);
  }

  /**
   * Tests put call parity.
   */
  @Test
  public void presentValuePutCallParityForeign() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition callDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong,
        false);
    final ForexOptionDigitalDefinition putDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong,
        false);
    final ForexOptionDigital call = callDefinition.toDerivative(REFERENCE_DATE);
    final ForexOptionDigital put = putDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvCall = METHOD_DIGITAL_SPREAD.presentValue(call, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvPut = METHOD_DIGITAL_SPREAD.presentValue(put, SMILE_MULTICURVES);
    final Double pvCash = Math.abs(put.getUnderlyingForex().getPaymentCurrency1().accept(PVDC, MULTICURVES).getAmount(EUR));
    assertEquals("Forex Digital option: call spread method - present value", pvCall.getAmount(EUR) + pvPut.getAmount(EUR), Math.abs(pvCash),
        TOLERANCE_PV_FLAT);
  }

  /**
   * Tests the present value long/short parity.
   */
  @Test
  public void presentValueLongShort() {
    final ForexOptionDigitalDefinition forexOptionShortDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE,
        IS_CALL, !IS_LONG);
    final ForexOptionDigital forexOptionShort = forexOptionShortDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvShort = METHOD_DIGITAL_SPREAD.presentValue(forexOptionShort, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvLong = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: present value long/short parity", pvLong.getAmount(USD), -pvShort.getAmount(USD), 1E-2);
    final MultipleCurrencyAmount ceShort = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionShort, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ceLong = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: currency exposure long/short parity", ceLong.getAmount(USD), -ceShort.getAmount(USD), 1E-2);
    assertEquals("Forex Digital option: currency exposure long/short parity", ceLong.getAmount(EUR), -ceShort.getAmount(EUR), 1E-2);
  }

  /**
   * Tests the currency exposure in a flat smile case.
   */
  @Test
  public void currencyExposureFlatDomestic() {
    final MultipleCurrencyAmount ceSpread = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_FLAT_MULTICURVES);
    final MultipleCurrencyAmount ceBlack = METHOD_DIGITAL_BLACK.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_FLAT_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceBlack.getAmount(USD), ceSpread.getAmount(USD),
        TOLERANCE_CE_FLAT);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceBlack.getAmount(EUR), ceSpread.getAmount(EUR),
        TOLERANCE_CE_FLAT);
  }

  /**
   * Tests the currency exposure with an explicit computation.
   */
  @Test
  public void currencyExposureDomestic() {
    final double spread = 0.0001; // Relative spread.
    final double strikeM = STRIKE * (1 - spread);
    final double strikeP = STRIKE * (1 + spread);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final MultipleCurrencyAmount ceP = METHOD_VANILLA_BLACK.currencyExposure(vanillaP, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ceM = METHOD_VANILLA_BLACK.currencyExposure(vanillaM, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ceExpected = ceM.plus(ceP.multipliedBy(-1.0))
        .multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final MultipleCurrencyAmount ceComputed = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(USD), ceComputed.getAmount(USD),
        TOLERANCE_PV);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(EUR), ceComputed.getAmount(EUR),
        TOLERANCE_PV);
  }

  /**
   * Tests the currency exposure with an explicit computation.
   */
  @Test
  public void currencyExposureForeign() {
    final double strikeM = STRIKE * (1 - CALL_SPREAD);
    final double strikeP = STRIKE * (1 + CALL_SPREAD);
    final double amountPaid = Math.abs(FOREX_DIGITAL_CALL_FOR.getUnderlyingForex().getPaymentCurrency1().getAmount());
    final double strikeRelM = 1.0 / strikeP;
    final double strikeRelP = 1.0 / strikeM;
    final double amount = amountPaid / (strikeRelP - strikeRelM);
    final Forex forexM = new Forex(FOREX_DIGITAL_CALL_FOR.getUnderlyingForex().getPaymentCurrency2().withAmount(amount),
        FOREX_DIGITAL_CALL_FOR.getUnderlyingForex()
            .getPaymentCurrency1().withAmount(-strikeRelM * amount));
    final Forex forexP = new Forex(FOREX_DIGITAL_CALL_FOR.getUnderlyingForex().getPaymentCurrency2().withAmount(amount),
        FOREX_DIGITAL_CALL_FOR.getUnderlyingForex()
            .getPaymentCurrency1().withAmount(-strikeRelP * amount));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_FOR.getExpirationTime(), !IS_CALL, false);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_FOR.getExpirationTime(), !IS_CALL, true);
    final MultipleCurrencyAmount ceP = METHOD_VANILLA_BLACK.currencyExposure(vanillaP, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ceM = METHOD_VANILLA_BLACK.currencyExposure(vanillaM, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ceExpected = ceM.plus(ceP);
    final MultipleCurrencyAmount ceComputed = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_FOR, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(USD), ceComputed.getAmount(USD),
        TOLERANCE_PV);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(EUR), ceComputed.getAmount(EUR),
        TOLERANCE_PV);
  }

  /**
   * Tests the currency exposure with a FD rate shift.
   */
  @Test
  public void currencyExposureForeign2() {
    final double shift = 0.000005;
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT + shift);
    final MulticurveProviderDiscount multicurvesP = MULTICURVES.copy();
    multicurvesP.setForexMatrix(fxMatrixP);
    final BlackForexSmileProviderDiscount smileP = new BlackForexSmileProviderDiscount(multicurvesP, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final MultipleCurrencyAmount ce = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_FOR, SMILE_FLAT_MULTICURVES);
    final MultipleCurrencyAmount pv = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_FOR, SMILE_FLAT_MULTICURVES);
    final MultipleCurrencyAmount pvP = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_FOR, smileP);
    assertEquals("Forex Digital option: call spread method - currency exposure - PL EUR", pvP.getAmount(EUR) - pv.getAmount(EUR),
        ce.getAmount(USD)
            * (1.0 / (SPOT + shift) - 1 / SPOT),
        TOLERANCE_PV);
    assertEquals("Forex Digital option: call spread method - currency exposure - PL USD",
        pvP.getAmount(EUR) * (SPOT + shift) - pv.getAmount(EUR) * SPOT,
        ce.getAmount(EUR) * (SPOT + shift - SPOT), TOLERANCE_PV);
  }

  /**
   * Tests the currency exposure against the present value.
   */
  @Test
  public void currencyExposureVsPresentValue() {
    final MultipleCurrencyAmount pv = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ce = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - currency exposure vs present value",
        ce.getAmount(USD) + ce.getAmount(EUR) * SPOT, pv.getAmount(USD),
        TOLERANCE_PV);
  }

  /**
   * Tests the put/call parity currency exposure.
   */
  @Test
  public void currencyExposurePutCallParityDomestic() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition forexOptionDefinitionCall = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate,
        isCall, isLong);
    final ForexOptionDigitalDefinition forexOptionDefinitionPut = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate,
        !isCall, isLong);
    final ForexOptionDigital forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE);
    final ForexOptionDigital forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount currencyExposureCall = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionCall, SMILE_MULTICURVES);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionPut, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvCash = forexOptionPut.getUnderlyingForex().getPaymentCurrency2().accept(PVDC, MULTICURVES);
    assertEquals("Forex Digital option: currency exposure put/call parity foreign", 0,
        currencyExposureCall.getAmount(EUR) + currencyExposurePut.getAmount(EUR),
        TOLERANCE_PV);
    assertEquals("Forex Digital option: currency exposure put/call parity domestic", Math.abs(pvCash.getAmount(USD)),
        currencyExposureCall.getAmount(USD)
            + currencyExposurePut.getAmount(USD),
        TOLERANCE_PV);
  }

  /**
   * Tests the put/call parity currency exposure.
   */
  @Test
  public void currencyExposurePutCallParityForeign() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition forexOptionDefinitionCall = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate,
        isCall, isLong, false);
    final ForexOptionDigitalDefinition forexOptionDefinitionPut = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate,
        !isCall, isLong, false);
    final ForexOptionDigital forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE);
    final ForexOptionDigital forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount currencyExposureCall = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionCall, SMILE_MULTICURVES);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionPut, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvCash = forexOptionPut.getUnderlyingForex().getPaymentCurrency1().accept(PVDC, MULTICURVES);
    assertEquals("Forex Digital option: currency exposure put/call parity foreign", 0,
        currencyExposureCall.getAmount(USD) + currencyExposurePut.getAmount(USD),
        TOLERANCE_PV);
    assertEquals("Forex Digital option: currency exposure put/call parity domestic", Math.abs(pvCash.getAmount(EUR)),
        currencyExposureCall.getAmount(EUR)
            + currencyExposurePut.getAmount(EUR),
        TOLERANCE_PV);
  }

  /**
   * Tests the gamma for Forex option. Payment in domestic currency.
   */
  @Test
  public void gammaDomestic() {
    final double strikeM = STRIKE * (1 - CALL_SPREAD);
    final double strikeP = STRIKE * (1 + CALL_SPREAD);
    final double notional = Math.abs(FOREX.getPaymentCurrency2().getAmount()) / (strikeP - strikeM);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(notional),
        FOREX.getPaymentCurrency2().withAmount(-notional * strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(notional),
        FOREX.getPaymentCurrency2().withAmount(-notional * strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, !IS_LONG);
    final CurrencyAmount gammaP = METHOD_VANILLA_BLACK.gamma(vanillaP, SMILE_MULTICURVES, true);
    final CurrencyAmount gammaM = METHOD_VANILLA_BLACK.gamma(vanillaM, SMILE_MULTICURVES, true);
    final CurrencyAmount gammaExpected = gammaM.plus(gammaP);
    final CurrencyAmount gammaComputed = METHOD_DIGITAL_SPREAD.gamma(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - gamma", gammaExpected.getAmount(), gammaComputed.getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests the gamma for Forex option. Payment in foreign currency.
   */
  @Test
  public void gammaForeign() {
    final ForexOptionDigitalDefinition digitalForeignDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE,
        IS_CALL, IS_LONG, false);
    final ForexOptionDigital digitalForeign = digitalForeignDefinition.toDerivative(REFERENCE_DATE);
    final double strikeM = STRIKE * (1 - CALL_SPREAD);
    final double strikeP = STRIKE * (1 + CALL_SPREAD);
    final double amountPaid = Math.abs(digitalForeign.getUnderlyingForex().getPaymentCurrency1().getAmount());
    final double strikeRelM = 1.0 / strikeP;
    final double strikeRelP = 1.0 / strikeM;
    final double amount = amountPaid / (strikeRelP - strikeRelM);
    final Forex forexM = new Forex(digitalForeign.getUnderlyingForex().getPaymentCurrency2().withAmount(amount),
        digitalForeign.getUnderlyingForex()
            .getPaymentCurrency1().withAmount(-strikeRelM * amount));
    final Forex forexP = new Forex(digitalForeign.getUnderlyingForex().getPaymentCurrency2().withAmount(amount),
        digitalForeign.getUnderlyingForex()
            .getPaymentCurrency1().withAmount(-strikeRelP * amount));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, digitalForeign.getExpirationTime(), !IS_CALL, false);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, digitalForeign.getExpirationTime(), !IS_CALL, true);
    final CurrencyAmount gammaP = METHOD_VANILLA_BLACK.gamma(vanillaP, SMILE_MULTICURVES, false);
    final CurrencyAmount gammaM = METHOD_VANILLA_BLACK.gamma(vanillaM, SMILE_MULTICURVES, false);
    final CurrencyAmount gammaExpected = gammaM.plus(gammaP);
    final CurrencyAmount gammaComputed = METHOD_DIGITAL_SPREAD.gamma(digitalForeign, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - gamma", gammaExpected.getAmount(), gammaComputed.getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests the gamma for Forex option. Payment in domestic currency.
   */
  @Test
  public void gammaSpotDomestic() {
    final double strikeM = STRIKE * (1 - CALL_SPREAD);
    final double strikeP = STRIKE * (1 + CALL_SPREAD);
    final double notional = Math.abs(FOREX.getPaymentCurrency2().getAmount()) / (strikeP - strikeM);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(notional),
        FOREX.getPaymentCurrency2().withAmount(-notional * strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(notional),
        FOREX.getPaymentCurrency2().withAmount(-notional * strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, !IS_LONG);
    final CurrencyAmount gammaP = METHOD_VANILLA_BLACK.gammaSpot(vanillaP, SMILE_MULTICURVES, true);
    final CurrencyAmount gammaM = METHOD_VANILLA_BLACK.gammaSpot(vanillaM, SMILE_MULTICURVES, true);
    final CurrencyAmount gammaExpected = gammaM.plus(gammaP);
    final CurrencyAmount gammaComputed = METHOD_DIGITAL_SPREAD.gammaSpot(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - gamma spot", gammaExpected.getAmount(), gammaComputed.getAmount(),
        TOLERANCE_PV);
  }

  /**
   * Tests the gamma for Forex option. Payment in foreign currency.
   */
  @Test
  public void gammaSpotForeign() {
    final ForexOptionDigitalDefinition digitalForeignDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE,
        IS_CALL, IS_LONG, false);
    final ForexOptionDigital digitalForeign = digitalForeignDefinition.toDerivative(REFERENCE_DATE);
    final double strikeM = STRIKE * (1 - CALL_SPREAD);
    final double strikeP = STRIKE * (1 + CALL_SPREAD);
    final double amountPaid = Math.abs(digitalForeign.getUnderlyingForex().getPaymentCurrency1().getAmount());
    final double strikeRelM = 1.0 / strikeP;
    final double strikeRelP = 1.0 / strikeM;
    final double amount = amountPaid / (strikeRelP - strikeRelM);
    final Forex forexM = new Forex(digitalForeign.getUnderlyingForex().getPaymentCurrency2().withAmount(amount),
        digitalForeign.getUnderlyingForex()
            .getPaymentCurrency1().withAmount(-strikeRelM * amount));
    final Forex forexP = new Forex(digitalForeign.getUnderlyingForex().getPaymentCurrency2().withAmount(amount),
        digitalForeign.getUnderlyingForex()
            .getPaymentCurrency1().withAmount(-strikeRelP * amount));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, digitalForeign.getExpirationTime(), !IS_CALL, false);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, digitalForeign.getExpirationTime(), !IS_CALL, true);
    final CurrencyAmount gammaP = METHOD_VANILLA_BLACK.gammaSpot(vanillaP, SMILE_MULTICURVES, false);
    final CurrencyAmount gammaM = METHOD_VANILLA_BLACK.gammaSpot(vanillaM, SMILE_MULTICURVES, false);
    final CurrencyAmount gammaExpected = gammaM.plus(gammaP);
    final CurrencyAmount gammaComputed = METHOD_DIGITAL_SPREAD.gammaSpot(digitalForeign, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - gamma spot", gammaExpected.getAmount(), gammaComputed.getAmount(),
        TOLERANCE_PV);
  }

  /**
   * Tests the relative gamma for Forex option.
   */
  @Test
  public void gammaRelative() {
    final CurrencyAmount gamma = METHOD_DIGITAL_SPREAD.gamma(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    final double gammaRelativeExpected = gamma.getAmount()
        / Math.abs(FOREX_DIGITAL_CALL_DOM.getUnderlyingForex().getPaymentCurrency2().getAmount());
    final double gammaRelativeComputed = METHOD_DIGITAL_SPREAD.gammaRelative(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex: relative gamma", gammaRelativeExpected, gammaRelativeComputed, TOLERANCE_RELATIVE);
  }

  /**
   * Tests the present value curve sensitivity.
   */
  @Test
  public void presentValueCurveSensitivity() {
    final double spread = 0.0001; // Relative spread.
    final double strikeM = STRIKE * (1 - spread);
    final double strikeP = STRIKE * (1 + spread);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, !IS_LONG);
    final MultipleCurrencyMulticurveSensitivity pvcsP = METHOD_VANILLA_BLACK.presentValueCurveSensitivity(vanillaP, SMILE_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsM = METHOD_VANILLA_BLACK.presentValueCurveSensitivity(vanillaM, SMILE_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsExpected = pvcsM.plus(pvcsP).multipliedBy(
        1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final MultipleCurrencyMulticurveSensitivity pvcsComputed = METHOD_DIGITAL_SPREAD.presentValueCurveSensitivity(FOREX_DIGITAL_CALL_DOM,
        SMILE_MULTICURVES);
    AssertSensitivityObjects.assertEquals("Forex Digital option: call spread method - present value", pvcsExpected, pvcsComputed,
        TOLERANCE_DELTA);
  }

  /**
   * Tests the present value curve sensitivity.
   */
  @Test
  public void presentValueBlackVolatilitySensitivity() {
    final double strikeM = STRIKE * (1 - CALL_SPREAD);
    final double strikeP = STRIKE * (1 + CALL_SPREAD);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final PresentValueForexBlackVolatilitySensitivity pvbvP = METHOD_VANILLA_BLACK.presentValueBlackVolatilitySensitivity(vanillaP,
        SMILE_MULTICURVES);
    final PresentValueForexBlackVolatilitySensitivity pvbvM = METHOD_VANILLA_BLACK.presentValueBlackVolatilitySensitivity(vanillaM,
        SMILE_MULTICURVES);
    final PresentValueForexBlackVolatilitySensitivity pvbvExpected = pvbvM.plus(pvbvP.multipliedBy(-1.0)).multipliedBy(
        1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final PresentValueForexBlackVolatilitySensitivity pvbvComputed = METHOD_DIGITAL_SPREAD.presentValueBlackVolatilitySensitivity(
        FOREX_DIGITAL_CALL_DOM,
        SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - present value volatility sensitivity", pvbvComputed.getVega().getMap().size(),
        2);
    assertTrue("Forex Digital option: call spread method - present value volatility sensitivity",
        PresentValueForexBlackVolatilitySensitivity.compare(pvbvExpected, pvbvComputed, TOLERANCE_DELTA));
  }

  /**
   * Tests the present value. Spread change.
   */
  @Test
  public void presentValueSpreadChange() {
    final double spread1 = 0.0001;
    final double spread2 = 0.0002;
    final ForexOptionDigitalCallSpreadBlackSmileMethod methodCallSpreadBlack1 = new ForexOptionDigitalCallSpreadBlackSmileMethod(spread1);
    final ForexOptionDigitalCallSpreadBlackSmileMethod methodCallSpreadBlack2 = new ForexOptionDigitalCallSpreadBlackSmileMethod(spread2);
    final MultipleCurrencyAmount pv1 = methodCallSpreadBlack1.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pv2 = methodCallSpreadBlack2.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: call spread method - present value", pv1.getAmount(USD), pv2.getAmount(USD), 10.0);
    final MultipleCurrencyAmount pvBlack = METHOD_DIGITAL_BLACK.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertNotEquals(pvBlack.getAmount(USD), pv2.getAmount(USD), 10.0);
  }

  /**
   * Tests present value volatility node sensitivity.
   */
  @Test
  public void presentValueBlackVolatilityNodeSensitivity() {
    final double strikeM = STRIKE * (1 - CALL_SPREAD);
    final double strikeP = STRIKE * (1 + CALL_SPREAD);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensi = METHOD_DIGITAL_SPREAD
        .presentValueBlackVolatilityNodeSensitivity(FOREX_DIGITAL_CALL_DOM,
            SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: vega node size", SMILE_TERM.getNumberExpiration(), sensi.getVega().getData().length);
    assertEquals("Forex vanilla option: vega node size", SMILE_TERM.getNumberStrike(), sensi.getVega().getData()[0].length);
    final Pair<Currency, Currency> currencyPair = Pairs.of(EUR, USD);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = METHOD_DIGITAL_SPREAD.presentValueBlackVolatilitySensitivity(
        FOREX_DIGITAL_CALL_DOM,
        SMILE_MULTICURVES);
    final double df = MULTICURVES.getDiscountFactor(USD, TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * MULTICURVES.getDiscountFactor(EUR, TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE)) / df;
    final VolatilityAndBucketedSensitivities volAndSensitivitiesDown = SMILE_TERM.getVolatilityAndSensitivities(
        FOREX_DIGITAL_CALL_DOM.getExpirationTime(), strikeM,
        forward);
    final VolatilityAndBucketedSensitivities volAndSensitivitiesUp = SMILE_TERM.getVolatilityAndSensitivities(
        FOREX_DIGITAL_CALL_DOM.getExpirationTime(), strikeP,
        forward);
    final double[][] nodeWeightM = volAndSensitivitiesDown.getBucketedSensitivities();
    final double[][] nodeWeightP = volAndSensitivitiesUp.getBucketedSensitivities();
    final DoublesPair pointM = DoublesPair.of(FOREX_DIGITAL_CALL_DOM.getExpirationTime(), strikeM);
    final DoublesPair pointP = DoublesPair.of(FOREX_DIGITAL_CALL_DOM.getExpirationTime(), strikeP);
    for (int i = 0; i < SMILE_TERM.getNumberExpiration(); i++) {
      for (int j = 0; j < SMILE_TERM.getNumberStrike(); j++) {
        assertEquals("Forex vanilla digital: vega node", nodeWeightM[i][j] * pointSensitivity.getVega().getMap().get(pointM)
            + nodeWeightP[i][j] * pointSensitivity.getVega().getMap().get(pointP),
            sensi.getVega().getData()[i][j], TOLERANCE_DELTA);
      }
    }
  }

  /**
   * Analyzes the profile for digital options.
   */
  @Test(enabled = false)
  public void profile() {
    final Interpolator1D interpolator = NamedInterpolator1dFactory.of(DoubleQuadraticInterpolator1dAdapter.NAME,
        LinearExtrapolator1dAdapter.NAME,
        LinearExtrapolator1dAdapter.NAME);
    final SmileDeltaTermStructureParametersStrikeInterpolation smileTerm = FXDataSets.smile5points(REFERENCE_DATE, interpolator);
    final BlackForexSmileProviderDiscount smile = new BlackForexSmileProviderDiscount(MULTICURVES, smileTerm, Pairs.of(EUR, USD));

    final int n = 100;
    final double range = 0.40; // Spot = 1.40
    final double shift = 2 * range / n;
    final double[] strike = new double[n + 1];
    final ForexOptionDigital[] forexOptionDigital = new ForexOptionDigital[n + 1];
    final ForexOptionVanilla[] forexOptionVanilla = new ForexOptionVanilla[n + 1];
    for (int i = 0; i <= n; i++) {
      strike[i] = SPOT - range + i * shift;
      final ForexDefinition forexDefinitonUSD = ForexDefinition.fromAmounts(EUR, USD, OPTION_EXP_DATE, -NOTIONAL / strike[i],
          NOTIONAL);
      final ForexDefinition forexDefinitonEUR = ForexDefinition.fromAmounts(EUR, USD, OPTION_EXP_DATE, -1.0, strike[i]);
      final ForexOptionDigitalDefinition forexOptionDigitalDefiniton = new ForexOptionDigitalDefinition(forexDefinitonUSD, OPTION_EXP_DATE,
          IS_CALL, IS_LONG);
      final ForexOptionVanillaDefinition forexOptionVanillaDefiniton = new ForexOptionVanillaDefinition(forexDefinitonEUR, OPTION_EXP_DATE,
          IS_CALL, IS_LONG);
      forexOptionDigital[i] = forexOptionDigitalDefiniton.toDerivative(REFERENCE_DATE);
      forexOptionVanilla[i] = forexOptionVanillaDefiniton.toDerivative(REFERENCE_DATE);
    }
    final double[] pvDigitalSpread = new double[n + 1];
    final double[] pvDigitalBlack = new double[n + 1];
    final double[] pvVanillaBlack = new double[n + 1];
    final double[] gammaDigitalSpread = new double[n + 1];
    for (int i = 0; i <= n; i++) {
      pvDigitalSpread[i] = METHOD_DIGITAL_SPREAD.presentValue(forexOptionDigital[i], smile).getAmount(USD);
      pvDigitalBlack[i] = METHOD_DIGITAL_BLACK.presentValue(forexOptionDigital[i], smile).getAmount(USD);
      pvVanillaBlack[i] = METHOD_VANILLA_BLACK.presentValue(forexOptionVanilla[i], smile).getAmount(USD);
      gammaDigitalSpread[i] = METHOD_DIGITAL_SPREAD.gamma(forexOptionDigital[i], smile).getAmount();
    }

    final double dfDomestic = MULTICURVES.getDiscountFactor(USD, forexOptionDigital[0].getUnderlyingForex().getPaymentTime());
    final double dfForeign = MULTICURVES.getDiscountFactor(EUR, forexOptionDigital[0].getUnderlyingForex().getPaymentTime());
    final double forward = SPOT * dfForeign / dfDomestic;
    final double[] volBlack = new double[n + 1];
    for (int i = 0; i <= n; i++) {
      volBlack[i] = smile.getVolatility(EUR, USD, forexOptionDigital[i].getExpirationTime(), strike[i], forward);
    }
    final double[] density = new double[n - 1];
    for (int i = 0; i < n - 1; i++) {
      density[i] = (pvVanillaBlack[i + 2] + pvVanillaBlack[i] - 2 * pvVanillaBlack[i + 1])
          / (shift * shift);
    }
  }

}
