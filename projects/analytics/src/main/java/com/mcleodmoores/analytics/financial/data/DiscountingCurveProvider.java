/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public interface DiscountingCurveProvider extends IdCurveProvider {

  @Override
  DiscountingCurveProvider copy();

  double getDiscountFactor(UniqueIdentifiable id, double time);

}
