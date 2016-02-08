/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.TreeSet;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * A calendar that contains information about weekends and holiday dates. {@link #isHoliday(LocalDate)} will return true
 * if the date is a weekend or contained in the collection of holidays.
 */
public class SimpleWorkingDayCalendar implements WorkingDayCalendar {
  /** The name of the calendar */
  private final String _name;
  /** The holiday dates */
  private final Collection<LocalDate> _holidays;
  /** The first weekend day */
  private final DayOfWeek _weekendDay1;
  /** The second weekend day */
  private final DayOfWeek _weekendDay2;

  /**
   * Creates an instance.
   * @param name  the name of the calendar, not null
   * @param holidays  the holiday dates, not null
   * @param weekendDay1  the first weekend date, not null
   * @param weekendDay2  the second weekend date, not null
   */
  public SimpleWorkingDayCalendar(final String name, final Collection<LocalDate> holidays, final DayOfWeek weekendDay1,
      final DayOfWeek weekendDay2) {
    _name = ArgumentChecker.notNull(name, "name");
    _holidays = Collections.unmodifiableSet(new TreeSet<>(ArgumentChecker.notNull(holidays, "holidays")));
    _weekendDay1 = ArgumentChecker.notNull(weekendDay1, "weekendDay1");
    _weekendDay2 = ArgumentChecker.notNull(weekendDay2, "weekendDay2");
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return !(isHoliday(date) || isWeekend(date));
  }

  @Override
  public boolean isHoliday(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return _holidays.contains(date);
  }

  @Override
  public boolean isWeekend(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return date.getDayOfWeek() == _weekendDay1 || date.getDayOfWeek() == _weekendDay2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _holidays.hashCode();
    result = prime * result + _name.hashCode();
    result = prime * result + _weekendDay1.hashCode();
    result = prime * result + _weekendDay2.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SimpleWorkingDayCalendar)) {
      return false;
    }
    final SimpleWorkingDayCalendar other = (SimpleWorkingDayCalendar) obj;
    if (_weekendDay1 != other._weekendDay1) {
      return false;
    }
    if (_weekendDay2 != other._weekendDay2) {
      return false;
    }
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    if (!Objects.equals(_holidays, other._holidays)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(_name);
    sb.append(": [");
    sb.append(_weekendDay1);
    sb.append(", ");
    sb.append(_weekendDay2);
    sb.append("], ");
    sb.append(new TreeSet<>(_holidays));
    return sb.toString();
  }

}
