/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_ACT_AFB;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_AFB} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "Act/Act AFB", aliases = "Actual/Actual AFB")
public final class ActActAfbFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter called "Act/Act AFB" and
   * implementation {@link DayCountConvention_ACT_ACT_AFB}.
   */
  public ActActAfbFinmathDayCount() {
    super("Act/Act AFB", new DayCountConvention_ACT_ACT_AFB());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_ACT_ACT_AFB}.
   * @param name  the convention name, not null
   */
  public ActActAfbFinmathDayCount(final String name) {
    super(name, new DayCountConvention_ACT_ACT_AFB());
  }
}
