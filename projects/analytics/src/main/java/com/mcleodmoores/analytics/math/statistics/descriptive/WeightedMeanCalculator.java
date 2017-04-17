/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;

/**
 * Calculates the weighted mean of a series of values:
 * $$
 * \mu^* = \frac{\sum_{i=1}^n w_i x_i}{\sum_{i=1}^n w_i}
 * $$
 */
public class WeightedMeanCalculator implements Function2<WeightFunction<Double>, double[], Double> {

  @Override
  public Double apply(final WeightFunction<Double> weights, final double[] values) {
    ArgumentChecker.notNull(weights, "weights");
    ArgumentChecker.notNull(values, "values");
    final int n = values.length;
    double sumW = 0;
    double mean = 0;
    for (int i = 0; i < n; i++) {
      final double weight = weights.get();
      sumW += weight;
      mean = mean + weight * (values[i] - mean) / sumW;
    }
    return mean;
  }


}
