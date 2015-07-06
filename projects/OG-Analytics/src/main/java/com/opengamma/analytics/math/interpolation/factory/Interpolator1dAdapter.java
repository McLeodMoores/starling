/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import java.util.Objects;

import org.joda.convert.FromStringFactory;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * An adapter for {@link Interpolator1D} that implements {@link NamedInterpolator}.
 */
@FromStringFactory(factory = NamedInterpolator1dFactory.class)
public class Interpolator1dAdapter extends Interpolator1D implements NamedInterpolator<Interpolator1DDataBundle, Double> {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /** The underlying interpolator */
  private final Interpolator1D _interpolator;
  /** The interpolator name */
  private final String _name;

  /**
   * Creates an instance.
   * @param interpolator The interpolator, not null
   * @param name The interpolator name, not null
   */
  public Interpolator1dAdapter(final Interpolator1D interpolator, final String name) {
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.notNull(name, "name");
    _interpolator = interpolator;
    _name = name;
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
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value, final boolean useFiniteDifferenceForSensitivities) {
    return _interpolator.getNodeSensitivitiesForValue(data, value, useFiniteDifferenceForSensitivities);
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
  public String getName() {
    return _name;
  }

  /**
   * Gets the underlying interpolator.
   * @return The underlying interpolator
   */
  public Interpolator1D getUnderlyingInterpolator() {
    return _interpolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _interpolator.hashCode();
    result = prime * result + _name.hashCode();
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
    if (!(obj instanceof Interpolator1dAdapter)) {
      return false;
    }
    final Interpolator1dAdapter other = (Interpolator1dAdapter) obj;
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    return Objects.equals(_interpolator, other._interpolator);
  }


}
