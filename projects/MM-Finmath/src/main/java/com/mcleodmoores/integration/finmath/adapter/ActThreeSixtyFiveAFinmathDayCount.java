/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_365A;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_365A} that allows use of the named instance
 * factory.
 */
public class ActThreeSixtyFiveAFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name Act/365A and implementation {@link net.finmath.time.daycount.DayCountConvention_ACT_365A}.
   */
  public ActThreeSixtyFiveAFinmathDayCount() {
    super("Act/365A", new DayCountConvention_ACT_365A());
  }
}
