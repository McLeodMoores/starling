/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.bond;

import com.opengamma.util.NamedInstance;

/**
 * An interface for instances of bond yield conventions. Classes that implement this interface can be obtained
 * from a {@link BondYieldConventionFactory}.
 */
public interface BondYieldConvention extends NamedInstance {

  <RESULT_TYPE> RESULT_TYPE accept(BondYieldConventionVisitor<?, RESULT_TYPE> visitor);

  <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(BondYieldConventionVisitor<DATA_TYPE, RESULT_TYPE> visitor, DATA_TYPE data);
}
