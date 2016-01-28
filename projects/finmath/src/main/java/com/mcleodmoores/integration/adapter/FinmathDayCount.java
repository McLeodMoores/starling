/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import java.util.Objects;

import org.joda.convert.FromStringFactory;
import org.joda.time.LocalDate;

import com.mcleodmoores.integration.convention.DayCount;
import com.opengamma.util.ArgumentChecker;

import net.finmath.time.daycount.DayCountConventionInterface;

/**
 * Top-level wrapper for {@link DayCountConventionInterface} classes.
 */
@FromStringFactory(factory = FinmathDayCountFactory.class)
public abstract class FinmathDayCount implements DayCount, DayCountConventionInterface {
  /** The name of the convention */
  private final String _name;
  /** The day count */
  private final DayCountConventionInterface _dayCount;

  /**
   * Creates an instance.
   * @param name The name, not null
   * @param dayCount The day count, not null
   */
  protected FinmathDayCount(final String name, final DayCountConventionInterface dayCount) {
    _name = ArgumentChecker.notNull(name, "name");
    _dayCount = ArgumentChecker.notNull(dayCount, "dayCount");
  }

  @Override
  public double getDaycount(final LocalDate startDate, final LocalDate endDate) {
    return getConvention().getDaycount(startDate, endDate);
  }

  @Override
  public double getDaycountFraction(final LocalDate startDate, final LocalDate endDate) {
    return getConvention().getDaycountFraction(startDate, endDate);
  }

  /**
   * Gets the convention.
   * @return The convention
   */
  public DayCountConventionInterface getConvention() {
    return _dayCount;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_name == null ? 0 : _name.hashCode());
    result = prime * result + (_dayCount == null ? 0 : _dayCount.getClass().getName().hashCode());
    // don't use day-count directly because hashCode() is not implemented
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FinmathDayCount)) {
      return false;
    }
    final FinmathDayCount other = (FinmathDayCount) obj;
    return Objects.equals(_name, other._name)
        && Objects.equals(_dayCount.getClass().getName(), other._dayCount.getClass().getName());
    //don't use day-count directly because equals() is not implemented
  }

}
