/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import java.util.List;
import java.util.Set;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public interface CurveProvider extends DataProvider {

  @Override
  CurveProvider copy();

  //TODO should this be in here? probably a separate calculator
  double[] parameterSensitivity(UniqueIdentifiable id, List<DoublesPair> pointSensitivity);

  Set<UniqueIdentifiable> getIdentifiers();

  Set<UniqueIdentifiable> getIdentifiersForScheme(String scheme);
}
