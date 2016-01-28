/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_30E_360;

/**
 * An adapter for {@link DayCountConvention_30E_360} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "30E/360")
public final class ThirtyEThreeSixtyFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "30E/360" and implementation {@link DayCountConvention_30E_360}.
   */
  public ThirtyEThreeSixtyFinmathDayCount() {
    super("30E/360", new DayCountConvention_30E_360());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_30E_360}.
   * @param name  the name of the convention, not null
   */
  public ThirtyEThreeSixtyFinmathDayCount(final String name) {
    super(name, new DayCountConvention_30E_360());
  }
}
