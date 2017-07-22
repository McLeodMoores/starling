/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.date;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Settlement days for FX (apart from some Latin American currencies, see {@link LatAmFxSettlementDayCalculator} are calculated
 * using the following rules:
 * <ul>
 *  <li> Move to the next non-weekend day.
 *  <li> Move forward <code>n</code> business days, skipping any intermediate holidays and weekends in the non-US currency / currencies
 *  but ignoring US holidays.
 *  <li> Move to the next business day for both currencies and the US if the currency pair is a cross.
 * </ul>
 */
public final class FxSettlementDayCalculator implements SettlementDateCalculator<FxWorkingDayCalendar> {
  /** An instance */
  private static final SettlementDateCalculator<FxWorkingDayCalendar> INSTANCE = new FxSettlementDayCalculator();

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
  private FxSettlementDayCalculator() {
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
    // add settlement days ignoring any US intermediate holidays but counting weekends and other currency holidays
    final Map<Currency, WorkingDayCalendar> calendarsWithoutUs = new HashMap<>(calendar.getPerCurrencyCalendars());
    calendarsWithoutUs.remove(Currency.USD);
    count = daysToSettle - count;
    while (count > 0) {
      settlementDate = settlementDate.plusDays(1);
      boolean isWorkingDay = true;
      for (final Map.Entry<Currency, WorkingDayCalendar> entry : calendarsWithoutUs.entrySet()) {
        if (entry.getValue().isHoliday(settlementDate) || calendar.isWeekend(settlementDate)) {
          isWorkingDay = false;
        }
      }
      if (isWorkingDay) {
        count--;
      }
    }
    // move to next working day for all currencies including US
    while (!calendar.isWorkingDay(settlementDate)) {
      settlementDate = settlementDate.plusDays(1);
    }
    return settlementDate;
  }

}
