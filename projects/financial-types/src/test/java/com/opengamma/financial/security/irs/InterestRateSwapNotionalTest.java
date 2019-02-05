/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security.irs;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;


/**
 * Tests for {@link InterestRateSwapNotional}.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateSwapNotionalTest {

  private final static double TOLERANCE = 1e-10;

  /**
   * Tests a constant notional value.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testConstantNotional() {
    final InterestRateSwapNotional notional = InterestRateSwapNotional.of(Currency.GBP, 1e6);
    Assert.assertEquals(1e6, notional.getInitialAmount(), TOLERANCE);
    Assert.assertEquals(1e6, notional.getAmount(), TOLERANCE);
    Assert.assertEquals(1e6, notional.getAmount(LocalDate.MAX), TOLERANCE);
    Assert.assertEquals(1e6, notional.getAmount(LocalDate.MIN), TOLERANCE);
    Assert.assertEquals(1e6, notional.getAmount(LocalDate.now()), TOLERANCE);
    Assert.assertEquals(Currency.GBP, notional.getCurrency());
  }

  /**
   * Tests an amortizing notional using outright values.
   */
  @Test
  public void testAmortizingNotional() {
    final LocalDate start = LocalDate.now();
    final List<LocalDate> dates = Lists.newArrayList(start, start.plusYears(1), start.plusYears(2), start.plusYears(3));
    final List<Double> notionals = Lists.newArrayList(1e6, 1e5, 1e4, 1e3);
    final List<Rate.ShiftType> shiftTypes = Lists.newArrayList(Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT);
    final InterestRateSwapNotional amortizing = InterestRateSwapNotional.of(Currency.GBP, dates, notionals, shiftTypes);
    Assert.assertEquals(1e6, amortizing.getInitialAmount(), TOLERANCE);
    Assert.assertEquals(1e3, amortizing.getAmount(LocalDate.MAX), TOLERANCE);
    Assert.assertEquals(1e6, amortizing.getAmount(LocalDate.MIN), TOLERANCE);
    Assert.assertEquals(1e6, amortizing.getAmount(start), TOLERANCE);
    Assert.assertEquals(1e6, amortizing.getAmount(start.plusMonths(11)), TOLERANCE);
    Assert.assertEquals(1e5, amortizing.getAmount(start.plusYears(1)), TOLERANCE);
    Assert.assertEquals(1e4, amortizing.getAmount(start.plusYears(2)), TOLERANCE);
    Assert.assertEquals(1e3, amortizing.getAmount(start.plusYears(3)), TOLERANCE);
    Assert.assertEquals(1e4, amortizing.getAmount(start.plusYears(3).minusDays(1)), TOLERANCE);
    Assert.assertEquals(Currency.GBP, amortizing.getCurrency());
  }

  /**
   * Tests an amortizing notional using the default shift (outright).
   */
  @Test
  public void testAmortizingNotional2() {
    final LocalDate start = LocalDate.now();
    final List<LocalDate> dates = Lists.newArrayList(start, start.plusYears(1), start.plusYears(2), start.plusYears(3));
    final List<Double> notionals = Lists.newArrayList(1e6, 1e5, 1e4, 1e3);
    final InterestRateSwapNotional amortizing = InterestRateSwapNotional.of(Currency.GBP, dates, notionals);
    Assert.assertEquals(1e6, amortizing.getInitialAmount(), TOLERANCE);
    Assert.assertEquals(1e3, amortizing.getAmount(LocalDate.MAX), TOLERANCE);
    Assert.assertEquals(1e6, amortizing.getAmount(LocalDate.MIN), TOLERANCE);
    Assert.assertEquals(1e6, amortizing.getAmount(start), TOLERANCE);
    Assert.assertEquals(1e6, amortizing.getAmount(start.plusMonths(11)), TOLERANCE);
    Assert.assertEquals(1e5, amortizing.getAmount(start.plusYears(1)), TOLERANCE);
    Assert.assertEquals(1e4, amortizing.getAmount(start.plusYears(2)), TOLERANCE);
    Assert.assertEquals(1e3, amortizing.getAmount(start.plusYears(3)), TOLERANCE);
    Assert.assertEquals(1e4, amortizing.getAmount(start.plusYears(3).minusDays(1)), TOLERANCE);
    Assert.assertEquals(Currency.GBP, amortizing.getCurrency());
  }

  /**
   * Tests an notional using additive shifts.
   */
  @Test
  public void testAdditiveShifts() {
    final LocalDate start = LocalDate.now();
    final List<LocalDate> dates = Lists.newArrayList(start, start.plusYears(1), start.plusYears(2), start.plusYears(3));
    final List<Double> notionals = Lists.newArrayList(1e6d, -2.5e5d, -2.5e5d, -2.5e5d);
    final List<Rate.ShiftType> shiftTypes = Lists.newArrayList(Rate.ShiftType.OUTRIGHT, Rate.ShiftType.ADDITIVE, Rate.ShiftType.ADDITIVE, Rate.ShiftType.ADDITIVE);
    final InterestRateSwapNotional amortizing = InterestRateSwapNotional.of(Currency.GBP, dates, notionals, shiftTypes);
    Assert.assertEquals(1e6, amortizing.getInitialAmount(), TOLERANCE);
    Assert.assertEquals(2.5e5d, amortizing.getAmount(LocalDate.MAX), TOLERANCE);
    Assert.assertEquals(1e6, amortizing.getAmount(LocalDate.MIN), TOLERANCE);
    Assert.assertEquals(1e6, amortizing.getAmount(start), TOLERANCE);
    Assert.assertEquals(1e6, amortizing.getAmount(start.plusMonths(11)), TOLERANCE);
    Assert.assertEquals(7.5e5d, amortizing.getAmount(start.plusYears(1)), TOLERANCE);
    Assert.assertEquals(5e5d, amortizing.getAmount(start.plusYears(2)), TOLERANCE);
    Assert.assertEquals(2.5e5d, amortizing.getAmount(start.plusYears(3)), TOLERANCE);
    Assert.assertEquals(5e5d, amortizing.getAmount(start.plusYears(3).minusDays(1)), TOLERANCE);
    Assert.assertEquals(Currency.GBP, amortizing.getCurrency());
  }

  /**
   * Tests that the first shift type must be outright.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDeltaInitialNotional() {
    final LocalDate start = LocalDate.now();
    final List<LocalDate> dates = Lists.newArrayList(start, start.plusYears(1), start.plusYears(2), start.plusYears(3));
    final List<Double> notionals = Lists.newArrayList(1e6, 1e5, 1e4, 1e3);
    final List<Rate.ShiftType> shiftTypes = Lists.newArrayList(Rate.ShiftType.DELTA, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT);
    InterestRateSwapNotional.of(Currency.GBP, dates, notionals, shiftTypes);
  }

}
