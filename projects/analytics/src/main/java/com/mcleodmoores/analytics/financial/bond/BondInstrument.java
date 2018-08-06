/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import com.mcleodmoores.analytics.financial.instruments.Instrument;

/**
 *
 */
public interface BondInstrument extends Instrument {

  <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(DataYieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, DATA_TYPE data);

  <RESULT_TYPE> RESULT_TYPE accept(YieldConventionTypeVisitor<RESULT_TYPE> visitor);
}
