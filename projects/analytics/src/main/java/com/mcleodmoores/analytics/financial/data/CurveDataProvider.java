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
public interface CurveDataProvider extends DataProvider {

  @Override
  CurveDataProvider copy();

  double[] parameterSensitivity(UniqueIdentifiable id, List<DoublesPair> pointSensitivity);

  Set<String> getAllCurveNames();

  String getCurveNameForId(UniqueIdentifiable id);

  Set<UniqueIdentifiable> getIdentifiers();

  Set<UniqueIdentifiable> getIdentifiersForScheme(String scheme);
}
