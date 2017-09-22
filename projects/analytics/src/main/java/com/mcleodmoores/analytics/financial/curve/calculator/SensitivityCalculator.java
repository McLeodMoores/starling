/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.calculator;

import java.util.List;

import com.mcleodmoores.analytics.financial.data.CurveProvider;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public interface SensitivityCalculator<SENSITIVITY_TYPE> {

  double[] getSensitivities(UniqueIdentifiable id, CurveProvider curveProvider, List<SENSITIVITY_TYPE> pointSensitivities);

}
