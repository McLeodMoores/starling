/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatistic;
import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * The second moment of a series of asset return data can be used as a measure
 * of the risk of that asset. However, in many instances a large positive
 * return is not regarded as a risk. The partial moment can be used as an
 * alternative.
 * <p>
 * The lower (higher) partial moment considers only those values that are below
 * (above) a threshold. Given a series of data $x_1, x_2, \dots, x_n$ sorted
 * from lowest to highest, the first (last) $k$ values are below (above) the
 * threshold $x_0$. The partial moment is given by:
 * $$
 * \begin{align*}
 * \text{pm} = \sqrt{\frac{1}{k}\sum\limits_{i=1}^{k}(x_i - x_0)^2}
 * \end{align*}
 * $$
 */
@DescriptiveStatistic(name = PartialMomentCalculator.NAME, aliases = "Partial Moment")
public class PartialMomentCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "PartialMoment";

  /** The threshold */
  private final double _threshold;
  /** True to use data below the threshold */
  private final boolean _useDownSide;

  /**
   * Creates a calculator with default values: threshold = 0 and useDownSide = true.
   */
  public PartialMomentCalculator() {
    this(0, true);
  }

  /**
   * Creates a calculator.
   * @param threshold  the threshold value for the data
   * @param useDownSide  true if data below the threshold is used, false for data above the threshold
   */
  public PartialMomentCalculator(final double threshold, final boolean useDownSide) {
    _threshold = threshold;
    _useDownSide = useDownSide;
  }

  /**
   * @param x  the array of data, not null or empty
   * @return  the partial moment
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notEmpty(x, "x");
    double sum = 0;
    int count = 0;
    if (_useDownSide) {
      for (final double d : x) {
        if (d < _threshold) {
          sum += (d - _threshold) * (d - _threshold);
          count++;
        }
      }
      if (count == 0) {
        return 0.;
      }
      return Math.sqrt(sum / count);
    }
    for (final double d : x) {
      if (d > _threshold) {
        sum += (d - _threshold) * (d - _threshold);
        count++;
      }
    }
    if (count == 0) {
      return 0.;
    }
    return Math.sqrt(sum / count);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
