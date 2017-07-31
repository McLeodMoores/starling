/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

/**
 *
 */
public class IndexConverter {

  public static IborIndex toIborIndex(final IborTypeIndex index) {
    // TODO handle getPeriod with an explicit exception
    return new IborIndex(index.getCurrency(), index.getTenor().getPeriod(), index.getSpotLag(), index.getDayCount(), index.getBusinessDayConvention(), index.isEndOfMonth(), index.getName());
  }
}
