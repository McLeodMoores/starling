/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.date.FxWorkingDayCalendar;
import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link FxWorkingDayCalendar}.
 */
public class FxWorkingDayCalendarTest {
  /** The name of the calendar */
  private static final String NAME = "GBPEUR";
  /** An EUR calendar */
  private static final WorkingDayCalendar EUR_CALENDAR = new SimpleWorkingDayCalendar("EUR",
      Arrays.asList(LocalDate.of(2015, 12, 25)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  /** An GBP calendar */
  private static final WorkingDayCalendar GBP_CALENDAR = new SimpleWorkingDayCalendar("GBP",
      Arrays.asList(LocalDate.of(2015, 12, 31)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  /** An USD calendar */
  private static final WorkingDayCalendar USD_CALENDAR = new SimpleWorkingDayCalendar("USD",
      Arrays.asList(LocalDate.of(2016, 1, 1)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  /** The calendar map */
  private static final Map<Currency, WorkingDayCalendar> CALENDAR_MAP = new HashMap<>();
  /** The FX calendar */
  private static final FxWorkingDayCalendar FX_CALENDAR;

  static {
    CALENDAR_MAP.put(Currency.EUR, EUR_CALENDAR);
    CALENDAR_MAP.put(Currency.GBP, GBP_CALENDAR);
    CALENDAR_MAP.put(Currency.USD, USD_CALENDAR);
    FX_CALENDAR = new FxWorkingDayCalendar(NAME, CALENDAR_MAP);
  }

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new FxWorkingDayCalendar(null, CALENDAR_MAP);
  }

  /**
   * Tests the behaviour when the map is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap() {
    new FxWorkingDayCalendar(NAME, null);
  }

  /**
   * Tests the behaviour when the map does not contain at least two currencies.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMissingCurrencies() {
    new FxWorkingDayCalendar(NAME, Collections.singletonMap(Currency.USD, USD_CALENDAR));
  }

  /**
   * Tests the behaviour when the map does not contain a USD calendar.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMissingUsdCalendar() {
    final Map<Currency, WorkingDayCalendar> calendars = new HashMap<>();
    calendars.put(Currency.EUR, EUR_CALENDAR);
    calendars.put(Currency.GBP, GBP_CALENDAR);
    new FxWorkingDayCalendar(NAME, calendars);
  }

  /**
   * Tests the behaviour when a calendar for a currency is requested that is not available.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnavailableCurrency() {
    FX_CALENDAR.getCalendar(Currency.AUD);
  }
  /**
   * Tests the hashCode, equals and toString methods.
   */
  @Test
  public void testObject() {
    assertEquals(FX_CALENDAR.getName(), "GBPEUR");
    assertEquals(FX_CALENDAR.getPerCurrencyCalendars(), CALENDAR_MAP);
    assertEquals(FX_CALENDAR.getCalendar(Currency.EUR), EUR_CALENDAR);
    assertEquals(FX_CALENDAR.getCalendar(Currency.GBP), GBP_CALENDAR);
    assertEquals(FX_CALENDAR.getCalendar(Currency.USD), USD_CALENDAR);
    final String toString = FX_CALENDAR.toString();
    final String eurString = "EUR: [SATURDAY, SUNDAY], [2015-12-25]";
    final String gbpString = "GBP: [SATURDAY, SUNDAY], [2015-12-31]";
    final String usdString = "USD: [SATURDAY, SUNDAY], [2016-01-01]";
    assertEquals(toString.length(), eurString.length() + gbpString.length() + usdString.length() + 10);
    assertTrue(toString.contains(eurString));
    assertTrue(toString.contains(gbpString));
    assertTrue(toString.contains(usdString));
    assertEquals(FX_CALENDAR, FX_CALENDAR);
    WorkingDayCalendar other = new FxWorkingDayCalendar(NAME, CALENDAR_MAP);
    assertEquals(FX_CALENDAR, other);
    assertEquals(FX_CALENDAR.hashCode(), other.hashCode());
    assertNotEquals(WeekendWorkingDayCalendar.SATURDAY_SUNDAY, other);
    final Map<Currency, WorkingDayCalendar> calendars = new HashMap<>();
    calendars.put(Currency.GBP, GBP_CALENDAR);
    calendars.put(Currency.USD, USD_CALENDAR);
    other = new FxWorkingDayCalendar("GBPUSD", CALENDAR_MAP);
    assertNotEquals(FX_CALENDAR, other);
    other = new FxWorkingDayCalendar("GBPEUR", calendars);
    assertNotEquals(FX_CALENDAR, other);
  }

  /**
   * Tests that weekends, holidays and working days are identified correctly.
   */
  @Test
  public void testCalendar() {
    assertTrue(FX_CALENDAR.isHoliday(LocalDate.of(2015, 12, 25)));
    assertFalse(FX_CALENDAR.isWeekend(LocalDate.of(2015, 12, 25)));
    assertFalse(FX_CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 25)));

    assertFalse(FX_CALENDAR.isHoliday(LocalDate.of(2015, 12, 26)));
    assertTrue(FX_CALENDAR.isWeekend(LocalDate.of(2015, 12, 26)));
    assertFalse(FX_CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 26)));

    assertFalse(FX_CALENDAR.isHoliday(LocalDate.of(2015, 12, 27)));
    assertTrue(FX_CALENDAR.isWeekend(LocalDate.of(2015, 12, 27)));
    assertFalse(FX_CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 27)));

    assertFalse(FX_CALENDAR.isHoliday(LocalDate.of(2015, 12, 28)));
    assertFalse(FX_CALENDAR.isWeekend(LocalDate.of(2015, 12, 28)));
    assertTrue(FX_CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 28)));

    assertFalse(FX_CALENDAR.isHoliday(LocalDate.of(2015, 12, 29)));
    assertFalse(FX_CALENDAR.isWeekend(LocalDate.of(2015, 12, 29)));
    assertTrue(FX_CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 29)));

    assertFalse(FX_CALENDAR.isHoliday(LocalDate.of(2015, 12, 30)));
    assertFalse(FX_CALENDAR.isWeekend(LocalDate.of(2015, 12, 30)));
    assertTrue(FX_CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 30)));

    assertTrue(FX_CALENDAR.isHoliday(LocalDate.of(2015, 12, 31)));
    assertFalse(FX_CALENDAR.isWeekend(LocalDate.of(2015, 12, 31)));
    assertFalse(FX_CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 31)));

    assertTrue(FX_CALENDAR.isHoliday(LocalDate.of(2016, 1, 1)));
    assertFalse(FX_CALENDAR.isWeekend(LocalDate.of(2016, 1, 1)));
    assertFalse(FX_CALENDAR.isWorkingDay(LocalDate.of(2016, 1, 1)));
  }
}
