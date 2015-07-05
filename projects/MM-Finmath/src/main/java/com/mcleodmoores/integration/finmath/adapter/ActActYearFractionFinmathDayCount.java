/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.daycount.DayCountConventionFactory;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_AFB} that allows use of the named instance
 * factory.
 */
public class ActActYearFractionFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name Act/Act YearFrac and implementation
   * {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_YEARFRAC}.
   */
  public ActActYearFractionFinmathDayCount() {
    super("Act/Act YearFrac", DayCountConventionFactory.getDayCountConvention("Act/Act YearFrac"));
  }
}
