/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.daycount.DayCountConventionFactory;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_30U_360} that allows use of the named instance
 * factory.
 */
//TODO need to come up with a name for for when EOM convention is used
public class ThirtyUThreeSixtyFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name 30U/360 and implementation {@link net.finmath.time.daycount.DayCountConvention_30U_360}.
   */
  public ThirtyUThreeSixtyFinmathDayCount() {
    super("30U/360", DayCountConventionFactory.getDayCountConvention("30U/360"));
  }
}
