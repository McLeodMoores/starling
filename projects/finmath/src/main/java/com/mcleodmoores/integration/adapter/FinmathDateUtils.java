/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import org.threeten.bp.LocalDate;

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
   * Converts a threeten LocalDate to the joda-time equivalent.
   * @param localDate  the date, not null
   * @return  the converted date
   */
  public static org.joda.time.LocalDate convertToJodaLocalDate(final LocalDate localDate) {
    ArgumentChecker.notNull(localDate, "localDate");
    return new org.joda.time.LocalDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
  }

  /**
   * Converts a joda-time LocalDate to the threeten equivalent.
   * @param localDate  the date, not null
   * @return  the converted date
   */
  public static LocalDate convertFromJodaLocalDateDate(final org.joda.time.LocalDate localDate) {
    ArgumentChecker.notNull(localDate, "localDate");
    return LocalDate.of(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth());
  }
}
