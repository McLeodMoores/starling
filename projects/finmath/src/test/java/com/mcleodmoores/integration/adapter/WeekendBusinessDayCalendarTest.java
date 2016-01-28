/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

/**
 * Units tests for {@link WeekendBusinessDayCalendar}.
 */
public class WeekendBusinessDayCalendarTest {
  /** The business day calendar */
  private static final FinmathBusinessDay BUSINESS_DAY = new WeekendBusinessDayCalendar();

  /**
   * Tests that all days in a period are business days except Saturdays and Sundays.
   */
  @Test
  public void test() {
    LocalDate date = new LocalDate(2000, 1, 1);
    final LocalDate end = new LocalDate(2015, 1, 1);
    while (date.isBefore(end)) {
      switch (date.getDayOfWeek()) {
        case 6:
        case 7:
          assertFalse(BUSINESS_DAY.isBusinessday(date));
          break;
        default:
          assertTrue(BUSINESS_DAY.isBusinessday(date));
          break;
      }
      date = date.plusDays(1);
    }
  }
}
