/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_NL_365;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_NL_365} that allows use of the named instance
 * factory.
 */
public class NlThreeSixtyFiveFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name NL/365 and implementation {@link net.finmath.time.daycount.DayCountConvention_NL_365}.
   */
  public NlThreeSixtyFiveFinmathDayCount() {
    super("NL/365", new DayCountConvention_NL_365());
  }
}
