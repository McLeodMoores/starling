/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.calculator;

import java.util.List;

import com.mcleodmoores.analytics.financial.data.IdCurveProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public class ForwardSensitivityCalculator implements SensitivityCalculator<ForwardSensitivity> {

  @Override
  public double[] getSensitivities(final UniqueIdentifiable id, final IdCurveProvider curveProvider, final List<ForwardSensitivity> pointSensitivities) {
    final YieldAndDiscountCurve curve = curveProvider.getCurve(id);
    final int n = curve.getNumberOfParameters();
    final double[] result = new double[n];
    // want to only return results for cases where a curve is present, even if they're empty
    if (pointSensitivities == null || pointSensitivities.size() <= 0) {
      // sensitivities are 0
      return new double[n];
    }
    for (final ForwardSensitivity pair : pointSensitivities) {
      final double t1 = pair.getStartTime();
      final double t2 = pair.getEndTime();
      final double fBar = pair.getValue();
      // only the sensitivity to the forward is available; the sensitivity to the pseudo-discount factors needs to be computed.
      final double dfT1 = curve.getDiscountFactor(t1);
      final double dfT2 = curve.getDiscountFactor(t2);
      final double dFdyT1 = pair.derivativeToYieldStart(dfT1, dfT2);
      final double dFdyT2 = pair.derivativeToYieldEnd(dfT1, dfT2);
      final double[] sensitivityT1 = curve.getInterestRateParameterSensitivity(t1);
      final double[] sensitivityT2 = curve.getInterestRateParameterSensitivity(t2);
      for (int i = 0; i < n; i++) {
        result[i] += dFdyT1 * sensitivityT1[i] * fBar;
        result[i] += dFdyT2 * sensitivityT2[i] * fBar;
      }
    }
    return result;
  }

}
