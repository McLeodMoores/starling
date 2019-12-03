/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.InterpolatorND;
import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.tuple.Triple;

/**
 * A cube that is defined by a set of nodal points <i>(x, y, z, value)</i> and an interpolator to return values for <i>(x, y, z)</i> when they are not nodal
 * points.
 */
public class InterpolatedDoublesCube extends DoublesCube {

  /**
   * @param xData
   *          An array containing <i>x</i> data, not null
   * @param yData
   *          An array containing <i>y</i> data, not null, must be the same length as the <i>x</i> array
   * @param zData
   *          An array containing <i>z</i> data, not null, must be the same length as the <i>x</i> array
   * @param values
   *          An array containing <i>value</i> data, not null, must be the same length as the <i>x</i> array
   * @param interpolator
   *          The interpolator, not null
   * @return An interpolated cube with automatically-generated name
   */
  public static InterpolatedDoublesCube from(final double[] xData, final double[] yData, final double[] zData, final double[] values,
      final InterpolatorND interpolator) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator);
  }

  /**
   * @param xData
   *          An array containing <i>x</i> data, not null
   * @param yData
   *          An array containing <i>y</i> data, not null, must be the same length as the <i>x</i> array
   * @param zData
   *          An array containing <i>z</i> data, not null, must be the same length as the <i>x</i> array
   * @param values
   *          An array containing <i>value</i> data, not null, must be the same length as the <i>x</i> array
   * @param interpolator
   *          The interpolator, not null
   * @return An interpolated cube with automatically-generated name
   */
  public static InterpolatedDoublesCube from(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] values,
      final InterpolatorND interpolator) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator);
  }

  /**
   * @param xData
   *          A list containing <i>x</i> data, not null
   * @param yData
   *          A list containing <i>y</i> data, not null, must be the same length as the <i>x</i> list
   * @param zData
   *          A list containing <i>z</i> data, not null, must be the same length as the <i>x</i> list
   * @param values
   *          A list containing <i>value</i> data, not null, must be the same length as the <i>x</i> list
   * @param interpolator
   *          The interpolator, not null
   * @return An interpolated cube with automatically-generated name
   */
  public static InterpolatedDoublesCube from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> values,
      final InterpolatorND interpolator) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator);
  }

  /**
   * @param xData
   *          An array containing <i>x</i> data, not null
   * @param yData
   *          An array containing <i>y</i> data, not null, must be the same length as the <i>x</i> array
   * @param zData
   *          An array containing <i>z</i> data, not null, must be the same length as the <i>x</i> array
   * @param values
   *          An array containing <i>value</i> data, not null, must be the same length as the <i>x</i> array
   * @param interpolator
   *          The interpolator, not null
   * @param name
   *          The name of the cube
   * @return An interpolated cube
   */
  public static InterpolatedDoublesCube from(final double[] xData, final double[] yData, final double[] zData, final double[] values,
      final InterpolatorND interpolator, final String name) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator, name);
  }

  /**
   * @param xData
   *          An array containing <i>x</i> data, not null
   * @param yData
   *          An array containing <i>y</i> data, not null, must be the same length as the <i>x</i> array
   * @param zData
   *          An array containing <i>z</i> data, not null, must be the same length as the <i>x</i> array
   * @param values
   *          An array containing <i>value</i> data, not null, must be the same length as the <i>x</i> array
   * @param interpolator
   *          The interpolator, not null
   * @param name
   *          The name of the cube
   * @return An interpolated cube
   */
  public static InterpolatedDoublesCube from(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] values,
      final InterpolatorND interpolator, final String name) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator, name);
  }

  /**
   * @param xData
   *          A list containing <i>x</i> data, not null
   * @param yData
   *          A list containing <i>y</i> data, not null, must be the same length as the <i>x</i> list
   * @param zData
   *          A list containing <i>z</i> data, not null, must be the same length as the <i>x</i> list
   * @param values
   *          A list containing <i>value</i> data, not null, must be the same length as the <i>x</i> list
   * @param interpolator
   *          The interpolator, not null
   * @param name
   *          The name of the cube
   * @return An interpolated cube
   */
  public static InterpolatedDoublesCube from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> values,
      final InterpolatorND interpolator, final String name) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator, name);
  }

  private final InterpolatorND _interpolator;
  private InterpolatorNDDataBundle _dataBundle;

  /**
   * @param xData
   *          An array containing <i>x</i> data, not null
   * @param yData
   *          An array containing <i>y</i> data, not null, must be the same length as the <i>x</i> array
   * @param zData
   *          An array containing <i>z</i> data, not null, must be the same length as the <i>x</i> array
   * @param values
   *          An array containing <i>value</i> data, not null, must be the same length as the <i>x</i> array
   * @param interpolator
   *          The interpolator, not null
   */
  public InterpolatedDoublesCube(final double[] xData, final double[] yData, final double[] zData, final double[] values, final InterpolatorND interpolator) {
    super(xData, yData, zData, values);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  /**
   * @param xData
   *          An array containing <i>x</i> data, not null
   * @param yData
   *          An array containing <i>y</i> data, not null, must be the same length as the <i>x</i> array
   * @param zData
   *          An array containing <i>z</i> data, not null, must be the same length as the <i>x</i> array
   * @param values
   *          An array containing <i>value</i> data, not null, must be the same length as the <i>x</i> array
   * @param interpolator
   *          The interpolator, not null
   */
  public InterpolatedDoublesCube(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] values, final InterpolatorND interpolator) {
    super(xData, yData, zData, values);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  /**
   * @param xData
   *          A list containing <i>x</i> data, not null
   * @param yData
   *          A list containing <i>y</i> data, not null, must be the same length as the <i>x</i> list
   * @param zData
   *          A list containing <i>z</i> data, not null, must be the same length as the <i>x</i> list
   * @param values
   *          A list containing <i>value</i> data, not null, must be the same length as the <i>x</i> list
   * @param interpolator
   *          The interpolator, not null
   */
  public InterpolatedDoublesCube(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> values,
      final InterpolatorND interpolator) {
    super(xData, yData, zData, values);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  /**
   * @param xData
   *          An array containing <i>x</i> data, not null
   * @param yData
   *          An array containing <i>y</i> data, not null, must be the same length as the <i>x</i> array
   * @param zData
   *          An array containing <i>z</i> data, not null, must be the same length as the <i>x</i> array
   * @param values
   *          An array containing <i>value</i> data, not null, must be the same length as the <i>x</i> array
   * @param interpolator
   *          The interpolator, not null
   * @param name
   *          The name of the cube
   */
  public InterpolatedDoublesCube(final double[] xData, final double[] yData, final double[] zData, final double[] values, final InterpolatorND interpolator,
      final String name) {
    super(xData, yData, zData, values, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  /**
   * @param xData
   *          An array containing <i>x</i> data, not null
   * @param yData
   *          An array containing <i>y</i> data, not null, must be the same length as the <i>x</i> array
   * @param zData
   *          An array containing <i>z</i> data, not null, must be the same length as the <i>x</i> array
   * @param values
   *          An array containing <i>value</i> data, not null, must be the same length as the <i>x</i> array
   * @param interpolator
   *          The interpolator, not null
   * @param name
   *          The name of the cube
   */
  public InterpolatedDoublesCube(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] values, final InterpolatorND interpolator,
      final String name) {
    super(xData, yData, zData, values, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  /**
   * @param xData
   *          A list containing <i>x</i> data, not null
   * @param yData
   *          A list containing <i>y</i> data, not null, must be the same length as the <i>x</i> list
   * @param zData
   *          A list containing <i>z</i> data, not null, must be the same length as the <i>x</i> list
   * @param values
   *          A list containing <i>value</i> data, not null, must be the same length as the <i>x</i> list
   * @param interpolator
   *          The interpolator, not null
   * @param name
   *          The name of the cube
   */
  public InterpolatedDoublesCube(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> values,
      final InterpolatorND interpolator, final String name) {
    super(xData, yData, zData, values, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  private void init() {
    _dataBundle = _interpolator.getDataBundle(getXDataAsPrimitive(), getYDataAsPrimitive(), getZDataAsPrimitive(), getValuesAsPrimitive());
  }

  @Override
  public Double getValue(final Double x, final Double y, final Double z) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    Validate.notNull(z, "z");
    return _interpolator.interpolate(_dataBundle, new double[] { x, y, z });
  }

  @Override
  public Double getValue(final Triple<Double, Double, Double> xyz) {
    Validate.notNull(xyz, "xyz");
    final Double x = xyz.getFirst();
    Validate.notNull(x, "x");
    final Double y = xyz.getSecond();
    Validate.notNull(y, "y");
    final Double z = xyz.getThird();
    Validate.notNull(z, "z");
    return _interpolator.interpolate(_dataBundle, new double[] { x, y, z });
  }

  /**
   * Gets the interpolator.
   * 
   * @return the interpolator
   */
  public InterpolatorND getInterpolator() {
    return _interpolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _interpolator.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InterpolatedDoublesCube other = (InterpolatedDoublesCube) obj;
    return ObjectUtils.equals(_interpolator, other._interpolator);
  }

}
