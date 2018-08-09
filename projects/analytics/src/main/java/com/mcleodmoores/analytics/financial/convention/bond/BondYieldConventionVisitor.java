/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;

/**
 *
 */
public abstract class BondYieldConventionVisitor<DATA_TYPE, RESULT_TYPE> extends InstrumentDerivativeVisitorAdapter<DATA_TYPE, RESULT_TYPE> {

  @Override
  public RESULT_TYPE visitBondFixedSecurity(final BondFixedSecurity bond) {
    return null; // YieldConventionAdapter.of(bond.getYieldConvention());
  }

  @Override
  public abstract RESULT_TYPE visitBondFixedSecurity(BondFixedSecurity bond, DATA_TYPE data);
}
