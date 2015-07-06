/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_365L;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_365L} that allows use of the named instance
 * factory.
 */
public class ActThreeSixtyFiveLFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name Act/365L and implementation {@link net.finmath.time.daycount.DayCountConvention_ACT_365L}.
   */
  public ActThreeSixtyFiveLFinmathDayCount() {
    super("Act/365L", new DayCountConvention_ACT_365L());
  }
}
