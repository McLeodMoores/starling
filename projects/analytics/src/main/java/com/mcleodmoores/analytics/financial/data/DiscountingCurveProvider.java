/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import java.util.List;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public interface DiscountingCurveProvider extends CurveProvider {

  @Override
  DiscountingCurveProvider copy();

  double getDiscountFactor(UniqueIdentifiable id, double time);

  //TODO should this be in here? probably a separate calculator
  double[] parameterForwardSensitivity(UniqueIdentifiable id, List<ForwardSensitivity> pointSensitivity);

}
