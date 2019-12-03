/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the discounting method for bond security.
 */
@Test(groups = TestGroup.UNIT)
public class BondSecurityDEDiscountingMethodTest {

  // Calculators
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final BondSecurityDiscountingMethod METHOD = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-8;

  // To derivatives
  private static final IssuerProviderDiscount CURVES = IssuerProviderDiscountDataSets.getCountrySpecificProvider();
  private static final Currency EUR = Currency.EUR;
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final DayCount DAY_COUNT_ACTACTICMA = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY_FIXED = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_FIXED = true;

  // DBR 1 1/2 02/15/23 - ISIN: DE0001102309
  private static final LegalEntity ISSUER_DE = new LegalEntity(null, "BUNDESREPUB. DEUTSCHLAND", null, null,
      Region.of("DEUTSCHLAND", Country.DE, Currency.EUR));
  private static final YieldConvention YIELD_CONVENTION_GERMANY = SimpleYieldConvention.GERMAN_BOND;
  private static final int SETTLEMENT_DAYS_DE = 3;
  private static final Period PAYMENT_TENOR_DE = Period.ofMonths(12);
  private static final int COUPON_PER_YEAR_DE = 1;
  private static final ZonedDateTime BOND_MATURITY_DE = DateUtils.getUTCDate(2023, 2, 15);
  private static final ZonedDateTime BOND_START_DE = DateUtils.getUTCDate(2013, 1, 18);
  private static final ZonedDateTime BOND_FIRSTCPN_DE = DateUtils.getUTCDate(2014, 2, 15);
  private static final double RATE_DE = 0.0150;
  private static final BondFixedSecurityDefinition BOND_DE_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(EUR, BOND_START_DE,
      BOND_FIRSTCPN_DE, BOND_MATURITY_DE, PAYMENT_TENOR_DE, RATE_DE,
      SETTLEMENT_DAYS_DE, CalendarAdapter.of(CALENDAR), DAY_COUNT_ACTACTICMA, BUSINESS_DAY_FIXED, YIELD_CONVENTION_GERMANY, IS_EOM_FIXED,
      ISSUER_DE);

  private static final ZonedDateTime REFERENCE_DATE_DE_1 = DateUtils.getUTCDate(2013, 5, 13);
  private static final ZonedDateTime SPOT_DATE_DE_1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_DE_1, SETTLEMENT_DAYS_DE, CALENDAR);
  private static final BondFixedSecurity BOND_DE_SECURITY_1 = BOND_DE_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_DE_1);

  /**
   *
   */
  @Test
  public void presentValueFixedMiddle() {
    final MulticurveProviderDiscountingDecoratedIssuer curves = new MulticurveProviderDiscountingDecoratedIssuer(CURVES, EUR,
        BOND_DE_SECURITY_1.getIssuerEntity());
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) BOND_DE_SECURITY_DEFINITION.getNominal().toDerivative(REFERENCE_DATE_DE_1);
    final AnnuityCouponFixed coupon = BOND_DE_SECURITY_DEFINITION.getCoupons().trimBefore(SPOT_DATE_DE_1).toDerivative(REFERENCE_DATE_DE_1);
    final MultipleCurrencyAmount pvNominal = nominal.accept(PVC, curves);
    final MultipleCurrencyAmount pvCoupon = coupon.accept(PVC, curves);
    final MultipleCurrencyAmount pv = METHOD.presentValue(BOND_DE_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: present value", pvNominal.plus(pvCoupon), pv);
  }

  /**
   *
   */
  @Test
  public void dirtyPriceFromYieldGerman() {
    final double yield = 0.02;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_DE_SECURITY_1, yield);
    final double dirtyPriceExpected = 0.9619145229504982; // To be check with another source.
    assertEquals("Fixed coupon bond security: dirty price from yield", dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  /**
   *
   */
  @Test
  public void cleanPriceFromYieldGerman() {
    final double yield = 0.02;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_DE_SECURITY_1, yield);
    final double cleanPriceExpected = METHOD.cleanPriceFromDirtyPrice(BOND_DE_SECURITY_1, dirtyPrice);
    final double cleanPrice = METHOD.cleanPriceFromYield(BOND_DE_SECURITY_1, yield);
    assertEquals("Fixed coupon bond security: dirty price from yield", cleanPriceExpected, cleanPrice, TOLERANCE_PRICE);
  }

  /**
   *
   */
  @Test
  public void dirtyPriceFromYieldGermanLastPeriod() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2022, 5, 15); // In last period
    final BondFixedSecurity bondSecurity = BOND_DE_SECURITY_DEFINITION.toDerivative(referenceDate);
    final double yield = 0.02;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = (1 + RATE_DE / COUPON_PER_YEAR_DE)
        / (1 + bondSecurity.getFactorToNextCoupon() * yield / COUPON_PER_YEAR_DE);
    assertEquals("Fixed coupon bond security: dirty price from yield German bond - last period", dirtyPriceExpected, dirtyPrice,
        TOLERANCE_PRICE);
  }

}
