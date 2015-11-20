/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * Settlement dates for most fixed income trades are calculated using the following rules:
 * <ul>
 *  <li> Move to the next business day.
 *  <li> Move forward or backwards <code>n</code> business days
 * </ul>
 */
public final class DefaultSettlementDateCalculator implements SettlementDateCalculator<WorkingDayCalendar> {
  /** An instance of this calculator */
  private static final SettlementDateCalculator<WorkingDayCalendar> INSTANCE = new DefaultSettlementDateCalculator();

  /**
   * Gets an instance.
   * @return  the instance
   */
  public static SettlementDateCalculator<WorkingDayCalendar> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private DefaultSettlementDateCalculator() {
  }

  @Override
  public LocalDate getSettlementDate(final LocalDate date, final int daysToSettle, final WorkingDayCalendar calendar) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(calendar, "calendar");
    LocalDate settlementDate = date;
    while (!calendar.isWorkingDay(settlementDate)) {
      settlementDate = settlementDate.plusDays(1);
    }
    int count = daysToSettle;
    if (daysToSettle > 0) {
      while (count > 0) {
        settlementDate = settlementDate.plusDays(1);
        if (calendar.isWorkingDay(settlementDate)) {
          count--;
        }
      }
    } else {
      while (count > 0) {
        settlementDate = settlementDate.minusDays(1);
        if (calendar.isWorkingDay(settlementDate)) {
          count--;
        }
      }
    }
    return settlementDate;
  }

}
