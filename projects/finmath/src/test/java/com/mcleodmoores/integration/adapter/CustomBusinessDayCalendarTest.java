/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;

import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link CustomBusinessDayCalendar}.
 */
@Test(groups = TestGroup.UNIT)
public class CustomBusinessDayCalendarTest {
  /** Holiday dates */
  private static final Collection<org.threeten.bp.LocalDate> HOLIDAYS;
  /** A test calendar */
  private static final WorkingDayCalendar CALENDAR;
  /** The adapter */
  private static final FinmathBusinessDay BUSINESS_DAY;

  static {
    HOLIDAYS = new HashSet<>();
    for (int i = 1; i < 13; i++) {
      HOLIDAYS.add(org.threeten.bp.LocalDate.of(2014, i, 1));
    }
    CALENDAR = new SimpleWorkingDayCalendar("Test", HOLIDAYS, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    BUSINESS_DAY = new CustomBusinessDayCalendar(CALENDAR);
  }


  /**
   * Tests that holidays are identified correctly.
   */
  @Test
  public void test() {
    final LocalDate end = new LocalDate(2015, 1, 1);
    LocalDate date = new LocalDate(2014, 1, 1);
    while (date.isBefore(end)) {
      if (HOLIDAYS.contains(FinmathDateUtils.convertFromJodaLocalDateDate(date)) || date.getDayOfWeek() == 6 || date.getDayOfWeek() == 7) {
        assertFalse(BUSINESS_DAY.isBusinessday(date));
      } else {
        assertTrue(BUSINESS_DAY.isBusinessday(date));
      }
      date = date.plusDays(1);
    }
  }
}
