/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_UNKNOWN;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_UNKNOWN} that allows use of the named instance
 * factory.
 */
public class UnknownFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name Unknown and implementation {@link net.finmath.time.daycount.DayCountConvention_UNKNOWN}.
   */
  public UnknownFinmathDayCount() {
    super("Unknown", new DayCountConvention_UNKNOWN());
  }
}
