/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public interface MutableCurveProvider extends CurveProvider {

  @Override
  MutableCurveProvider copy();

  boolean setDiscountingCurve(UniqueIdentifiable id, YieldAndDiscountCurve curve);

  boolean setIndexCurve(UniqueIdentifiable id, YieldAndDiscountCurve curve);

  boolean setAll(CurveProvider provider);

  boolean setFxMatrix(FXMatrix fxMatrix);

  boolean removeCurve(UniqueIdentifiable id);

  void clear();
}
