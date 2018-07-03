/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;

/**
 *
 */
public interface YieldConventionType {

  <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(YieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, BondFixedSecurity bond, DATA_TYPE data);
}
