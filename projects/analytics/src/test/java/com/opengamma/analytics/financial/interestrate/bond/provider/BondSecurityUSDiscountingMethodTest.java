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
import org.threeten.bp.temporal.JulianFields;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.CleanPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.DirtyPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.MacaulayDurationFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ModifiedDurationFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscountingDecoratedIssuer;
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
public class BondSecurityUSDiscountingMethodTest {

  // Calculators
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final BondSecurityDiscountingMethod METHOD = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final IssuerProviderDiscount CURVES = IssuerProviderDiscountDataSets.getCountrySpecificProvider();

  // T 4 5/8 11/15/16 - ISIN - US912828FY19
  private static final LegalEntity ISSUER_US = new LegalEntity(null, "US TREASURY N/B", null, null,
      Region.of("US", Country.US, Currency.USD));
  private static final String REPO_TYPE = "General collateral";
  private static final Currency CUR = Currency.EUR;
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final Period PAYMENT_TENOR_FIXED_US = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR = 2;
  private static final DayCount DAY_COUNT_ACTACTICMA = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY_FIXED = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_FIXED = false;
  private static final Period BOND_TENOR_FIXED = Period.ofYears(10);
  private static final int SETTLEMENT_DAYS_US = 3;
  private static final ZonedDateTime START_ACCRUAL_DATE_FIXED = DateUtils.getUTCDate(2006, 11, 15);
  private static final ZonedDateTime MATURITY_DATE_FIXED = START_ACCRUAL_DATE_FIXED.plus(BOND_TENOR_FIXED);
  private static final double RATE_FIXED = 0.04625;
  private static final double NOTIONAL = 100;
  private static final YieldConvention YIELD_CONVENTION_FIXED = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR,
      MATURITY_DATE_FIXED, START_ACCRUAL_DATE_FIXED, PAYMENT_TENOR_FIXED_US,
      RATE_FIXED, SETTLEMENT_DAYS_US, NOTIONAL, CalendarAdapter.of(CALENDAR), DAY_COUNT_ACTACTICMA, BUSINESS_DAY_FIXED,
      YIELD_CONVENTION_FIXED, IS_EOM_FIXED, ISSUER_US, REPO_TYPE);

  // To derivatives
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  // Spot: middle coupon
  private static final ZonedDateTime REFERENCE_DATE_US_1 = DateUtils.getUTCDate(2011, 8, 18);
  private static final ZonedDateTime SPOT_1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_US_1, SETTLEMENT_DAYS_US, CALENDAR);
  private static final double REFERENCE_TIME_1 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_US_1, SPOT_1);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_1 = BOND_FIXED_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_US_1);
  // Spot: on coupon date
  private static final ZonedDateTime REFERENCE_DATE_US_2 = DateUtils.getUTCDate(2012, 1, 10);
  private static final ZonedDateTime SPOT_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_US_2, SETTLEMENT_DAYS_US, CALENDAR);
  private static final double REFERENCE_TIME_2 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_US_2, SPOT_2);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_2 = BOND_FIXED_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_US_2);

  // Spot: one day after coupon date

  /**
   *
   */
  @Test
  public void presentValueFixedMiddle() {
    final MulticurveProviderDiscountingDecoratedIssuer curves = new MulticurveProviderDiscountingDecoratedIssuer(CURVES, CUR,
        BOND_FIXED_SECURITY_1.getIssuerEntity());
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) BOND_FIXED_SECURITY_DEFINITION.getNominal().toDerivative(REFERENCE_DATE_US_1);
    AnnuityCouponFixed coupon = BOND_FIXED_SECURITY_DEFINITION.getCoupons().toDerivative(REFERENCE_DATE_US_1);
    coupon = coupon.trimBefore(REFERENCE_TIME_1);
    final MultipleCurrencyAmount pvNominal = nominal.accept(PVC, curves);
    final MultipleCurrencyAmount pvCoupon = coupon.accept(PVC, curves);
    final MultipleCurrencyAmount pv = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals(pvNominal.plus(pvCoupon).getAmount(CUR), pv.getAmount(CUR));
  }

  /**
   *
   */
  @Test
  public void presentValueFixedOnCoupon() {
    final MulticurveProviderDiscountingDecoratedIssuer curves = new MulticurveProviderDiscountingDecoratedIssuer(CURVES, CUR,
        BOND_FIXED_SECURITY_1.getIssuerEntity());
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) BOND_FIXED_SECURITY_DEFINITION.getNominal().toDerivative(REFERENCE_DATE_US_2);
    AnnuityCouponFixed coupon = BOND_FIXED_SECURITY_DEFINITION.getCoupons().toDerivative(REFERENCE_DATE_US_2);
    coupon = coupon.trimBefore(REFERENCE_TIME_2);
    final MultipleCurrencyAmount pvNominal = nominal.accept(PVC, curves);
    final MultipleCurrencyAmount pvCoupon = coupon.accept(PVC, curves);
    final MultipleCurrencyAmount pv = METHOD.presentValue(BOND_FIXED_SECURITY_2, CURVES);
    assertEquals(pvNominal.plus(pvCoupon).getAmount(CUR), pv.getAmount(CUR));
  }

  /**
   *
   */
  @Test
  public void presentValueFixedMethodVsCalculator() {
    final double pvMethod = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES).getAmount(CUR);
    final double pvCalculator = BOND_FIXED_SECURITY_1.accept(PVIC, CURVES).getAmount(CUR);
    assertEquals(pvMethod, pvCalculator);
  }

  // TODO
  //
  // /**
  // * Tests the present value from curves and a z-spread.
  // */
  // @Test
  // public void presentValueFromZSpread() {
  // final double pv = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES).getAmount(CUR);
  // double zSpread = 0.0;
  // final double pvZ0 = METHOD.presentValueFromZSpread(BOND_FIXED_SECURITY_1, CURVES, zSpread).getAmount(CUR);
  // assertEquals(pv, pvZ0, TOLERANCE_PRICE);
  // final YieldCurveBundle shiftedBundle = new YieldCurveBundle();
  // shiftedBundle.addAll(CURVES);
  // YieldAndDiscountCurve shiftedCredit = CURVES.getCurve(CREDIT_CURVE_NAME).withParallelShift(zSpread);
  // shiftedBundle.replaceCurve(CREDIT_CURVE_NAME, shiftedCredit);
  // double pvZ = METHOD.presentValueFromZSpread(BOND_FIXED_SECURITY_1, CURVES, zSpread);
  // double pvZExpected = METHOD.presentValue(BOND_FIXED_SECURITY_1, shiftedBundle);
  // assertEquals("Fixed coupon bond security: present value from z-spread", pvZExpected, pvZ, TOLERANCE_PRICE);
  // zSpread = 0.0010; // 10bps
  // shiftedCredit = CURVES.getCurve(CREDIT_CURVE_NAME).withParallelShift(zSpread);
  // shiftedBundle.replaceCurve(CREDIT_CURVE_NAME, shiftedCredit);
  // pvZ = METHOD.presentValueFromZSpread(BOND_FIXED_SECURITY_1, CURVES, zSpread);
  // pvZExpected = METHOD.presentValue(BOND_FIXED_SECURITY_1, shiftedBundle);
  // assertEquals("Fixed coupon bond security: present value from z-spread", pvZExpected, pvZ, TOLERANCE_PRICE);
  // final double pvZ2 = METHOD.presentValueFromZSpread(BOND_FIXED_SECURITY_1, CURVES, zSpread);
  // assertEquals("Fixed coupon bond security: present value from z-spread", pvZ, pvZ2, 1E-8);
  // }

  /**
   * Tests the present value z-spread sensitivity.
   */
  @Test
  public void presentValueZSpreadSensitivity() {
    final double zSpread = 0.0050; // 50bps
    final double shift = 0.00001;
    final double pvzs = METHOD.presentValueZSpreadSensitivity(BOND_FIXED_SECURITY_1, CURVES, zSpread);
    final double pvZUp = METHOD.presentValueFromZSpread(BOND_FIXED_SECURITY_1, CURVES, zSpread + shift).getAmount(CUR);
    final double pvZDown = METHOD.presentValueFromZSpread(BOND_FIXED_SECURITY_1, CURVES, zSpread - shift).getAmount(CUR);
    assertEquals((pvZUp - pvZDown) / (2 * shift), pvzs, 1E-6);
  }

  /**
   * Tests the bond security present value from clean price.
   */
  @Test
  public void presentValueFromCleanPrice() {
    final double cleanPrice = METHOD.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final double pvClean = METHOD.presentValueFromCleanPrice(BOND_FIXED_SECURITY_1, CURVES.getMulticurveProvider(), cleanPrice)
        .getAmount(CUR);
    final double pvCleanExpected = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES).getAmount(CUR);
    assertEquals(pvCleanExpected, pvClean, TOLERANCE_PRICE);
  }

  /**
   * Tests the z-spread computation from the present value.
   */
  @Test
  public void zSpreadFromPresentValue() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES);
    final double zSpread = METHOD.zSpreadFromCurvesAndPV(BOND_FIXED_SECURITY_1, CURVES, pv);
    assertEquals(0.0, zSpread, TOLERANCE_PRICE);
    final double zSpreadExpected = 0.0025; // 25bps
    final MultipleCurrencyAmount pvZSpread = METHOD.presentValueFromZSpread(BOND_FIXED_SECURITY_1, CURVES, zSpreadExpected);
    final double zSpread2 = METHOD.zSpreadFromCurvesAndPV(BOND_FIXED_SECURITY_1, CURVES, pvZSpread);
    assertEquals(zSpreadExpected, zSpread2, TOLERANCE_PRICE);
    final double zSpreadExpected3 = 0.0250; // 2.50%
    final MultipleCurrencyAmount pvZSpread3 = METHOD.presentValueFromZSpread(BOND_FIXED_SECURITY_1, CURVES, zSpreadExpected3);
    final double zSpread3 = METHOD.zSpreadFromCurvesAndPV(BOND_FIXED_SECURITY_1, CURVES, pvZSpread3);
    assertEquals(zSpreadExpected3, zSpread3, TOLERANCE_PRICE);
  }

  /**
   * Tests the z-spread sensitivity computation from the present value.
   */
  @Test
  public void zSpreadSensitivityFromPresentValue() {
    final double zSpread = 0.0025; // 25bps
    final MultipleCurrencyAmount pvZSpread = METHOD.presentValueFromZSpread(BOND_FIXED_SECURITY_1, CURVES, zSpread);
    final double zsComputed = METHOD.presentValueZSpreadSensitivityFromCurvesAndPV(BOND_FIXED_SECURITY_1, CURVES, pvZSpread);
    final double zsExpected = METHOD.presentValueZSpreadSensitivity(BOND_FIXED_SECURITY_1, CURVES, zSpread);
    assertEquals(zsExpected, zsComputed, 1E-6);
  }

  // /**
  // * Tests the z-spread computation from the clean price.
  // */
  // @Test
  // public void zSpreadFromCleanPrice() {
  // final double zSpreadExpected = 0.0025; // 25bps
  // final YieldCurveBundle shiftedBundle = new YieldCurveBundle();
  // shiftedBundle.addAll(CURVES);
  // final YieldAndDiscountCurve shiftedCredit = CURVES.getCurve(CREDIT_CURVE_NAME).withParallelShift(zSpreadExpected);
  // shiftedBundle.replaceCurve(CREDIT_CURVE_NAME, shiftedCredit);
  // final double cleanZSpread = METHOD.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, shiftedBundle);
  // final double zSpread = METHOD.zSpreadFromCurvesAndClean(BOND_FIXED_SECURITY_1, CURVES, cleanZSpread);
  // assertEquals("Fixed coupon bond security: present value from z-spread", zSpreadExpected, zSpread, 1E-8);
  // }

  // @Test
  // /**
  // * Tests the z-spread sensitivity computation from the present value.
  // */
  // public void zSpreadSensitivityFromCleanPrice() {
  // final double zSpread = 0.0025; // 25bps
  // final YieldCurveBundle shiftedBundle = new YieldCurveBundle();
  // shiftedBundle.addAll(CURVES);
  // final YieldAndDiscountCurve shiftedCredit = CURVES.getCurve(CREDIT_CURVE_NAME).withParallelShift(zSpread);
  // shiftedBundle.replaceCurve(CREDIT_CURVE_NAME, shiftedCredit);
  // final double cleanZSpread = METHOD.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, shiftedBundle);
  // final double zsComputed = METHOD.presentValueZSpreadSensitivityFromCurvesAndClean(BOND_FIXED_SECURITY_1, CURVES, cleanZSpread);
  // final double zsExpected = METHOD.presentValueZSpreadSensitivity(BOND_FIXED_SECURITY_1, CURVES, zSpread);
  // assertEquals("Fixed coupon bond security: z-spread sensitivity", zsExpected, zsComputed, 1E-6);
  // }

  /**
   *
   */
  @Test
  public void dirtyPriceFixed() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES);
    final double df = CURVES.getMulticurveProvider().getDiscountFactor(CUR, REFERENCE_TIME_1);
    final double dirty = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals(pv.getAmount(CUR) / df / BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getNotional(), dirty);
    assertTrue(0.50 < dirty && dirty < 2.0);
  }

  /**
   *
   */
  @Test
  public void dirtyPriceFixedMethodVsCalculator() {
    final double dirtyMethod = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final DirtyPriceFromCurvesCalculator calculator = DirtyPriceFromCurvesCalculator.getInstance();
    final double dirtyCalculator = BOND_FIXED_SECURITY_1.accept(calculator, CURVES);
    assertEquals(dirtyMethod, dirtyCalculator);
  }

  /**
   *
   */
  @Test
  public void cleanPriceFixed() {
    final double dirty = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final double clean = METHOD.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals(dirty - BOND_FIXED_SECURITY_1.getAccruedInterest() / BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getNotional(),
        clean);
  }

  /**
   *
   */
  @Test
  public void cleanPriceFixedMethodVsCalculator() {
    final double cleanMethod = METHOD.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final CleanPriceFromCurvesCalculator calculator = CleanPriceFromCurvesCalculator.getInstance();
    final double cleanCalculator = BOND_FIXED_SECURITY_1.accept(calculator, CURVES);
    assertEquals(cleanMethod * 100, cleanCalculator);
  }

  /**
   *
   */
  @Test
  public void cleanAndDirtyPriceFixed() {
    final double cleanPrice = 0.90;
    final double accruedInterest = BOND_FIXED_SECURITY_1.getAccruedInterest()
        / BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getNotional();
    assertEquals(cleanPrice + accruedInterest,
        METHOD.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_1, cleanPrice));
    final double dirtyPrice = 0.95;
    assertEquals(dirtyPrice - accruedInterest,
        METHOD.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice));
    assertEquals(cleanPrice,
        METHOD.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, METHOD.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_1, cleanPrice)));
  }

  /**
   *
   */
  @Test
  public void dirtyPriceFromYieldUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double dirtyPriceExpected = 1.04173525; // To be check with another source.
    assertEquals(dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  /**
   *
   */
  @Test
  public void cleanPriceFromYieldUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double cleanPriceExpected = METHOD.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double cleanPrice = METHOD.cleanPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    assertEquals(cleanPriceExpected, cleanPrice, 1E-8);
  }

  /**
   *
   */
  @Test
  public void dirtyPriceFromYieldUSStreetLastPeriod() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2016, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION.toDerivative(referenceDate);
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = (1 + RATE_FIXED / COUPON_PER_YEAR)
        / (1 + bondSecurity.getFactorToNextCoupon() * yield / COUPON_PER_YEAR);
    assertEquals(dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  /**
   *
   */
  @Test
  public void yieldFromDirtyPriceUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double yieldComputed = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals(yield, yieldComputed, 1E-10);
  }

  /**
   *
   */
  @Test
  public void yieldFromCurvesUSStreet() {
    final double dirtyPrice = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final double yieldExpected = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double yieldComputed = METHOD.yieldFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals(yieldExpected, yieldComputed, 1E-10);
  }

  /**
   *
   */
  @Test
  public void yieldFromCleanPriceUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double cleanPrice = METHOD.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double yieldComputed = METHOD.yieldFromCleanPrice(BOND_FIXED_SECURITY_1, cleanPrice);
    assertEquals(yield, yieldComputed, 1E-10);
    final double cleanPrice2 = METHOD.cleanPriceFromYield(BOND_FIXED_SECURITY_1, yieldComputed);
    assertEquals(cleanPrice, cleanPrice2, 1E-10);
  }

  /**
   *
   */
  @Test
  public void modifiedDurationFromYieldUSStreet() {
    final double yield = 0.04;
    final double modifiedDuration = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double modifiedDurationExpected = 4.566199225; // To be check with another source.
    assertEquals(modifiedDurationExpected,
        modifiedDuration, 1E-8);
    final double shift = 1.0E-6;
    final double dirty = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double dirtyP = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield + shift);
    final double dirtyM = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield - shift);
    final double modifiedDurationFD = -(dirtyP - dirtyM) / (2 * shift) / dirty;
    assertEquals(modifiedDurationFD,
        modifiedDuration, 1E-8);
  }

  /**
   *
   */
  @Test
  public void modifiedDurationFromCurvesUSStreet() {
    final double yield = METHOD.yieldFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final double modifiedDurationExpected = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double modifiedDuration = METHOD.modifiedDurationFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals(modifiedDurationExpected, modifiedDuration, 1E-8);
  }

  /**
   *
   */
  @Test
  public void modifiedDurationFromDirtyPriceUSStreet() {
    final double dirtyPrice = 0.95;
    final double yield = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double modifiedDurationExpected = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double modifiedDuration = METHOD.modifiedDurationFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals(modifiedDurationExpected, modifiedDuration, 1E-8);
  }

  /**
   *
   */
  @Test
  public void modifiedDurationFromYieldUSStreetLastPeriod() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2016, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION.toDerivative(referenceDate);
    final double yield = 0.04;
    final double dirtyPrice = METHOD.modifiedDurationFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = bondSecurity.getFactorToNextCoupon() / COUPON_PER_YEAR
        / (1 + bondSecurity.getFactorToNextCoupon() * yield / COUPON_PER_YEAR);
    assertEquals(dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  /**
   *
   */
  @Test
  public void modifiedDurationFromCurvesUSStreetMethodVsCalculator() {
    final double mdMethod = METHOD.modifiedDurationFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final ModifiedDurationFromCurvesCalculator calculator = ModifiedDurationFromCurvesCalculator.getInstance();
    final double mdCalculator = BOND_FIXED_SECURITY_1.accept(calculator, CURVES);
    assertEquals(mdMethod, mdCalculator, 1E-8);
  }

  /**
   * Tests Macauley duration vs a hard coded value (US Street convention).
   */
  @Test
  public void macauleyDurationFromYieldUSStreet() {
    final double yield = 0.04;
    final double mc = METHOD.macaulayDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double dirty = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double mcExpected = 4.851906106 / dirty;
    assertEquals(mcExpected, mc, 1E-8);
    final double md = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    assertEquals(md * (1 + yield / COUPON_PER_YEAR), mc, 1E-8);
  }

  /**
   * Tests Macauley duration from the curves (US Street convention).
   */
  @Test
  public void macauleyDurationFromCurvesUSStreet() {
    final double yield = METHOD.yieldFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final double macauleyDurationExpected = METHOD.macaulayDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double macauleyDuration = METHOD.macaulayDurationFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals(macauleyDurationExpected, macauleyDuration, 1E-8);
  }

  /**
   * Tests Macauley duration from a dirty price (US Street convention).
   */
  @Test
  public void macauleyDurationFromDirtyPriceUSStreet() {
    final double dirtyPrice = 0.95;
    final double yield = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double macauleyDurationExpected = METHOD.macaulayDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double macauleyDuration = METHOD.macaulayDurationFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals(macauleyDurationExpected, macauleyDuration, 1E-8);
  }

  /**
   * Tests Macauley duration: Method vs Calculator (US Street convention).
   */
  @Test
  public void macauleyDurationFromCurvesUSStreetMethodVsCalculator() {
    final double mcMethod = METHOD.macaulayDurationFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final MacaulayDurationFromCurvesCalculator calculator = MacaulayDurationFromCurvesCalculator.getInstance();
    final double mcCalculator = BOND_FIXED_SECURITY_1.accept(calculator, CURVES);
    assertEquals(mcMethod, mcCalculator, 1E-8);
  }

  /**
   * Tests convexity vs a hard coded value (US Street convention).
   */
  @Test
  public void convexityDurationFromYieldUSStreet() {
    final double yield = 0.04;
    final double cv = METHOD.convexityFromYield(BOND_FIXED_SECURITY_1, yield);
    final double dirty = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double cvExpected = 25.75957016 / dirty;
    assertEquals(cvExpected, cv, 1E-8);
    final double shift = 1.0E-6;
    final double dirtyP = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield + shift);
    final double dirtyM = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield - shift);
    final double cvFD = (dirtyP + dirtyM - 2 * dirty) / (shift * shift) / dirty;
    assertEquals(cvFD, cv, 1E-2);
  }

  /**
   * Tests convexity from the curves (US Street convention).
   */
  @Test
  public void convexityFromCurvesUSStreet() {
    final double yield = METHOD.yieldFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final double convexityExpected = METHOD.convexityFromYield(BOND_FIXED_SECURITY_1, yield);
    final double convexity = METHOD.convexityFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals(convexityExpected, convexity, 1E-8);
  }

  /**
   * Tests convexity from a dirty price (US Street convention).
   */
  @Test
  public void convexityFromDirtyPriceUSStreet() {
    final double dirtyPrice = 0.95;
    final double yield = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double convexityExpected = METHOD.convexityFromYield(BOND_FIXED_SECURITY_1, yield);
    final double convexity = METHOD.convexityFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals(convexityExpected, convexity, 1E-8);
  }

  /**
   * Tests the second order expansion of price wrt yield.
   */
  @Test
  public void expansionYieldUSStreet() {
    final double yield = 0.0500;
    final double price0 = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double[] shift = { -0.001, 0.001, 0.01 };

    final double modDur = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double convexity = METHOD.convexityFromYield(BOND_FIXED_SECURITY_1, yield);
    for (final double element : shift) {
      final double price = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield + element);
      final double price1 = price0 * (1 - element * modDur);
      final double price2 = price0 * (1 - element * modDur + 0.5 * element * element * convexity);
      assertEquals(price, price1, 2.0E-1 * Math.abs(element));
      assertEquals(price, price2, 3.0E-3 * Math.abs(element));
    }

  }

  // /**
  // *
  // */
  // @Test
  // public void dirtyPriceCurveSensitivity() {
  // MulticurveSensitivity sensi = METHOD.dirtyPriceCurveSensitivity(BOND_FIXED_SECURITY_1, CURVES);
  // sensi = sensi.cleaned();
  // MultipleCurrencyAmount pv = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES);
  // final double dfSettle = CURVES.getMulticurveProvider().getDiscountFactor(CUR, BOND_FIXED_SECURITY_1.getSettlementTime());
  // assertEquals(BOND_FIXED_SECURITY_1.getSettlementTime(), sensi.getYieldDiscountingSensitivities().get(REPO_CURVE_NAME).get(0).first,
  // 1E-8);
  // assertEquals("Fixed coupon bond security: dirty price curve sensitivity: repo curve",
  // BOND_FIXED_SECURITY_1.getSettlementTime() / dfSettle * pv / NOTIONAL,
  // sensi.getSensitivities().get(REPO_CURVE_NAME).get(0).second, 1E-8);
  // final double dfCpn0 = CURVES.getCurve(CREDIT_CURVE_NAME)
  // .getDiscountFactor(BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getPaymentTime());
  // assertEquals("Fixed coupon bond security: dirty price curve sensitivity: repo curve",
  // BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getPaymentTime(),
  // sensi.getSensitivities().get(CREDIT_CURVE_NAME).get(0).first, 1E-8);
  // assertEquals("Fixed coupon bond security: dirty price curve sensitivity: repo curve",
  // -BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getPaymentTime()
  // * BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getAmount() * dfCpn0 / dfSettle / NOTIONAL,
  // sensi.getSensitivities().get(CREDIT_CURVE_NAME).get(0).second, 1E-8);
  // }

  /**
   * Tests that the clean price for consecutive dates in the future are relatively smooth (no jump due to miscalculated accrued or missing coupon).
   */
  @Test
  public void cleanPriceSmoothness() {
    final int nbDateForward = 150;
    final ZonedDateTime[] forwardDate = new ZonedDateTime[nbDateForward];
    forwardDate[0] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_US_2, SETTLEMENT_DAYS_US, CALENDAR); // Spot
    final long[] jumpDays = new long[nbDateForward - 1];
    for (int i = 1; i < nbDateForward; i++) {
      forwardDate[i] = ScheduleCalculator.getAdjustedDate(forwardDate[i - 1], 1, CALENDAR);
      jumpDays[i - 1] = forwardDate[i].getLong(JulianFields.MODIFIED_JULIAN_DAY)
          - forwardDate[i - 1].getLong(JulianFields.MODIFIED_JULIAN_DAY);
    }
    final double[] cleanPriceForward = new double[nbDateForward];
    for (int i = 0; i < nbDateForward; i++) {
      final BondFixedSecurity bondForward = BOND_FIXED_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_US_2, forwardDate[i]);
      cleanPriceForward[i] = METHOD.cleanPriceFromCurves(bondForward, CURVES);
    }
    // Test note: 0.005 is roughly the difference between the coupon and the repo rate. The clean price is decreasing naturally by this
    // amount divided by (roughly) 365 every day.
    // Test note: On the coupon date there is a jump in the clean price: If the coupon is included the clean price due to coupon is
    // 0.04625/2*exp(-t*0.05)*exp(t*0.04) - 0.04625/2 = 7.94738E-05;
    // if the coupon is not included the impact is 0. The clean price is thus expected to jump by the above amount when the settlement is on
    // the coupon date 15-May-2012.
    final double couponJump = 7.94738E-05;
    for (int i = 1; i < nbDateForward; i++) {
      // TODO
      // assertEquals(cleanPriceForward[i] - (i == 87 ? couponJump : 0.0), cleanPriceForward[i - 1] - jumpDays[i - 1] * (0.005 / 365.0),
      // 3.0E-5);
    }
  }

}
