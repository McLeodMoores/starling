/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the rate (continuously compounded).
 */
public class GeneratorPriceIndexCurveInterpolatedNode extends GeneratorPriceIndexCurve {

  /**
   * The nodes (times) on which the interpolated curves is constructed.
   */
  private final double[] _nodePoints;
  /**
   * The interpolator used for the curve.
   */
  private final Interpolator1D _interpolator;
  /**
   * The number of points (or nodes). Is the length of _nodePoints.
   */
  private final int _nbPoints;

  /**
   * Constructor.
   * @param nodePoints The node points (X) used to define the interpolated curve. 
   * @param interpolator The interpolator.
   */
  public GeneratorPriceIndexCurveInterpolatedNode(double[] nodePoints, Interpolator1D interpolator) {
    ArgumentChecker.notNull(nodePoints, "Node points");
    ArgumentChecker.notNull(interpolator, "Interpolator");
    _nodePoints = nodePoints;
    _nbPoints = _nodePoints.length;
    _interpolator = interpolator;
  }

  @Override
  public int getNumberOfParameter() {
    return _nbPoints;
  }

  @Override
  public PriceIndexCurve generateCurve(String name, double[] parameters) {
    ArgumentChecker.isTrue(parameters.length == _nbPoints, "Incorrect dimension for the price indices");
    return new PriceIndexCurve(new InterpolatedDoublesCurve(_nodePoints, parameters, _interpolator, true, name));
  }

  @Override
  public PriceIndexCurve generateCurve(String name, InflationProviderInterface inflation, double[] parameters) {
    return generateCurve(name, parameters);
  }

}
