/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import java.util.Calendar;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * Utilities for {@link FinmathDayCount} and related classes.
 */
public final class FinmathDateUtils {

  /**
   * Restricted constructor.
   */
  private FinmathDateUtils() {
  }

  /**
   * Converts a {@link LocalDate} to {@link Calendar}.
   * @param localDate The date, not null
   * @return A calendar
   */
  public static Calendar convertLocalDate(final LocalDate localDate) {
    ArgumentChecker.notNull(localDate, "localDate");
    final Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
    return calendar;
  }

  public static org.joda.time.LocalDate convertToJodaDate(final LocalDate localDate) {
    ArgumentChecker.notNull(localDate, "localDate");
    return new org.joda.time.LocalDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
  }

  /**
   * Converts a {@link Calendar} to {@link LocalDate}.
   * @param calendar The calendar, not null
   * @return A date
   */
  public static LocalDate convertToLocalDate(final Calendar calendar) {
    ArgumentChecker.notNull(calendar, "calendar");
    return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
  }

  /**
   * Converts a {@link ZonedDateTime} to {@link Calendar}.
   * @param zonedDateTime The date, not null
   * @return A calendar
   */
  public static Calendar convertZonedDateTime(final ZonedDateTime zonedDateTime) {
    ArgumentChecker.notNull(zonedDateTime, "zonedDateTime");
    final Calendar calendar = Calendar.getInstance(); //TODO ignoring zone
    calendar.clear();
    calendar.set(zonedDateTime.getYear(), zonedDateTime.getMonthValue() - 1, zonedDateTime.getDayOfMonth(), zonedDateTime.getHour(),
        zonedDateTime.getMinute(), zonedDateTime.getSecond());
    return calendar;
  }

  public static LocalDate convertFromJodaDate(final org.joda.time.LocalDate localDate) {
    ArgumentChecker.notNull(localDate, "localDate");
    return LocalDate.of(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth());
  }
}
