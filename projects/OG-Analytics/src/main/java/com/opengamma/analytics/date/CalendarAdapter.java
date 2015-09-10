/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import java.util.Objects;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * An adapter for {@link WorkingDayCalendar}s that converts them into {@link Calendar}s. This class
 * should be used when backwards compatibility is required.
 */
public class CalendarAdapter implements Calendar {
  /** The underlying working day calendar */
  private final WorkingDayCalendar _calendar;

  /**
   * Creates an adapter.
   * @param calendar  the underlying working day calendar, not null
   */
  public CalendarAdapter(final WorkingDayCalendar calendar) {
    _calendar = ArgumentChecker.notNull(calendar, "calendar");
  }

  @Override
  public String getName() {
    return _calendar.getName();
  }

  @Override
  public String getConventionName() {
    return _calendar.getName();
  }

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    return !(_calendar.isHoliday(date) || _calendar.isWeekend(date));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calendar.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CalendarAdapter)) {
      return false;
    }
    final CalendarAdapter other = (CalendarAdapter) obj;
    return Objects.equals(_calendar, other._calendar);
  }

}
