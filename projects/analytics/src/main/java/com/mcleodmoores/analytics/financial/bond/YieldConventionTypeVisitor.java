/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;

/**
 *
 */
public interface YieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> {

  RESULT_TYPE visitUsStreet(BondFixedSecurity security, DATA_TYPE data);

  RESULT_TYPE visitUkDmo(BondFixedSecurity security, DATA_TYPE data);

  RESULT_TYPE visitFranceCompound(BondFixedSecurity security, DATA_TYPE data);

  RESULT_TYPE visitItalyTreasury(BondFixedSecurity security, DATA_TYPE data);

}
