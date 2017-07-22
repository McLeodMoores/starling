/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.forex.provider;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.ImmutableFxMatrix;
import com.opengamma.analytics.financial.model.UncheckedMutableFxMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.calculator.blackforex.CurrencyExposureForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
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
 * Tests related to the valuation of non-deliverable options using the Black model.
 */
@Test(groups = TestGroup.UNIT)
public class ForexNonDeliverableOptionBlackSmileMethodTest {
  /** The calculator being tested */
  private static final ForexNonDeliverableOptionBlackSmileMethod CALCULATOR = ForexNonDeliverableOptionBlackSmileMethod.getInstance();
  /** Discounting curves */
  private static final MulticurveProviderDiscount CURVES = MulticurveProviderDiscountForexDataSets.createMulticurvesForex();
  /** The working day calendar */
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  /** The business day convention used for adjusting dates */
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  /** The number of settlement days */
  private static final int SETTLEMENT_DAYS = 2;
  /** KRW */
  private static final Currency KRW = Currency.of("KRW");
  /** The FX fixing date */
  private static final ZonedDateTime FX_FIXING_DATE = DateUtils.getUTCDate(2012, 5, 2);
  /** The payment date */
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2012, 5, 4);
  /** The EUR notional - the deliverable amount */
  private static final double EUR_NOTIONAL = 123;
  /** The KRWEUR strike */
  private static final double STRIKE = 1.600;
  /** The underlying non-deliverable forward definition */
  private static final ForexNonDeliverableForwardDefinition NDF_DEFINITION = new ForexNonDeliverableForwardDefinition(KRW, Currency.EUR, EUR_NOTIONAL, STRIKE, FX_FIXING_DATE,
      PAYMENT_DATE);
  /** The underlying deliverable forward equivalent */
  private static final ForexDefinition DELIVERABLE_EQUIVALENT_DEFINITION = new ForexDefinition(KRW, Currency.EUR, PAYMENT_DATE, -EUR_NOTIONAL * STRIKE, 1.0 / STRIKE);
  /** Is the option a call */
  private static final boolean IS_CALL = true;
  /** Is the option long */
  private static final boolean IS_LONG = true;
  /** The non-deliverable option definition */
  private static final ForexNonDeliverableOptionDefinition NDO_DEFINITION = new ForexNonDeliverableOptionDefinition(NDF_DEFINITION, IS_CALL, IS_LONG);
  /** The equivalent deliverable option definition */
  private static final ForexOptionVanillaDefinition DELIVERABLE_EQUIVALENT_OPTION_DEFINITION = new ForexOptionVanillaDefinition(DELIVERABLE_EQUIVALENT_DEFINITION, FX_FIXING_DATE, IS_CALL, IS_LONG);
  /** The reference date */
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2011, 11, 10);
  /** The non-deliverable option derivative */
  private static final ForexNonDeliverableOption NDO = NDO_DEFINITION.toDerivative(VALUATION_DATE);
  /** The equivalent deliverable option derivative */
  private static final ForexOptionVanilla DELIVERABLE_EQUIVALENT_OPTION = DELIVERABLE_EQUIVALENT_OPTION_DEFINITION.toDerivative(VALUATION_DATE);
  /** FX volatility expiry periods */
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(5)};
  /** The expiry times for the volatility surface */
  private static final double[] TIME_TO_EXPIRY = new double[EXPIRY_PERIOD.length + 1];
  static {
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(VALUATION_DATE, SETTLEMENT_DAYS, CALENDAR);
    final ZonedDateTime[] paymentDates = new ZonedDateTime[EXPIRY_PERIOD.length];
    final ZonedDateTime[] expiryDates = new ZonedDateTime[EXPIRY_PERIOD.length];
    TIME_TO_EXPIRY[0] = 0.0;
    for (int i = 0; i < EXPIRY_PERIOD.length; i++) {
      paymentDates[i] = ScheduleCalculator.getAdjustedDate(spotDate, EXPIRY_PERIOD[i], BUSINESS_DAY, CALENDAR);
      expiryDates[i] = ScheduleCalculator.getAdjustedDate(paymentDates[i], -SETTLEMENT_DAYS, CALENDAR);
      TIME_TO_EXPIRY[i + 1] = TimeCalculator.getTimeBetween(VALUATION_DATE, expiryDates[i]);
    }
  }
  /** The ATM volatilities */
  private static final double[] ATM = {0.175, 0.185, 0.18, 0.17, 0.16, 0.16};
  /** The delta */
  private static final double[] DELTA = new double[] {0.10, 0.25};
  /** Risk reversals */
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090},
    {-0.014, -0.0090}};
  /** Strangles */
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}, {0.0340, 0.0140}};
  /** The volatility surface */
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM = new SmileDeltaTermStructureParametersStrikeInterpolation(TIME_TO_EXPIRY, DELTA,
      ATM, RISK_REVERSAL, STRANGLE);
  /** All market data */
  private static final BlackForexSmileProviderDiscount MARKET_DATA = new BlackForexSmileProviderDiscount(CURVES, SMILE_TERM, Pairs.of(Currency.EUR, KRW));
  /** The calculation tolerance */
  private static final double EPS = 1e-9;

  /**
   * Tests that the value of a NDF is the same as an FX option with inverted strike and notional.
   */
  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvNDO = CALCULATOR.presentValue(NDO, MARKET_DATA);
    final MultipleCurrencyAmount pvFXO = ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(DELIVERABLE_EQUIVALENT_OPTION, MARKET_DATA);
    assertEquals(pvFXO, pvNDO, "Forex non-deliverable option: present value");
  }

  /**
   * Checks that the calculator points to the correct method.
   */
  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = CALCULATOR.presentValue(NDO, MARKET_DATA);
    final MultipleCurrencyAmount pvCalculator = NDO.accept(PresentValueForexBlackSmileCalculator.getInstance(), MARKET_DATA);
    assertEquals(pvMethod, pvCalculator, "Forex non-deliverable option: present value");
  }

  /**
   * Tests the currency exposure against the present value.
   */
  @Test
  public void currencyExposureVsPresentValue() {
    final MultipleCurrencyAmount pv = CALCULATOR.presentValue(NDO, MARKET_DATA);
    final MultipleCurrencyAmount ce = CALCULATOR.currencyExposure(NDO, MARKET_DATA);
    final double usdKrw = CURVES.getFxRate(Currency.EUR, KRW);
    assertEquals(ce.getAmount(Currency.EUR) + ce.getAmount(KRW) / usdKrw, pv.getAmount(Currency.EUR), EPS, "Forex vanilla option: currency exposure vs present value");
  }

  /**
   * Checks that the calculator points to the correct method.
   */
  @Test
  public void currencyExposureMethodVsCalculator() {
    final MultipleCurrencyAmount ceMethod = CALCULATOR.currencyExposure(NDO, MARKET_DATA);
    final MultipleCurrencyAmount ceCalculator = NDO.accept(CurrencyExposureForexBlackSmileCalculator.getInstance(), MARKET_DATA);
    assertEquals(ceMethod, ceCalculator, "Forex non-deliverable option: currency exposure");
  }

  /**
   * Tests that the curve sensitivities of a NDO is the same as an FX option with inverted strike and notional.
   */
  @Test
  public void presentValueCurveSensitivity() {
    final double tolerance = 1.0e-2;
    final MultipleCurrencyMulticurveSensitivity pvcsNDO = CALCULATOR.presentValueCurveSensitivity(NDO, MARKET_DATA).cleaned();
    final MultipleCurrencyMulticurveSensitivity pvcsFXO = ForexOptionVanillaBlackSmileMethod.getInstance().presentValueCurveSensitivity(DELIVERABLE_EQUIVALENT_OPTION, MARKET_DATA).cleaned();
    AssertSensitivityObjects.assertEquals("Forex non-deliverable option: present value curve sensitivity", pvcsFXO, pvcsNDO, tolerance);
  }

  /**
   * Tests the forward rate of a NDO.
   */
  @Test
  public void forwardForexRate() {
    final double fwd = CALCULATOR.forwardForexRate(NDO, CURVES);
    final double fwdExpected = ForexNonDeliverableForwardDiscountingMethod.getInstance().forwardForexRate(NDO.getUnderlyingNDF(), CURVES);
    assertEquals(fwdExpected, fwd, EPS, "Forex non-deliverable option: forward rate");
  }

  /**
   * Tests the volatility sensitivity of a NDO is the same as an FX option with inverted strike and notional.
   */
  @Test
  public void presentValueVolatilitySensitivity() {
    final PresentValueForexBlackVolatilitySensitivity pvvsNDO = CALCULATOR.presentValueBlackVolatilitySensitivity(NDO, MARKET_DATA);
    final PresentValueForexBlackVolatilitySensitivity pvvsFXO = ForexOptionVanillaBlackSmileMethod.getInstance().presentValueBlackVolatilitySensitivity(DELIVERABLE_EQUIVALENT_OPTION, MARKET_DATA);
    final DoublesPair point = DoublesPair.of(NDO.getExpiryTime(), NDO.getStrike());
    assertEquals(pvvsFXO.getVega().getMap().get(point), pvvsNDO.getVega().getMap().get(point), EPS, "Forex non-deliverable option: present value curve sensitivity");
  }

  /**
   * Tests the volatility node sensitivities of a NDO is the same as an FX option with inverted strike and notional.
   */
  @Test
  public void presentValueVolatilityNodeSensitivity() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle nsNDO = CALCULATOR.presentValueVolatilityNodeSensitivity(NDO, MARKET_DATA);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle nsFXO = ForexOptionVanillaBlackSmileMethod.getInstance().presentValueBlackVolatilityNodeSensitivity(DELIVERABLE_EQUIVALENT_OPTION, MARKET_DATA);
    for (int i = 0; i < EXPIRY_PERIOD.length; i++) {
      for (int j = 0; j < nsNDO.getDelta().getNumberOfElements(); j++) {
        assertEquals(nsFXO.getVega().getEntry(i, j), nsNDO.getVega().getEntry(i, j), EPS, "Forex non-deliverable option: vega node");
      }
    }
  }

  /**
   * Tests the spot delta calculation against an approximation calculated using central finite difference.
   */
  @Test
  public void testSpotDelta() {
    final double eps = 1e-4;
    final double spotFx = CURVES.getFxRate(KRW, Currency.EUR);
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    fxUp.addCurrency(KRW, Currency.EUR, spotFx + eps);
    final MulticurveProviderDiscount spotUpCurves = new MulticurveProviderDiscount(CURVES.getDiscountingCurves(), Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface spotUp = new BlackForexSmileProviderDiscount(spotUpCurves, MARKET_DATA.getVolatility(), Pairs.of(KRW, Currency.EUR));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    fxDown.addCurrency(KRW, Currency.EUR, spotFx - eps);
    final MulticurveProviderDiscount spotDownCurves = new MulticurveProviderDiscount(CURVES.getDiscountingCurves(), Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface spotDown = new BlackForexSmileProviderDiscount(spotDownCurves, MARKET_DATA.getVolatility(), Pairs.of(KRW, Currency.EUR));
    final double spotDelta = CALCULATOR.theoreticalSpotDelta(NDO, MARKET_DATA);
    //final double delta = CALCULATOR.delta(NDO, MARKET_DATA, true).getAmount();
    final double pvUp = CALCULATOR.presentValue(NDO, spotUp).getAmount(Currency.EUR);
    final double pvDown = CALCULATOR.presentValue(NDO, spotDown).getAmount(Currency.EUR);
    final double pv = CALCULATOR.presentValue(NDO, MARKET_DATA).getAmount(Currency.EUR);
    final double temp = (pvUp - pvDown) / 2 / eps / EUR_NOTIONAL;
    final double temp1 = temp / spotDelta;
    final double temp2 = temp * spotDelta;
    final double temp3 = spotDelta / temp;
//    assertEquals(spotDelta, (pvUp - pvDown) / 2 / eps / EUR_NOTIONAL, eps * eps * 10);
    //assertEquals(delta, spotDelta * EUR_NOTIONAL, eps);
  }

  @Test
  public void temp() {
    final double eps = 1e-6;
    final double spotFx = CURVES.getFxRate(KRW, Currency.EUR);
    final UncheckedMutableFxMatrix fxUp = UncheckedMutableFxMatrix.of();
    fxUp.addCurrency(KRW, Currency.EUR, spotFx + eps);
    final MulticurveProviderDiscount spotUpCurves = new MulticurveProviderDiscount(CURVES.getDiscountingCurves(), Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxUp));
    final BlackForexSmileProviderInterface spotUp = new BlackForexSmileProviderDiscount(spotUpCurves, MARKET_DATA.getVolatility(), Pairs.of(KRW, Currency.EUR));
    final UncheckedMutableFxMatrix fxDown = UncheckedMutableFxMatrix.of();
    fxDown.addCurrency(KRW, Currency.EUR, spotFx - eps);
    final MulticurveProviderDiscount spotDownCurves = new MulticurveProviderDiscount(CURVES.getDiscountingCurves(), Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(),
        Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), ImmutableFxMatrix.of(fxDown));
    final BlackForexSmileProviderInterface spotDown = new BlackForexSmileProviderDiscount(spotDownCurves, MARKET_DATA.getVolatility(), Pairs.of(KRW, Currency.EUR));
    final double spotDelta = ForexOptionVanillaBlackSmileMethod.getInstance().spotDeltaTheoretical(DELIVERABLE_EQUIVALENT_OPTION, MARKET_DATA);
    final double pvUp = ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(DELIVERABLE_EQUIVALENT_OPTION, spotUp).getAmount(Currency.EUR);
    final double pvDown = ForexOptionVanillaBlackSmileMethod.getInstance().presentValue(DELIVERABLE_EQUIVALENT_OPTION, spotDown).getAmount(Currency.EUR);
    final double delta = (pvUp - pvDown) / 2 / eps / STRIKE / EUR_NOTIONAL;
    System.err.println(spotDelta);
    System.err.println(delta);
  }

}
