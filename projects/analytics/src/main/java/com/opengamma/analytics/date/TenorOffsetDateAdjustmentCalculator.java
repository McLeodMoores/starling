/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.util.time.Tenor;

/**
 * Interface for classes that adjust a date by a tenor according to a business day convention and holiday calendar.
 */
//TODO this should be combined with the settlement date adjustment calculator
public interface TenorOffsetDateAdjustmentCalculator {

  LocalDate getSettlementDate(LocalDate date, Tenor tenor, BusinessDayConvention convention, WorkingDayCalendar calendar);
}
