/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.testutils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple implementation of a calendar for testing purposes that is backed by a Set and can combine
 * other holidays.
 */
public class ListCalendar implements Calendar {
  /** The calendar name */
  private final String _name;
  /** The holiday dates */
  private final Set<LocalDate> _holidayDays;
  /** A set of calendars to combine */
  private final Collection<Calendar> _calendars;

  /**
   * Creates an instance that does not combine other calendars with the holiday dates.
   * @param name  the calendar name, not null
   * @param holidayDays  the holiday days, not null
   */
  public ListCalendar(final String name, final Set<LocalDate> holidayDays) {
    this(name, holidayDays, new HashSet<Calendar>());
  }

  /**
   * Creates an instance that does not combine other calendars with the holiday dates.
   * @param name  the calendar name, not null
   * @param holidayDays  the holiday days, not null
   * @param calendar  the calendar, not null
   */
  public ListCalendar(final String name, final Set<LocalDate> holidayDays, final Calendar calendar) {
    this(name, holidayDays, Collections.singleton(ArgumentChecker.notNull(calendar, "calendar")));
  }

  /**
   * Creates an instance that does not combine other calendars with the holiday dates.
   * @param name  the calendar name, not null
   * @param holidayDays  the holiday days, not null
   * @param calendars  the calendars, not null
   */
  public ListCalendar(final String name, final Set<LocalDate> holidayDays, final Collection<Calendar> calendars) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(holidayDays, "holidayDays");
    ArgumentChecker.notNull(calendars, "calendars");
    _name = name;
    _holidayDays = holidayDays;
    _calendars = calendars;
  }

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    // check the other calendars first, as it is likely one is a weekend calendar
    for (final Calendar calendar : _calendars) {
      if (!calendar.isWorkingDay(date)) {
        return false;
      }
    }
    return !_holidayDays.contains(date);
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
