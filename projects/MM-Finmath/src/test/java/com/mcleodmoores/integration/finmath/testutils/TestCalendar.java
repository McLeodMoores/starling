/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.testutils;

import java.util.Collection;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Test calendar implementation.
 */
public class TestCalendar implements Calendar {
  /** The calendar name */
  private final String _name;
  /** The holiday dates */
  private final Collection<LocalDate> _holidays;

  /**
   * Creates an instance.
   * @param name The name
   * @param holidays The holiday dates.
   */
  public TestCalendar(final String name, final Collection<LocalDate> holidays) {
    _name = name;
    _holidays = holidays;
  }

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    return !_holidays.contains(date);
  }

  @Override
  public String getConventionName() {
    return _name;
  }

  @Override
  public String getName() {
    return _name;
  }
}
