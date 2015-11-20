/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Calculates the arithmetic mean of a series of data.
 * <p>
 * The arithmetic mean $\mu$ of a series of elements $x_1, x_2, \dots, x_n$ is given by:
 * $$
 * \begin{align*}
 * \mu = \frac{1}{n}\left({\sum\limits_{i=1}^n x_i}\right)
 * \end{align*}
 * $$
 */
public class MeanCalculator extends Function1D<double[], Double> {

  /**
   * @param x The array of data, not null or empty
   * @return The arithmetic mean
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x);
    Validate.isTrue(x.length > 0, "x cannot be empty");
    if (x.length == 1) {
      return x[0];
    }
    double sum = 0;
    for (final Double d : x) {
      sum += d;
    }
    return sum / x.length;
  }

}
