/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.instruments;

/**
 *
 */
public interface Instrument {

  <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(DataBondSecurityVisitor<DATA_TYPE, RESULT_TYPE> visitor, DATA_TYPE data);

  <RESULT_TYPE> RESULT_TYPE accept(BondSecurityVisitor<RESULT_TYPE> visitor);
}
