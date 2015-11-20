/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class MultipleYieldCurveFinderFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {
  private final InstrumentDerivativeVisitor<YieldCurveBundle, Double> _calculator;
  private final MultipleYieldCurveFinderDataBundle _data;
  private final YieldCurveBundleBuildingFunction _curveBuilderFunction; //TODO this could be moved into MultipleYieldCurveFinderDataBundle

  public MultipleYieldCurveFinderFunction(final MultipleYieldCurveFinderDataBundle data, final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator) {
    Validate.notNull(data, "data");
    Validate.notNull(calculator, "calculator");
    _calculator = calculator;
    _data = data;
    _curveBuilderFunction = new InterpolatedYieldCurveBuildingFunction(data.getUnknownCurveNodePoints(), data.getUnknownCurveInterpolators());
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {

    final YieldCurveBundle curves = _curveBuilderFunction.evaluate(x);

    // set any known (i.e. fixed) curves
    final YieldCurveBundle knownCurves = _data.getKnownCurves();
    if (knownCurves != null) {
      curves.addAll(knownCurves);
    }

    final double[] res = new double[_data.getNumInstruments()];
    for (int i = 0; i < _data.getNumInstruments(); i++) {
      res[i] = _data.getDerivative(i).accept(_calculator, curves) - _data.getMarketValue(i);
    }

    return new DoubleMatrix1D(res);
  }

}
