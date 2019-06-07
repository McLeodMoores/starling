/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.convention.businessday;

import org.joda.convert.ToString;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.mcleodmoores.date.WorkingDayCalendar;

/**
 * Convention for handling business days.
 * <p>
 * This convention is a replacement for {@link com.opengamma.financial.convention.businessday.BusinessDayConvention}, which uses the
 * deprecated {@link com.opengamma.financial.convention.calendar.Calendar} class.
 */
public interface BusinessDayConvention extends com.opengamma.financial.convention.businessday.BusinessDayConvention {

  /**
   * Adjusts the specified date using the working day calendar.
   *
   * @param workingDayCalendar
   *          the working days, not null
   * @param date
   *          the date to adjust, not null
   * @return the adjusted date, not null
   */
  LocalDate adjustDate(WorkingDayCalendar workingDayCalendar, LocalDate date);

  /**
   * Adjusts the specified date using the working day calendar.
   *
   * @param workingDayCalendar
   *          the working days, not null
   * @param date
   *          the date to adjust, not null
   * @return the adjusted date, not null
   */
  LocalDateTime adjustDate(WorkingDayCalendar workingDayCalendar, LocalDateTime date);

  /**
   * Adjusts the specified date-time using the working day calendar.
   *
   * @param workingDayCalendar
   *          the working days, not null
   * @param dateTime
   *          the date-time to adjust, not null
   * @return the adjusted date-time, not null
   */
  ZonedDateTime adjustDate(WorkingDayCalendar workingDayCalendar, ZonedDateTime dateTime);

  /**
   * Converts this convention to a {@code TemporalAdjuster} using the specified working day calendar.
   *
   * @param workingDayCalendar
   *          the working days, not null
   * @return the date adjuster, not null
   */
  TemporalAdjuster getTemporalAdjuster(WorkingDayCalendar workingDayCalendar);

  /**
   * Gets the name of the convention.
   *
   * @return the name, not null
   */
  @Override
  @ToString
  String getName();

}
