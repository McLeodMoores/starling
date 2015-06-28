/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

/**
 * Unit tests for {@link SimpleWorkingDayCalendar}.
 */
public class SimpleWorkingDayCalendarTest {
  /** The name */
  private static final String NAME = "Calendar";
  /** The holiday dates */
  private static final Collection<LocalDate> HOLIDAYS = Arrays.asList(LocalDate.of(2015, 12, 25), LocalDate.of(2015, 12, 31), LocalDate.of(2016, 1, 1));
  /** A working day calendar */
  private static final WorkingDayCalendar CALENDAR = new SimpleWorkingDayCalendar(NAME, HOLIDAYS, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new SimpleWorkingDayCalendar(null, HOLIDAYS, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  }

  /**
   * Tests the behaviour when the holiday dates are null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHolidays() {
    new SimpleWorkingDayCalendar(NAME, null, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  }

  /**
   * Tests the behaviour when the first weekend day is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeekendDay1() {
    new SimpleWorkingDayCalendar(NAME, HOLIDAYS, null, DayOfWeek.SUNDAY);
  }

  /**
   * Tests the behaviour when the second weekend day is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeekendDay2() {
    new SimpleWorkingDayCalendar(NAME, HOLIDAYS, DayOfWeek.SATURDAY, null);
  }

  /**
   * Tests the hashCode, equals and toString methods.
   */
  @Test
  public void testObject() {
    final DayOfWeek weekendDay1 = DayOfWeek.SATURDAY;
    final DayOfWeek weekendDay2 = DayOfWeek.SUNDAY;
    final WorkingDayCalendar calendar = new SimpleWorkingDayCalendar(NAME, HOLIDAYS, weekendDay1, weekendDay2);
    assertEquals(calendar.getName(), NAME);
    assertEquals(calendar.toString(), "Calendar: [SATURDAY, SUNDAY], [2015-12-25, 2015-12-31, 2016-01-01]");
    assertEquals(calendar, calendar);
    WorkingDayCalendar other = new SimpleWorkingDayCalendar(NAME, HOLIDAYS, weekendDay1, weekendDay2);
    assertEquals(calendar, other);
    assertEquals(calendar.hashCode(), other.hashCode());
    assertNotEquals(WeekendWorkingDayCalendar.SATURDAY_SUNDAY, other);
    other = new SimpleWorkingDayCalendar(NAME + "1", HOLIDAYS, weekendDay1, weekendDay2);
    assertNotEquals(calendar, other);
    other = new SimpleWorkingDayCalendar(NAME, Collections.singleton(HOLIDAYS.iterator().next()), weekendDay1, weekendDay2);
    assertNotEquals(calendar, other);
    other = new SimpleWorkingDayCalendar(NAME, HOLIDAYS, weekendDay2, weekendDay2);
    assertNotEquals(calendar, other);
    other = new SimpleWorkingDayCalendar(NAME, HOLIDAYS, weekendDay1, weekendDay1);
    assertNotEquals(calendar, other);
  }

  /**
   * Tests that weekends, holidays and working days are identified correctly.
   */
  @Test
  public void testCalendar() {
    assertTrue(CALENDAR.isHoliday(LocalDate.of(2015, 12, 25)));
    assertFalse(CALENDAR.isWeekend(LocalDate.of(2015, 12, 25)));
    assertFalse(CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 25)));

    assertFalse(CALENDAR.isHoliday(LocalDate.of(2015, 12, 26)));
    assertTrue(CALENDAR.isWeekend(LocalDate.of(2015, 12, 26)));
    assertFalse(CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 26)));

    assertFalse(CALENDAR.isHoliday(LocalDate.of(2015, 12, 27)));
    assertTrue(CALENDAR.isWeekend(LocalDate.of(2015, 12, 27)));
    assertFalse(CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 27)));

    assertFalse(CALENDAR.isHoliday(LocalDate.of(2015, 12, 28)));
    assertFalse(CALENDAR.isWeekend(LocalDate.of(2015, 12, 28)));
    assertTrue(CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 28)));

    assertFalse(CALENDAR.isHoliday(LocalDate.of(2015, 12, 29)));
    assertFalse(CALENDAR.isWeekend(LocalDate.of(2015, 12, 29)));
    assertTrue(CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 29)));

    assertFalse(CALENDAR.isHoliday(LocalDate.of(2015, 12, 30)));
    assertFalse(CALENDAR.isWeekend(LocalDate.of(2015, 12, 30)));
    assertTrue(CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 30)));

    assertTrue(CALENDAR.isHoliday(LocalDate.of(2015, 12, 31)));
    assertFalse(CALENDAR.isWeekend(LocalDate.of(2015, 12, 31)));
    assertFalse(CALENDAR.isWorkingDay(LocalDate.of(2015, 12, 31)));

    assertTrue(CALENDAR.isHoliday(LocalDate.of(2016, 1, 1)));
    assertFalse(CALENDAR.isWeekend(LocalDate.of(2016, 1, 1)));
    assertFalse(CALENDAR.isWorkingDay(LocalDate.of(2016, 1, 1)));
  }
}
