/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.bond.calculator.CleanPriceFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.DirtyPriceFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.MacaulayDurationFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromCleanPriceCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.CleanPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ConvexityFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.DirtyPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.MacaulayDurationFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ModifiedDurationFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.YieldFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the discounting method for bond security.
 */
@Test(groups = TestGroup.UNIT)
public class BondSecurityUKDiscountingMethodTest {

  // Calculators
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final BondSecurityDiscountingMethod METHOD = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-8;

  private static final WorkingDayCalendar LON = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;

  // To derivatives
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final IssuerProviderDiscount CURVES = IssuerProviderDiscountDataSets.getCountrySpecificProvider();

  // UKT 5 09/07/14 - ISIN-GB0031829509 To check figures in the ex-dividend period
  private static final LegalEntity ISSUER_G = new LegalEntity(null, "UK", null, null, Region.of("UK", Country.GB, Currency.GBP));
  private static final String REPO_TYPE_G = "General collateral";
  private static final Currency CUR_G = Currency.GBP;
  private static final Period PAYMENT_TENOR_G = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR_G = 2;
  private static final WorkingDayCalendar CALENDAR_G = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final DayCount DAY_COUNT_G = DayCounts.ACT_ACT_ICMA; // To check
  private static final BusinessDayConvention BUSINESS_DAY_G = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_G = false;
  private static final Period BOND_TENOR_G = Period.ofYears(12);
  private static final int SETTLEMENT_DAYS_G = 1;
  private static final int EX_DIVIDEND_DAYS_G = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_G = DateUtils.getUTCDate(2002, 9, 7);
  private static final ZonedDateTime MATURITY_DATE_G = START_ACCRUAL_DATE_G.plus(BOND_TENOR_G);
  private static final double RATE_G = 0.0500;
  private static final double NOTIONAL_G = 100;
  private static final YieldConvention YIELD_CONVENTION_G = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION_G = BondFixedSecurityDefinition.from(CUR_G,
      START_ACCRUAL_DATE_G, MATURITY_DATE_G, PAYMENT_TENOR_G, RATE_G, SETTLEMENT_DAYS_G, NOTIONAL_G, EX_DIVIDEND_DAYS_G,
      CalendarAdapter.of(CALENDAR_G),
      DAY_COUNT_G, BUSINESS_DAY_G, YIELD_CONVENTION_G, IS_EOM_G, ISSUER_G, REPO_TYPE_G);
  private static final ZonedDateTime REFERENCE_DATE_3 = DateUtils.getUTCDate(2011, 9, 2); // Ex-dividend is 30-Aug-2011
  private static final BondFixedSecurity BOND_FIXED_SECURITY_G = BOND_FIXED_SECURITY_DEFINITION_G.toDerivative(REFERENCE_DATE_3);
  private static final ZonedDateTime SPOT_3 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_3, SETTLEMENT_DAYS_G, LON);
  private static final double REFERENCE_TIME_3 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_3, SPOT_3);
  private static final YieldFromCurvesCalculator YFCC = YieldFromCurvesCalculator.getInstance();
  private static final ModifiedDurationFromCurvesCalculator MDFC = ModifiedDurationFromCurvesCalculator.getInstance();
  private static final ModifiedDurationFromYieldCalculator MDFY = ModifiedDurationFromYieldCalculator.getInstance();
  private static final ModifiedDurationFromCleanPriceCalculator MDFP = ModifiedDurationFromCleanPriceCalculator.getInstance();
  private static final MacaulayDurationFromCurvesCalculator MCDFC = MacaulayDurationFromCurvesCalculator.getInstance();
  private static final MacaulayDurationFromYieldCalculator MCDFY = MacaulayDurationFromYieldCalculator.getInstance();
  private static final DirtyPriceFromYieldCalculator DPFY = DirtyPriceFromYieldCalculator.getInstance();
  private static final DirtyPriceFromCurvesCalculator DPFC = DirtyPriceFromCurvesCalculator.getInstance();
  private static final ConvexityFromCurvesCalculator CFC = ConvexityFromCurvesCalculator.getInstance();
  private static final CleanPriceFromYieldCalculator CPFY = CleanPriceFromYieldCalculator.getInstance();
  private static final CleanPriceFromCurvesCalculator CPFC = CleanPriceFromCurvesCalculator.getInstance();

  /**
   *
   */
  @Test
  public void presentValueFixedExDividend() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(BOND_FIXED_SECURITY_G, CURVES);
    final BondFixedSecurityDefinition bondNoExDefinition = BondFixedSecurityDefinition.from(CUR_G, START_ACCRUAL_DATE_G, MATURITY_DATE_G,
        PAYMENT_TENOR_G, RATE_G, SETTLEMENT_DAYS_G, NOTIONAL_G, 0,
        CalendarAdapter.of(CALENDAR_G), DAY_COUNT_G, BUSINESS_DAY_G, YIELD_CONVENTION_G, IS_EOM_G, ISSUER_G, REPO_TYPE_G);
    final BondFixedSecurity bondNoEx = bondNoExDefinition.toDerivative(REFERENCE_DATE_3);
    final MultipleCurrencyAmount pvNoEx = METHOD.presentValue(bondNoEx, CURVES);
    final CouponFixedDefinition couponDefinitionEx = BOND_FIXED_SECURITY_DEFINITION_G.getCoupons().getNthPayment(17);
    final MultipleCurrencyAmount pvCpn = couponDefinitionEx.toDerivative(REFERENCE_DATE_3).accept(PVC, CURVES.getMulticurveProvider());
    assertEquals(pvNoEx.plus(pvCpn.multipliedBy(-1)).getAmount(CUR_G), pv.getAmount(CUR_G), 1e-2);
  }

  /**
   *
   */
  @Test
  public void dirtyPriceFixedExDividend() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(BOND_FIXED_SECURITY_G, CURVES);
    final double df = CURVES.getMulticurveProvider().getDiscountFactor(CUR_G, REFERENCE_TIME_3);
    final double dirty = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_G, CURVES);
    assertEquals(pv.getAmount(CUR_G) / df / BOND_FIXED_SECURITY_G.getCoupon().getNthPayment(0).getNotional(), dirty);
    assertTrue(0.50 < dirty && dirty < 2.0);
  }

  /**
   *
   */
  @Test
  public void dirtyPriceFromYieldUKExDividend() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield);
    final double dirtyPriceExpected = 1.0277859038; // To be check with another source.
    assertEquals(dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  /**
   *
   */
  @Test
  public void dirtyPriceFromYieldUKLastPeriod() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2014, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION_G.toDerivative(referenceDate);
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = (1 + RATE_G / COUPON_PER_YEAR_G)
        * Math.pow(1 + yield / COUPON_PER_YEAR_G, -bondSecurity.getFactorToNextCoupon());
    assertEquals(dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  /**
   *
   */
  @Test
  public void yieldFromDirtyPriceUKExDividend() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield);
    final double yieldComputed = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_G, dirtyPrice);
    assertEquals(yield, yieldComputed, 1E-10);
  }

  /**
   *
   */
  @Test
  public void modifiedDurationFromYieldUKExDividend() {
    final double yield = 0.04;
    final double modifiedDuration = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_G, yield);
    final double modifiedDurationExpected = 2.7757118292; // To be check with another source.
    assertEquals(modifiedDurationExpected, modifiedDuration, TOLERANCE_PRICE);
    final double shift = 1.0E-6;
    final double dirty = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield);
    final double dirtyP = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield + shift);
    final double dirtyM = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield - shift);
    final double modifiedDurationFD = -(dirtyP - dirtyM) / (2 * shift) / dirty;
    assertEquals(modifiedDurationFD, modifiedDuration, TOLERANCE_PRICE);
  }

  /**
   *
   */
  @Test
  public void macauleyDurationFromYieldUKExDividend() {
    final double yield = 0.04;
    final double macauleyDuration = METHOD.macaulayDurationFromYield(BOND_FIXED_SECURITY_G, yield);
    final double macauleyDurationExpected = 2.909894241 / METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield); // To be check with
                                                                                                                    // another source.
    assertEquals(macauleyDurationExpected, macauleyDuration, TOLERANCE_PRICE);
  }

  // UKT 6 1/4 11/25/10
  private static final DayCount DAY_COUNT_G2 = DayCounts.ACT_ACT_ICMA; // To check
  private static final int SETTLEMENT_DAYS_G2 = 1;
  private static final int EX_DIVIDEND_DAYS_G2 = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_G2 = DateUtils.getUTCDate(1999, 11, 25);
  private static final ZonedDateTime MATURITY_DATE_G2 = DateUtils.getUTCDate(2010, 11, 25);
  private static final double RATE_G2 = 0.0625;
  private static final double NOTIONAL_G2 = 100;
  private static final YieldConvention YIELD_CONVENTION_G2 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION_G2 = BondFixedSecurityDefinition.from(CUR_G,
      START_ACCRUAL_DATE_G2, MATURITY_DATE_G2, PAYMENT_TENOR_G, RATE_G2,
      SETTLEMENT_DAYS_G2, NOTIONAL_G2, EX_DIVIDEND_DAYS_G2, CalendarAdapter.of(CALENDAR_G), DAY_COUNT_G2, BUSINESS_DAY_G,
      YIELD_CONVENTION_G2, IS_EOM_G,
      ISSUER_G, REPO_TYPE_G);
  private static final ZonedDateTime REFERENCE_DATE_4 = DateUtils.getUTCDate(2001, 8, 10);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_G2 = BOND_FIXED_SECURITY_DEFINITION_G2.toDerivative(REFERENCE_DATE_4);

  /**
   *
   */
  @Test
  public void dirtyPriceFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double dirtyPrice = METHOD.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double dirtyPriceExpected = 1.11558696;
    assertEquals(dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  /**
   *
   */
  @Test
  public void yieldPriceFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double yield = METHOD.yieldFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double yieldExpected = 0.04870;
    assertEquals(yieldExpected, yield, 1E-5);
  }

  /**
   *
   */
  @Test
  public void modifiedDurationFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double dirtyPrice = METHOD.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double md = METHOD.modifiedDurationFromDirtyPrice(BOND_FIXED_SECURITY_G2, dirtyPrice);
    final double mdExpected = 7.039;
    assertEquals(mdExpected, md, 1E-3);
  }

  /**
   *
   */
  @Test
  public void yieldFromCurvesMethodVsCalculator() {
    final double yieldMethod = METHOD.yieldFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    final double yieldCalculator = BOND_FIXED_SECURITY_G2.accept(YFCC, CURVES);
    assertEquals(yieldMethod, yieldCalculator, 1e-9);
  }

  /**
   *
   */
  @Test
  public void modifiedDurationMethodVsCalculator() {
    double method = METHOD.modifiedDurationFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    double calculator = BOND_FIXED_SECURITY_G2.accept(MDFC, CURVES);
    assertEquals(method, calculator, 1e-9);
    method = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_G2, 0.05);
    calculator = BOND_FIXED_SECURITY_G2.accept(MDFY, 0.05);
    assertEquals(method, calculator, 1e-9);
    method = METHOD.modifiedDurationFromCleanPrice(BOND_FIXED_SECURITY_G2, 1.00);
    calculator = BOND_FIXED_SECURITY_G2.accept(MDFP, 1.00);
    assertEquals(method, calculator, 1e-9);
  }

  /**
   *
   */
  @Test
  public void macaulayDurationMethodVsCalculator() {
    double method = METHOD.macaulayDurationFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    double calculator = BOND_FIXED_SECURITY_G2.accept(MCDFC, CURVES);
    assertEquals(method, calculator, 1e-9);
    method = METHOD.macaulayDurationFromYield(BOND_FIXED_SECURITY_G2, 0.05);
    calculator = BOND_FIXED_SECURITY_G2.accept(MCDFY, 0.05);
    assertEquals(method, calculator, 1e-9);
  }

  /**
   *
   */
  @Test
  public void dirtyPriceMethodVsCalculator() {
    double method = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    double calculator = BOND_FIXED_SECURITY_G2.accept(DPFC, CURVES);
    assertEquals(method, calculator, 1e-9);
    method = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G2, 0.05);
    calculator = BOND_FIXED_SECURITY_G2.accept(DPFY, 0.05);
    assertEquals(method, calculator, 1e-9);
  }

  /**
   *
   */
  @Test
  public void convexityMethodVsCalculator() {
    final double method = METHOD.convexityFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    final double calculator = BOND_FIXED_SECURITY_G2.accept(CFC, CURVES);
    assertEquals(method, calculator * 100, 1e-9);
  }

  /**
   *
   */
  @Test
  public void cleanPriceMethodVsCalculator() {
    double method = METHOD.cleanPriceFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    double calculator = BOND_FIXED_SECURITY_G2.accept(CPFC, CURVES);
    assertEquals(method * 100, calculator, 1e-9);
    method = METHOD.cleanPriceFromYield(BOND_FIXED_SECURITY_G2, 0.05);
    calculator = BOND_FIXED_SECURITY_G2.accept(CPFY, 0.05);
    assertEquals(method, calculator / 100, 1e-9);
  }
}
