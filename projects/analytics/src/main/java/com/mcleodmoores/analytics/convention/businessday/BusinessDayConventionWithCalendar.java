/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.convention.businessday;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class BusinessDayConventionWithCalendar implements TemporalAdjuster {
  private final BusinessDayConvention _businessDayConvention;
  private final WorkingDayCalendar _workingDayCalendar;

  protected BusinessDayConventionWithCalendar(final BusinessDayConvention businessDayConvention, final WorkingDayCalendar workingDayCalendar) {
    _businessDayConvention = ArgumentChecker.notNull(businessDayConvention, "businessDayConvention");
    _workingDayCalendar = ArgumentChecker.notNull(workingDayCalendar, "workingDayCalendar");
  }

  @Override
  public Temporal adjustInto(final Temporal temporal) {
    final TemporalAdjuster result = _businessDayConvention.adjustDate(_workingDayCalendar, LocalDate.from(temporal));
    return temporal.with(result);
  }
}
