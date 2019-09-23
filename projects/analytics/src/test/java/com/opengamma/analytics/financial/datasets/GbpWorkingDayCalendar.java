/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.datasets;

import java.util.Collection;
import java.util.HashSet;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;

/**
 * GBP holidays for tests.
 */
public final class GbpWorkingDayCalendar {

  /**
   * An instance of the calendar.
   */
  public static final WorkingDayCalendar INSTANCE;

  static {
    final Collection<LocalDate> dates = new HashSet<>();
    final int startYear = 2013;
    final int endYear = 2063;
    for (int i = startYear; i <= endYear; i++) {
      dates.add(LocalDate.of(i, 1, 1));
      dates.add(LocalDate.of(i, 12, 25));
      dates.add(LocalDate.of(i, 12, 26));
    }
    final LocalDate[] easter = new LocalDate[] { LocalDate.of(2013, 3, 31), LocalDate.of(2014, 4, 20), LocalDate.of(2015, 4, 5),
        LocalDate.of(2016, 3, 27), LocalDate.of(2017, 4, 16), LocalDate.of(2018, 4, 1), LocalDate.of(2019, 4, 21),
        LocalDate.of(2020, 4, 12),
        LocalDate.of(2021, 4, 4), LocalDate.of(2022, 4, 17), LocalDate.of(2023, 4, 9), LocalDate.of(2024, 3, 31), LocalDate.of(2025, 4, 20),
        LocalDate.of(2026, 4, 5), LocalDate.of(2027, 3, 28), LocalDate.of(2028, 4, 16), LocalDate.of(2029, 4, 1), LocalDate.of(2030, 4, 21),
        LocalDate.of(2031, 4, 13), LocalDate.of(2032, 3, 28), LocalDate.of(2033, 4, 17), LocalDate.of(2034, 4, 9),
        LocalDate.of(2035, 3, 25),
        LocalDate.of(2036, 4, 13), LocalDate.of(2037, 4, 5), LocalDate.of(2038, 4, 25), LocalDate.of(2039, 4, 10), LocalDate.of(2040, 4, 1),
        LocalDate.of(2041, 4, 21), LocalDate.of(2042, 4, 6), LocalDate.of(2043, 3, 29), LocalDate.of(2044, 4, 17), LocalDate.of(2045, 4, 9),
        LocalDate.of(2046, 3, 25), LocalDate.of(2047, 4, 14), LocalDate.of(2048, 4, 5), LocalDate.of(2049, 4, 18) };
    for (final LocalDate element : easter) {
      dates.add(element.minusDays(2)); // Easter Friday
      dates.add(element.plusDays(1)); // Easter Monday
    }
    dates.add(LocalDate.of(2014, 4, 18));
    dates.add(LocalDate.of(2014, 4, 21));
    dates.add(LocalDate.of(2014, 5, 5));
    dates.add(LocalDate.of(2014, 5, 26));
    dates.add(LocalDate.of(2014, 8, 25));
    dates.add(LocalDate.of(2015, 4, 3));
    dates.add(LocalDate.of(2015, 4, 6));
    dates.add(LocalDate.of(2015, 5, 4));
    dates.add(LocalDate.of(2015, 5, 25));
    dates.add(LocalDate.of(2015, 8, 31));
    dates.add(LocalDate.of(2015, 12, 28));
    dates.add(LocalDate.of(2016, 3, 25));
    dates.add(LocalDate.of(2016, 3, 28));
    dates.add(LocalDate.of(2016, 5, 2));
    dates.add(LocalDate.of(2016, 5, 30));
    dates.add(LocalDate.of(2016, 8, 29));
    dates.add(LocalDate.of(2016, 12, 27));
    dates.add(LocalDate.of(2017, 1, 2));
    dates.add(LocalDate.of(2017, 4, 14));
    dates.add(LocalDate.of(2017, 4, 17));
    dates.add(LocalDate.of(2017, 5, 1));
    dates.add(LocalDate.of(2017, 5, 29));
    dates.add(LocalDate.of(2017, 8, 28));
    dates.add(LocalDate.of(2018, 3, 30));
    dates.add(LocalDate.of(2018, 4, 2));
    dates.add(LocalDate.of(2018, 5, 7));
    dates.add(LocalDate.of(2018, 5, 28));
    dates.add(LocalDate.of(2018, 8, 27));
    dates.add(LocalDate.of(2019, 4, 19));
    dates.add(LocalDate.of(2019, 4, 22));
    dates.add(LocalDate.of(2019, 5, 6));
    dates.add(LocalDate.of(2019, 5, 27));
    dates.add(LocalDate.of(2019, 8, 26));
    INSTANCE = new SimpleWorkingDayCalendar("GBP", dates, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  }

  private GbpWorkingDayCalendar() {
  }
}
