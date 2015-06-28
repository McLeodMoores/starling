/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.LocalDate;

import com.opengamma.util.NamedInstance;

/**
 * An interface that supplies working day information. Weekends and holiday dates must explicitly be defined.
 * This allows date adjustments for asset classes such as FX to be calculated.
 */
public interface WorkingDayCalendar extends NamedInstance {

  /**
   * Returns true if the date is a working day. This will usually return the intersection of the
   * holiday calendar(s) with weekend days.
   * @param date  the date, not null
   * @return  true if the date is a working day
   */
  boolean isWorkingDay(LocalDate date);

  /**
   * Returns true if the date is a holiday, which may or may not return weekend days.
   * @param date  the date, not null
   * @return  true if the date is a holiday
   */
  boolean isHoliday(LocalDate date);

  /**
   * Returns true if the date is a weekend.
   * @param date  the date, not null
   * @return  true if the date is a weekend
   */
  boolean isWeekend(LocalDate date);

}
