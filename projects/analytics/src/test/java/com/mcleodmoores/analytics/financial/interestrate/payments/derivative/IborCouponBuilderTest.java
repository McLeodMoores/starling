/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.interestrate.payments.derivative;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link IborCouponBuilder}.
 */
public class IborCouponBuilderTest {
  private static final Currency CCY = Currency.AUD;
  private static final double PAYMENT_TIME = 1.26;
  private static final double PAYMENT_YEAR_FRACTION = 1;
  private static final double NOTIONAL = 10000;
  private static final double FIXING_TIME = 1.001;
  private static final double FIXING_START = 1.002;
  private static final double FIXING_END = 1.252;
  private static final double FIXING_YEAR_FRACTION = 0.25;
  private static final IborIndex INDEX = new IborIndex(CCY, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false, "Ibor");
  private static final CouponIbor COUPON = new CouponIbor(CCY, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START, FIXING_END,
      FIXING_YEAR_FRACTION);

  /**
   * Tests that the coupon cannot be built unless the currency is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrencyIsSet() {
    IborCouponBuilder.builder()
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .build();
  }

  /**
   * Tests that the coupon cannot be built unless the fixing end time is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixingEndTimeIsSet() {
    IborCouponBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .build();
  }

  /**
   * Tests that the coupon cannot be built unless the fixing start time is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixingStartTimeIsSet() {
    IborCouponBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .build();
  }

  /**
   * Tests that the coupon cannot be built unless the fixing year fraction is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixingYearFractionIsSet() {
    IborCouponBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .build();
  }

  /**
   * Tests that the coupon cannot be built unless the fixing time is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixingTimeIsSet() {
    IborCouponBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .build();
  }

  /**
   * Tests that the coupon cannot be built unless the index is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testIndexIsSet() {
    IborCouponBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .build();
  }

  /**
   * Tests that the coupon cannot be built unless the notional is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNotionalIsSet() {
    IborCouponBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .build();
  }

  /**
   * Tests that the coupon cannot be built unless the payment time is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testPaymentTimeIsSet() {
    IborCouponBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingTime(FIXING_TIME)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .build();
  }

  /**
   * Tests that the coupon cannot be built unless the payment year fraction is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testPaymentYearFractionIsSet() {
    IborCouponBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .build();
  }

  /**
   * Tests building a coupon.
   */
  @Test
  public void testBuild() {
    final IborCouponBuilder builder = IborCouponBuilder.builder()
        .withCurrency(CCY)
        .withFixingPeriodEndTime(FIXING_END)
        .withFixingPeriodStartTime(FIXING_START)
        .withFixingYearFraction(FIXING_YEAR_FRACTION)
        .withFixingTime(FIXING_TIME)
        .withIndex(INDEX)
        .withNotional(NOTIONAL)
        .withPaymentTime(PAYMENT_TIME)
        .withPaymentYearFraction(PAYMENT_YEAR_FRACTION);
    assertEquals(builder.build(), COUPON);
  }
}
