/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConventionFactory;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_30E_360_ISDA} that allows use of the named instance
 * factory.
 */
public class ThirtyEThreeSixtyIsdaFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name 30/360 and implementation {@link net.finmath.time.daycount.DayCountConvention_30E_360_ISDA}.
   */
  public ThirtyEThreeSixtyIsdaFinmathDayCount() {
    super("30/360", DayCountConventionFactory.getDayCountConvention("30/360"));
  }
}
