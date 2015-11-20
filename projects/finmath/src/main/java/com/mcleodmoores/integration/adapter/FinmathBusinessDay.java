/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import java.util.Objects;

import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface;

import org.joda.convert.FromStringFactory;

import com.mcleodmoores.integration.convention.BusinessDay;

/**
 * Top-level wrapper for {@link BusinessdayCalendarInterface} classes.
 */
@FromStringFactory(factory = FinmathBusinessDayFactory.class)
public abstract class FinmathBusinessDay implements BusinessDay, BusinessdayCalendarInterface {
  /** The name of the convention */
  private final String _name;
  /** The underlying calendar implementation */
  private final BusinessdayCalendarInterface _businessDay;

  /**
   * Creates an instance.
   * @param name The name, can be null
   * @param businessDay The business day calendar, can be null
   */
  protected FinmathBusinessDay(final String name, final BusinessdayCalendarInterface businessDay) {
    _name = name;
    _businessDay = businessDay;
  }

  @Override
  public boolean isBusinessday(final java.util.Calendar date) {
    return _businessDay.isBusinessday(date);
  }

  @Override
  public java.util.Calendar getAdjustedDate(final java.util.Calendar date, final DateRollConvention dateRollConvention) {
    return _businessDay.getAdjustedDate(date, dateRollConvention);
  }

  @Override
  public java.util.Calendar getAdjustedDate(final java.util.Calendar baseDate, final String dateOffsetCode, final DateRollConvention dateRollConvention) {
    return _businessDay.getAdjustedDate(baseDate, dateOffsetCode, dateRollConvention);
  }

  @Override
  public java.util.Calendar getRolledDate(final java.util.Calendar baseDate, final int businessDays) {
    // equivalent to ScheduleCalculator.getAdjustedDate
    return _businessDay.getRolledDate(baseDate, businessDays);
  }

  @Override
  public String getName() {
    return _name;
  }

  /**
   * Gets the underlying calendar.
   * @return The underlying calendar
   */
  public BusinessdayCalendarInterface getUnderlyingCalendar() {
    return _businessDay;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_name == null ? 0 : _name.hashCode());
    // doesn't make sense to wrap a calendar
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FinmathBusinessDay)) {
      return false;
    }
    final FinmathBusinessDay other = (FinmathBusinessDay) obj;
    // doesn't make sense to wrap a calendar
    return Objects.equals(_name, other._name);
  }

}
