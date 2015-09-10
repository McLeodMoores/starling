/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * An adapter for {@link WorkingDayCalendar}s that converts them into {@link Calendar}s. This class
 * should be used when backwards compatibility is required.
 */
public class CalendarAdapter implements WorkingDayCalendar, Calendar {
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
  public boolean isHoliday(final LocalDate date) {
    return _calendar.isHoliday(date);
  }

  @Override
  public boolean isWeekend(final LocalDate date) {
    return _calendar.isWeekend(date);
  }

}
