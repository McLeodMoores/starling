/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_365A;

/**
 * An adapter for {@link DayCountConvention_ACT_365A} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "Act/365A", aliases = "Actual/365A")
public final class ActThreeSixtyFiveAFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "Act/365A" and implementation {@link DayCountConvention_ACT_365A}.
   */
  public ActThreeSixtyFiveAFinmathDayCount() {
    super("Act/365A", new DayCountConvention_ACT_365A());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_ACT_365A}.
   * @param name  the convention name, not null
   */
  public ActThreeSixtyFiveAFinmathDayCount(final String name) {
    super(name, new DayCountConvention_ACT_365A());
  }
}
