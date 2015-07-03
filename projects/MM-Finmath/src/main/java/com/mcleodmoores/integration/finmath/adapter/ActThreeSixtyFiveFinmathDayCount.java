/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.daycount.DayCountConventionFactory;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_365} that allows use of the named instance
 * factory.
 */
public class ActThreeSixtyFiveFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name Act/365 and implementation {@link net.finmath.time.daycount.DayCountConvention_ACT_365}.
   */
  public ActThreeSixtyFiveFinmathDayCount() {
    super("Act/365", DayCountConventionFactory.getDayCountConvention("Act/365"));
  }
}
