/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_NL_365;

/**
 * An adapter for {@link DayCountConvention_NL_365} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "NL/365")
public final class NlThreeSixtyFiveFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "NL/365" and implementation {@link DayCountConvention_NL_365}.
   */
  public NlThreeSixtyFiveFinmathDayCount() {
    super("NL/365", new DayCountConvention_NL_365());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_NL_365}.
   * @param name  the name of the convention, not null
   */
  public NlThreeSixtyFiveFinmathDayCount(final String name) {
    super(name, new DayCountConvention_NL_365());
  }
}
