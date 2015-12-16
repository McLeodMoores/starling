/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.financial.analytics.model.irfutureoption.FutureOptionUtils;
import com.opengamma.util.time.Tenor;

/**
 * Utility methods for futures.
 */
public final class QuandlFutureUtils {
  /** Codes for each month */
  private static final BiMap<Month, Character> MONTH_CODES;

  static {
    MONTH_CODES = HashBiMap.create();
    MONTH_CODES.put(Month.JANUARY, 'F');
    MONTH_CODES.put(Month.FEBRUARY, 'G');
    MONTH_CODES.put(Month.MARCH, 'H');
    MONTH_CODES.put(Month.APRIL, 'J');
    MONTH_CODES.put(Month.MAY, 'K');
    MONTH_CODES.put(Month.JUNE, 'M');
    MONTH_CODES.put(Month.JULY, 'N');
    MONTH_CODES.put(Month.AUGUST, 'Q');
    MONTH_CODES.put(Month.SEPTEMBER, 'U');
    MONTH_CODES.put(Month.OCTOBER, 'V');
    MONTH_CODES.put(Month.NOVEMBER, 'X');
    MONTH_CODES.put(Month.DECEMBER, 'Z');
  }

  /**
   * Restricted constructor.
   */
  private QuandlFutureUtils() {
  }

  /**
   * Gets the expiry code for the n<sup>th</sup> future from a given date e.g. the 4th quarterly future code.
   * This method will throw an exception if the tenor is not one or three months.
   * @param futureTenor The future tenor, not null
   * @param nthFuture The future number, greater than zero
   * @param date The date, not null
   * @return The expiry code
   */
  public static String getCodeForFuture(final Tenor futureTenor, final int nthFuture, final LocalDate date) {
    ArgumentChecker.notNull(futureTenor, "futureTenor");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    final LocalDate expiry;
    if (futureTenor.equals(Tenor.THREE_MONTHS)) {
      expiry = FutureOptionUtils.getApproximateIRFutureQuarterlyExpiry(nthFuture, date);
    } else if (futureTenor.equals(Tenor.ONE_MONTH)) {
      expiry = FutureOptionUtils.getApproximateIRFutureMonth(nthFuture, date);
    } else {
      throw new Quandl4OpenGammaRuntimeException("Cannot handle futures with tenor " + futureTenor);
    }
    final StringBuilder sb = new StringBuilder();
    sb.append(MONTH_CODES.get(expiry.getMonth()));
    sb.append(expiry.getYear());
    return sb.toString();
  }

  /**
   * Gets the expiry year for the n<sup>th</sup> future from a given date e.g. the year when the 4th quarterly
   * future expires. This method will throw an exception if the tenor is not one or three months.
   * @param futureTenor The future tenor, not null
   * @param nthFuture The future number, greater than zero
   * @param date The date, not null
   * @return The expiry year
   */
  public static int getExpiryYear(final Tenor futureTenor, final int nthFuture, final LocalDate date) {
    ArgumentChecker.notNull(futureTenor, "futureTenor");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    final LocalDate expiry;
    if (futureTenor.equals(Tenor.THREE_MONTHS)) {
      expiry = FutureOptionUtils.getApproximateIRFutureQuarterlyExpiry(nthFuture, date);
    } else if (futureTenor.equals(Tenor.ONE_MONTH)) {
      expiry = FutureOptionUtils.getApproximateIRFutureMonth(nthFuture, date);
    } else {
      throw new Quandl4OpenGammaRuntimeException("Cannot handle futures with tenor " + futureTenor);
    }
    return expiry.getYear();
  }

  /**
   * Gets the month code for the n<sup>th</sup> future from a given date e.g. the code for the 4th quarterly
   * future on 2015-01-01 will be "Z". This method will throw an exception if the tenor is not one or three months.
   * @param futureTenor The future tenor, not null
   * @param nthFuture The future number, greater than zero
   * @param date The date, not null
   * @return The month code
   */
  public static Character getMonthCode(final Tenor futureTenor, final int nthFuture, final LocalDate date) {
    ArgumentChecker.notNull(futureTenor, "futureTenor");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    final LocalDate expiry;
    if (futureTenor.equals(Tenor.THREE_MONTHS)) {
      expiry = FutureOptionUtils.getApproximateIRFutureQuarterlyExpiry(nthFuture, date);
    } else if (futureTenor.equals(Tenor.ONE_MONTH)) {
      expiry = FutureOptionUtils.getApproximateIRFutureMonth(nthFuture, date);
    } else {
      throw new Quandl4OpenGammaRuntimeException("Cannot handle futures with tenor " + futureTenor);
    }
    return MONTH_CODES.get(expiry.getMonth());
  }
}
