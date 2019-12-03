/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link AnalyticBond}.
 */
@Test(groups = TestGroup.UNIT)
public class AnalyticBondTest {
  private static final LocalDate START_DATE = LocalDate.of(2019, 3, 15);
  private static final LocalDate END_DATE = LocalDate.of(2021, 3, 15);
  private static final Period FREQUENCY = Period.ofMonths(3);
  private static final StubType STUB_TYPE = StubType.FRONTLONG;
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final boolean PROTECT_START = true;
  private static final LocalDate TODAY = LocalDate.of(2019, 3, 20);
  private static final double COUPON = 0.03;
  private static final double RECOVERY_RATE = 0.4;
  private static final DayCount ACCRUAL_DC = DayCounts.ACT_360;
  private static final DayCount CURVE_DC = DayCounts.ACT_36525;
  private static final ISDAPremiumLegSchedule SCHEDULE = 
      new ISDAPremiumLegSchedule(START_DATE, END_DATE, FREQUENCY, STUB_TYPE, BDC, CalendarAdapter.of(CALENDAR), PROTECT_START);
  
  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullToday1() {
    new BondAnalytic(null, COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC);
  }
  
  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullToday2() {
    new BondAnalytic(null, COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC, CURVE_DC);
  }

  /**
   * Tests that the coupon cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeCoupon1() {
    new BondAnalytic(TODAY, -COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC);
  }
  
  /**
   * Tests that the coupon cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeCoupon2() {
    new BondAnalytic(TODAY, -COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC, CURVE_DC);
  }

  /**
   * Tests that the schedule cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSchedule1() {
    new BondAnalytic(TODAY, COUPON, null, RECOVERY_RATE, ACCRUAL_DC);
  }
  
  /**
   * Tests that the schedule cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSchedule2() {
    new BondAnalytic(TODAY, COUPON, null, RECOVERY_RATE, ACCRUAL_DC, CURVE_DC);
  }

  /**
   * Tests that the recovery rate cannot be negative. 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeRecoveryRate1() {
    new BondAnalytic(TODAY, COUPON, SCHEDULE, -RECOVERY_RATE, ACCRUAL_DC);
  }
  
  /**
   * Tests that the recovery rate cannot be negative. 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeRecoveryRate2() {
    new BondAnalytic(TODAY, COUPON, SCHEDULE, -RECOVERY_RATE, ACCRUAL_DC, CURVE_DC);
  }

  /**
   * Tests that the recovery rate cannot be negative. 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeRecoveryRate3() {
    new BondAnalytic(new double[] {1, 2}, new double[] {100, 100}, -RECOVERY_RATE, 0.04);
  }

  /**
   * Tests that the recovery rate cannot be greater than one.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRecoveryRateGreaterThanOne1() {
    new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE + 1, ACCRUAL_DC);
  }
  
  /**
   * Tests that the recovery rate cannot be greater than one.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRecoveryRateGreaterThanOne2() {
    new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE + 1, ACCRUAL_DC, CURVE_DC);
  }

  /**
   * Tests that the recovery rate cannot be greater than one.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRecoveryRateGreaterThanOne3() {
    new BondAnalytic(new double[] {1, 2}, new double[] {100, 100}, RECOVERY_RATE + 1, 0.04);
  }

  /**
   * Tests that the accrual daycount cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualDayCount1() {
    new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE, null);
  }

  /**
   * Tests that the accrual daycount cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualDayCount2() {
    new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE, null, CURVE_DC);
  }
  
  /**
   * Tests that the curve daycount cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveDayCount() {
    new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC, null);
  }
 
  /**
   * Tests that the payment times cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentTimes() {
    new BondAnalytic(null, new double[] {100, 100}, RECOVERY_RATE, 0);
  }
  
  /**
   * Tests that the payment amounts cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentAmounts() {
    new BondAnalytic(new double[] {1, 2}, null, RECOVERY_RATE, 0);
  }
  
  /**
   * Tests that there must be an amount per payment time.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentAmounts() {
    new BondAnalytic(new double[] {1, 2}, new double[] {1}, RECOVERY_RATE, 0);
  }
  
  /**
   * Tests that the accrued interest must not be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeAccruedInterest() {
    new BondAnalytic(new double[] {1}, new double[] {100}, RECOVERY_RATE, -0.3);
  }
  
  /**
   * Tests that the payment times must be positive.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositivePaymentTimes() {
    new BondAnalytic(new double[] {-1, 2}, new double[] {100, 200}, RECOVERY_RATE, 0);
  }
  
  /**
   * Tests that the payment times must be increasing.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIncreasingPaymentTimes() {
    new BondAnalytic(new double[] {1, 3, 2}, new double[] {100, 100, 100}, RECOVERY_RATE, 0);
  }
  
  /**
   * Tests constructor equivalence.
   */
  @Test
  public void testConstructorEquivalence() {
    final BondAnalytic bond1 = new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC);
    final BondAnalytic bond2 = new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC, DayCounts.ACT_365);
    final double[] paymentTimes = new double[] {0.24383561643835616, 0.4931506849315068, 0.7424657534246575, 0.9917808219178083, 
        1.2410958904109588, 1.4931506849315068, 1.7424657534246575, 1.989041095890411};
    final double[] paymentAmounts = new double[] {0.007833333333333333, 0.0075833333333333326, 
        0.0075833333333333326, 0.0075833333333333326, 0.0075833333333333326, 0.007666666666666665, 0.0075833333333333326, 
        1.0075833333333333};
    final BondAnalytic bond3 = new BondAnalytic(paymentTimes, paymentAmounts, RECOVERY_RATE, 4.1666666666666664E-4);
    assertEquals(bond1, bond2);
    assertEquals(bond1, bond3);
    assertEquals(bond2, bond3);
    for (int i = 0; i < 8; i++) {
      assertEquals(bond3.getPaymentAmount(i), paymentAmounts[i], 1e-15);
      assertEquals(bond3.getPaymentTime(i), paymentTimes[i], 1e-15);
    }
  }
  
  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final BondAnalytic bond = new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC, CURVE_DC);
    BondAnalytic other = new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC, CURVE_DC);
    assertEquals(bond.getNPayments(), 8);
    assertEquals(bond.getRecoveryRate(), RECOVERY_RATE, 1e-15);
    assertEquals(bond.getAccruedInterest(), 25 / 60000., 1e-15);
    assertEquals(bond, bond);
    assertEquals(bond.toString(), "BondAnalytic[accruedInterest=4.1666666666666664E-4, recoveryRate=0.4, "
        + "paymentTimes=[0.243668720054757, 0.4928131416837782, 0.7419575633127995, 0.9911019849418207, 1.2402464065708418, "
        + "1.4921286789869952, 1.7412731006160165, 1.9876796714579055], paymentAmounts=[0.007833333333333333, 0.0075833333333333326, "
        + "0.0075833333333333326, 0.0075833333333333326, 0.0075833333333333326, 0.007666666666666665, 0.0075833333333333326, "
        + "1.0075833333333333]]");
    assertNotEquals(SCHEDULE, bond);
    other = new BondAnalytic(TODAY.plusDays(1), COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC, CURVE_DC);
    assertNotEquals(other, bond);
    other = new BondAnalytic(TODAY, COUPON * 0.5, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC, CURVE_DC);
    assertNotEquals(other, bond);
    other = new BondAnalytic(TODAY, COUPON, 
        new ISDAPremiumLegSchedule(START_DATE, END_DATE, Period.ofMonths(6), STUB_TYPE, BDC, CalendarAdapter.of(CALENDAR), PROTECT_START), 
        RECOVERY_RATE, ACCRUAL_DC, CURVE_DC);
    assertNotEquals(other, bond);
    other = new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE * 0.5, ACCRUAL_DC, CURVE_DC);
    assertNotEquals(other, bond);
    other = new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE, CURVE_DC, CURVE_DC);
    assertNotEquals(other, bond);
    other = new BondAnalytic(TODAY, COUPON, SCHEDULE, RECOVERY_RATE, ACCRUAL_DC, ACCRUAL_DC);
    assertNotEquals(other, bond);
  }
  
}
