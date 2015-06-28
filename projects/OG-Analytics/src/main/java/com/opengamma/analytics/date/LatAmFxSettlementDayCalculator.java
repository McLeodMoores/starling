/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * Settlement days for FX trades where one of the currencies is a Latin American currency (excluding BRL) are calculated
 * using the following rules:
 * <ul>
 *  <li> Move to the next non-weekend day.
 *  <li> Move forward <code>n</code> business days, skipping any intermediate holidays and weekends in the currency / currencies
 *  including US holidays.
 *  <li> Move to the next business day for both currencies and the US if the currency pair is a cross.
 * </ul>
 */
public final class LatAmFxSettlementDayCalculator implements SettlementDateCalculator<FxWorkingDayCalendar> {
  /** An instance */
  private static final SettlementDateCalculator<FxWorkingDayCalendar> INSTANCE = new LatAmFxSettlementDayCalculator();

  /**
   * Gets an instance.
   * @return  the instance
   */
  public static SettlementDateCalculator<FxWorkingDayCalendar> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private LatAmFxSettlementDayCalculator() {
  }

  @Override
  public LocalDate getSettlementDate(final LocalDate date, final int daysToSettle, final FxWorkingDayCalendar calendar) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNegative(daysToSettle, "daysToSettle");
    ArgumentChecker.notNull(calendar, "calendar");
    int count = 0;
    // advance beyond weekend (weekend start dates are possible if trade is entered after cutoff time)
    LocalDate settlementDate = date;
    while (calendar.isWeekend(settlementDate)) {
      settlementDate = settlementDate.plusDays(1);
      count = 1;
    }
    // add settlement days counting weekends and all intermediate holidays including US holidays
    // US holidays are considered so the date calculated in this loop is a valid FX date
    count = daysToSettle - count;
    while (count > 0) {
      settlementDate = settlementDate.plusDays(1);
      if (calendar.isWorkingDay(settlementDate)) {
        count--;
      }
    }
    return settlementDate;
  }

}
