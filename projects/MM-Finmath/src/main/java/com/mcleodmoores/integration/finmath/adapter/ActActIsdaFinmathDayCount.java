/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.daycount.DayCountConventionFactory;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_ISDA} that allows use of the named instance
 * factory.
 */
public class ActActIsdaFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of the adapter with name Act/Act ISDA and implementation {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_ISDA}.
   */
  public ActActIsdaFinmathDayCount() {
    super("Act/Act ISDA", DayCountConventionFactory.getDayCountConvention("Act/Act ISDA"));
  }
}
