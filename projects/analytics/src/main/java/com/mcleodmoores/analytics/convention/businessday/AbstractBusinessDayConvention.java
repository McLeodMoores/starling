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

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.mcleodmoores.date.WorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 *
 */
public abstract class AbstractBusinessDayConvention extends com.opengamma.financial.convention.businessday.AbstractBusinessDayConvention
    implements BusinessDayConvention {

  @Override
  public LocalDate adjustDate(final Calendar calendar, final LocalDate date) {
    return adjustDate(WorkingDayCalendarAdapter.of(calendar, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), date);
  }

  @Override
  public LocalDateTime adjustDate(final WorkingDayCalendar workingDayCalendar, final LocalDateTime dateTime) {
    final LocalDate adjusted = adjustDate(workingDayCalendar, dateTime.toLocalDate());
    return adjusted.atTime(dateTime.toLocalTime());
  }

  @Override
  public ZonedDateTime adjustDate(final WorkingDayCalendar workingDayCalendar, final ZonedDateTime dateTime) {
    final LocalDate adjusted = adjustDate(workingDayCalendar, dateTime.toLocalDate());
    return adjusted.atTime(dateTime.toLocalTime()).atZone(dateTime.getZone());
  }

  @Override
  public TemporalAdjuster getTemporalAdjuster(final WorkingDayCalendar workingDayCalendar) {
    return null;
  }

}
