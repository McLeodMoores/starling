/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_NONE;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_NONE} that allows use of the named instance
 * factory.
 */
public class NoneFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name None and implementation {@link net.finmath.time.daycount.DayCountConvention_NONE}.
   */
  public NoneFinmathDayCount() {
    super("None", new DayCountConvention_NONE());
  }
}
