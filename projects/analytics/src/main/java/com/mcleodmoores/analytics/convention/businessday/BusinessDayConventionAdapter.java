/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.convention.businessday;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 *
 */
public class BusinessDayConventionAdapter implements BusinessDayConvention {

  public static BusinessDayConvention of(final com.opengamma.financial.convention.businessday.BusinessDayConvention convention) {
    return new BusinessDayConventionAdapter(convention);
  }

  private final com.opengamma.financial.convention.businessday.BusinessDayConvention _convention;

  private BusinessDayConventionAdapter(final com.opengamma.financial.convention.businessday.BusinessDayConvention convention) {
    _convention = convention;
  }

  @Override
  public LocalDate adjustDate(final Calendar workingDayCalendar, final LocalDate date) {
    return _convention.adjustDate(workingDayCalendar, date);
  }

  @Override
  public ZonedDateTime adjustDate(final Calendar workingDayCalendar, final ZonedDateTime dateTime) {
    return _convention.adjustDate(workingDayCalendar, dateTime);
  }

  @Override
  public TemporalAdjuster getTemporalAdjuster(final Calendar workingDayCalendar) {
    return _convention.getTemporalAdjuster(workingDayCalendar);
  }

  @Override
  public String getConventionName() {
    return _convention.getConventionName();
  }

  @Override
  public LocalDate adjustDate(final WorkingDayCalendar workingDayCalendar, final LocalDate date) {
    return adjustDate(CalendarAdapter.of(workingDayCalendar), date);
  }

  @Override
  public LocalDateTime adjustDate(final WorkingDayCalendar workingDayCalendar, final LocalDateTime date) {
    return adjustDate(CalendarAdapter.of(workingDayCalendar), date.toLocalDate()).atTime(date.toLocalTime());
  }

  @Override
  public ZonedDateTime adjustDate(final WorkingDayCalendar workingDayCalendar, final ZonedDateTime dateTime) {
    return adjustDate(CalendarAdapter.of(workingDayCalendar), dateTime);
  }

  @Override
  public TemporalAdjuster getTemporalAdjuster(final WorkingDayCalendar workingDayCalendar) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    return _convention.getName();
  }
}
