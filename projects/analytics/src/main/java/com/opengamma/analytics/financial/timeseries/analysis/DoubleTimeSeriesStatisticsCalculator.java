/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class DoubleTimeSeriesStatisticsCalculator implements Function<DoubleTimeSeries<?>, Double> {
  private final Function<double[], Double> _statistic;

  public DoubleTimeSeriesStatisticsCalculator(final Function<double[], Double> statistic) {
    _statistic = ArgumentChecker.notNull(statistic, "statistic");
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?>... x) {
    ArgumentChecker.notEmpty(x, "x");
    ArgumentChecker.noNulls(x, "x");
    final int n = x.length;
    final double[][] arrays = new double[n][];
    for (int i = 0; i < n; i++) {
      arrays[i] = x[i].valuesArrayFast();
    }
    return _statistic.evaluate(arrays);
  }
}
