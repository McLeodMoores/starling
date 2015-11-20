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

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 * Unit tests for {@link WorkingDayCalendarAdapter}.
 */
public class WorkingDayCalendarAdapterTest {
  /** Holiday dates */
  private static final Collection<LocalDate> DATES = Arrays.asList(
      LocalDate.of(2015, 9, 14),
      LocalDate.of(2015, 10, 9),
      LocalDate.of(2015, 12, 23));
  /** A calendar */
  private static final TestCalendar CALENDAR = new TestCalendar(DATES);
  /** A working day calendar */
  private static final WorkingDayCalendar WORKING_DAY_CALENDAR = new SimpleWorkingDayCalendar("Simple", DATES, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  /** The adapter */
  private static final WorkingDayCalendar ADAPTER = new WorkingDayCalendarAdapter(CALENDAR, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

  /**
   * Tests the behaviour when the calendar is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    new WorkingDayCalendarAdapter(null, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  }

  /**
   * Tests the behaviour when the first weekend day is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeekendDay1() {
    new WorkingDayCalendarAdapter(CALENDAR, null, DayOfWeek.SUNDAY);
  }

  /**
   * Tests the behaviour when the second weekend day is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeekendDay2() {
    new WorkingDayCalendarAdapter(CALENDAR, DayOfWeek.SATURDAY, null);
  }

  /**
   * Tests the behaviour of the object.
   */
  @Test
  public void testObject() {
    assertEquals(ADAPTER.getName(), CALENDAR.getName());
    assertEquals(ADAPTER, ADAPTER);
    assertFalse(ADAPTER.equals(WORKING_DAY_CALENDAR));
    WorkingDayCalendar other = new WorkingDayCalendarAdapter(CALENDAR, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    assertEquals(other, ADAPTER);
    assertEquals(other.hashCode(), ADAPTER.hashCode());
    other = new WorkingDayCalendarAdapter(new MondayToFridayCalendar("Weekend"), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    assertNotEquals(other, ADAPTER);
    other = new WorkingDayCalendarAdapter(CALENDAR, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY);
    assertNotEquals(other, ADAPTER);
    other = new WorkingDayCalendarAdapter(CALENDAR, DayOfWeek.SATURDAY, DayOfWeek.MONDAY);
    assertNotEquals(other, ADAPTER);
  }

  /**
   * Tests that the adapter functions as expected as a {@link WorkingDayCalendar}.
   */
  @Test
  public void testWorkingDays() {
    LocalDate date = LocalDate.of(2015, 9, 1);
    while (date.isBefore(LocalDate.of(2016, 1, 1))) {
      if (!WORKING_DAY_CALENDAR.isWorkingDay(date)) {
        assertFalse(ADAPTER.isWorkingDay(date));
        assertFalse(CALENDAR.isWorkingDay(date));
      } else {
        assertTrue(ADAPTER.isWorkingDay(date));
        assertTrue(CALENDAR.isWorkingDay(date));
      }
      if (WORKING_DAY_CALENDAR.isWeekend(date)) {
        assertFalse(CALENDAR.isWorkingDay(date));
        assertTrue(ADAPTER.isWeekend(date));
      } else {
        assertFalse(ADAPTER.isWeekend(date));
      }
      if (WORKING_DAY_CALENDAR.isHoliday(date)) {
        assertFalse(CALENDAR.isWorkingDay(date));
        assertTrue(ADAPTER.isHoliday(date));
      } else {
        assertFalse(ADAPTER.isHoliday(date));
      }
      date = date.plusDays(1);
    }
  }

}
