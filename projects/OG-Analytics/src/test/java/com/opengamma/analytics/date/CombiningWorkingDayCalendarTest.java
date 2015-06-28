/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

/**
 * Unit tests for {@link CombiningWorkingDayCalendar}.
 */
public class CombiningWorkingDayCalendarTest {
  /** A US calendar */
  private static final WorkingDayCalendar US_CALENDAR = new SimpleWorkingDayCalendar("US",
      Arrays.asList(LocalDate.of(2015, 12, 25), LocalDate.of(2016, 1, 1)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  /** A SA calendar */
  private static final WorkingDayCalendar SA_CALENDAR = new SimpleWorkingDayCalendar("SA",
      Arrays.asList(LocalDate.of(2015, 12, 30), LocalDate.of(2016, 1, 1)), DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

  /**
   * Tests the behaviour when the collection of calendars is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendars1() {
    new CombiningWorkingDayCalendar((Collection<WorkingDayCalendar>) null);
  }

  /**
   * Tests the behaviour when the collection of calendars is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCalendars1() {
    new CombiningWorkingDayCalendar(Collections.<WorkingDayCalendar>emptySet());
  }

  /**
   * Tests the behaviour when the array of calendars is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendars2() {
    new CombiningWorkingDayCalendar((WorkingDayCalendar[]) null);
  }

  /**
   * Tests the behaviour when the collection of calendars is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCalendars2() {
    new CombiningWorkingDayCalendar(new WorkingDayCalendar[0]);
  }

  /**
   * Tests the hashCode, equals and toString methods.
   */
  @Test
  public void testObject() {
    final WorkingDayCalendar calendar = new CombiningWorkingDayCalendar(Arrays.asList(US_CALENDAR, SA_CALENDAR));
    assertEquals(calendar.getName(), "US+SA");
    final String toString = calendar.toString();
    if (!toString.equals("US: [SATURDAY, SUNDAY], [2015-12-25, 2016-01-01] + SA: [THURSDAY, FRIDAY], [2015-12-30, 2016-01-01]")
        && !toString.equals("SA: [THURSDAY, FRIDAY], [2015-12-30, 2016-01-01] + US: [SATURDAY, SUNDAY], [2015-12-25, 2016-01-01]")) {
      fail();
    }
    assertEquals(calendar, calendar);
    WorkingDayCalendar other = new CombiningWorkingDayCalendar(US_CALENDAR, SA_CALENDAR);
    assertEquals(calendar, other);
    assertEquals(calendar.hashCode(), other.hashCode());
    assertNotEquals(WeekendWorkingDayCalendar.SATURDAY_SUNDAY, other);
    other = new CombiningWorkingDayCalendar(US_CALENDAR, SA_CALENDAR);
    assertEquals(calendar, other);
    other = new CombiningWorkingDayCalendar(US_CALENDAR);
    assertNotEquals(calendar, other);
    final WorkingDayCalendar saCalendar = new SimpleWorkingDayCalendar("SA", Collections.<LocalDate>emptySet(), DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    other = new CombiningWorkingDayCalendar(US_CALENDAR, saCalendar);
    assertNotEquals(calendar, other);
  }

  /**
   * Tests that weekends, holidays and working days are identified correctly.
   */
  @Test
  public void testCalendar() {
    final WorkingDayCalendar calendar = new CombiningWorkingDayCalendar(Arrays.asList(US_CALENDAR, SA_CALENDAR));

    assertTrue(calendar.isHoliday(LocalDate.of(2015, 12, 25)));
    assertTrue(calendar.isWeekend(LocalDate.of(2015, 12, 25)));
    assertFalse(calendar.isWorkingDay(LocalDate.of(2015, 12, 25)));

    assertFalse(calendar.isHoliday(LocalDate.of(2015, 12, 26)));
    assertTrue(calendar.isWeekend(LocalDate.of(2015, 12, 26)));
    assertFalse(calendar.isWorkingDay(LocalDate.of(2015, 12, 26)));

    assertFalse(calendar.isHoliday(LocalDate.of(2015, 12, 27)));
    assertTrue(calendar.isWeekend(LocalDate.of(2015, 12, 27)));
    assertFalse(calendar.isWorkingDay(LocalDate.of(2015, 12, 27)));

    assertFalse(calendar.isHoliday(LocalDate.of(2015, 12, 28)));
    assertFalse(calendar.isWeekend(LocalDate.of(2015, 12, 28)));
    assertTrue(calendar.isWorkingDay(LocalDate.of(2015, 12, 28)));

    assertFalse(calendar.isHoliday(LocalDate.of(2015, 12, 29)));
    assertFalse(calendar.isWeekend(LocalDate.of(2015, 12, 29)));
    assertTrue(calendar.isWorkingDay(LocalDate.of(2015, 12, 29)));

    assertTrue(calendar.isHoliday(LocalDate.of(2015, 12, 30)));
    assertFalse(calendar.isWeekend(LocalDate.of(2015, 12, 30)));
    assertFalse(calendar.isWorkingDay(LocalDate.of(2015, 12, 30)));

    assertFalse(calendar.isHoliday(LocalDate.of(2015, 12, 31)));
    assertTrue(calendar.isWeekend(LocalDate.of(2015, 12, 31)));
    assertFalse(calendar.isWorkingDay(LocalDate.of(2015, 12, 31)));

    assertTrue(calendar.isHoliday(LocalDate.of(2016, 1, 1)));
    assertTrue(calendar.isWeekend(LocalDate.of(2016, 1, 1)));
    assertFalse(calendar.isWorkingDay(LocalDate.of(2016, 1, 1)));

    assertFalse(calendar.isHoliday(LocalDate.of(2016, 1, 4)));
    assertFalse(calendar.isWeekend(LocalDate.of(2016, 1, 4)));
    assertTrue(calendar.isWorkingDay(LocalDate.of(2016, 1, 4)));
  }
}
