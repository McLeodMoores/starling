/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import java.util.Set;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public interface CurveProvider extends DataProvider {

  @Override
  CurveProvider copy();

  Set<UniqueIdentifiable> getIdentifiers();

  Set<UniqueIdentifiable> getIdentifiersForScheme(String scheme);

  YieldAndDiscountCurve getCurve(UniqueIdentifiable id);
}
