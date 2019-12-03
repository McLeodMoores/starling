/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the discounting method for Italian Government bond security.
 */
@Test(groups = TestGroup.UNIT)
public class BondSecurityAUDiscountingMethodTest {

  private static final LegalEntity ISSUER_LEGAL_ENTITY = IssuerProviderDiscountDataSets.getIssuersAUS();
  private static final Currency AUD = Currency.AUD;

  // AUD defaults
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 3;
  private static final int EX_DIVIDEND_DAYS = 7;
  private static final YieldConvention YIELD_CONVENTION = SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND;
  private static final double NOTIONAL = 100;

  private static final int NB_BOND = 5;
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[NB_BOND];
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final double[] RATE = new double[NB_BOND];
  private static final double[] YIELD = new double[NB_BOND];
  private static final double[] DIRTY_PRICE = new double[NB_BOND];
  private static final double[] ACCRUED = new double[NB_BOND];
  private static final double[] MODIFIED_DURATION = new double[NB_BOND];
  private static final double[] CONVEXITY = new double[NB_BOND];
  private static final BondFixedSecurityDefinition[] BOND_FIXED_SECURITY_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  private static final ZonedDateTime[] SETTLEMENT_DATE = new ZonedDateTime[NB_BOND];
  private static final ZonedDateTime[] REFERENCE_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurity[] BOND_FIXED_SECURITY = new BondFixedSecurity[NB_BOND];
  // Bond 1
  static {
    final int index = 0;
    START_ACCRUAL_DATE[index] = DateUtils.getUTCDate(1999, 7, 15);
    MATURITY_DATE[index] = DateUtils.getUTCDate(2005, 7, 15);
    SETTLEMENT_DATE[index] = DateUtils.getUTCDate(2000, 3, 10);
    RATE[index] = 0.07;
    YIELD[index] = 0.075;
    DIRTY_PRICE[index] = 0.988739378;
    ACCRUED[index] = 0.010576923;
    MODIFIED_DURATION[index] = 4.333429426;
    CONVEXITY[index] = 23.04976975;
  }
  // Bond 2
  static {
    final int index = 1;
    START_ACCRUAL_DATE[index] = DateUtils.getUTCDate(1999, 5, 15);
    MATURITY_DATE[index] = DateUtils.getUTCDate(2004, 5, 15);
    SETTLEMENT_DATE[index] = DateUtils.getUTCDate(2000, 5, 11);
    RATE[index] = 0.054;
    YIELD[index] = 0.065;
    DIRTY_PRICE[index] = 0.96111981;
    ACCRUED[index] = -0.000593407;
    MODIFIED_DURATION[index] = 3.538390927;
    CONVEXITY[index] = 14.96819851;
  }
  // Bond 3
  static {
    final int index = 2;
    START_ACCRUAL_DATE[index] = DateUtils.getUTCDate(1999, 2, 15);
    MATURITY_DATE[index] = DateUtils.getUTCDate(2001, 2, 15);
    SETTLEMENT_DATE[index] = DateUtils.getUTCDate(2000, 8, 9);
    RATE[index] = 0.07;
    YIELD[index] = 0.076;
    DIRTY_PRICE[index] = 0.997038448 - 0.001153846;
    ACCRUED[index] = -0.001153846;
    MODIFIED_DURATION[index] = 0.497575642;
    CONVEXITY[index] = 0.487261501;
  }
  // Bond 4
  static {
    final int index = 3;
    START_ACCRUAL_DATE[index] = DateUtils.getUTCDate(1999, 7, 15);
    MATURITY_DATE[index] = DateUtils.getUTCDate(2001, 7, 15);
    SETTLEMENT_DATE[index] = DateUtils.getUTCDate(2001, 4, 13);
    RATE[index] = 0.0525;
    YIELD[index] = 0.0700;
    DIRTY_PRICE[index] = 1.008266938;
    ACCRUED[index] = 88.0d / 181.0d * RATE[index] * 0.5d;
    MODIFIED_DURATION[index] = 0.250329735;
    CONVEXITY[index] = 0.125329953;
  }
  // Bond 5
  static {
    final int index = 4;
    START_ACCRUAL_DATE[index] = DateUtils.getUTCDate(1999, 7, 15);
    MATURITY_DATE[index] = DateUtils.getUTCDate(2001, 7, 15);
    SETTLEMENT_DATE[index] = DateUtils.getUTCDate(2001, 7, 12);
    RATE[index] = 0.0525;
    YIELD[index] = 0.0700;
    DIRTY_PRICE[index] = 0.999424988;
    ACCRUED[index] = -3.0d / 181.0d * RATE[index] * 0.5d;
    MODIFIED_DURATION[index] = 0.008214452;
    CONVEXITY[index] = 0.000134954;
  }
  static {
    for (int i = 0; i < NB_BOND; i++) {
      BOND_FIXED_SECURITY_DEFINITION[i] = BondFixedSecurityDefinition.from(AUD, START_ACCRUAL_DATE[i], MATURITY_DATE[i], PAYMENT_TENOR,
          RATE[i], SETTLEMENT_DAYS, NOTIONAL, EX_DIVIDEND_DAYS, CalendarAdapter.of(CALENDAR), DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION,
          IS_EOM, ISSUER_LEGAL_ENTITY, "Repo");
      REFERENCE_DATE[i] = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE[i], -SETTLEMENT_DAYS, CALENDAR);
      BOND_FIXED_SECURITY[i] = BOND_FIXED_SECURITY_DEFINITION[i].toDerivative(REFERENCE_DATE[i]);
    }
  }

  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-7;
  private static final double TOLERANCE_YIELD = 1.0E-7;
  private static final double TOLERANCE_CONV = 1.0E-8;

  /**
   *
   */
  @Test
  public void accruedInterest() {
    for (int i = 0; i < NB_BOND; i++) {
      final double accruedAtSettle = BOND_FIXED_SECURITY[i].getAccruedInterest();
      assertEquals("Fixed coupon bond security: AUD - accrued interest - bond:" + i, ACCRUED[i] * NOTIONAL, accruedAtSettle,
          TOLERANCE_PRICE);
    }
  }

  /**
   *
   */
  @Test
  public void prices() {
    for (int i = 0; i < NB_BOND; i++) {
      final double dirtyPriceComputed = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY[i], YIELD[i]);
      assertEquals("Fixed coupon bond security: AUD - dirty price from yield - bond:" + i, DIRTY_PRICE[i], dirtyPriceComputed,
          TOLERANCE_PRICE);
      final double cleanFromDirty = METHOD_BOND_SECURITY.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY[i], DIRTY_PRICE[i]);
      assertEquals("Fixed coupon bond security: AUD - clean price - bond:" + i,
          DIRTY_PRICE[i] - BOND_FIXED_SECURITY[i].getAccruedInterest() / NOTIONAL, cleanFromDirty,
          TOLERANCE_PRICE);
    }
  }

  /**
   *
   */
  @Test
  public void yield() {
    for (int i = 0; i < NB_BOND; i++) {
      final double yieldComputed = METHOD_BOND_SECURITY.yieldFromDirtyPrice(BOND_FIXED_SECURITY[i], DIRTY_PRICE[i]);
      assertEquals("Fixed coupon bond security: AUD - yield - bond:" + i, YIELD[i], yieldComputed, TOLERANCE_YIELD);
    }
  }

  /**
   *
   */
  @Test
  public void duration() {
    for (int i = 0; i < NB_BOND; i++) {
      final double mdComputed = METHOD_BOND_SECURITY.modifiedDurationFromYield(BOND_FIXED_SECURITY[i], YIELD[i]);
      assertEquals("Fixed coupon bond security: AUD - duration - bond:" + i, MODIFIED_DURATION[i], mdComputed,
          TOLERANCE_YIELD);
    }
  }

  /**
   *
   */
  @Test
  public void convexity() {
    for (int i = 0; i < NB_BOND; i++) {
      final double convexityComputed = METHOD_BOND_SECURITY.convexityFromYield(BOND_FIXED_SECURITY[i], YIELD[i]);
      assertEquals("Fixed coupon bond security: AUD - convexity - bond:" + i, CONVEXITY[i], convexityComputed,
          TOLERANCE_CONV);
    }
  }

}
