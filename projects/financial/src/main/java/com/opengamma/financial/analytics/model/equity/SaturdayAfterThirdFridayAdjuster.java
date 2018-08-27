/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 * {@code DatAdjuster} that finds the Saturday immediately following the 3rd Friday of the date's month.
 * This may be before or after the date itself.
 */
public class SaturdayAfterThirdFridayAdjuster implements TemporalAdjuster {

  private static final TemporalAdjuster THIRD_FRIDAY = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY);

  @Override
  public Temporal adjustInto(final Temporal temporal) {
    return temporal.with(THIRD_FRIDAY).plus(1, DAYS);
  }

}
