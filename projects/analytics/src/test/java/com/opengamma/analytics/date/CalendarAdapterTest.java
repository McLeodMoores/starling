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

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Unit tests for {@link CalendarAdapter}.
 */
public class CalendarAdapterTest {
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
  private static final Calendar ADAPTER = new CalendarAdapter(WORKING_DAY_CALENDAR);

  /**
   * Tests the behaviour when the working day calendar is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    new CalendarAdapter(null);
  }

  /**
   * Tests the behaviour of the object.
   */
  @Test
  public void testObject() {
    assertEquals(ADAPTER.getConventionName(), WORKING_DAY_CALENDAR.getName());
    assertEquals(ADAPTER.getName(), WORKING_DAY_CALENDAR.getName());
    assertEquals(ADAPTER, ADAPTER);
    assertFalse(ADAPTER.equals(CALENDAR));
    Calendar other = new CalendarAdapter(new SimpleWorkingDayCalendar("Simple", DATES, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    assertEquals(other, ADAPTER);
    assertEquals(other.hashCode(), ADAPTER.hashCode());
    other = new CalendarAdapter(WeekendWorkingDayCalendar.FRIDAY_SATURDAY);
    assertNotEquals(other, ADAPTER);
  }

  /**
   * Tests that the adapter functions as expected as a {@link Calendar}.
   */
  @Test
  public void testWorkingDays() {
    LocalDate date = LocalDate.of(2015, 9, 1);
    while (date.isBefore(LocalDate.of(2016, 1, 1))) {
      if (!CALENDAR.isWorkingDay(date)) {
        assertFalse(ADAPTER.isWorkingDay(date));
        assertFalse(WORKING_DAY_CALENDAR.isWorkingDay(date));
      } else {
        assertTrue(ADAPTER.isWorkingDay(date));
        assertTrue(WORKING_DAY_CALENDAR.isWorkingDay(date));
      }
      date = date.plusDays(1);
    }
  }

}
