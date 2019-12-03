/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;

/**
 * Calculates the weighted variance of a series of numbers, where the weights are reliability weights.
 */
public class WeightedPopulationVarianceCalculator implements Function2<WeightFunction<Double>, double[], Double> {

  @Override
  public Double apply(final WeightFunction<Double> weights, final double[] values) {
    ArgumentChecker.notNull(weights, "weights");
    ArgumentChecker.notNull(values, "values");
    final int n = values.length;
    double sumW = 0;
    double mean = 0;
    double previousMean = 0;
    double variance = 0;
    for (int i = 0; i < n; i++) {
      final double weight = weights.get();
      sumW += weight;
      mean = mean + weight * (values[i] - mean) / sumW;
      variance = variance + weight * (values[i] - previousMean) * (values[i] - mean);
      previousMean = mean;
    }
    return variance / sumW;
  }

}
