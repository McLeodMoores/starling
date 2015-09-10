/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * An adapter for {@link Calendar}s that converts them into {@link WorkingDayCalendar}s that allows the weekends
 * to be defined explicitly.
 * <p>
 * This class should be used when backwards compatibility is required.
 */
public class WorkingDayCalendarAdapter implements WorkingDayCalendar, Calendar {
  /** The underlying calendar */
  private final Calendar _calendar;
  /** The first weekend day */
  private final DayOfWeek _weekendDay1;
  /** The second weekend day */
  private final DayOfWeek _weekendDay2;

  /**
   * Creates an adapter.
   * @param calendar  the underlying calendar, not null
   * @param weekendDay1  the first weekend day, not null
   * @param weekendDay2  the second weekend day, not null
   */
  public WorkingDayCalendarAdapter(final Calendar calendar, final DayOfWeek weekendDay1, final DayOfWeek weekendDay2) {
    _calendar = ArgumentChecker.notNull(calendar, "calendar");
    _weekendDay1 = ArgumentChecker.notNull(weekendDay1, "weekendDay1");
    _weekendDay2 = ArgumentChecker.notNull(weekendDay2, "weekendDay2");
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
    return _calendar.isWorkingDay(date);
  }

  @Override
  public boolean isHoliday(final LocalDate date) {
    // holidays exclude weekend days so short circuit
    if (date.getDayOfWeek() == _weekendDay1 || date.getDayOfWeek() == _weekendDay2) {
      return false;
    }
    return !_calendar.isWorkingDay(date);
  }

  @Override
  public boolean isWeekend(final LocalDate date) {
    return date.getDayOfWeek() == _weekendDay1 || date.getDayOfWeek() == _weekendDay2;
  }

}
