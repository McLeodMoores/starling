/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of Ibor coupon.
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final WorkingDayCalendar TARGET = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final IndexIborMaster INDEX_IBOR_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex INDEX_EURIBOR3M = INDEX_IBOR_MASTER.getIndex("EURIBOR3M");
  private static final Currency EUR = INDEX_EURIBOR3M.getCurrency();
  // Coupon
  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365;
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 8, 24);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE, -INDEX_EURIBOR3M.getSpotLag(), TARGET); // In arrears
  private static final ZonedDateTime FIXING_START_DATE = ACCRUAL_END_DATE;
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, INDEX_EURIBOR3M, TARGET);

  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
  private static final double FIXING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE);
  private static final double FIXING_START_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_START_DATE);
  private static final double FIXING_END_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL_FACTOR = INDEX_EURIBOR3M.getDayCount().getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);

  private static final CouponIbor CPN_IBOR = new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
      FIXING_ACCRUAL_FACTOR);

  /**
   * Tests that the currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CouponIbor(null, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
  }

  /**
   * Tests that the index cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, null, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
  }

  /**
   * Tests that the coupon currency must be the same as the index currency.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIncompatibleCurrency() {
    new CouponIbor(Currency.USD, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
  }

  /**
   * Tests that the fixing period start must be less than the fixing period end.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFixingBeforeFixingPeriod() {
    new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_START_TIME + 2e-3, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_ACCRUAL_FACTOR);
  }

  /**
   * Tests that the fixing period start must be less than the fixing period end.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartBeforeEnd() {
    new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_END_TIME, FIXING_START_TIME,
        FIXING_ACCRUAL_FACTOR);
  }

  /**
   * Tests that the fixing year fraction cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonNegativeFixingYearFraction() {
    new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
        -FIXING_ACCRUAL_FACTOR);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    assertEquals(CPN_IBOR.getCurrency(), EUR);
    assertEquals(CPN_IBOR.getFixingAccrualFactor(), FIXING_ACCRUAL_FACTOR);
    assertEquals(CPN_IBOR.getFixingPeriodEndTime(), FIXING_END_TIME);
    assertEquals(CPN_IBOR.getFixingPeriodStartTime(), FIXING_START_TIME);
    assertEquals(CPN_IBOR.getFixingTime(), FIXING_TIME);
    assertEquals(CPN_IBOR.getIndex(), INDEX_EURIBOR3M);
    assertEquals(CPN_IBOR.getNotional(), NOTIONAL);
    assertEquals(CPN_IBOR.getPaymentTime(), PAYMENT_TIME);
    assertEquals(CPN_IBOR.getPaymentYearFraction(), ACCRUAL_FACTOR);
    assertEquals(CPN_IBOR.getReferenceAmount(), NOTIONAL);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetter() {
    assertEquals(CPN_IBOR.getCurrency(), EUR);
    assertEquals(CPN_IBOR.getIndex(), INDEX_EURIBOR3M);
    assertEquals(CPN_IBOR.getFixingPeriodStartTime(), FIXING_START_TIME);
    assertEquals(CPN_IBOR.getFixingPeriodEndTime(), FIXING_END_TIME);
    assertEquals(CPN_IBOR.getFixingAccrualFactor(), FIXING_ACCRUAL_FACTOR);
  }

  /**
   * Tests the method that replaces the notional.
   */
  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 1000;
    final CouponIbor expected = new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, notional, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_ACCRUAL_FACTOR);
    assertEquals(CPN_IBOR.withNotional(notional), expected);
  }

  /**
   * Tests the equal and hash code.
   */
  @Test
  public void testEqualHash() {
    assertEquals(CPN_IBOR, CPN_IBOR);
    CouponIbor other = new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
    assertEquals(other, CPN_IBOR);
    assertEquals(other.hashCode(), CPN_IBOR.hashCode());
    other = new CouponIbor(EUR, PAYMENT_TIME + 0.1, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
    assertNotEquals(CPN_IBOR, other);
    other = new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR + 0.1, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
    assertNotEquals(CPN_IBOR, other);
    other = new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL + 1.0, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
    assertNotEquals(CPN_IBOR, other);
    other = new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME - 0.1, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
    assertNotEquals(CPN_IBOR, other);
    other = new CouponIbor(Currency.USD, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_IBOR_MASTER.getIndex("USDLIBOR3M"), FIXING_START_TIME,
        FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
    assertNotEquals(CPN_IBOR, other);
    other = new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME + 0.1, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR);
    assertNotEquals(CPN_IBOR, other);
    other = new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME + 0.1, FIXING_ACCRUAL_FACTOR);
    assertNotEquals(CPN_IBOR, other);
    other = new CouponIbor(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR + 0.1);
    assertNotEquals(CPN_IBOR, other);
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    final String expected = "CouponIbor[_index=" + INDEX_EURIBOR3M.toString() + ", _currency=" + EUR.toString() + ", _notional=" + NOTIONAL
        + ", _fixingTime=" + FIXING_TIME + ", _fixingPeriodStartTime=" + FIXING_START_TIME + ", _fixingPeriodEndTime=" + FIXING_END_TIME
        + ", _fixingAccrualFactor=" + FIXING_ACCRUAL_FACTOR + ", _paymentTime=" + PAYMENT_TIME
        + ", _paymentYearFraction=" + ACCRUAL_FACTOR + ", _referenceAmount=" + NOTIONAL + "]";
    assertEquals(CPN_IBOR.toString(), expected);
  }

  /**
   * Tests that the funding curve name cannot be retrieved if it has not been set.
   */
  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetFundingCurveName() {
    CPN_IBOR.getFundingCurveName();
  }

  /**
   * Tests that the forward curve name cannot be retrieved if it has not been set.
   */
  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetForwardCurveName() {
    CPN_IBOR.getForwardCurveName();
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
    final CouponIbor cap = new CouponIbor(EUR, PAYMENT_TIME, funding, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_ACCRUAL_FACTOR, forward);
    assertEquals(cap.getFundingCurveName(), funding);
    assertEquals(cap.getForwardCurveName(), forward);
    CouponIbor other = new CouponIbor(EUR, PAYMENT_TIME, forward, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_ACCRUAL_FACTOR, forward);
    assertNotEquals(cap, other);
    other = new CouponIbor(EUR, PAYMENT_TIME, funding, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_ACCRUAL_FACTOR, funding);
    assertNotEquals(cap, other);
    other = new CouponIbor(EUR, PAYMENT_TIME, funding, ACCRUAL_FACTOR, NOTIONAL * 2, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_ACCRUAL_FACTOR, forward);
    assertEquals(cap.withNotional(NOTIONAL * 2), other);
    final String expected = "CouponIbor[_index=" + INDEX_EURIBOR3M.toString() + ", _currency=" + EUR.toString() + ", _notional=" + NOTIONAL
        + ", _fixingTime=" + FIXING_TIME + ", _fixingPeriodStartTime=" + FIXING_START_TIME + ", _fixingPeriodEndTime=" + FIXING_END_TIME
        + ", _fixingAccrualFactor=" + FIXING_ACCRUAL_FACTOR + ", _paymentTime=" + PAYMENT_TIME + ", _paymentYearFraction=" + ACCRUAL_FACTOR
        + ", _referenceAmount=" + NOTIONAL + ", _fundingCurveName=" + funding + ", _forwardCurveName=" + forward + "]";
    assertEquals(cap.toString(), expected);
  }

}
