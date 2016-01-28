/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_30E_360_ISDA;

/**
 * An adapter for {@link DayCountConvention_30E_360_ISDA} with no special treatment for terminations that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "30E/360 ISDA", aliases = "30/360")
public final class ThirtyEThreeSixtyIsdaFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "30E/360 ISDA" and implementation {@link DayCountConvention_30E_360_ISDA}.
   */
  public ThirtyEThreeSixtyIsdaFinmathDayCount() {
    super("30E/360 ISDA", new DayCountConvention_30E_360_ISDA());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_30E_360_ISDA}.
   * @param name  the name of the convention
   */
  public ThirtyEThreeSixtyIsdaFinmathDayCount(final String name) {
    super(name, new DayCountConvention_30E_360_ISDA());
  }
}
