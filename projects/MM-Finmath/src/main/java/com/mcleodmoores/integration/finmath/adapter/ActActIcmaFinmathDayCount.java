/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import org.apache.commons.lang.NotImplementedException;

/**
 * An adapter for {@link net.finmath.time.daycount.DayCountConvention_ACT_ACT_AFB} that allows use of the named instance
 * factory.
 */
public class ActActIcmaFinmathDayCount extends FinmathDayCount {

  /**
   * Placeholder constructor that throws {@link NotImplementedException}.
   */
  public ActActIcmaFinmathDayCount() {
    super(null, null);
    throw new NotImplementedException();
  }
}
