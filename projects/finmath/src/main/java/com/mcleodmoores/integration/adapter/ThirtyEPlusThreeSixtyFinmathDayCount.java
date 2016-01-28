/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_30E_360;

/**
 * An adapter for {@link DayCountConvention_30E_360} using E+ that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "30E+/360", aliases = { "Eurobond basis", "30/360 ISMA" })
public class ThirtyEPlusThreeSixtyFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name 30E+/360 and implementation {@link DayCountConvention_30E_360}.
   */
  public ThirtyEPlusThreeSixtyFinmathDayCount() {
    super("30E+/360", new DayCountConvention_30E_360(true));
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_30E_360}.
   * @param name  the name of the convention, not null
   */
  public ThirtyEPlusThreeSixtyFinmathDayCount(final String name) {
    super(name, new DayCountConvention_30E_360(true));
  }
}
