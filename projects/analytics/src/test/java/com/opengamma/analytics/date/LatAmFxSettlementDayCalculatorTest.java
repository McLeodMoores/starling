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

import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link LatAmFxSettlementDayCalculator}.
 */
public class LatAmFxSettlementDayCalculatorTest {

  /**
   * Tests the behaviour when the date is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.EUR, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    LatAmFxSettlementDayCalculator.getInstance().getSettlementDate(null, 1, new FxWorkingDayCalendar("", currencyCalendars));
  }

  /**
   * Tests the behaviour when the number of settlement days is less than zero.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDaysToSettle() {
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.EUR, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    LatAmFxSettlementDayCalculator.getInstance().getSettlementDate(LocalDate.of(2009, 1, 1), -1, new FxWorkingDayCalendar("", currencyCalendars));
  }

  /**
   * Tests the behaviour when the calendar is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    LatAmFxSettlementDayCalculator.getInstance().getSettlementDate(LocalDate.of(2009, 1, 1), 1, null);
  }

  /**
   * Tests settlement date calculation when there is no intermediate holiday and the current date is a weekend.
   * The settlement should be two days after the end of the weekend.
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
    assertEquals(LatAmFxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

  /**
   * Tests settlement date calculation when there is no intermediate holiday. The settlement date should be two
   * days after the current date.
   */
  @Test
  public void testTPlusTwoSpotDateCountNoIntermediateHoliday() {
    final int settlementDays = 2;
    final LocalDate today = LocalDate.of(2009, 11, 10);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 11, 12);
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    currencyCalendars.put(Currency.MXN, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(LatAmFxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

  /**
   * Tests settlement date calculation when there is an intermediate US holiday. The settlement date should be three days
   * (two working days) after the current date.
   */
  @Test
  public void testTPlusTwoSpotDateCountIntermediateUsHoliday() {
    final int settlementDays = 2;
    final LocalDate today = LocalDate.of(2009, 11, 10);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 11, 13);
    final Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.USD, new SimpleWorkingDayCalendar("",
        Collections.singleton(LocalDate.of(2009, 11, 11)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    currencyCalendars.put(Currency.MXN, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(LatAmFxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }

  /**
   * Tests settlement date calculation when the settlement date must be adjusted because it falls on a holiday.
   * The settlement date should be three days (two working days) after the current date.
   */
  @Test
  public void testTPlusTwoSpotDateHolidayOnSettlement() {
    final int settlementDays = 2;
    final LocalDate today = LocalDate.of(2009, 11, 10);
    final LocalDate expectedSpotDate = LocalDate.of(2009, 11, 13);
    Map<Currency, WorkingDayCalendar> currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.USD, new SimpleWorkingDayCalendar("",
        Collections.singleton(LocalDate.of(2009, 11, 12)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    currencyCalendars.put(Currency.MXN, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    FxWorkingDayCalendar calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(LatAmFxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);

    currencyCalendars = new HashMap<>();
    currencyCalendars.put(Currency.MXN, new SimpleWorkingDayCalendar("",
        Collections.singleton(LocalDate.of(2009, 11, 12)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    currencyCalendars.put(Currency.USD, WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    calendars = new FxWorkingDayCalendar("", currencyCalendars);
    assertEquals(LatAmFxSettlementDayCalculator.getInstance().getSettlementDate(today, settlementDays, calendars), expectedSpotDate);
  }
}
