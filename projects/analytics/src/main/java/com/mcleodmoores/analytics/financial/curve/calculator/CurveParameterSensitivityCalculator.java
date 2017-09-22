/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.calculator;

import java.util.List;

import com.mcleodmoores.analytics.financial.data.CurveProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class CurveParameterSensitivityCalculator implements SensitivityCalculator<DoublesPair> {

  @Override
  public double[] getSensitivities(final UniqueIdentifiable id, final CurveProvider curveProvider, final List<DoublesPair> pointSensitivities) {
    final YieldAndDiscountCurve curve = curveProvider.getCurve(id);
    // want to only return results for cases where a curve is present, even if they're empty
    final int n = curve.getNumberOfParameters();
    if (pointSensitivities == null || pointSensitivities.size() <= 0) {
      // sensitivities are 0
      return new double[n];
    }
    final double[] result = new double[n];
    for (final DoublesPair pair : pointSensitivities) {
      final double t = pair.getFirst();
      final double dy = pair.getSecond();
      final double[] sensitivities = curve.getInterestRateParameterSensitivity(t);
      for (int i = 0; i < n; i++) {
        result[i] += dy * sensitivities[i];
      }
    }
    return result;
  }

}
