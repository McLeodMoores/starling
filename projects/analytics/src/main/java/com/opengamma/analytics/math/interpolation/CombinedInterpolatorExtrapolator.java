/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of a one-dimensional interpolator that can extrapolate if extrapolation methods are supplied.
 * The left and right extrapolation methods can be different.
 */
public class CombinedInterpolatorExtrapolator extends Interpolator1D {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /** The interpolator */
  private final Interpolator1D _interpolator;
  /** The left extrapolator */
  private final Interpolator1D _leftExtrapolator;
  /** The right extrapolator */
  private final Interpolator1D _rightExtrapolator;

  /**
   * Creates an instance that does not extrapolate.
   * @param interpolator  the interpolator, not null
   */
  public CombinedInterpolatorExtrapolator(final Interpolator1D interpolator) {
    _interpolator = ArgumentChecker.notNull(interpolator, "interpolator");
    _leftExtrapolator = null;
    _rightExtrapolator = null;
  }

  /**
   * Creates an instance that uses the extrapolator for the left and right side.
   * @param interpolator  the interpolator, not null
   * @param extrapolator  the extrapolator, not null
   */
  public CombinedInterpolatorExtrapolator(final Interpolator1D interpolator, final Interpolator1D extrapolator) {
    _interpolator = ArgumentChecker.notNull(interpolator, "interpolator");
    _leftExtrapolator = ArgumentChecker.notNull(extrapolator, "extrapolator");
    _rightExtrapolator = extrapolator;
  }

  /**
   * Creates an instance.
   * @param interpolator  the interpolator, not null
   * @param leftExtrapolator  the left extrapolator, not null
   * @param rightExtrapolator  the right extrapolator, not null
   */
  public CombinedInterpolatorExtrapolator(final Interpolator1D interpolator, final Interpolator1D leftExtrapolator, final Interpolator1D rightExtrapolator) {
    _interpolator = ArgumentChecker.notNull(interpolator, "interpolator");
    _leftExtrapolator = ArgumentChecker.notNull(leftExtrapolator, "left extrapolator");
    _rightExtrapolator = ArgumentChecker.notNull(rightExtrapolator, "right extrapolator");
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return _interpolator.getDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return _interpolator.getDataBundleFromSortedArrays(x, y);
  }

  /**
   * Gets the interpolator.
   * @return  the interpolator
   */
  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  /**
   * Gets the left extrapolator.
   * @return  the left extrapolator, not null
   */
  public Interpolator1D getLeftExtrapolator() {
    return _leftExtrapolator;
  }

  /**
   * Gets the right extrapolator.
   * @return  the right extrapolator, not null
   */
  public Interpolator1D getRightExtrapolator() {
    return _rightExtrapolator;
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(value, "value");
    if (value < data.firstKey()) {
      if (_leftExtrapolator != null) {
        return _leftExtrapolator.interpolate(data, value);
      }
    } else if (value > data.lastKey()) {
      if (_rightExtrapolator != null) {
        return _rightExtrapolator.interpolate(data, value);
      }
    }
    return _interpolator.interpolate(data, value);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(value, "value");
    if (value < data.firstKey()) {
      if (_leftExtrapolator != null) {
        return _leftExtrapolator.firstDerivative(data, value);
      }
    } else if (value > data.lastKey()) {
      if (_rightExtrapolator != null) {
        return _rightExtrapolator.firstDerivative(data, value);
      }
    }
    return _interpolator.firstDerivative(data, value);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(value, "value");
    if (value < data.firstKey()) {
      if (_leftExtrapolator != null) {
        return _leftExtrapolator.getNodeSensitivitiesForValue(data, value);
      }
    } else if (value > data.lastKey()) {
      if (_rightExtrapolator != null) {
        return _rightExtrapolator.getNodeSensitivitiesForValue(data, value);
      }
    }
    return _interpolator.getNodeSensitivitiesForValue(data, value);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Interpolator[interpolator=");
    sb.append(_interpolator.toString());
    sb.append(", left extrapolator=");
    if (_leftExtrapolator == null) {
      sb.append("null");
    } else {
      sb.append(_leftExtrapolator.toString());
    }
    sb.append(", right extrapolator=");
    if (_rightExtrapolator == null) {
      sb.append("null");
    } else {
      sb.append(_rightExtrapolator.toString());
    }
    sb.append("]");
    return sb.toString();
  }
}
