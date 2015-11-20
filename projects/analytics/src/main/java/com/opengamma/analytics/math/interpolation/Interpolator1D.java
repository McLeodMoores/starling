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

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A base class for interpolation in one dimension. This class also calculates the first derivatives of the data and the
 * sensitivity of the interpolated value to the y data by finite difference, although sub-classes can implement analytic
 * versions of this calculation.
 */
public abstract class Interpolator1D implements Interpolator<Interpolator1DDataBundle, Double>, Serializable {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /** The default epsilon used for calculating the gradient via finite difference */
  private static final double EPS = 1e-6;

  @Override
  public abstract Double interpolate(Interpolator1DDataBundle data, Double value);

  /**
   * Computes the gradient of the interpolant at the value.
   * @param data  interpolation data, not null
   * @param value  the value for which the gradient is computed, not null
   * @return  the gradient
   */
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(value, "value");
    final double vm = value - EPS;
    final double vp = value + EPS;

    if (vm < data.firstKey()) {
      final double up = interpolate(data, value + EPS);
      final double mid = interpolate(data, value);
      return (up - mid) / EPS;
    } else if (vp > data.lastKey()) {
      final double down = interpolate(data, vm);
      final double mid = interpolate(data, value);
      return (mid - down) / EPS;
    }
    final double up = interpolate(data, value + EPS);
    final double down = interpolate(data, vm);
    return (up - down) / 2 / EPS;
  }

  /**
   * Computes the sensitivities of the interpolated value to the input data y.
   * @param data  the interpolation data, not null
   * @param value  the value for which the interpolation is computed, not null
   * @param useFiniteDifferenceSensitivities  use finite difference approximation if true
   * @return  the sensitivities at each node
   */
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value, final boolean useFiniteDifferenceSensitivities) {
    return useFiniteDifferenceSensitivities ? getFiniteDifferenceSensitivities(data, value) : getNodeSensitivitiesForValue(data, value);
  }

  /**
   * Computes the sensitivities of the interpolated value to the input data y by using a methodology defined in a respective subclass.
   * @param data  the interpolation data, not null
   * @param value  the value for which the interpolation is computed, not null
   * @return  the sensitivities
   */
  public abstract double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value);

  /**
   * Computes the sensitivities of the interpolated value to the input data y by using central finite difference approximation.
   * @param data  the interpolation data, not null
   * @param value  the value for which the interpolation is computed, not null
   * @return  the sensitivities
   */
  protected double[] getFiniteDifferenceSensitivities(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(value, "value");
    final double[] x = data.getKeys();
    final double[] y = data.getValues();
    final int n = x.length;
    final double[] result = new double[n];
    final Interpolator1DDataBundle dataUp = getDataBundleFromSortedArrays(x, y);
    final Interpolator1DDataBundle dataDown = getDataBundleFromSortedArrays(x, y);
    for (int i = 0; i < n; i++) {
      if (i != 0) {
        dataUp.setYValueAtIndex(i - 1, y[i - 1]);
        dataDown.setYValueAtIndex(i - 1, y[i - 1]);
      }
      dataUp.setYValueAtIndex(i, y[i] + EPS);
      dataDown.setYValueAtIndex(i, y[i] - EPS);
      final double up = interpolate(dataUp, value);
      final double down = interpolate(dataDown, value);
      result[i] = (up - down) / 2 / EPS;
    }
    return result;
  }

  /**
   * Construct an {@link Interpolator1DDataBundle} from unsorted arrays. The bundle may contain information such as the derivatives
   * at each data point. The x data need not be sorted.
   * @param x  x values of data, not null
   * @param y  y values of data, not null
   * @return  the data bundle
   */
  public abstract Interpolator1DDataBundle getDataBundle(double[] x, double[] y);

  /**
   * Construct an {@link Interpolator1DDataBundle} from sorted arrays, i.e, x[0] < x[1] < x[2]. The bundle may contain information such as the derivatives
   * at each data point.
   * @param x  x values of data, not null
   * @param y  y values of data, not null
   * @return  the data bundle
   */
  public abstract Interpolator1DDataBundle getDataBundleFromSortedArrays(double[] x, double[] y);

  /**
   * Constructs an {@link Interpolator1DDataBundle}. The bundle may contain information such as the derivatives
   * at each data point. The x data need not be sorted.
   * @param data  data containing x values and y values, not null
   * @return Interpolator1DDataBundle
   */
  public Interpolator1DDataBundle getDataBundle(final Map<Double, Double> data) {
    ArgumentChecker.notEmpty(data, "data");
    if (data instanceof SortedMap) {
      final double[] keys = ArrayUtils.toPrimitive(data.keySet().toArray(new Double[data.size()]));
      final double[] values = ArrayUtils.toPrimitive(data.values().toArray(new Double[data.size()]));
      return getDataBundleFromSortedArrays(keys, values);
    }
    final double[] keys = new double[data.size()];
    final double[] values = new double[data.size()];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      keys[i] = entry.getKey();
      values[i] = entry.getValue();
      i++;
    }
    return getDataBundle(keys, values);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }

  /**
   * @param o  the reference class
   * @return  true if two objects are the same class
   */
  protected boolean classEquals(final Object o) {
    if (o == null) {
      return false;
    }
    return getClass().equals(o.getClass());
  }
}
