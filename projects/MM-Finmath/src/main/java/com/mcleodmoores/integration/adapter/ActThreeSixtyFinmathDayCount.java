/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConventionFactory;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_360} that allows use of the named instance
 * factory.
 */
public class ActThreeSixtyFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name Act/360 and implementation {@link net.finmath.time.daycount.DayCountConvention_ACT_360}.
   */
  public ActThreeSixtyFinmathDayCount() {
    super("Act/360", DayCountConventionFactory.getDayCountConvention("Act/360"));
  }
}
