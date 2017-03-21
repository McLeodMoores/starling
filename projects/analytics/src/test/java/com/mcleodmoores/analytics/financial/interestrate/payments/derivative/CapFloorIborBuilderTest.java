/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.interestrate.payments.derivative;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link CapFloorIborBuilder}.
 */
public class CapFloorIborBuilderTest {
  private static final Currency CCY = Currency.AUD;
  private static final double PAYMENT_TIME = 1.26;
  private static final double PAYMENT_YEAR_FRACTION = 1;
  private static final double NOTIONAL = 10000;
  private static final double FIXING_TIME = 1.001;
  private static final double FIXING_START = 1.002;
  private static final double FIXING_END = 1.252;
  private static final double FIXING_YEAR_FRACTION = 0.25;
  private static final IborIndex INDEX = new IborIndex(CCY, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false, "Ibor");
  private static final double STRIKE = 0.01;
  private static final CapFloorIbor CAP = new CapFloorIbor(CCY, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START, FIXING_END,
      FIXING_YEAR_FRACTION, STRIKE, true);
  private static final CapFloorIbor FLOOR = new CapFloorIbor(CCY, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START, FIXING_END, FIXING_YEAR_FRACTION, STRIKE,
      false);

  /**
   * Tests that the cap/floor cannot be built unless the currency is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrencyIsSet() {
    CapFloorIborBuilder.builder()
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .withStrike(STRIKE)
    .buildCap();
  }

  /**
   * Tests that the cap/floor cannot be built unless the fixing end time is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixingEndTimeIsSet() {
    CapFloorIborBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .withStrike(STRIKE)
    .buildCap();
  }

  /**
   * Tests that the cap/floor cannot be built unless the fixing start time is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixingStartTimeIsSet() {
    CapFloorIborBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .withStrike(STRIKE)
    .buildCap();
  }

  /**
   * Tests that the cap/floor cannot be built unless the fixing year fraction is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixingYearFractionIsSet() {
    CapFloorIborBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .withStrike(STRIKE)
    .buildCap();
  }

  /**
   * Tests that the cap/floor cannot be built unless the fixing time is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixingTimeIsSet() {
    CapFloorIborBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .withStrike(STRIKE)
    .buildCap();
  }

  /**
   * Tests that the cap/floor cannot be built unless the index is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testIndexIsSet() {
    CapFloorIborBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .withStrike(STRIKE)
    .buildCap();
  }

  /**
   * Tests that the cap/floor cannot be built unless the notional is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNotionalIsSet() {
    CapFloorIborBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .withStrike(STRIKE)
    .buildCap();
  }

  /**
   * Tests that the cap/floor cannot be built unless the payment time is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testPaymentTimeIsSet() {
    CapFloorIborBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingTime(FIXING_TIME)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .withStrike(STRIKE)
    .buildCap();
  }

  /**
   * Tests that the cap/floor cannot be built unless the payment year fraction is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testPaymentYearFractionIsSet() {
    CapFloorIborBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withStrike(STRIKE)
    .buildCap();
  }

  /**
   * Tests that the cap/floor cannot be built unless the strike is set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testStrikeIsSet() {
    CapFloorIborBuilder.builder()
    .withCurrency(CCY)
    .withFixingPeriodEndTime(FIXING_END)
    .withFixingPeriodStartTime(FIXING_START)
    .withFixingYearFraction(FIXING_YEAR_FRACTION)
    .withFixingTime(FIXING_TIME)
    .withIndex(INDEX)
    .withNotional(NOTIONAL)
    .withPaymentTime(PAYMENT_TIME)
    .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
    .buildCap();
  }

  /**
   * Tests building a cap.
   */
  @Test
  public void testBuildCap() {
    final CapFloorIborBuilder builder = CapFloorIborBuilder.builder()
        .withCurrency(CCY)
        .withFixingPeriodEndTime(FIXING_END)
        .withFixingPeriodStartTime(FIXING_START)
        .withFixingYearFraction(FIXING_YEAR_FRACTION)
        .withFixingTime(FIXING_TIME)
        .withIndex(INDEX)
        .withNotional(NOTIONAL)
        .withPaymentTime(PAYMENT_TIME)
        .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
        .withStrike(STRIKE);
    assertEquals(builder.build(true), CAP);
    assertEquals(builder.buildCap(), CAP);
  }

  /**
   * Tests building a floor.
   */
  @Test
  public void testBuildFloor() {
    final CapFloorIborBuilder builder = CapFloorIborBuilder.builder()
        .withCurrency(CCY)
        .withFixingPeriodEndTime(FIXING_END)
        .withFixingPeriodStartTime(FIXING_START)
        .withFixingYearFraction(FIXING_YEAR_FRACTION)
        .withFixingTime(FIXING_TIME)
        .withIndex(INDEX)
        .withNotional(NOTIONAL)
        .withPaymentTime(PAYMENT_TIME)
        .withPaymentYearFraction(PAYMENT_YEAR_FRACTION)
        .withStrike(STRIKE);
    assertEquals(builder.build(false), FLOOR);
    assertEquals(builder.buildFloor(), FLOOR);
  }
}
