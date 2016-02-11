/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import java.util.Objects;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * An interpolator that can perform left and right extrapolation. Note that instances of this class are not cached,
 * unlike {@link Interpolator1dAdapter} instances, which can be accessed from {@link NamedInterpolator1dFactory}.
 */
public class CombinedInterpolatorExtrapolator1dAdapter extends NamedInterpolator1d {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /** The interpolator */
  private final CombinedInterpolatorExtrapolator _interpolator;
  /** The name of this interpolator */
  private final String _name;

  /**
   * Creates an interpolator that can perform left and right extrapolation.
   * @param interpolator  the interpolator, not null
   * @param extrapolator  the left and right extrapolator, not null
   */
  public CombinedInterpolatorExtrapolator1dAdapter(final Interpolator1dAdapter interpolator, final Interpolator1dAdapter extrapolator) {
    _interpolator = new CombinedInterpolatorExtrapolator(interpolator, extrapolator);
    final StringBuilder sb = new StringBuilder("Combined[interpolator = ");
    sb.append(interpolator.getName());
    sb.append(", left extrapolator = ");
    sb.append(extrapolator.getName());
    sb.append(", right extrapolator = ");
    sb.append(extrapolator.getName());
    sb.append("]");
    _name = sb.toString();
  }

  /**
   * Creates an interpolator that can perform left and right extrapolation.
   * @param interpolator  the interpolator, not null
   * @param leftExtrapolator  the left extrapolator, not null
   * @param rightExtrapolator  the right extrapolator, not null
   */
  public CombinedInterpolatorExtrapolator1dAdapter(final Interpolator1dAdapter interpolator, final Interpolator1dAdapter leftExtrapolator,
      final Interpolator1dAdapter rightExtrapolator) {
    _interpolator = new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);
    final StringBuilder sb = new StringBuilder("Combined[interpolator = ");
    sb.append(interpolator.getName());
    sb.append(", left extrapolator = ");
    sb.append(leftExtrapolator.getName());
    sb.append(", right extrapolator = ");
    sb.append(rightExtrapolator.getName());
    sb.append("]");
    _name = sb.toString();
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return _interpolator.getDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return _interpolator.getDataBundleFromSortedArrays(x, y);
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    return _interpolator.interpolate(data, value);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    return _interpolator.firstDerivative(data, value);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    return _interpolator.getNodeSensitivitiesForValue(data, value);
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public boolean isExtrapolator() {
    return true;
  }

  /**
   * Gets the interpolator.
   * @return  the interpolator
   */
  public Interpolator1D getInterpolator() {
    return _interpolator.getInterpolator();
  }

  /**
   * Gets the left extrapolator.
   * @return  the left extrapolator
   */
  public Interpolator1D getLeftExtrapolator() {
    return _interpolator.getLeftExtrapolator();
  }

  /**
   * Gets the right extrapolator.
   * @return  the right extrapolator
   */
  public Interpolator1D getRightExtrapolator() {
    return _interpolator.getRightExtrapolator();
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
    if (!(obj instanceof CombinedInterpolatorExtrapolator1dAdapter)) {
      return false;
    }
    final CombinedInterpolatorExtrapolator1dAdapter other = (CombinedInterpolatorExtrapolator1dAdapter) obj;
    return Objects.equals(_interpolator, other._interpolator);
  }

  @Override
  public String toString() {
    return _name;
  }

}
