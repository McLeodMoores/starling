/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

/**
 * Performs intersections of time series.
 */
public final class TimeSeriesIntersector {

  /**
   * Performs the intersection of two or more time series.
   * <ul>
   * <li>If one series is provided, the original series is returned.</li>
   * <li>Create the smallest possible series by calculating the intersection of
   * series 1, 2, 3 etc. on series 0</li>
   * <li>Ensure all series are the same size by calculating the intersection of
   * this new series 0 on all other series.
   * </ul>
   * The original array is altered.
   *
   * @param series
   *          one or more input series, not null
   * @return the original array with the series replaced with the intersections
   */
  public static DoubleTimeSeries<?>[] intersect(final DoubleTimeSeries<?>... series) {
    TimeSeriesUtils.notNull(series, "The input array was null");
    if (series.length <= 1) {
      return series;
    }

    // Make the smallest series we can
    for (int i = 1; i < series.length; i++) {
      series[0] = series[0].intersectionFirstValue(series[i]);
    }
    // Shrink everything else
    for (int i = 1; i < series.length; i++) {
      series[i] = series[i].intersectionFirstValue(series[0]);
    }
    return series;
  }

  private TimeSeriesIntersector() {
  }
}
