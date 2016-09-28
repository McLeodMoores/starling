/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.LocalDate;

/**
 * An implementation of a working day calendar that contains no holiday dates (i.e. every day including weekends is a working day).
 */
public final class EmptyWorkingDayCalendar implements WorkingDayCalendar {

  /**
   * An instance of this calendar.
   */
  public static final WorkingDayCalendar INSTANCE = new EmptyWorkingDayCalendar();
  /** The calendar name */
  private static final String NAME = "Empty";

  /**
   * Restricted constructor.
   */
  private EmptyWorkingDayCalendar() {
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    return true;
  }

  @Override
  public boolean isHoliday(final LocalDate date) {
    return false;
  }

  @Override
  public boolean isWeekend(final LocalDate date) {
    return false;
  }

}
