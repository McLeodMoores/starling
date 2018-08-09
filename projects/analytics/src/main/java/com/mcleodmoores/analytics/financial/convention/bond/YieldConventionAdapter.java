/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.bond;

import com.opengamma.financial.convention.yield.YieldConvention;

/**
 *
 */
public class YieldConventionAdapter implements BondYieldConvention {

  public static BondYieldConvention of(final YieldConvention original) {
    return new YieldConventionAdapter(original);
  }

  private final BondYieldConvention _underlying;

  private YieldConventionAdapter(final YieldConvention original) {
    _underlying = BondYieldConventionFactory.of(original.getName());
  }
  @Override
  public String getName() {
    return _underlying.getName();
  }

  @Override
  public <RESULT_TYPE> RESULT_TYPE accept(final BondYieldConventionVisitor<?, RESULT_TYPE> visitor) {
    return _underlying.accept(visitor);
  }

  @Override
  public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final BondYieldConventionVisitor<DATA_TYPE, RESULT_TYPE> visitor, final DATA_TYPE data) {
    return _underlying.accept(visitor, data);
  }
}
