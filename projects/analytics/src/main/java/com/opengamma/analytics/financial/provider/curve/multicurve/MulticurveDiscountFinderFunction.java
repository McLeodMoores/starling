/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.multicurve;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Function computing the error of valuation produce by an array representing the curve parameters.
 */
public class MulticurveDiscountFinderFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  /**
   * The instrument value calculator.
   */
  private final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> _calculator;
  /**
   * The data required for curve building.
   */
  private final MulticurveDiscountBuildingData _data;

  /**
   * Constructor.
   *
   * @param calculator
   *          The instrument value calculator.
   * @param data
   *          The data required for curve building.
   */
  public MulticurveDiscountFinderFunction(final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final MulticurveDiscountBuildingData data) {
    ArgumentChecker.notNull(calculator, "Calculator");
    ArgumentChecker.notNull(data, "Data");
    _calculator = calculator;
    _data = data;
  }

  @Override
  public DoubleMatrix1D apply(final DoubleMatrix1D x) {
    final MulticurveProviderDiscount bundle = _data.getKnownData().copy();
    final MulticurveProviderDiscount newCurves = _data.getGeneratorMarket().apply(x);
    bundle.setAll(newCurves);
    final double[] res = new double[_data.getNumberOfInstruments()];
    for (int i = 0; i < _data.getNumberOfInstruments(); i++) {
      res[i] = _data.getInstrument(i).accept(_calculator, bundle);
    }
    return new DoubleMatrix1D(res);
  }

}
