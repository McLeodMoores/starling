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
 * An adapter for {@link Interpolator1D} that perform extrapolation that implements {@link NamedExtrapolator}.
 */
@FromStringFactory(factory = NamedInterpolator1dFactory.class)
public class Extrapolator1dAdapter extends Interpolator1D implements NamedExtrapolator<Interpolator1DDataBundle, Double> {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /** The underlying interpolator */
  private final Interpolator1D _extrapolator;
  /** The interpolator name */
  private final String _name;

  /**
   * Creates an instance.
   * @param extrapolator The extrapolator, not null
   * @param name The extrapolator name, not null
   */
  public Extrapolator1dAdapter(final Interpolator1D extrapolator, final String name) {
    ArgumentChecker.notNull(extrapolator, "extrapolator");
    ArgumentChecker.notNull(name, "name");
    _extrapolator = extrapolator;
    _name = name;
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    return _extrapolator.interpolate(data, value);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    return _extrapolator.firstDerivative(data, value);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    return _extrapolator.getNodeSensitivitiesForValue(data, value);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value, final boolean useFiniteDifferenceForSensitivities) {
    return _extrapolator.getNodeSensitivitiesForValue(data, value, useFiniteDifferenceForSensitivities);
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return _extrapolator.getDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return _extrapolator.getDataBundleFromSortedArrays(x, y);
  }

  @Override
  public String getName() {
    return _name;
  }

  /**
   * Gets the underlying extrapolator.
   * @return The underlying extrapolator
   */
  public Interpolator1D getUnderlyingExtrapolator() {
    return _extrapolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _extrapolator.hashCode();
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
    if (!(obj instanceof Extrapolator1dAdapter)) {
      return false;
    }
    final Extrapolator1dAdapter other = (Extrapolator1dAdapter) obj;
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    return Objects.equals(_extrapolator, other._extrapolator);
  }


}
