/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 *
 */
public class WorkingDayCalendarAdapter implements WorkingDayCalendar, Calendar {
  private final WorkingDayCalendar _calendar;

  public WorkingDayCalendarAdapter(final WorkingDayCalendar calendar) {
    _calendar = calendar;
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
