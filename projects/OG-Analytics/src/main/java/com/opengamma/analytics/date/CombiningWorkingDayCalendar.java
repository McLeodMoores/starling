/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;

/**
 * A calendar that combines {@link WorkingDayCalendar}s. This calendar does not merge the underlying calendars,
 * but uses each underlying calendar's weekend and holiday definition in turn.
 */
public class CombiningWorkingDayCalendar implements WorkingDayCalendar {
  /** The name of the combined calendar */
  private final String _name;
  /** The calendars to combine */
  private final Set<WorkingDayCalendar> _calendars;

  /**
   * Creates an instance.
   * @param calendars  the calendars, not null or empty
   */
  public CombiningWorkingDayCalendar(final Collection<WorkingDayCalendar> calendars) {
    _calendars = Collections.unmodifiableSet(new HashSet<>(ArgumentChecker.notEmpty(calendars, "calendars")));
    final StringBuilder sb = new StringBuilder();
    for (final WorkingDayCalendar calendar : calendars) {
      sb.append(calendar.getName());
      sb.append("+");
    }
    _name = sb.substring(0, sb.length() - 1);
  }

  /**
   * Creates an instance.
   * @param calendars  the calendars, not null or empty
   */
  public CombiningWorkingDayCalendar(final WorkingDayCalendar... calendars) {
    _calendars = Collections.unmodifiableSet(Sets.newHashSet(ArgumentChecker.notEmpty(calendars, "calendars")));
    final StringBuilder sb = new StringBuilder();
    for (final WorkingDayCalendar calendar : calendars) {
      sb.append(calendar.getName());
      sb.append("+");
    }
    _name = sb.substring(0, sb.length() - 1);
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    return !(isWeekend(date) || isHoliday(date));
  }

  @Override
  public boolean isHoliday(final LocalDate date) {
    for (final WorkingDayCalendar calendar : _calendars) {
      if (calendar.isHoliday(date)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isWeekend(final LocalDate date) {
    for (final WorkingDayCalendar calendar : _calendars) {
      if (calendar.isWeekend(date)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calendars.hashCode();
    result = prime * result + _name.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CombiningWorkingDayCalendar)) {
      return false;
    }
    final CombiningWorkingDayCalendar other = (CombiningWorkingDayCalendar) obj;
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    if (!Objects.equals(_calendars, other._calendars)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final WorkingDayCalendar calendar : _calendars) {
      sb.append(calendar.toString());
      sb.append(" + ");
    }
    return sb.substring(0, sb.length() - 3);
  }
}
