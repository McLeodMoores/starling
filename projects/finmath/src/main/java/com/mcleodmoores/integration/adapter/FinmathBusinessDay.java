/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import java.util.Objects;

import org.joda.convert.FromStringFactory;
import org.joda.time.LocalDate;

import com.mcleodmoores.integration.convention.BusinessDay;
import com.opengamma.util.ArgumentChecker;

import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface;

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
   * @param name The name, not null
   * @param businessDay The business day calendar, not null
   */
  protected FinmathBusinessDay(final String name, final BusinessdayCalendarInterface businessDay) {
    _name = ArgumentChecker.notNull(name, "name");
    _businessDay = ArgumentChecker.notNull(businessDay, "businessDay");
  }

  @Override
  public boolean isBusinessday(final LocalDate date) {
    return _businessDay.isBusinessday(date);
  }

  @Override
  public LocalDate getAdjustedDate(final LocalDate date, final DateRollConvention dateRollConvention) {
    return _businessDay.getAdjustedDate(date, dateRollConvention);
  }

  @Override
  public LocalDate getAdjustedDate(final LocalDate baseDate, final String dateOffsetCode, final DateRollConvention dateRollConvention) {
    return _businessDay.getAdjustedDate(baseDate, dateOffsetCode, dateRollConvention);
  }

  @Override
  public LocalDate getRolledDate(final LocalDate baseDate, final int businessDays) {
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
    result = prime * result + _name.hashCode();
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
