/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_NONE;

/**
 * An adapter for {@link DayCountConvention_NONE} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "None")
public final class NoneFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "None" and implementation {@link DayCountConvention_NONE}.
   */
  public NoneFinmathDayCount() {
    super("None", new DayCountConvention_NONE());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_NONE}.
   * @param name  the name of the convention, not null
   */
  public NoneFinmathDayCount(final String name) {
    super(name, new DayCountConvention_NONE());
  }
}
