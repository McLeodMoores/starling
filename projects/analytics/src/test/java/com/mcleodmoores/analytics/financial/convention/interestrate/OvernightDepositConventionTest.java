/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link OvernightDepositConvention}.
 */
public class OvernightDepositConventionTest {
  private static final Currency CCY = Currency.USD;
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final OvernightDepositConvention CONVENTION = OvernightDepositConvention.builder().withCalendar(CALENDAR).withCurrency(CCY)
      .withDayCount(DAY_COUNT).build();

  /**
   * Tests that the calendar cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    OvernightDepositConvention.builder().withCalendar(null);
  }

  /**
   * Tests that the currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    OvernightDepositConvention.builder().withCurrency(null);
  }

  /**
   * Tests that the day count cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    OvernightDepositConvention.builder().withDayCount(null);
  }

  /**
   * Tests that the calendar must be set before building.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCalendarSet() {
    OvernightDepositConvention.builder().withCurrency(CCY).withDayCount(DAY_COUNT).build();
  }

  /**
   * Tests that the currency must be set before building.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrencySet() {
    OvernightDepositConvention.builder().withCalendar(CALENDAR).withDayCount(DAY_COUNT).build();
  }

  /**
   * Tests that the day count must be set before building.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testDayCountSet() {
    OvernightDepositConvention.builder().withCalendar(CALENDAR).withCurrency(CCY).build();
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    CONVENTION.toCurveInstrument(null, Tenor.ON, Tenor.ONE_MONTH, 1, 0.02);
  }

  /**
   * Tests that the start tenor cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartTenor() {
    CONVENTION.toCurveInstrument(ZonedDateTime.now(), null, Tenor.ONE_MONTH, 1, 0.02);
  }

  /**
   * Tests that the end tenor cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndTenor() {
    CONVENTION.toCurveInstrument(ZonedDateTime.now(), Tenor.ON, null, 1, 0.02);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(CONVENTION.getCalendar(), CALENDAR);
    assertEquals(CONVENTION.getCurrency(), CCY);
    assertEquals(CONVENTION.getDayCount(), DAY_COUNT);
    OvernightDepositConvention other = OvernightDepositConvention.builder().withCalendar(CALENDAR).withCurrency(CCY)
        .withDayCount(DAY_COUNT).build();
    assertEquals(CONVENTION, other);
    assertEquals(CONVENTION.hashCode(), other.hashCode());
    final String expected =
        "OvernightDepositConvention [currency=USD, calendar=Saturday / Sunday, dayCount=Actual/360]";
    assertEquals(CONVENTION.toString(), expected);
    other = OvernightDepositConvention.builder().withCalendar(WeekendWorkingDayCalendar.FRIDAY_SATURDAY).withCurrency(CCY)
        .withDayCount(DAY_COUNT).build();
    assertNotEquals(CONVENTION, other);
    other = OvernightDepositConvention.builder().withCalendar(CALENDAR).withCurrency(Currency.EUR)
        .withDayCount(DAY_COUNT).build();
    assertNotEquals(CONVENTION, other);
    other = OvernightDepositConvention.builder().withCalendar(CALENDAR).withCurrency(CCY)
        .withDayCount(DayCounts.ACT_365).build();
    assertNotEquals(CONVENTION, other);
  }

  /**
   * Tests that the cash definition is the same as that produced using the generator.
   */
  @Test
  public void testGeneratorEquivalence() {
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 7, 31);
    final Tenor startTenor = Tenor.of(Period.ZERO);
    final Tenor endTenor = Tenor.ONE_MONTH;
    final double rate = 0.01;
    final GeneratorDepositON generator = new GeneratorDepositON("", CCY, new CalendarAdapter(CALENDAR), DAY_COUNT);
    final GeneratorAttributeIR attribute = new GeneratorAttributeIR(startTenor.getPeriod(), endTenor.getPeriod());
    assertEquals(CONVENTION.toCurveInstrument(date, startTenor, endTenor, 1, rate), generator.generateInstrument(date, rate, 1, attribute));
  }

  /**
   * Tests the cash definition.
   */
  @Test
  public void testCashDefinition() {
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 1, 27);
    final Tenor startTenor = Tenor.ON;
    final Tenor endTenor = Tenor.ONE_MONTH;
    final double rate = 0.01;
    final CashDefinition cash = CONVENTION.toCurveInstrument(date, startTenor, endTenor, 1, rate);
    assertEquals(cash.getStartDate(), DateUtils.getUTCDate(2017, 1, 30));
    assertEquals(cash.getEndDate(), DateUtils.getUTCDate(2017, 1, 31));
    assertEquals(cash.getAccrualFactor(), 1 / 360., 1e-15);
    assertEquals(cash.getRate(), rate, 1e-15);
    assertEquals(cash.getCurrency(), CCY);
    assertEquals(cash.getNotional(), 1, 1e-15);
  }
}
