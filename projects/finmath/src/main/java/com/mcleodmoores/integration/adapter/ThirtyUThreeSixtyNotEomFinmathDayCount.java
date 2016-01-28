/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_30U_360;

/**
 * An adapter for {@link DayCountConvention_30U_360} with the end-of-month convention that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "30U/360 not EOM", aliases = "Bond basis not EOM")
public final class ThirtyUThreeSixtyNotEomFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "30U/360 not EOM" and implementation {@link DayCountConvention_30U_360}.
   */
  public ThirtyUThreeSixtyNotEomFinmathDayCount() {
    super("30U/360 not EOM", new DayCountConvention_30U_360(false));
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_30U_360}.
   * @param name  the name of the convention, not null
   */
  public ThirtyUThreeSixtyNotEomFinmathDayCount(final String name) {
    super(name, new DayCountConvention_30U_360(false));
  }
}
