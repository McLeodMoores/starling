/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.date;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Adjusts a date by adding the number of spot days and then adding a tenor to the date. If this date is a holiday,
 * the business day convention is used to adjust to the appropriate business day (e.g. the preceding business day
 * if the convention is {@link com.opengamma.financial.convention.businessday.PrecedingBusinessDayConvention}.
 */
public final class DefaultDateAdjustmentCalculator implements TenorOffsetDateAdjustmentCalculator {
  /** A static instance */
  private static final TenorOffsetDateAdjustmentCalculator INSTANCE = new DefaultDateAdjustmentCalculator();

  /**
   * Returns a static instance.
   * @return  the instance.
   */
  public static TenorOffsetDateAdjustmentCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private DefaultDateAdjustmentCalculator() {
  }

  @Override
  public LocalDate getSettlementDate(final LocalDate date, final Tenor tenor, final BusinessDayConvention convention,
      final WorkingDayCalendar workingDayCalendar) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(workingDayCalendar, "workingDayCalendar");
    final Calendar calendar = new CalendarAdapter(workingDayCalendar);
    final LocalDate endDate = TenorUtils.adjustDateByTenor(date, tenor, calendar, 0);
    if (tenor.isBusinessDayTenor()) {
      // ON, T/N, S/N
      return endDate;
    }
    return convention.adjustDate(calendar, endDate);
  }


}
