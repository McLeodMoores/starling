/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;

/**
 * Unit tests for {@link WeekendWorkingDayCalendar}.
 */
public class WeekendWorkingDayCalendarTest {

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new WeekendWorkingDayCalendar(null, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  }

  /**
   * Tests the behaviour when the first weekend day is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeekendDay1() {
    new WeekendWorkingDayCalendar("NAME", null, DayOfWeek.SUNDAY);
  }

  /**
   * Tests the behaviour when the second weekend day is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeekendDay2() {
    new WeekendWorkingDayCalendar("NAME", DayOfWeek.SATURDAY, null);
  }

  /**
   * Tests the behaviour when the date to test is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateToTest1() {
    WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isHoliday(null);
  }

  /**
   * Tests the behaviour when the date to test is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateToTest2() {
    WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWeekend(null);
  }

  /**
   * Tests the behaviour when the date to test is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateToTest3() {
    WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWorkingDay(null);
  }

  /**
   * Tests the hashCode, equals and toString methods.
   */
  @Test
  public void testObject() {
    final String name = "Calendar";
    final DayOfWeek weekendDay1 = DayOfWeek.SATURDAY;
    final DayOfWeek weekendDay2 = DayOfWeek.SUNDAY;
    final WorkingDayCalendar calendar = new WeekendWorkingDayCalendar(name, weekendDay1, weekendDay2);
    assertEquals(calendar.getName(), name);
    assertEquals(calendar.toString(), "Calendar: [SATURDAY, SUNDAY]");
    assertEquals(calendar, calendar);
    WorkingDayCalendar other = new WeekendWorkingDayCalendar(name, weekendDay1, weekendDay2);
    assertEquals(calendar, other);
    assertEquals(calendar.hashCode(), other.hashCode());
    assertFalse(calendar.equals(DayOfWeek.SATURDAY));
    other = new SimpleWorkingDayCalendar(name, Collections.<LocalDate>emptySet(), weekendDay1, weekendDay2);
    assertNotEquals(calendar, other);
    other = new WeekendWorkingDayCalendar(name + "1", weekendDay1, weekendDay2);
    assertNotEquals(calendar, other);
    other = new WeekendWorkingDayCalendar(name, weekendDay2, weekendDay2);
    assertNotEquals(calendar, other);
    other = new WeekendWorkingDayCalendar(name, weekendDay1, weekendDay1);
    assertNotEquals(calendar, other);
  }

  /**
   * Tests that weekends, holidays and working days are identified correctly.
   */
  @Test
  public void testCalendar() {
    assertTrue(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2015, 6, 20)));
    assertTrue(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2015, 6, 21)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2015, 6, 22)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2015, 6, 23)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2015, 6, 24)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2015, 6, 25)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2015, 6, 26)));

    assertTrue(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isHoliday(LocalDate.of(2015, 6, 20)));
    assertTrue(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isHoliday(LocalDate.of(2015, 6, 21)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isHoliday(LocalDate.of(2015, 6, 22)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isHoliday(LocalDate.of(2015, 6, 23)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isHoliday(LocalDate.of(2015, 6, 24)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isHoliday(LocalDate.of(2015, 6, 25)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isHoliday(LocalDate.of(2015, 6, 26)));

    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWorkingDay(LocalDate.of(2015, 6, 20)));
    assertFalse(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWorkingDay(LocalDate.of(2015, 6, 21)));
    assertTrue(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWorkingDay(LocalDate.of(2015, 6, 22)));
    assertTrue(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWorkingDay(LocalDate.of(2015, 6, 23)));
    assertTrue(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWorkingDay(LocalDate.of(2015, 6, 24)));
    assertTrue(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWorkingDay(LocalDate.of(2015, 6, 25)));
    assertTrue(WeekendWorkingDayCalendar.SATURDAY_SUNDAY.isWorkingDay(LocalDate.of(2015, 6, 26)));
  }
}
