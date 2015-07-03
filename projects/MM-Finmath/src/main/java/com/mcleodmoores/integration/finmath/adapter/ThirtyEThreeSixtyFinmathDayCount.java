/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.daycount.DayCountConventionFactory;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_30E_360} that allows use of the named instance
 * factory.
 */
public class ThirtyEThreeSixtyFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name 30E/360 and implementation {@link net.finmath.time.daycount.DayCountConvention_30E_360}.
   */
  public ThirtyEThreeSixtyFinmathDayCount() {
    super("30E/360", DayCountConventionFactory.getDayCountConvention("30E/360"));
  }
}
