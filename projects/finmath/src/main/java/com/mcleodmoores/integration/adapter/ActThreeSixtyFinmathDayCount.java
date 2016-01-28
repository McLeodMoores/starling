/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_360;

/**
 * An adapter for {@link DayCountConvention_ACT_360} that allows use of the named instance
 * factory.
 */
@FinmathDayCountType(name = "Act/360", aliases = "Actual/360")
public final class ActThreeSixtyFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name "Act/360" and implementation {@link DayCountConvention_ACT_360}.
   */
  public ActThreeSixtyFinmathDayCount() {
    super("Act/360", new DayCountConvention_ACT_360());
  }

  /**
   * Creates an instance that wraps {@link DayCountConvention_ACT_360}.
   * @param name  the name of the convention, not null
   */
  public ActThreeSixtyFinmathDayCount(final String name) {
    super(name, new DayCountConvention_ACT_360());
  }
}
