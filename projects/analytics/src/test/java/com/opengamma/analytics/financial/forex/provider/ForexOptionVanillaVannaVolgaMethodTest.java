/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

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
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProvider;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexVannaVolgaProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.DoubleQuadraticInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionVanillaVannaVolgaMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountForexDataSets.createMulticurvesForex();

  private static final FXMatrix FX_MATRIX = MULTICURVES.getFxRates();
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final double SPOT = FX_MATRIX.getFxRate(EUR, USD);
  // General
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  // Smile data
  private static final Period[] EXPIRY_PERIOD = new Period[] { Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1),
      Period.ofYears(2), Period.ofYears(5) };
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final ZonedDateTime REFERENCE_SPOT = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime[] PAY_DATE = new ZonedDateTime[NB_EXP];
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXP];
  private static final double[] TIME_TO_EXPIRY = new double[NB_EXP + 1];
  static {
    TIME_TO_EXPIRY[0] = 0.0;
    for (int i = 0; i < NB_EXP; i++) {
      PAY_DATE[i] = ScheduleCalculator.getAdjustedDate(REFERENCE_SPOT, EXPIRY_PERIOD[i], BUSINESS_DAY, CALENDAR);
      EXPIRY_DATE[i] = ScheduleCalculator.getAdjustedDate(PAY_DATE[i], -SETTLEMENT_DAYS, CALENDAR);
      TIME_TO_EXPIRY[i + 1] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRY_DATE[i]);
    }
  }
  private static final double[] ATM = { 0.11, 0.115, 0.12, 0.12, 0.125, 0.13 };
  private static final double[] DELTA = new double[] { 0.25 };
  private static final double[][] RISK_REVERSAL = new double[][] { { 0.015 }, { 0.020 }, { 0.025 }, { 0.03 }, { 0.025 }, { 0.030 } };
  private static final double[][] STRANGLE = new double[][] { { 0.002 }, { 0.003 }, { 0.004 }, { 0.0045 }, { 0.0045 }, { 0.0045 } };
  private static final double[][] RISK_REVERSAL_FLAT = new double[][] { { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 } };
  private static final double[][] STRANGLE_FLAT = new double[][] { { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 } };
  private static final Interpolator1D INTERPOLATOR_STRIKE = NamedInterpolator1dFactory.of(DoubleQuadraticInterpolator1dAdapter.NAME,
      LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
  private static final SmileDeltaTermStructureParameters SMILE_TERM = new SmileDeltaTermStructureParameters(TIME_TO_EXPIRY, DELTA, ATM,
      RISK_REVERSAL, STRANGLE);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_STRIKE_INT = new SmileDeltaTermStructureParametersStrikeInterpolation(
      TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE, INTERPOLATOR_STRIKE);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_STRIKE_INT_FLAT = new SmileDeltaTermStructureParametersStrikeInterpolation(
      TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL_FLAT, STRANGLE_FLAT, INTERPOLATOR_STRIKE);

  // Methods and curves
  private static final ForexOptionVanillaVannaVolgaMethod METHOD_VANNA_VOLGA = ForexOptionVanillaVannaVolgaMethod.getInstance();
  private static final ForexOptionVanillaBlackSmileMethod METHOD_BLACK = ForexOptionVanillaBlackSmileMethod.getInstance();

  private static final ForexDiscountingMethod METHOD_DISC = ForexDiscountingMethod.getInstance();

  private static final BlackForexSmileProvider SMILE_MULTICURVES = new BlackForexSmileProvider(MULTICURVES, SMILE_TERM_STRIKE_INT,
      Pairs.of(EUR, USD));
  private static final BlackForexSmileProvider SMILE_FLAT_MULTICURVES = new BlackForexSmileProvider(MULTICURVES, SMILE_TERM_STRIKE_INT_FLAT,
      Pairs.of(EUR, USD));
  private static final BlackForexVannaVolgaProvider VANNAVOLGA_MULTICURVES = new BlackForexVannaVolgaProvider(MULTICURVES, SMILE_TERM,
      Pairs.of(EUR, USD));
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E-0;
  private static final double TOLERANCE_W = 1.0E-10;

  /**
   * Tests put/call parity.
   */
  @Test
  public void putCallParity() {
    final int nStrike = 20;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nStrike + 1];
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] call = new ForexOptionVanilla[nStrike + 1];
    final ForexOptionVanilla[] put = new ForexOptionVanilla[nStrike + 1];
    for (int i = 0; i <= nStrike; i++) {
      strikes[i] = strikeMin + i * strikeRange / nStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      final ForexOptionVanillaDefinition callDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall,
          isLong);
      final ForexOptionVanillaDefinition putDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, !isCall,
          !isLong);
      call[i] = callDefinition.toDerivative(REFERENCE_DATE);
      put[i] = putDefinition.toDerivative(REFERENCE_DATE);
      final Forex forexForward = forexUnderlyingDefinition.toDerivative(REFERENCE_DATE);
      // Present value
      final MultipleCurrencyAmount pvCall = METHOD_VANNA_VOLGA.presentValue(call[i], VANNAVOLGA_MULTICURVES);
      final MultipleCurrencyAmount pvPut = METHOD_VANNA_VOLGA.presentValue(put[i], VANNAVOLGA_MULTICURVES);
      final MultipleCurrencyAmount pvForward = METHOD_DISC.presentValue(forexForward, MULTICURVES);
      assertEquals("Forex vanilla option: vanna-volga present value put/call parity",
          pvForward.getAmount(USD) + pvForward.getAmount(EUR) * SPOT, pvCall.getAmount(USD)
              + pvPut.getAmount(USD),
          TOLERANCE_PV);
      // Currency exposure
      final MultipleCurrencyAmount ceCall = METHOD_VANNA_VOLGA.currencyExposure(call[i], VANNAVOLGA_MULTICURVES);
      final MultipleCurrencyAmount cePut = METHOD_VANNA_VOLGA.currencyExposure(put[i], VANNAVOLGA_MULTICURVES);
      final MultipleCurrencyAmount ceForward = METHOD_DISC.currencyExposure(forexForward, MULTICURVES);
      assertEquals("Forex vanilla option: vanna-volga currency exposure put/call parity", ceForward.getAmount(USD),
          ceCall.getAmount(USD) + cePut.getAmount(USD),
          TOLERANCE_PV);
      assertEquals("Forex vanilla option: vanna-volga currency exposure put/call parity", ceForward.getAmount(EUR),
          ceCall.getAmount(EUR) + cePut.getAmount(EUR),
          TOLERANCE_PV);
      // Vega
      final PresentValueForexBlackVolatilitySensitivity pvbsCall = METHOD_VANNA_VOLGA
          .presentValueBlackVolatilitySensitivity(call[i], VANNAVOLGA_MULTICURVES);
      final PresentValueForexBlackVolatilitySensitivity pvbsPut = METHOD_VANNA_VOLGA.presentValueBlackVolatilitySensitivity(put[i],
          VANNAVOLGA_MULTICURVES);
      assertTrue(
          "Forex vanilla option: vanna-volga sensitivity put/call parity - strike " + i,
          PresentValueForexBlackVolatilitySensitivity.compare(pvbsCall.plus(pvbsPut),
              new PresentValueForexBlackVolatilitySensitivity(EUR, USD, SurfaceValue.from(DoublesPair.of(0.0d, 0.0d), 0.0d)),
              TOLERANCE_PV));
      // Curve sensitivty
      final MultipleCurrencyMulticurveSensitivity pvcsCall = METHOD_VANNA_VOLGA.presentValueCurveSensitivity(call[i],
          VANNAVOLGA_MULTICURVES);
      final MultipleCurrencyMulticurveSensitivity pvcsPut = METHOD_VANNA_VOLGA.presentValueCurveSensitivity(put[i],
          VANNAVOLGA_MULTICURVES);
      final MultipleCurrencyMulticurveSensitivity pvcsForward = METHOD_DISC.presentValueCurveSensitivity(forexForward, MULTICURVES)
          .converted(USD, FX_MATRIX);
      final MultipleCurrencyMulticurveSensitivity pvcsOpt = pvcsCall.plus(pvcsPut).cleaned();
      AssertSensitivityObjects.assertEquals("Forex vanilla option: vanna-volga curve sensitivity put/call parity", pvcsForward, pvcsOpt,
          TOLERANCE_PV_DELTA);
    }
  }

  /**
   * Tests vanna-volga weights.
   */
  @Test
  public void weight() {
    final int nStrike = 10;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nStrike + 1];
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nStrike + 1];
    for (int i = 0; i <= nStrike; i++) {
      strikes[i] = strikeMin + i * strikeRange / nStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry,
          isCall, isLong);
      forexOption[i] = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    }
    final double forward = METHOD_BLACK.forwardForexRate(forexOption[0], MULTICURVES);
    final double dfDomestic = MULTICURVES.getDiscountFactor(USD, forexOption[0].getUnderlyingForex().getPaymentTime()); // USD
    final SmileDeltaParameters smileAtTime = VANNAVOLGA_MULTICURVES.getSmile(EUR, USD, forexOption[0].getTimeToExpiry());
    final double[] strikesVV = smileAtTime.getStrike(forward);
    final double[] volVV = smileAtTime.getVolatility();
    for (int i = 0; i <= nStrike; i++) {
      final double[] weightsComputed = METHOD_VANNA_VOLGA.vannaVolgaWeights(forexOption[i], forward, dfDomestic, strikesVV, volVV);
      final double[] vega = new double[3];
      final BlackFunctionData dataBlackATM = new BlackFunctionData(forward, dfDomestic, volVV[1]);
      for (int j = 0; j < 3; j++) {
        final EuropeanVanillaOption optionVV = new EuropeanVanillaOption(strikesVV[j], forexOption[i].getTimeToExpiry(),
            true);
        vega[j] = BLACK_FUNCTION.getVegaFunction(optionVV).evaluate(dataBlackATM);
      }
      final double vegaFlat = BLACK_FUNCTION.getVegaFunction(forexOption[i]).evaluate(dataBlackATM);
      vega[1] = vegaFlat;
      final double lnk21 = Math.log(strikesVV[1] / strikesVV[0]);
      final double lnk31 = Math.log(strikesVV[2] / strikesVV[0]);
      final double lnk32 = Math.log(strikesVV[2] / strikesVV[1]);
      final double[] lnk = new double[3];
      for (int j = 0; j < 3; j++) {
        lnk[j] = Math.log(strikesVV[j] / strikes[i]);
      }
      final double[] weightExpected = new double[3];
      weightExpected[0] = vegaFlat * lnk[1] * lnk[2] / (vega[0] * lnk21 * lnk31);
      weightExpected[1] = -vegaFlat * lnk[0] * lnk[2] / (vega[1] * lnk21 * lnk32);
      weightExpected[2] = vegaFlat * lnk[0] * lnk[1] / (vega[2] * lnk31 * lnk32);
      for (int j = 0; j < 3; j = j + 2) {
        assertEquals("Vanna-volga: adjustment weights", weightExpected[j], weightsComputed[j], TOLERANCE_W);
      }
    }
  }

  /**
   * Tests the method with hard-coded values.
   */
  @Test
  public void presentValueHardCoded() {
    final int nStrike = 10;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nStrike + 1];
    final double[] pvExpected = new double[] { 3.860405407112769E7, 3.0897699603079587E7, 2.3542824458812844E7, 1.6993448607300103E7,
        1.1705393621236656E7, 7865881.826,
        5312495.846, 3680367.677, 2607701.430, 1849818.30, 1282881.98 };
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nStrike + 1];
    for (int i = 0; i <= nStrike; i++) {
      strikes[i] = strikeMin + i * strikeRange / nStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry,
          isCall, isLong);
      forexOption[i] = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    }
    final double[] pvVV = new double[nStrike + 1];
    for (int i = 0; i <= nStrike; i++) {
      pvVV[i] = METHOD_VANNA_VOLGA.presentValue(forexOption[i], VANNAVOLGA_MULTICURVES).getAmount(USD);
      assertEquals("Forex vanilla option: present value vanna-volga / hard-coded", pvExpected[i], pvVV[i], TOLERANCE_PV);
    }
  }

  /**
   * Check the price implied by the vanna-volga method and compares it to the market prices at the market data points.
   */
  @Test
  public void presentValueAtMarketData() {
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingSpotDefinition = new ForexDefinition(EUR, USD, optionPay, notional, SPOT);
    final ForexOptionVanillaDefinition forexOptionSpotDefinition = new ForexOptionVanillaDefinition(forexUnderlyingSpotDefinition,
        optionExpiry, isCall, isLong);
    final ForexOptionVanilla forexOptionSpot = forexOptionSpotDefinition.toDerivative(REFERENCE_DATE);
    final double forward = METHOD_BLACK.forwardForexRate(forexOptionSpot, MULTICURVES);
    final SmileDeltaParameters smileTime = SMILE_TERM.getSmileForTime(forexOptionSpot.getTimeToExpiry());
    final double[] strikes = smileTime.getStrike(forward);
    final int nStrike = strikes.length;
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nStrike];
    for (int i = 0; i < nStrike; i++) {
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry,
          isCall, isLong);
      forexOption[i] = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    }
    final double[] pvVV = new double[nStrike];
    final double[] pvInt = new double[nStrike];
    for (int i = 0; i < nStrike; i++) {
      pvVV[i] = METHOD_VANNA_VOLGA.presentValue(forexOption[i], VANNAVOLGA_MULTICURVES).getAmount(USD);
      pvInt[i] = METHOD_BLACK.presentValue(forexOption[i], SMILE_MULTICURVES).getAmount(USD);
      assertEquals("Forex vanilla option: currency exposure put/call parity domestic", pvInt[i], pvVV[i], TOLERANCE_PV);
    }
  }

  /**
   * Tests the currency exposure in the Vanna-Volga method.
   */
  @Test
  public void currencyExposure() {
    final int nStrike = 10;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nStrike + 1];
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nStrike + 1];
    for (int i = 0; i <= nStrike; i++) {
      strikes[i] = strikeMin + i * strikeRange / nStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry,
          isCall, isLong);
      forexOption[i] = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    }
    final double forward = METHOD_BLACK.forwardForexRate(forexOption[0], MULTICURVES);
    final double dfDomestic = MULTICURVES.getDiscountFactor(USD, forexOption[0].getUnderlyingForex().getPaymentTime()); // USD
    final SmileDeltaParameters smileAtTime = VANNAVOLGA_MULTICURVES.getSmile(EUR, USD, forexOption[0].getTimeToExpiry());
    final double[] strikesVV = smileAtTime.getStrike(forward);
    final double[] volVV = smileAtTime.getVolatility();
    final ForexOptionVanilla[] optReference = new ForexOptionVanilla[3];
    for (int i = 0; i < 3; i++) {
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikesVV[i]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry,
          isCall, isLong);
      optReference[i] = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    }
    final MultipleCurrencyAmount[] ceVV = new MultipleCurrencyAmount[nStrike + 1];
    final MultipleCurrencyAmount[] ceFlat = new MultipleCurrencyAmount[nStrike + 1];
    final MultipleCurrencyAmount[] ceExpected = new MultipleCurrencyAmount[nStrike + 1];
    final MultipleCurrencyAmount[] ceVVATM = new MultipleCurrencyAmount[3];
    final MultipleCurrencyAmount[] ceVVsmile = new MultipleCurrencyAmount[3];
    final MultipleCurrencyAmount[] ceVVadj = new MultipleCurrencyAmount[3];
    for (int i = 0; i < 3; i++) {
      ceVVATM[i] = METHOD_BLACK.currencyExposure(optReference[i], SMILE_FLAT_MULTICURVES);
      ceVVsmile[i] = METHOD_BLACK.currencyExposure(optReference[i], SMILE_MULTICURVES);
      ceVVadj[i] = ceVVsmile[i].plus(ceVVATM[i].multipliedBy(-1.0));
    }
    for (int i = 0; i <= nStrike; i++) {
      ceVV[i] = METHOD_VANNA_VOLGA.currencyExposure(forexOption[i], VANNAVOLGA_MULTICURVES);
      final double[] weights = METHOD_VANNA_VOLGA.vannaVolgaWeights(forexOption[i], forward, dfDomestic, strikesVV, volVV);
      ceFlat[i] = METHOD_BLACK.currencyExposure(forexOption[i], SMILE_FLAT_MULTICURVES);
      ceExpected[i] = ceFlat[i];
      for (int j = 0; j < 3; j++) {
        ceExpected[i] = ceExpected[i].plus(ceVVadj[j].multipliedBy(weights[j]));
      }
      assertEquals("Forex vanilla option: currency exposure vanna-volga", ceExpected[i].getAmount(EUR),
          ceVV[i].getAmount(EUR), TOLERANCE_PV);
      assertEquals("Forex vanilla option: currency exposure vanna-volga", ceExpected[i].getAmount(USD),
          ceVV[i].getAmount(USD), TOLERANCE_PV);
    }

  }

  /**
   * Compare results with the Black results. They should be different but not too much.
   */
  @Test
  public void comparisonBlack() {

    final int nStrike = 20;
    final double strikeMin = 1.00;
    final double strikeRange = 0.50;
    final double[] strikes = new double[nStrike + 1];

    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nStrike + 1];
    for (int i = 0; i <= nStrike; i++) {
      strikes[i] = strikeMin + i * strikeRange / nStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry,
          isCall, isLong);
      forexOption[i] = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    }
    final double[] pvVV = new double[nStrike + 1];
    final double[] pvInt = new double[nStrike + 1];
    for (int i = 0; i <= nStrike; i++) {
      pvVV[i] = METHOD_VANNA_VOLGA.presentValue(forexOption[i], VANNAVOLGA_MULTICURVES).getAmount(USD);
      pvInt[i] = METHOD_BLACK.presentValue(forexOption[i], SMILE_MULTICURVES).getAmount(USD);
      assertEquals("Forex vanilla option: present value vanna-volga vs Black " + i, 1, pvVV[i] / pvInt[i], 0.15);
    }
    final MultipleCurrencyAmount[] ceVV = new MultipleCurrencyAmount[nStrike + 1];
    final MultipleCurrencyAmount[] ceInt = new MultipleCurrencyAmount[nStrike + 1];
    for (int i = 0; i <= nStrike; i++) {
      ceVV[i] = METHOD_VANNA_VOLGA.currencyExposure(forexOption[i], VANNAVOLGA_MULTICURVES);
      ceInt[i] = METHOD_BLACK.currencyExposure(forexOption[i], SMILE_MULTICURVES);
      assertEquals("Forex vanilla option: currency exposure vanna-volga vs Black " + i, 1,
          ceVV[i].getAmount(EUR) / ceInt[i].getAmount(EUR),
          0.15);
    }

    final MultipleCurrencyMulticurveSensitivity[] pvcsVV = new MultipleCurrencyMulticurveSensitivity[nStrike + 1];
    final MultipleCurrencyMulticurveSensitivity[] pvcsInt = new MultipleCurrencyMulticurveSensitivity[nStrike + 1];
    for (int i = 0; i <= nStrike; i++) {
      pvcsVV[i] = METHOD_VANNA_VOLGA.presentValueCurveSensitivity(forexOption[i], VANNAVOLGA_MULTICURVES);
      pvcsInt[i] = METHOD_BLACK.presentValueCurveSensitivity(forexOption[i], SMILE_MULTICURVES);
      AssertSensitivityObjects.assertEquals("Forex vanilla option: curve sensitivity vanna-volga vs Black " + i,
          pvcsVV[i], pvcsInt[i], 3.0E+6);
    }
  }

  /**
   * Analyzes the smile implied by the vanna-volga method and compares it to a quadratic interpolation/linear extrapolation. Used to produce
   * the graphs of the documentation.
   */
  @Test(enabled = false)
  public void analysisSmileCall() {

    final int nbStrike = 50;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nbStrike + 1];

    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    for (int i = 0; i <= nbStrike; i++) {
      strikes[i] = strikeMin + i * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry,
          isCall, isLong);
      forexOption[i] = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    }
    final double[] pvVV = new double[nbStrike + 1];
    final double[] pvInt = new double[nbStrike + 1];
    final double[] volVV = new double[nbStrike + 1];
    final double[] volInt = new double[nbStrike + 1];
    for (int i = 0; i <= nbStrike; i++) {
      pvVV[i] = METHOD_VANNA_VOLGA.presentValue(forexOption[i], VANNAVOLGA_MULTICURVES).getAmount(USD);
      pvInt[i] = METHOD_BLACK.presentValue(forexOption[i], SMILE_MULTICURVES).getAmount(USD);
      final double forward = METHOD_BLACK.forwardForexRate(forexOption[i], MULTICURVES);
      final double df = MULTICURVES.getDiscountFactor(USD, forexOption[i].getUnderlyingForex().getPaymentTime());
      volVV[i] = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(forward, df, 0.20), forexOption[i],
          pvVV[i] / notional);
      volInt[i] = METHOD_BLACK.impliedVolatility(forexOption[i], SMILE_MULTICURVES);
    }
  }

  /**
   * Analyzes the vega for different strikes. Used to produce the graphs of the documentation.
   */
  @Test(enabled = false)
  public void analysisVega() {

    final int nbStrike = 50;
    final double strikeMin = 0.85;
    final double strikeRange = 1.00;
    final double[] strikes = new double[nbStrike + 1];

    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    for (int i = 0; i <= nbStrike; i++) {
      strikes[i] = strikeMin + i * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry,
          isCall, isLong);
      forexOption[i] = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    }

    final double forward = METHOD_BLACK.forwardForexRate(forexOption[0], MULTICURVES);
    final SmileDeltaParameters smileTime = SMILE_TERM.getSmileForTime(forexOption[0].getTimeToExpiry());
    final double[] strikesVV = smileTime.getStrike(forward);

    final PresentValueForexBlackVolatilitySensitivity[] vegaObject = new PresentValueForexBlackVolatilitySensitivity[nbStrike + 1];
    final double[][] vegaVV = new double[3][nbStrike + 1];
    final double[] vegaBlack = new double[nbStrike + 1];
    for (int i = 0; i <= nbStrike; i++) {
      vegaObject[i] = METHOD_VANNA_VOLGA.presentValueBlackVolatilitySensitivity(forexOption[i], VANNAVOLGA_MULTICURVES);
      for (int j = 0; j < 3; j++) {
        final DoublesPair point = DoublesPair.of(forexOption[i].getTimeToExpiry(), strikesVV[j]);
        vegaVV[j][i] = vegaObject[i].getVega().getMap().get(point);
      }
      vegaBlack[i] = METHOD_BLACK.presentValueBlackVolatilitySensitivity(forexOption[i], SMILE_MULTICURVES)
          .toSingleValue().getAmount();
    }
  }

  /**
   * Analyzes the price implied by the vanna-volga method and compares it to the market prices at the market data points.
   */
  @Test(enabled = true)
  public void analysisAtData() {

    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingSpotDefinition = new ForexDefinition(EUR, USD, optionPay, notional, SPOT);
    final ForexOptionVanillaDefinition forexOptionSpotDefinition = new ForexOptionVanillaDefinition(forexUnderlyingSpotDefinition,
        optionExpiry, isCall, isLong);
    final ForexOptionVanilla forexOptionSpot = forexOptionSpotDefinition.toDerivative(REFERENCE_DATE);
    final double forward = METHOD_BLACK.forwardForexRate(forexOptionSpot, MULTICURVES);

    final SmileDeltaParameters smileTime = SMILE_TERM.getSmileForTime(forexOptionSpot.getTimeToExpiry());

    final double[] strikes = smileTime.getStrike(forward);
    final int nbStrike = strikes.length;
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike];
    for (int i = 0; i < nbStrike; i++) {
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry,
          isCall, isLong);
      forexOption[i] = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    }
    final double[] pvVV = new double[nbStrike];
    final double[] pvInt = new double[nbStrike];
    final double[] volVV = new double[nbStrike];
    final double[] volInt = new double[nbStrike];
    for (int i = 0; i < nbStrike; i++) {
      pvVV[i] = METHOD_VANNA_VOLGA.presentValue(forexOption[i], VANNAVOLGA_MULTICURVES).getAmount(USD);
      pvInt[i] = METHOD_BLACK.presentValue(forexOption[i], SMILE_MULTICURVES).getAmount(USD);
      // USD discounting
      final double df = MULTICURVES.getDiscountFactor(USD, forexOption[i].getUnderlyingForex().getPaymentTime());
      volVV[i] = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(forward, df, 0.20), forexOption[i],
          pvVV[i] / notional);
      volInt[i] = METHOD_BLACK.impliedVolatility(forexOption[i], SMILE_MULTICURVES);
    }
  }

  /**
   * Analyzes the performance of the vanna-volga method.
   */
  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000; // 1000

    final int nbStrike = 50;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nbStrike + 1];
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    final ForexOptionVanillaDefinition[] forexOptionDefinition = new ForexOptionVanillaDefinition[nbStrike + 1];
    for (int i = 0; i <= nbStrike; i++) {
      strikes[i] = strikeMin + i * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[i]);
      forexOptionDefinition[i] = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
    }
    final double[] pvVV = new double[nbStrike + 1];
    final double[] pvInt = new double[nbStrike + 1];
    final MultipleCurrencyAmount[] ceVV = new MultipleCurrencyAmount[nbStrike + 1];
    final MultipleCurrencyAmount[] ceInt = new MultipleCurrencyAmount[nbStrike + 1];
    final PresentValueForexBlackVolatilitySensitivity[] pvbsVV = new PresentValueForexBlackVolatilitySensitivity[nbStrike + 1];
    final PresentValueForexBlackVolatilitySensitivity[] pvbsInt = new PresentValueForexBlackVolatilitySensitivity[nbStrike + 1];
    final MultipleCurrencyMulticurveSensitivity[] pvcsVV = new MultipleCurrencyMulticurveSensitivity[nbStrike + 1];
    final MultipleCurrencyMulticurveSensitivity[] pvcsInt = new MultipleCurrencyMulticurveSensitivity[nbStrike + 1];

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      for (int j = 0; j <= nbStrike; j++) {
        forexOption[j] = forexOptionDefinition[j].toDerivative(REFERENCE_DATE);
        pvInt[j] = METHOD_BLACK.presentValue(forexOption[j], SMILE_MULTICURVES).getAmount(USD);
        ceInt[j] = METHOD_BLACK.currencyExposure(forexOption[j], SMILE_MULTICURVES);
        pvbsInt[j] = METHOD_BLACK.presentValueBlackVolatilitySensitivity(forexOption[j], SMILE_MULTICURVES);
        pvcsInt[j] = METHOD_BLACK.presentValueCurveSensitivity(forexOption[j], SMILE_MULTICURVES);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x " + (nbStrike + 1) + " vanilla forex options with Black: " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      for (int j = 0; j <= nbStrike; j++) {
        forexOption[j] = forexOptionDefinition[j].toDerivative(REFERENCE_DATE);
        pvVV[j] = METHOD_VANNA_VOLGA.presentValue(forexOption[j], VANNAVOLGA_MULTICURVES).getAmount(USD);
        ceVV[j] = METHOD_VANNA_VOLGA.currencyExposure(forexOption[j], VANNAVOLGA_MULTICURVES);
        pvbsVV[j] = METHOD_VANNA_VOLGA.presentValueBlackVolatilitySensitivity(forexOption[j], VANNAVOLGA_MULTICURVES);
        pvcsVV[j] = METHOD_VANNA_VOLGA.presentValueCurveSensitivity(forexOption[j], VANNAVOLGA_MULTICURVES);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x " + (nbStrike + 1) + " vanilla forex options with Vanna-Volga: " + (endTime - startTime) + " ms");
  }

}
