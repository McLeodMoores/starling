/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import java.util.Collection;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 * Implementation of a {@link Calendar} for use in tests. The weekend days are Saturday and Sundays.
 */
@SuppressWarnings("deprecation")
/* package */ class TestCalendar extends MondayToFridayCalendar {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /** The holiday dates */
  private final Collection<LocalDate> _dates;

  /**
   * Creates an instance.
   * @param dates  the holiday dates
   */
  public TestCalendar(final Collection<LocalDate> dates) {
    super("Weekend");
    _dates = dates;
  }

  @Override
  protected boolean isNormallyWorkingDay(final LocalDate date) {
    if (_dates.contains(date)) {
      return false;
    }
    return super.isNormallyWorkingDay(date);
  }
}