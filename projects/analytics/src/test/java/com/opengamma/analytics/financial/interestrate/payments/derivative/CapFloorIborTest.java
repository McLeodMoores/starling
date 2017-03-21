/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the construction of Cap/floor on Ibor ({@link CapFloorIbor}).
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorIborTest {
  private static final Currency CUR = Currency.EUR;
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");

  private static final double NOTIONAL = 1000000;
  private static final double STRIKE = 0.04;
  // The dates are not standard but selected to ensure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR, BUSINESS_DAY, CALENDAR);
  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365;
  private static final double PAYMENT_YEAR_FRACTION = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FIXING_YEAR_FRACTION = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  // Reference date and time.
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27); //For conversion to derivative
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE, LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_START_DATE);
  private static final double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);

  private static final CapFloorIbor CAP = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
      FIXING_YEAR_FRACTION, STRIKE, true);
  private static final CapFloorIbor FLOOR = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_YEAR_FRACTION, STRIKE,
      false);

  /**
   * Tests that the fixing period start must be less than the fixing period end.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFixingBeforeFixingPeriod() {
    new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_START_TIME + 2e-3, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
  }

  /**
   * Tests that the fixing period start must be less than the fixing period end.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartBeforeEnd() {
    new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_END_TIME, FIXING_START_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
  }

  /**
   * Tests that the fixing year fraction cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonNegativeFixingYearFraction() {
    new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        -FIXING_YEAR_FRACTION, STRIKE, true);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    assertEquals(CAP.getCurrency(), CUR);
    assertEquals(CAP.getFixingAccrualFactor(), FIXING_YEAR_FRACTION);
    assertEquals(CAP.getFixingPeriodEndTime(), FIXING_END_TIME);
    assertEquals(CAP.getFixingPeriodStartTime(), FIXING_START_TIME);
    assertEquals(CAP.getFixingTime(), FIXING_TIME);
    assertEquals(CAP.getIndex(), INDEX);
    assertEquals(CAP.getNotional(), NOTIONAL);
    assertEquals(CAP.getPaymentTime(), PAYMENT_TIME);
    assertEquals(CAP.getPaymentYearFraction(), PAYMENT_YEAR_FRACTION);
    assertEquals(CAP.getReferenceAmount(), NOTIONAL);
    assertEquals(CAP.getStrike(), STRIKE);
    assertTrue(CAP.isCap());
    assertFalse(FLOOR.isCap());
  }

  /**
   * Tests the payoff function for caps and floors.
   */
  @Test
  public void testPayOff() {
    final double fixingRate = 0.05;
    assertEquals(CAP.payOff(fixingRate), Math.max(fixingRate - STRIKE, 0));
    final CapFloorIbor floor = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, false);
    assertEquals(floor.payOff(fixingRate), Math.max(STRIKE - fixingRate, 0));
  }

  /**
   * Tests the construction of a new cap/floor with a different strike.
   */
  @Test
  public void withStrike() {
    final double otherStrike = STRIKE + 0.01;
    final CapFloorIbor otherCap = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, otherStrike, true);
    final CapFloorIbor otherCapWith = CAP.withStrike(otherStrike);
    assertEquals(otherCapWith.getStrike(), otherStrike);
    assertEquals(otherCapWith, otherCap);
    final CapFloorIbor otherFloor = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, otherStrike, false);
    assertEquals(otherFloor, FLOOR.withStrike(otherStrike));
  }

  /**
   * Tests the construction of a new cap/floor with a different notional.
   */
  @Test
  public void withNotional() {
    final double notional = NOTIONAL + 10000;
    final CapFloorIbor cap = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, notional, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertEquals(cap, CAP.withNotional(notional));
    final CapFloorIbor otherFloor = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, notional, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, false);
    assertEquals(otherFloor, FLOOR.withNotional(notional));
  }

  /**
   * Tests the conversion of a cap or floor to the underlying coupon.
   */
  @Test
  public void testToCoupon() {
    final CouponIborSpread expected = new CouponIborSpread(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_YEAR_FRACTION, 0);
    assertEquals(CAP.toCoupon(), expected);
    assertEquals(FLOOR.toCoupon(), expected);
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    final CapFloorIbor floor = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, false);
    CapFloorIbor other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, false);
    assertEquals(floor, other);
    assertEquals(floor.hashCode(), other.hashCode());
    final CapFloorIbor cap = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertEquals(cap, other);
    assertEquals(cap.hashCode(), other.hashCode());
    other = new CapFloorIbor(Currency.AUD, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertNotEquals(other, cap);
    other = new CapFloorIbor(CUR, PAYMENT_TIME + 1, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertNotEquals(other, cap);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION + 1, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertNotEquals(other, cap);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL + 1, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertNotEquals(other, cap);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME - 1e-8, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertNotEquals(other, cap);
    final IborIndex index = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM, "Ibor");
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, index, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertNotEquals(other, cap);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME - 1e-8, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertNotEquals(other, cap);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME + 1,
        FIXING_YEAR_FRACTION, STRIKE, true);
    assertNotEquals(other, cap);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION + 1, STRIKE, true);
    assertNotEquals(other, cap);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE + 1, true);
    assertNotEquals(other, cap);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, false);
    assertNotEquals(other, cap);
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    String expected = "CapFloorIbor[_index=" + INDEX.toString() + ", _currency=" + CUR.toString() + ", _notional=" + NOTIONAL
        + ", _strike=" + STRIKE + ", _isCap=true" + ", _fixingTime=" + FIXING_TIME + ", _fixingPeriodStartTime=" + FIXING_START_TIME
        + ", _fixingPeriodEndTime=" + FIXING_END_TIME + ", _fixingAccrualFactor=" + FIXING_YEAR_FRACTION + ", _paymentTime=" + PAYMENT_TIME
        + ", _paymentYearFraction=" + PAYMENT_YEAR_FRACTION + ", _referenceAmount=" + NOTIONAL + "]";
    assertEquals(CAP.toString(), expected);
    expected = "CapFloorIbor[_index=" + INDEX.toString() + ", _currency=" + CUR.toString() + ", _notional=" + NOTIONAL
        + ", _strike=" + STRIKE + ", _isCap=false" + ", _fixingTime=" + FIXING_TIME + ", _fixingPeriodStartTime=" + FIXING_START_TIME
        + ", _fixingPeriodEndTime=" + FIXING_END_TIME + ", _fixingAccrualFactor=" + FIXING_YEAR_FRACTION + ", _paymentTime=" + PAYMENT_TIME
        + ", _paymentYearFraction=" + PAYMENT_YEAR_FRACTION + ", _referenceAmount=" + NOTIONAL + "]";
    assertEquals(FLOOR.toString(), expected);
  }

  /**
   * Tests building a cap/floor from a coupon.
   */
  @Test
  public void testBuildFromCoupon() {
    final CouponIbor coupon = new CouponIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_YEAR_FRACTION);
    assertEquals(CapFloorIbor.from(coupon, STRIKE, true), CAP);
    assertEquals(CapFloorIbor.from(coupon, STRIKE, false), FLOOR);
  }

  /**
   * Tests that the funding curve name cannot be retrieved if it has not been set.
   */
  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetFundingCurveName() {
    CAP.getFundingCurveName();
  }

  /**
   * Tests that the forward curve name cannot be retrieved if it has not been set.
   */
  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetForwardCurveName() {
    CAP.getForwardCurveName();
  }

  /**
   * Tests the deprecated version of this class i.e. the one with the funding and forward curve names. This test is here
   * to make sure that the null fields are handled correctly.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testDeprecated() {
    final String funding = "Funding";
    final String forward = "Forward";
    final CapFloorIbor cap = new CapFloorIbor(CUR, PAYMENT_TIME, funding, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, forward, STRIKE, true);
    assertEquals(cap.getFundingCurveName(), funding);
    assertEquals(cap.getForwardCurveName(), forward);
    CapFloorIbor other = new CapFloorIbor(CUR, PAYMENT_TIME, forward, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, forward, STRIKE, true);
    assertNotEquals(cap, other);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, funding, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, funding, STRIKE, true);
    assertNotEquals(cap, other);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, funding, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, forward, STRIKE + 1, true);
    assertEquals(cap.withStrike(STRIKE + 1), other);
    other = new CapFloorIbor(CUR, PAYMENT_TIME, funding, PAYMENT_YEAR_FRACTION, NOTIONAL * 2, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, forward, STRIKE, true);
    assertEquals(cap.withNotional(NOTIONAL * 2), other);
    final CouponIborSpread spreadCoupon = new CouponIborSpread(CUR, PAYMENT_TIME, funding, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME,
        FIXING_END_TIME, FIXING_YEAR_FRACTION, 0, forward);
    assertEquals(cap.toCoupon(), spreadCoupon);
    final String expected = "CapFloorIbor[_index=" + INDEX.toString() + ", _currency=" + CUR.toString() + ", _notional=" + NOTIONAL
        + ", _strike=" + STRIKE + ", _isCap=true" + ", _fixingTime=" + FIXING_TIME + ", _fixingPeriodStartTime=" + FIXING_START_TIME
        + ", _fixingPeriodEndTime=" + FIXING_END_TIME + ", _fixingAccrualFactor=" + FIXING_YEAR_FRACTION + ", _paymentTime=" + PAYMENT_TIME
        + ", _paymentYearFraction=" + PAYMENT_YEAR_FRACTION + ", _referenceAmount=" + NOTIONAL + ", _fundingCurveName=" + funding + ", _forwardCurveName=" + forward + "]";
    assertEquals(cap.toString(), expected);
    final CouponIbor coupon = new CouponIbor(CUR, PAYMENT_TIME, funding, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_YEAR_FRACTION, forward);
    assertEquals(CapFloorIbor.from(coupon, STRIKE, true), cap);
  }
}
