/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics;

import java.util.Collection;
import java.util.HashSet;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.date.SimpleWorkingDayCalendar;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.impl.SimpleHoliday;
import com.opengamma.core.holiday.impl.SimpleHolidayWithWeekend;

/**
 * Utility class that contains a {@link WorkingDayCalendar} and {@link SimpleHolidayWithWeekend}
 * that returns TARGET holiday dates for tests. Based on {@link CalendarTarget}.
 */
public final class TargetWorkingDayCalendar {
  /** Holiday dates */
  private static final Collection<LocalDate> DATES = new HashSet<>();

  static {
    final int startYear = 2013;
    final int endYear = 2063;

    for (int loopy = startYear; loopy <= endYear; loopy++) {
      DATES.add(LocalDate.of(loopy, 1, 1));
      DATES.add(LocalDate.of(loopy, 5, 1));
      DATES.add(LocalDate.of(loopy, 12, 25));
      DATES.add(LocalDate.of(loopy, 12, 26));
    }
    final LocalDate[] easter = new LocalDate[] {LocalDate.of(2013, 3, 31), LocalDate.of(2014, 4, 20), LocalDate.of(2015, 4, 5),
        LocalDate.of(2016, 3, 27), LocalDate.of(2017, 4, 16), LocalDate.of(2018, 4, 1), LocalDate.of(2019, 4, 21), LocalDate.of(2020, 4, 12),
        LocalDate.of(2021, 4, 4), LocalDate.of(2022, 4, 17), LocalDate.of(2023, 4, 9), LocalDate.of(2024, 3, 31), LocalDate.of(2025, 4, 20),
        LocalDate.of(2026, 4, 5), LocalDate.of(2027, 3, 28), LocalDate.of(2028, 4, 16), LocalDate.of(2029, 4, 1), LocalDate.of(2030, 4, 21),
        LocalDate.of(2031, 4, 13), LocalDate.of(2032, 3, 28), LocalDate.of(2033, 4, 17), LocalDate.of(2034, 4, 9), LocalDate.of(2035, 3, 25),
        LocalDate.of(2036, 4, 13), LocalDate.of(2037, 4, 5), LocalDate.of(2038, 4, 25), LocalDate.of(2039, 4, 10), LocalDate.of(2040, 4, 1),
        LocalDate.of(2041, 4, 21), LocalDate.of(2042, 4, 6), LocalDate.of(2043, 3, 29), LocalDate.of(2044, 4, 17), LocalDate.of(2045, 4, 9),
        LocalDate.of(2046, 3, 25), LocalDate.of(2047, 4, 14), LocalDate.of(2048, 4, 5), LocalDate.of(2049, 4, 18) };
    for (final LocalDate element : easter) {
      DATES.add(element.minusDays(2)); // Easter Friday
      DATES.add(element.plusDays(1)); // Easter Monday
    }
  }
  /**
   * TARGET working day calendar.
   */
  public static final WorkingDayCalendar TARGET_CALENDAR = new SimpleWorkingDayCalendar("TARGET", DATES, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  /**
   * TARGET holidays.
   */
  public static final SimpleHoliday TARGET_HOLIDAY = new SimpleHolidayWithWeekend(DATES, WeekendType.SATURDAY_SUNDAY);
  /**
   * Restricted constructor.
   */
  private TargetWorkingDayCalendar() {
  }
}
