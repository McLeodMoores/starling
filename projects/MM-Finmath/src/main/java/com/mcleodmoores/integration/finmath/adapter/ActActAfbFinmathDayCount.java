/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.daycount.DayCountConvention_ACT_ACT_AFB;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_AFB} that allows use of the named instance
 * factory.
 */
public class ActActAfbFinmathDayCount extends FinmathDayCount {

  /**
   * Creates an instance of this adapter with name Act/Act AFB and implementation {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_AFB}.
   */
  public ActActAfbFinmathDayCount() {
    super("Act/Act AFB", new DayCountConvention_ACT_ACT_AFB());
  }
}
