/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_ACT_YEARFRAC;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_YEARFRAC} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "Act/Act YearFrac", aliases = { "Actual/Actual YearFrac", "Actual/Actual Year Fraction" })
public final class ActActYearFractionFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "Act/Act YearFrac" and implementation
   * {@link DayCountConvention_ACT_ACT_YEARFRAC}.
   */
  public ActActYearFractionFinmathDayCount() {
    super("Act/Act YearFrac", new DayCountConvention_ACT_ACT_YEARFRAC());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_ACT_ACT_YEARFRAC}.
   * @param name  the name of the convention, not null
   */
  public ActActYearFractionFinmathDayCount(final String name) {
    super(name, new DayCountConvention_ACT_ACT_YEARFRAC());
  }
}
