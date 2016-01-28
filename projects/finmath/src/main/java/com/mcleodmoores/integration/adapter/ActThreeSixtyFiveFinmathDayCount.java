/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_365;

/**
 * An adapter for {@link DayCountConvention_ACT_365} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "Act/365", aliases = { "Actual/365" })
public final class ActThreeSixtyFiveFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "Act/365" and implementation {@link DayCountConvention_ACT_365}.
   */
  public ActThreeSixtyFiveFinmathDayCount() {
    super("Act/365", new DayCountConvention_ACT_365());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_ACT_365}.
   * @param name  the name of the convention, not null
   */
  public ActThreeSixtyFiveFinmathDayCount(final String name) {
    super(name, new DayCountConvention_ACT_365());
  }
}
