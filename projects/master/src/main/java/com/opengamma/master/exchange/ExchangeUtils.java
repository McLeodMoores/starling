/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Utilities for working with Exchanges.
 */
public class ExchangeUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeUtils.class);

  /**
   * THIS IS NOT READY FOR PRIME TIME YET.
   *
   * @param exchangeSource
   *          a source of exchanges, we assume it provides ManageableExchanges, not null
   * @param isoMic
   *          an external id with the ISO MIC code of the exchange
   * @param today
   *          the date today (to allow for changes in opening hours over time)
   * @param defaultTime
   *          a fallback time to use if a close time could not be established, if set to null, will return null in time field.
   * @return a pair of values, the end of trading period and the time zone or null if no exchange with that code was found. Time can be null if
   *         defaultTime==null.
   */
  public static Pair<LocalTime, ZoneId> getTradingCloseTime(final ExchangeSource exchangeSource, final ExternalId isoMic, final LocalDate today,
      final LocalTime defaultTime) {
    ArgumentChecker.notNull(exchangeSource, "exchangeSource");
    final ManageableExchange exchange = (ManageableExchange) exchangeSource.getSingle(isoMic);
    if (exchange != null) {
      for (final ManageableExchangeDetail detail : exchange.getDetail()) {
        if (detail.getPhaseName().equals("Trading")
            && (detail.getCalendarStart() == null || detail.getCalendarStart().equals(today) || detail.getCalendarStart().isBefore(today))
            && (detail.getCalendarEnd() == null || detail.getCalendarEnd().equals(today) || detail.getCalendarEnd().isAfter(today))) {
          final LocalTime endTime = detail.getPhaseEnd();
          if (endTime != null) {
            return Pairs.of(endTime, exchange.getTimeZone());
          }
        }
      }
      LOGGER.warn("Couldn't find exchange close time for {}, defaulting to supplied default", isoMic);
      return Pairs.of(defaultTime, exchange.getTimeZone());
    }
    return null;
  }

  /**
   * Returns true if a date is on or before today or if the date is null (i.e. not set).
   *
   * @param date
   *          the date to compare
   * @param today
   *          today's date
   * @return true if the date is on or before today
   */
  public static boolean isDateOnOrBeforeToday(final LocalDate date, final LocalDate today) {
    return date == null || date.equals(today) || date.isBefore(today);
  }
}
