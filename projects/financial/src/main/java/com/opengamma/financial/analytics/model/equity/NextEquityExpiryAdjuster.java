/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.EnumSet;
import java.util.Set;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.financial.analytics.ircurve.DayOfWeekInMonthPlusOffsetAdjuster;
import com.opengamma.financial.analytics.ircurve.NextQuarterAdjuster;

/**
 * {@code DatAdjuster} that finds the next Expiry in Equity Futures Options.
 * This is the Saturday immediately following the 3rd Friday of the next IMM Future Expiry Month.
 */
public class NextEquityExpiryAdjuster implements TemporalAdjuster {

  /** An adjuster finding the Saturday following the 3rd Friday in a month. May be before or after date */
  private static final TemporalAdjuster DAY_OF_MONTH = new DayOfWeekInMonthPlusOffsetAdjuster(3, DayOfWeek.FRIDAY, 1);

  /** An adjuster moving to the next quarter */
  private static final TemporalAdjuster NEXT_QUARTER_ADJUSTER = new NextQuarterAdjuster();

  /** The IMM Expiry months  */
  private final Set<Month> _immFutureQuarters = EnumSet.of(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER);

  @Override
  public Temporal adjustInto(final Temporal temporal) {
    LocalDate date = LocalDate.from(temporal);
    if (_immFutureQuarters.contains(date.getMonth()) &&
        date.with(DAY_OF_MONTH).isAfter(date)) { // in a quarter
      date = date.with(DAY_OF_MONTH);
    } else {
      date = date.with(NEXT_QUARTER_ADJUSTER).with(DAY_OF_MONTH);
    }
    return temporal.with(date);
  }

}
