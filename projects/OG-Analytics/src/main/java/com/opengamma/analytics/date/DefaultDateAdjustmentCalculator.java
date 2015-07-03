/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public final class DefaultDateAdjustmentCalculator implements TenorOffsetDateAdjustmentCalculator {
  private static final TenorOffsetDateAdjustmentCalculator INSTANCE = new DefaultDateAdjustmentCalculator();

  public static TenorOffsetDateAdjustmentCalculator getInstance() {
    return INSTANCE;
  }

  private DefaultDateAdjustmentCalculator() {
  }

  @Override
  public LocalDate getSettlementDate(final LocalDate date, final Tenor tenor, final BusinessDayConvention convention,
      final WorkingDayCalendar workingDayCalendar) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(workingDayCalendar, "workingDayCalendar");
    final Calendar calendar = new WorkingDayCalendarAdapter(workingDayCalendar);
    final LocalDate endDate = TenorUtils.adjustDateByTenor(date, tenor, calendar, 0);
    if (tenor.isBusinessDayTenor()) {
      // ON, T/N, S/N
      return endDate;
    }
    return convention.adjustDate(calendar, endDate);
  }


}
