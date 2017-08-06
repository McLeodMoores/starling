/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for converting {@link Index} to {@link IndexDeposit}, which will be deprecated.
 */
public final class IndexConverter {

  /**
   * Converts an ibor type index.
   * @param index  the index, not null
   * @return  the converted index
   */
  public static IborIndex toIborIndex(final IborTypeIndex index) {
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isFalse(index.getTenor().isBusinessDayTenor(), "Unhandled ibor tenor type {}", index.getTenor());
    return new IborIndex(index.getCurrency(), index.getTenor().getPeriod(), index.getSpotLag(), index.getDayCount(),
        index.getBusinessDayConvention(), index.isEndOfMonth(), index.getName());
  }

  /**
   * Converts an overnight index.
   * @param index  the index, not null
   * @return  the converted index
   */
  public static IndexON toIndexOn(final OvernightIndex index) {
    ArgumentChecker.notNull(index, "index");
    return new IndexON(index.getName(), index.getCurrency(), index.getDayCount(), index.getPublicationLag());
  }

  private IndexConverter() {
  }
}
