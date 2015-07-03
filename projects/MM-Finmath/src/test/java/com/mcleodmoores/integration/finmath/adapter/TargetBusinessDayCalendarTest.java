/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

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
    LocalDate date = LocalDate.of(2013, 1, 1);
    final LocalDate end = LocalDate.of(2015, 1, 1);
    final Set<LocalDate> holidays = new HashSet<>();
    holidays.add(LocalDate.of(2013, 1, 1));
    holidays.add(LocalDate.of(2013, 3, 29));
    holidays.add(LocalDate.of(2013, 4, 1));
    holidays.add(LocalDate.of(2013, 5, 1));
    holidays.add(LocalDate.of(2013, 12, 25));
    holidays.add(LocalDate.of(2013, 12, 26));
    holidays.add(LocalDate.of(2013, 12, 31));
    holidays.add(LocalDate.of(2014, 1, 1));
    holidays.add(LocalDate.of(2014, 4, 18));
    holidays.add(LocalDate.of(2014, 4, 21));
    holidays.add(LocalDate.of(2014, 5, 1));
    holidays.add(LocalDate.of(2014, 12, 25));
    holidays.add(LocalDate.of(2014, 12, 26));
    holidays.add(LocalDate.of(2014, 12, 31));
    while (date.isBefore(end)) {
      switch (date.getDayOfWeek()) {
        case SATURDAY:
        case SUNDAY:
          assertFalse(BUSINESS_DAY.isBusinessday(FinmathDateUtils.convertLocalDate(date)));
          break;
        default:
          if (holidays.contains(date)) {
            assertFalse(BUSINESS_DAY.isBusinessday(FinmathDateUtils.convertLocalDate(date)));
          } else {
            assertTrue(BUSINESS_DAY.isBusinessday(FinmathDateUtils.convertLocalDate(date)));
          }
          break;
      }
      date = date.plusDays(1);
    }
  }
}
