/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendarAdapter;
import com.opengamma.financial.analytics.model.irfutureoption.FutureOptionUtils;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Expiry calculator for IR futures.
 * <p>
 * Provides Expiries of IR Future Options from ordinals (i.e. nth future after valuationDate). This Calculator looks for Serial (Monthly) expiries for the first
 * 6, and then quarterly from then on, thus n=7 will be the first quarterly expiry after the 6th monthly one.
 */
@ExpiryCalculator
public final class IRFutureAndFutureOptionExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  private static final String NAME = "IRFutureAndFutureOptionExpiryCalculator";
  /** Singleton. */
  private static final IRFutureAndFutureOptionExpiryCalculator INSTANCE = new IRFutureAndFutureOptionExpiryCalculator();
  /** Calendar for weekdays. */
  private static final WorkingDayCalendar WEEKDAYS = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;

  /**
   * Gets the singleton instance.
   *
   * @return the instance, not null
   */
  public static IRFutureAndFutureOptionExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private IRFutureAndFutureOptionExpiryCalculator() {
  }

  // -------------------------------------------------------------------------
  @Deprecated
  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Calendar holidayCalendar) {
    return getExpiryDate(n, today, WorkingDayCalendarAdapter.of(holidayCalendar));
  }

  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final WorkingDayCalendar holidayCalendar) {
    return FutureOptionUtils.getIRFutureOptionWithSerialOptionsExpiry(n, today, holidayCalendar);
  }

  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    return FutureOptionUtils.getIRFutureOptionWithSerialOptionsExpiry(n, today, WEEKDAYS);
  }

  @Override
  public String getName() {
    return NAME;
  }

}
