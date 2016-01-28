/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_UNKNOWN;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_UNKNOWN} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "Unknown")
public final class UnknownFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "Unknown" and implementation {@link DayCountConvention_UNKNOWN}.
   */
  public UnknownFinmathDayCount() {
    super("Unknown", new DayCountConvention_UNKNOWN());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_UNKNOWN}.
   * @param name  the name of the convention, not null
   */
  public UnknownFinmathDayCount(final String name) {
    super(name, new DayCountConvention_UNKNOWN());
  }
}
