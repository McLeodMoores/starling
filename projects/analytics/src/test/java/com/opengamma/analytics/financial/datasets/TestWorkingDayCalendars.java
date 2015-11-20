/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.datasets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.date.SimpleWorkingDayCalendar;
import com.opengamma.analytics.date.WorkingDayCalendar;

/**
 * Simple calendars for use in tests. These calendars are not suitable for use elsewhere.
 */
public final class TestWorkingDayCalendars {
  /** USD holidays excluding weekends */
  private static final Collection<LocalDate> USD_HOLIDAYS = new ArrayList<>(Arrays.asList(
      LocalDate.of(2015,  1,  19),
      LocalDate.of(2015, 2, 16),
      LocalDate.of(2015, 5, 25),
      LocalDate.of(2015, 9, 7),
      LocalDate.of(2015, 10, 12),
      LocalDate.of(2015, 11, 11),
      LocalDate.of(2015, 11, 26),
      LocalDate.of(2016, 1, 18),
      LocalDate.of(2016, 2, 15),
      LocalDate.of(2016, 5, 30),
      LocalDate.of(2016, 9, 5),
      LocalDate.of(2016, 10, 10),
      LocalDate.of(2016, 11, 11),
      LocalDate.of(2016, 11, 24),
      LocalDate.of(2016, 12, 26),
      LocalDate.of(2017, 1, 2),
      LocalDate.of(2017, 1, 16),
      LocalDate.of(2017, 2, 20),
      LocalDate.of(2017, 5, 29),
      LocalDate.of(2017, 9, 4),
      LocalDate.of(2017, 10, 9),
      LocalDate.of(2017, 11, 23)));

  static {
    for (int i = 2013; i <= 2063; i++) {
      USD_HOLIDAYS.add(LocalDate.of(i, 1, 1));
      USD_HOLIDAYS.add(LocalDate.of(i, 7, 4));
      USD_HOLIDAYS.add(LocalDate.of(i, 12, 25));
    }
  }

  /**
   * Restricted constructor.
   */
  private TestWorkingDayCalendars() {
  }

  /**
   * A USD  calendar.
   */
  public static final WorkingDayCalendar USD_CALENDAR = new SimpleWorkingDayCalendar("USD", USD_HOLIDAYS, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
}
