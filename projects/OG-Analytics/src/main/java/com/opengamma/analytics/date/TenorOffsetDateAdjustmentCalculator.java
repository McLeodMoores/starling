/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public interface TenorOffsetDateAdjustmentCalculator {

  LocalDate getSettlementDate(LocalDate date, Tenor tenor, BusinessDayConvention convention, WorkingDayCalendar calendar);
}
