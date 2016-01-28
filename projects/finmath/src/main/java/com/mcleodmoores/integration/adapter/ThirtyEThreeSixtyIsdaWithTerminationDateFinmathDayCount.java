/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_30E_360_ISDA;

/**
 * An adapter for {@link DayCountConvention_30E_360_ISDA} with special treatment for termination dates that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "30E/360 ISDA Termination", aliases = "30/360 Termination")
public final class ThirtyEThreeSixtyIsdaWithTerminationDateFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "30E/360 ISDA Termination" and implementation {@link DayCountConvention_30E_360_ISDA}.
   */
  public ThirtyEThreeSixtyIsdaWithTerminationDateFinmathDayCount() {
    super("30E/360 ISDA Termination", new DayCountConvention_30E_360_ISDA(true));
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_30E_360_ISDA}.
   * @param name  the name of the convention
   */
  public ThirtyEThreeSixtyIsdaWithTerminationDateFinmathDayCount(final String name) {
    super(name, new DayCountConvention_30E_360_ISDA(true));
  }
}
