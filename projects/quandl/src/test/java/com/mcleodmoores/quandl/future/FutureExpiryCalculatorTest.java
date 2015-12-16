/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.mcleodmoores.quandl.convention.QuandlFinancialConvention;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.mcleodmoores.quandl.future.FutureExpiryCalculator;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;
import com.opengamma.util.time.Tenor;


/**
 * Unit tests for {@link FutureExpiryCalculator}.
 */
public class FutureExpiryCalculatorTest {
  //TODO once the test holiday source is in, check they are taken into account correctly.
  /** Short-term interest rate future convention */
  private static final QuandlFinancialConvention STIR = new QuandlStirFutureConvention("Test", ExternalIdBundle.of("Test", "Test"), Currency.USD,
      Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, "11:00", "America/Chicago", 2500, ExternalId.of("Test", "Test"), 3, DayOfWeek.MONDAY.name());
  /** Fed funds future convention */
  private static final QuandlFinancialConvention FF = new QuandlFedFundsFutureConvention("Test", ExternalIdBundle.of("Test", "Test"), "11:00",
      "America/Chicago", 50000, ExternalId.of("Test", "Test"));

  /**
   * Tests the behaviour when a null holiday source is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullHolidaySource() {
    new FutureExpiryCalculator(null);
  }

  /**
   * Tests the behaviour when a null expiry year is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullYear1() {
    final Function2<Character, Integer, Expiry> calculator = STIR.accept(new FutureExpiryCalculator());
    calculator.apply('H', null);
  }

  /**
   * Tests the behaviour when a bad month code is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testBadMonthCode1() {
    final Function2<Character, Integer, Expiry> calculator = STIR.accept(new FutureExpiryCalculator());
    calculator.apply('A', 2014);
  }

  /**
   * Tests the behaviour when a null expiry year is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullYear2() {
    final Function2<Character, Integer, Expiry> calculator = FF.accept(new FutureExpiryCalculator());
    calculator.apply('H', null);
  }

  /**
   * Tests the behaviour when a bad month code is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testBadMonthCode2() {
    final Function2<Character, Integer, Expiry> calculator = FF.accept(new FutureExpiryCalculator());
    calculator.apply('A', 2014);
  }

  /**
   * Tests the calculation of the expiry of a short-term interest rate future (usually an IMM expiry i.e.
   * the third Monday of a month).
   */
  @Test
  public void testStirFutureExpiry() {
    final Function2<Character, Integer, Expiry> expiryCalculator = STIR.accept(new FutureExpiryCalculator());
    final Expiry expiry = expiryCalculator.apply('H', 2014);
    assertEquals(expiry.getAccuracy(), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    assertEquals(expiry.getExpiry(), ZonedDateTime.of(LocalDateTime.of(2014, 3, 17, 11, 0), ZoneId.of("America/Chicago")));
  }

  /**
   * Tests the calculation of the expiry of a Fed funds future, which is the last business day of a month.
   */
  @Test
  public void testFedFundsFutureExpiry() {
    final Function2<Character, Integer, Expiry> expiryCalculator = FF.accept(new FutureExpiryCalculator());
    final Expiry expiry = expiryCalculator.apply('K', 2014);
    assertEquals(expiry.getAccuracy(), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    assertEquals(expiry.getExpiry(), ZonedDateTime.of(LocalDateTime.of(2014, 5, 30, 11, 0), ZoneId.of("America/Chicago")));
  }
}
