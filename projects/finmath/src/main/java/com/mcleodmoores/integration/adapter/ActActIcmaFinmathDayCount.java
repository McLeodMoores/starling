/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import org.apache.commons.lang.NotImplementedException;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_ICMA} that allows use of the named instance
 * factory.
 */
//TODO
public final class ActActIcmaFinmathDayCount extends FinmathDayCount {

  /**
   * Placeholder constructor that throws {@link NotImplementedException}.
   */
  public ActActIcmaFinmathDayCount() {
    super("", new NoneFinmathDayCount());
    throw new NotImplementedException();
  }
}
