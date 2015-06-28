/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import java.util.Objects;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * A calendar that contains only information about weekends. {@link #isHoliday(LocalDate)} will return true if the
 * date is a weekend day.
 */
public class WeekendWorkingDayCalendar implements WorkingDayCalendar {

  /**
   * A calendar with weekend days Saturday and Sunday.
   */
  public static final WorkingDayCalendar SATURDAY_SUNDAY =
      new WeekendWorkingDayCalendar("Saturday / Sunday", DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

  /**
   * A calendar with weekend days Thursday and Friday.
   */
  public static final WorkingDayCalendar THURSDAY_FRIDAY =
      new WeekendWorkingDayCalendar("Thursday / Friday", DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

  /**
   * A calendar with weekend days Friday and Saturday.
   */
  public static final WorkingDayCalendar FRIDAY_SATURDAY =
      new WeekendWorkingDayCalendar("Friday / Saturday", DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

  /** The name of the calendar */
  private final String _name;
  /** The first weekend day */
  private final DayOfWeek _weekendDay1;
  /** The second weekend day */
  private final DayOfWeek _weekendDay2;

  /**
   * Creates an instance.
   * @param name  the name of the calendar, not null
   * @param weekendDay1  the first weekend day, not null
   * @param weekendDay2  the second weekend day, not null
   */
  public WeekendWorkingDayCalendar(final String name, final DayOfWeek weekendDay1, final DayOfWeek weekendDay2) {
    _name = ArgumentChecker.notNull(name, "name");
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
    return !isWeekend(date);
  }

  @Override
  public boolean isHoliday(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return isWeekend(date);
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
    if (!(obj instanceof WeekendWorkingDayCalendar)) {
      return false;
    }
    final WeekendWorkingDayCalendar other = (WeekendWorkingDayCalendar) obj;
    if (_weekendDay1 != other._weekendDay1) {
      return false;
    }
    if (_weekendDay2 != other._weekendDay2) {
      return false;
    }
    if (!Objects.equals(_name, other._name)) {
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
    sb.append("]");
    return sb.toString();
  }
}
