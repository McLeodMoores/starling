/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

/**
 * A function that provides a stream of equal weights.
 * $$
 * w_i = 1 / n
 * $$
 * The weights sum to one. The number of weights that can be requested cannot be larger than $$n$$.
 */
public final class EqualWeightFunction implements WeightFunction<Double> {

  /**
   * Creates a function that provides a weight equal to the inverse of the input.
   * @param  x  the value
   * @return  the function
   */
  public static EqualWeightFunction ofInverse(final double x) {
    return new EqualWeightFunction(1 / x);
  }

  /**
   * Creates the function.
   * @param weight  the weight
   * @return  the function
   */
  public static EqualWeightFunction of(final double weight) {
    return new EqualWeightFunction(weight);
  }

  private final double _weight;

  private EqualWeightFunction(final double weight) {
    _weight = weight;
  }

  @Override
  public Double get() {
    return _weight;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    final long temp = Double.doubleToLongBits(_weight);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EqualWeightFunction)) {
      return false;
    }
    final EqualWeightFunction other = (EqualWeightFunction) obj;
    if (Double.compare(_weight, other._weight) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("EqualWeightFunction[_weight=");
    builder.append(_weight);
    builder.append("]");
    return builder.toString();
  }

}
