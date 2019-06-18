/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.calculator.YieldFromCleanPriceCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.YieldFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of bills security by discounting.
 */
@Test(groups = TestGroup.UNIT)
public class BillSecurityDiscountingMethodTest {

  private static final IssuerProviderDiscount ISSUER_MULTICURVE = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  private static final String[] ISSUER_NAMES = IssuerProviderDiscountDataSets.getIssuerNames();

  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final WorkingDayCalendar WEEKEND = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 17);

  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final int SETTLEMENT_DAYS = 2;
  private static final YieldConvention YIELD_IAM = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  private static final YieldConvention YIELD_DSC = YieldConventionFactory.INSTANCE.getYieldConvention("DISCOUNT");

  private static final ZonedDateTime END_DATE = DateUtils.getUTCDate(2012, 3, 15);
  private static final double NOTIONAL = 1000;
  private static final double YIELD = 0.00185; // External source
  private static final double PRICE = 0.99971; // External source

  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  // ISIN: BE0312677462
  private static final BillSecurityDefinition BILL_BEL_IAM_SEC_DEFINITION = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, WEEKEND,
      YIELD_IAM, ACT360, ISSUER_NAMES[1]);
  // private static final String BEL_NAME = ISSUER_NAMES[1];
  private static final BillSecurityDefinition BILL_US_DSC_SEC_DEFINITION = new BillSecurityDefinition(USD, END_DATE, NOTIONAL, SETTLEMENT_DAYS, WEEKEND,
      YIELD_DSC, ACT360, ISSUER_NAMES[0]);
  private static final BillSecurity BILL_BEL_IAM_SEC = BILL_BEL_IAM_SEC_DEFINITION.toDerivative(REFERENCE_DATE, SETTLE_DATE);
  private static final BillSecurity BILL_US_DSC_SEC = BILL_US_DSC_SEC_DEFINITION.toDerivative(REFERENCE_DATE, SETTLE_DATE);

  private static final BillSecurityDiscountingMethod METHOD_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final YieldFromCurvesCalculator YFCC = YieldFromCurvesCalculator.getInstance();
  private static final YieldFromCleanPriceCalculator YFPC = YieldFromCleanPriceCalculator.getInstance();

  private static final double SHIFT_FD = 1.0E-6;
  private static final ParameterSensitivityIssuerCalculator<ParameterIssuerProviderInterface> PS_PVI_C = new ParameterSensitivityIssuerCalculator<>(PVCSIC);
  private static final ParameterSensitivityIssuerDiscountInterpolatedFDCalculator PS_PVI_FDC = new ParameterSensitivityIssuerDiscountInterpolatedFDCalculator(
      PVIC, SHIFT_FD);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; // Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_EXTERNAL = 1.0E-5;
  private static final double TOLERANCE_YIELD = 1.0E-8;
  private static final double TOLERANCE_YIELD_EXTERNAL = 1.0E-4;
  private static final double TOLERANCE_YIELD_DERIVATIVE = 1.0E-6;

  /**
   * Tests the present value against explicit computation.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = METHOD_SECURITY.presentValue(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    final double pvExpected = NOTIONAL * ISSUER_MULTICURVE.getDiscountFactor(BILL_BEL_IAM_SEC.getIssuerEntity(), BILL_BEL_IAM_SEC.getEndTime());
    assertEquals("Bill Security: discounting method - present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  /**
   * Tests the present value: Method vs Calculator
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_SECURITY.presentValue(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    final MultipleCurrencyAmount pvCalculator = BILL_BEL_IAM_SEC.accept(PVIC, ISSUER_MULTICURVE);
    assertEquals("Bill Security: discounting method - present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  public void priceFromYield() {
    final double[] yields = new double[] { 0.0010, 0.0, -0.0010 };
    for (final double yield2 : yields) {
      final double priceComputed = METHOD_SECURITY.priceFromYield(BILL_BEL_IAM_SEC, yield2);
      final double priceExpected = 1.0 / (1 + BILL_BEL_IAM_SEC.getAccrualFactor() * yield2);
      assertEquals("Bill Security: discounting method - price", priceExpected, priceComputed, TOLERANCE_PRICE);
    }
  }

  public void priceFromYieldExternal() {
    final double priceComputed = METHOD_SECURITY.priceFromYield(BILL_BEL_IAM_SEC, YIELD);
    assertEquals("Bill Security: discounting method - price", PRICE, priceComputed, TOLERANCE_PRICE_EXTERNAL);
  }

  public void yieldFromPrice() {
    final double yieldComputedIAM = METHOD_SECURITY.yieldFromCleanPrice(BILL_BEL_IAM_SEC, PRICE);
    final double yieldExpectedIAM = (1.0 / PRICE - 1.0) / BILL_BEL_IAM_SEC.getAccrualFactor();
    assertEquals("Bill Security: discounting method - yield", yieldExpectedIAM, yieldComputedIAM, TOLERANCE_YIELD);
    final double yieldComputedDSC = METHOD_SECURITY.yieldFromCleanPrice(BILL_US_DSC_SEC, PRICE);
    final double yieldExpectedDSC = (1.0 - PRICE) / BILL_US_DSC_SEC.getAccrualFactor();
    assertEquals("Bill Security: discounting method - yield", yieldExpectedDSC, yieldComputedDSC, TOLERANCE_YIELD);
  }

  public void yieldFromPriceDerivative() {
    final double shift = 1.0E-8;
    final double yieldIAM = METHOD_SECURITY.yieldFromCleanPrice(BILL_BEL_IAM_SEC, PRICE);
    final double yieldPIAM = METHOD_SECURITY.yieldFromCleanPrice(BILL_BEL_IAM_SEC, PRICE + shift);
    final double yieldDerivativeExpectedIAM = (yieldPIAM - yieldIAM) / shift;
    final double yieldDerivativeComputedIAM = METHOD_SECURITY.yieldFromPriceDerivative(BILL_BEL_IAM_SEC, PRICE);
    assertEquals("Bill Security: discounting method - yield", yieldDerivativeExpectedIAM, yieldDerivativeComputedIAM, TOLERANCE_YIELD_DERIVATIVE);
    final double yieldDSC = METHOD_SECURITY.yieldFromCleanPrice(BILL_US_DSC_SEC, PRICE);
    final double yieldPDSC = METHOD_SECURITY.yieldFromCleanPrice(BILL_US_DSC_SEC, PRICE + shift);
    final double yieldDerivativeExpectedDSC = (yieldPDSC - yieldDSC) / shift;
    final double yieldDerivativeComputedDSC = METHOD_SECURITY.yieldFromPriceDerivative(BILL_US_DSC_SEC, PRICE);
    assertEquals("Bill Security: discounting method - yield", yieldDerivativeExpectedDSC, yieldDerivativeComputedDSC, TOLERANCE_YIELD_DERIVATIVE);
  }

  public void yieldFromPriceExternal() {
    final double yieldComputed = METHOD_SECURITY.yieldFromCleanPrice(BILL_BEL_IAM_SEC, PRICE);
    assertEquals("Bill Security: discounting method - yield", YIELD, yieldComputed, TOLERANCE_YIELD_EXTERNAL);
  }

  public void yieldFromPriceCoherence() {
    final double priceComputed = METHOD_SECURITY.priceFromYield(BILL_BEL_IAM_SEC, YIELD);
    final double yieldComputed = METHOD_SECURITY.yieldFromCleanPrice(BILL_BEL_IAM_SEC, priceComputed);
    assertEquals("Bill Security: discounting method - yield", YIELD, yieldComputed, TOLERANCE_YIELD);
  }

  public void priceFromYieldCoherence() {
    final double yieldComputed = METHOD_SECURITY.yieldFromCleanPrice(BILL_BEL_IAM_SEC, PRICE);
    final double priceComputed = METHOD_SECURITY.priceFromYield(BILL_BEL_IAM_SEC, yieldComputed);
    assertEquals("Bill Security: discounting method - price", PRICE, priceComputed, TOLERANCE_PRICE);
  }

  public void presentValueFromPrice() {
    final MultipleCurrencyAmount pvComputed = METHOD_SECURITY.presentValueFromPrice(BILL_BEL_IAM_SEC, PRICE, ISSUER_MULTICURVE);
    final double pvExpected = NOTIONAL * PRICE * ISSUER_MULTICURVE.getMulticurveProvider().getDiscountFactor(EUR, BILL_BEL_IAM_SEC.getSettlementTime());
    assertEquals("Bill Security: discounting method - present value", pvExpected, pvComputed.getAmount(BILL_BEL_IAM_SEC.getCurrency()), TOLERANCE_PV);
  }

  public void presentValueFromYield() {
    final MultipleCurrencyAmount pvComputed = METHOD_SECURITY.presentValueFromYield(BILL_BEL_IAM_SEC, YIELD, ISSUER_MULTICURVE);
    final double price = METHOD_SECURITY.priceFromYield(BILL_BEL_IAM_SEC, YIELD);
    final double pvExpected = NOTIONAL * price * ISSUER_MULTICURVE.getMulticurveProvider().getDiscountFactor(EUR, BILL_BEL_IAM_SEC.getSettlementTime());
    assertEquals("Bill Security: discounting method - present value", pvExpected, pvComputed.getAmount(BILL_BEL_IAM_SEC.getCurrency()), TOLERANCE_PV);
  }

  public void priceFromCurves() {
    final double priceComputed = METHOD_SECURITY.priceFromCurves(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    final MultipleCurrencyAmount pvComputed = METHOD_SECURITY.presentValue(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    final double priceExpected = pvComputed.getAmount(EUR)
        / (NOTIONAL * ISSUER_MULTICURVE.getMulticurveProvider().getDiscountFactor(EUR, BILL_BEL_IAM_SEC.getSettlementTime()));
    assertEquals("Bill Security: discounting method - price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  public void yieldFromCurves() {
    final double yieldComputed = METHOD_SECURITY.yieldFromCurves(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    final double priceComputed = METHOD_SECURITY.priceFromCurves(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    final double yieldExpected = METHOD_SECURITY.yieldFromCleanPrice(BILL_BEL_IAM_SEC, priceComputed);
    assertEquals("Bill Security: discounting method - yield", yieldExpected, yieldComputed, TOLERANCE_YIELD);
  }

  public void yieldFromCurvesMethodVsCalculator() {
    final double yieldMethod = METHOD_SECURITY.yieldFromCurves(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    final double yieldCalculator = BILL_BEL_IAM_SEC.accept(YFCC, ISSUER_MULTICURVE);
    assertEquals("Bill Security: discounting method - yield", yieldMethod, yieldCalculator, TOLERANCE_YIELD);
  }

  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PVI_C.calculateSensitivity(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE,
        ISSUER_MULTICURVE.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PVI_FDC.calculateSensitivity(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    AssertSensitivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD,
        TOLERANCE_PV_DELTA);
  }

  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_SECURITY.presentValueCurveSensitivity(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = BILL_BEL_IAM_SEC.accept(PVCSIC, ISSUER_MULTICURVE);
    AssertSensitivityObjects.assertEquals("Bill Security: discounting method - curve sensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV);
  }

  public void methodVsCalculator() {
    double yield1 = METHOD_SECURITY.yieldFromCurves(BILL_BEL_IAM_SEC, ISSUER_MULTICURVE);
    double yield2 = BILL_BEL_IAM_SEC.accept(YFCC, ISSUER_MULTICURVE);
    assertEquals("Bill Security: discounting method - yield from curves", yield1, yield2, TOLERANCE_YIELD);
    yield1 = METHOD_SECURITY.yieldFromCleanPrice(BILL_BEL_IAM_SEC, PRICE);
    yield2 = BILL_BEL_IAM_SEC.accept(YFPC, PRICE);
    assertEquals("Bill Security: discounting method - yield from price", yield1, yield2, TOLERANCE_YIELD);
  }

}
