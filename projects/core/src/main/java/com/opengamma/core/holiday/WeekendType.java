/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * Contains information about which days of the week are weekends. This allows the holiday sources to be used
 * without explicitly storing every weekend as a holiday while removing the hard coding found in some of the
 * sources.
 */
public enum WeekendType {

  /**
   * No weekend days specified. Useful in cases where backwards compatibility is required.
   */
  NONE(null, null) {

    @Override
    public boolean isWeekend(final LocalDate date) {
      return false;
    }
  },
  /**
   * Thursday and Friday. Used in some Islamic countries.
   */
  THURSDAY_FRIDAY(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),

  /**
   * Friday and Saturday. Used in some Islamic countries and Israel.
   */
  FRIDAY_SATURDAY(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),

  /**
   * Saturday and Sunday. Used in the rest of the world.
   */
  SATURDAY_SUNDAY(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

  /** The first day of the weekend */
  private final DayOfWeek _day1;
  /** The second day of the weekend */
  private final DayOfWeek _day2;

  /**
   * Creates an instance.
   * @param day1  the first day of the weekend
   * @param day2  the second day of the weekend
   */
  private WeekendType(final DayOfWeek day1, final DayOfWeek day2) {
    _day1 = day1;
    _day2 = day2;
  }

  /**
   * Returns true if the date falls on a weekend.
   * @param date  the date, not null
   * @return  true if the date falls on a weekend
   */
  public boolean isWeekend(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    final DayOfWeek day = date.getDayOfWeek();
    return day == _day1 || day == _day2;
  }
}
