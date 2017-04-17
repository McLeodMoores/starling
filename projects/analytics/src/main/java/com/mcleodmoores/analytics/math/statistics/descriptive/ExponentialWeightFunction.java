/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import com.opengamma.util.ArgumentChecker;

/**
 * A function that provides a stream of weights given by the formula:
 * $$
 * w_i = \alpha (1 - \alpha)^i
 * $$
 * For sufficiently large $$i$$, the weights will sum to one.
 */
public final class ExponentialWeightFunction implements WeightFunction<Double> {

  /**
   * Creates the function.
   * @param alpha  the weight, greater than zero and less than or equal to one
   * @return  the function
   */
  public static ExponentialWeightFunction of(final double alpha) {
    ArgumentChecker.isTrue(alpha > 0 && alpha <= 1, "The weights must be > 0 and <= 1");
    return new ExponentialWeightFunction(alpha);
  }

  private final double _alpha;
  private double _multiplier;

  private ExponentialWeightFunction(final double alpha) {
    _alpha = alpha;
    _multiplier = 1;
  }

  @Override
  public Double get() {
    final double weight = _alpha * _multiplier;
    _multiplier *= 1 - _alpha;
    return weight;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    final long temp = Double.doubleToLongBits(_alpha);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExponentialWeightFunction)) {
      return false;
    }
    final ExponentialWeightFunction other = (ExponentialWeightFunction) obj;
    if (Double.compare(_alpha, other._alpha) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("ExponentialWeightFunction[_alpha=");
    builder.append(_alpha);
    builder.append("]");
    return builder.toString();
  }

}
