/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.mcleodmoores.date.WorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Expiry calculator for Fed fund futures.
 */
@ExpiryCalculator
public final class FedFundFutureAndFutureOptionMonthlyExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  public static final String NAME = "FedFundFutureAndFutureOptionMonthlyExpiryCalculator";
  /** Singleton. */
  private static final FedFundFutureAndFutureOptionMonthlyExpiryCalculator INSTANCE = new FedFundFutureAndFutureOptionMonthlyExpiryCalculator();
  /** Adjuster. */
  private static final TemporalAdjuster EOM_ADJUSTER = TemporalAdjusters.lastDayOfMonth();
  /** Working days to settle. */
  private static final int WORKING_DAYS_TO_SETTLE = 2;

  /**
   * Gets the singleton instance.
   *
   * @return the instance, not null
   */
  public static FedFundFutureAndFutureOptionMonthlyExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private FedFundFutureAndFutureOptionMonthlyExpiryCalculator() {
  }

  // -------------------------------------------------------------------------
  @Deprecated
  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Calendar holidayCalendar) {
    return getExpiryDate(n, today, WorkingDayCalendarAdapter.of(holidayCalendar));
  }

  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final WorkingDayCalendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.notNull(holidayCalendar, "holiday calendar");
    final LocalDate result = today.plusMonths(n - 1).with(EOM_ADJUSTER);
    return adjustForSettlement(result, holidayCalendar);
  }

  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    return today.plusMonths(n - 1);
  }

  private static LocalDate adjustForSettlement(final LocalDate date, final WorkingDayCalendar holidayCalendar) {
    int days = 0;
    LocalDate result = date;
    while (days < WORKING_DAYS_TO_SETTLE) {
      result = result.minusDays(1);
      if (holidayCalendar.isWorkingDay(result)) {
        days++;
      }
    }
    return result;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
