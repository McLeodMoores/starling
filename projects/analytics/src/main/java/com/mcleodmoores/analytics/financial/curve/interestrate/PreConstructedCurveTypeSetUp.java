/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public interface PreConstructedCurveTypeSetUp {

  PreConstructedCurveTypeSetUp forDiscounting(UniqueIdentifiable id);

  PreConstructedCurveTypeSetUp forIndex(IborTypeIndex... indices);

  PreConstructedCurveTypeSetUp forIndex(OvernightIndex... indices);

}
