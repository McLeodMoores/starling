/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the discount factor. Only the lastTimeCalculator is
 * stored. The node are computed from the instruments.
 */
public class GeneratorCurveDiscountFactorInterpolatedAnchor extends GeneratorYDCurve {

  /**
   * Calculator of the node associated to instruments.
   */
  private final InstrumentDerivativeVisitorAdapter<Object, Double> _nodeTimeCalculator;
  /**
   * The interpolator used for the discount factors.
   */
  private final Interpolator1D _interpolator;

  /**
   * Constructor.
   * 
   * @param nodeTimeCalculator
   *          Calculator of the node associated to instruments.
   * @param interpolator
   *          The interpolator used for the curve.
   */
  public GeneratorCurveDiscountFactorInterpolatedAnchor(final InstrumentDerivativeVisitorAdapter<Object, Double> nodeTimeCalculator,
      final Interpolator1D interpolator) {
    _nodeTimeCalculator = nodeTimeCalculator;
    _interpolator = interpolator;
  }

  @Override
  public int getNumberOfParameter() {
    throw new UnsupportedOperationException("Cannot return the number of parameter for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface multicurve, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolated");
  }

  /**
   * The data passed should be one instrument for the anchor then one instrument for each of the nodes.
   * 
   * @param data
   *          The array of instruments.
   * @return The final generator.
   */
  @Override
  public GeneratorYDCurve finalGenerator(final Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    final InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    final double[] node = new double[instruments.length - 1];
    for (int loopins = 0; loopins < instruments.length - 1; loopins++) {
      node[loopins] = instruments[loopins + 1].accept(_nodeTimeCalculator);
    }
    final double anchor = instruments[0].accept(_nodeTimeCalculator);
    return new GeneratorCurveDiscountFactorInterpolatedAnchorNode(node, anchor, _interpolator);
  }

}
