/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.integration.adapter.FinmathBusinessDay;
import com.mcleodmoores.integration.adapter.FinmathDateUtils;
import com.mcleodmoores.integration.adapter.WeekendBusinessDayCalendar;

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
    LocalDate date = LocalDate.of(2000, 1, 1);
    final LocalDate end = LocalDate.of(2015, 1, 1);
    while (date.isBefore(end)) {
      switch (date.getDayOfWeek()) {
        case SATURDAY:
        case SUNDAY:
          assertFalse(BUSINESS_DAY.isBusinessday(FinmathDateUtils.convertLocalDate(date)));
          break;
        default:
          assertTrue(BUSINESS_DAY.isBusinessday(FinmathDateUtils.convertLocalDate(date)));
          break;
      }
      date = date.plusDays(1);
    }
  }
}
