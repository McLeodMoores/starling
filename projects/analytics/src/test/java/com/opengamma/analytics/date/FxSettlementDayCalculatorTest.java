/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.date.FxSettlementDayCalculator;
import com.mcleodmoores.date.FxWorkingDayCalendar;
import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link FxSettlementDayCalculator}.
 */
public class FxSettlementDayCalculatorTest {

  /**
   * Tests the behaviour when the date is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.EUR, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    FxSettlementDayCalculator.getInstance().getSettlementDate(null, 1, new FxWorkingDayCalendar("", currencyCalendars));
  }

  /**
   * Tests the behaviour when the number of settlement days is less than zero.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDaysToSettle() {
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.EUR, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    FxSettlementDayCalculator.getInstance().getSettlementDate(LocalDate.of(2009, 1, 1), -1, new FxWorkingDayCalendar("", currencyCalendars));
  }

  /**
   * Tests the behaviour when the calendar is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    FxSettlementDayCalculator.getInstance().getSettlementDate(LocalDate.of(2009, 1, 1), 1, null);
  }

  /**
   * Tests settlement date calculation when there are no weekends or holidays to be considered. The
   * settlement should be two days after the current date.
   */
  @Test
  public void testTPlusTwo() {
    final int settlementDays = 2;
    final LocalDate today = LocalDate.of(2009, 9, 28);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 9, 30);
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.EUR, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.GBP, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(FxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

  /**
   * Tests settlement date calculation when there are no weekends or holidays to be considered. The
   * settlement should be one day after the current date.
   */
  @Test
  public void testTPlusOne() {
    final int settlementDays = 1;
    final LocalDate today = LocalDate.of(2009, 2, 12);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 2, 13);
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.EUR, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.of("TRY"), WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(FxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

  /**
   * Tests settlement date calculation when there is no intermediate holiday and the current date is a weekend.
   * settlement should be two days after the end of the weekend.
   */
  @Test
  public void testTPlusTwoWeekendStart() {
    final int settlementDays = 2;
    final LocalDate today = LocalDate.of(2009, 6, 20);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 6, 23);
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.EUR, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.GBP, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(FxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

  /**
   * Tests settlement date calculation when the T+2 date is a holiday and T+3 is the start of a weekend. The
   * settlement date should be 5 days after the current date.
   */
  @Test
  public void testTPlusTwoSpotDateHoliday() {
    final int settlementDays = 2;
    final LocalDate today = LocalDate.of(2009, 4, 29);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 5, 4);
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.EUR,
        new SimpleWorkingDayCalendar("", Collections.singleton(LocalDate.of(2009, 5, 1)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    currencyCalendars.put(Currency.GBP, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(FxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

  /**
   * Tests settlement date calculation when there is an intermediate holiday in the non-USD currency.
   */
  @Test
  public void testTPlusTwoIncludeIntermediateHoliday() {
    final int settlementDays = 1;
    final LocalDate today = LocalDate.of(2009, 7, 31);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 8, 4);
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.CAD, new SimpleWorkingDayCalendar("",
        Collections.singleton(LocalDate.of(2009, 8, 3)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(FxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

  /**
   * Tests settlement date calculation when there is an intermediate holiday in the US.
   */
  @Test
  public void testTPlusTwoIgnoreIntermediateUsHoliday() {
    final int settlementDays = 2;
    final LocalDate today = LocalDate.of(2009, 7, 31);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 8, 4);
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.USD, new SimpleWorkingDayCalendar("",
        Collections.singleton(LocalDate.of(2009, 8, 3)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    currencyCalendars.put(Currency.GBP, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(FxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

  /**
   * Tests settlement date calculation when the T+2 date is a holiday.
   */
  @Test
  public void testTPlusTwoSpotDateIgnoreIntermediateUsHoliday() {
    final int settlementDays = 2;
    final LocalDate today = LocalDate.of(2009, 10, 8);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 10, 13);
    Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.EUR, new SimpleWorkingDayCalendar("",
        Collections.singleton(LocalDate.of(2009, 10, 12)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    currencyCalendars.put(Currency.GBP, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(FxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);

    currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.GBP, new SimpleWorkingDayCalendar("",
        Collections.singleton(LocalDate.of(2009, 10, 12)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    currencyCalendars.put(Currency.EUR, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(FxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);

    currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.USD, new SimpleWorkingDayCalendar("",
        Collections.singleton(LocalDate.of(2009, 10, 12)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    currencyCalendars.put(Currency.GBP, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.EUR, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(FxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

}
