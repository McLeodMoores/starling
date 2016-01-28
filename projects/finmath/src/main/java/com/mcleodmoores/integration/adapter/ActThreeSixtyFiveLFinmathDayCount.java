/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_365L;

/**
 * An adapter for {@link DayCountConvention_ACT_365L} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "Act/365L", aliases = { "Actual/365L" })
public final class ActThreeSixtyFiveLFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "Act/365L" and implementation {@link DayCountConvention_ACT_365L}.
   */
  public ActThreeSixtyFiveLFinmathDayCount() {
    super("Act/365L", new DayCountConvention_ACT_365L());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_ACT_365L}.
   * @param name  the name of the convention, not null
   */
  public ActThreeSixtyFiveLFinmathDayCount(final String name) {
    super(name, new DayCountConvention_ACT_365L());
  }
}
