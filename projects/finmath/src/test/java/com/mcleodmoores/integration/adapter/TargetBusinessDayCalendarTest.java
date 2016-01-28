/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

/**
 * Units tests for {@link TargetBusinessDayCalendar}.
 */
public class TargetBusinessDayCalendarTest {
  /** The business day calendar */
  private static final FinmathBusinessDay BUSINESS_DAY = new TargetBusinessDayCalendar();

  /**
   * Tests that all days in a period are business days except TARGET holidays.
   */
  @Test
  public void test() {
    LocalDate date = new LocalDate(2013, 1, 1);
    final LocalDate end = new LocalDate(2015, 1, 1);
    final Set<LocalDate> holidays = new HashSet<>();
    holidays.add(new LocalDate(2013, 1, 1));
    holidays.add(new LocalDate(2013, 3, 29));
    holidays.add(new LocalDate(2013, 4, 1));
    holidays.add(new LocalDate(2013, 5, 1));
    holidays.add(new LocalDate(2013, 12, 25));
    holidays.add(new LocalDate(2013, 12, 26));
    holidays.add(new LocalDate(2013, 12, 31));
    holidays.add(new LocalDate(2014, 1, 1));
    holidays.add(new LocalDate(2014, 4, 18));
    holidays.add(new LocalDate(2014, 4, 21));
    holidays.add(new LocalDate(2014, 5, 1));
    holidays.add(new LocalDate(2014, 12, 25));
    holidays.add(new LocalDate(2014, 12, 26));
    holidays.add(new LocalDate(2014, 12, 31));
    while (date.isBefore(end)) {
      switch (date.getDayOfWeek()) {
        case 6:
        case 7:
          assertFalse(BUSINESS_DAY.isBusinessday(date));
          break;
        default:
          if (holidays.contains(date)) {
            assertFalse(BUSINESS_DAY.isBusinessday(date));
          } else {
            assertTrue(BUSINESS_DAY.isBusinessday(date));
          }
          break;
      }
      date = date.plusDays(1);
    }
  }
}
