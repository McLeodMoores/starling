/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.daycount.DayCountConvention_30E_360;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_30E_360} that allows use of the named instance
 * factory.
 */
public class ThirtyEPlusThreeSixtyFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name 30E+/360 and implementation {@link net.finmath.time.daycount.DayCountConvention_30E_360}.
   */
  public ThirtyEPlusThreeSixtyFinmathDayCount() {
    super("30E+/360", new DayCountConvention_30E_360(true));
  }
}
