/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_ACT_ISDA;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_ISDA} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "Act/Act ISDA", aliases = "Actual/Actual ISDA")
public final class ActActIsdaFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of the adapter with name "Act/Act ISDA" and implementation {@link DayCountConvention_ACT_ACT_ISDA}.
   */
  public ActActIsdaFinmathDayCount() {
    super("Act/Act ISDA", new DayCountConvention_ACT_ACT_ISDA());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_ACT_ACT_ISDA}.
   * @param name  the name of the convention, not null
   */
  public ActActIsdaFinmathDayCount(final String name) {
    super(name, new DayCountConvention_ACT_ACT_ISDA());
  }
}
