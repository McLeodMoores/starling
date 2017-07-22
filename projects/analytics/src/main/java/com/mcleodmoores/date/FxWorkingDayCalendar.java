/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.date;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A calendar that contains per-currency working day calendars to be used in FX date calculations. For any
 * currency pair ABC/DEF, an appropriate working day calendar for each currency plus a US working day
 * calendar must be supplied.
 */
public class FxWorkingDayCalendar implements WorkingDayCalendar {
  /** The name of the calendar */
  private final String _name;
  /** A map from currency to working day calendar */
  private final Map<Currency, WorkingDayCalendar> _perCurrencyCalendars;

  /**
   * Creates an instance.
   * @param name  the name of the calendar, not null
   * @param perCurrencyCalendars  a map from currency to working day calendar, not null, must contain at least two calendars
   * and one for {@link Currency#USD}.
   */
  public FxWorkingDayCalendar(final String name, final Map<Currency, WorkingDayCalendar> perCurrencyCalendars) {
    _name = ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(perCurrencyCalendars, "perCurrencyCalendars");
    ArgumentChecker.isTrue(perCurrencyCalendars.size() > 1, "FX calendar must contain at least two calendars");
    ArgumentChecker.isTrue(perCurrencyCalendars.containsKey(Currency.USD), "FX calendar must contain a USD calendar");
    _perCurrencyCalendars = Collections.unmodifiableMap(perCurrencyCalendars);
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    for (final Map.Entry<Currency, WorkingDayCalendar> entry : _perCurrencyCalendars.entrySet()) {
      if (!entry.getValue().isWorkingDay(date)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isHoliday(final LocalDate date) {
    for (final Map.Entry<Currency, WorkingDayCalendar> entry : _perCurrencyCalendars.entrySet()) {
      if (entry.getValue().isHoliday(date)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isWeekend(final LocalDate date) {
    for (final Map.Entry<Currency, WorkingDayCalendar> entry : _perCurrencyCalendars.entrySet()) {
      if (entry.getValue().isWeekend(date)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the working day calendar for a currency. If no calendar is available for the currency, throws
   * an exception.
   * @param currency  the currency, not null
   * @return  the calendar
   */
  public WorkingDayCalendar getCalendar(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    final WorkingDayCalendar calendar = _perCurrencyCalendars.get(currency);
    if (calendar == null) {
      throw new IllegalArgumentException("Could not get calendar for " + currency);
    }
    return calendar;
  }

  /**
   * Gets the working day calendars.
   * @return  the working day calendars
   */
  public Map<Currency, WorkingDayCalendar> getPerCurrencyCalendars() {
    return _perCurrencyCalendars;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _name.hashCode();
    result = prime * result + _perCurrencyCalendars.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FxWorkingDayCalendar)) {
      return false;
    }
    final FxWorkingDayCalendar other = (FxWorkingDayCalendar) obj;
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    if (!Objects.equals(_perCurrencyCalendars, other._perCurrencyCalendars)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FX: ");
    for (final Map.Entry<Currency, WorkingDayCalendar> calendar : _perCurrencyCalendars.entrySet()) {
      sb.append(calendar.getValue().toString());
      sb.append(" + ");
    }
    return sb.substring(0, sb.length() - 3);
  }
}
