/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.HashMap;
import java.util.Map;

import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsFactory;
import com.opengamma.analytics.math.function.Function;

/**
 * Factory class for descriptive statistics calculators.
 * @deprecated Use {@link DescriptiveStatisticsFactory}.
 */
@Deprecated
public final class StatisticsCalculatorFactory {
  /** Mean. */
  public static final String MEAN = "Mean";
  /** Median. */
  public static final String MEDIAN = "Median";
  /** Mode. */
  public static final String MODE = "Mode";
  /** Pearson first skewness. */
  public static final String PEARSON_FIRST_SKEWNESS = "PearsonFirstSkewnessCoefficient";
  /** Pearson second skewness. */
  public static final String PEARSON_SECOND_SKEWNESS = "PearsonSecondSkewnessCoefficient";
  /** Population standard deviation. */
  public static final String POPULATION_STANDARD_DEVIATION = "PopulationStandardDeviation";
  /** Population variance. */
  public static final String POPULATION_VARIANCE = "PopulationVariance";
  /** Quartile skewness. */
  public static final String QUARTILE_SKEWNESS = "QuartileSkewness";
  /** Sample Fisher kurtosis. */
  public static final String SAMPLE_FISHER_KURTOSIS = "SampleFisherKurtosis";
  /** Sample Pearson kurtosis. */
  public static final String SAMPLE_PEARSON_KURTOSIS = "SamplePearsonKurtosis";
  /** Sample skewness. */
  public static final String SAMPLE_SKEWNESS = "SampleSkewness";
  /** Sample standard deviation. */
  public static final String SAMPLE_STANDARD_DEVIATION = "SampleStandardDeviation";
  /** Sample variance. */
  public static final String SAMPLE_VARIANCE = "SampleVariance";
  /** Geometric mean. */
  public static final String GEOMETRIC_MEAN = "GeometricMean";
  /** Sample covariance. */
  public static final String SAMPLE_COVARIANCE = "SampleCovarianceCalculator";
  /** Mean calculator. */
  public static final MeanCalculator MEAN_CALCULATOR = new MeanCalculator();
  /** Median calculator. */
  public static final MedianCalculator MEDIAN_CALCULATOR = new MedianCalculator();
  /** Mode calculator. */
  public static final ModeCalculator MODE_CALCULATOR = new ModeCalculator();
  /** Pearson first skewness calculator. */
  public static final PearsonFirstSkewnessCoefficientCalculator PEARSON_FIRST_SKEWNESS_CALCULATOR = new PearsonFirstSkewnessCoefficientCalculator();
  /** Pearson second skewness calculator. */
  public static final PearsonSecondSkewnessCoefficientCalculator PEARSON_SECOND_SKEWNESS_CALCULATOR = new PearsonSecondSkewnessCoefficientCalculator();
  /** Population standard deviation calculator. */
  public static final PopulationStandardDeviationCalculator POPULATION_STANDARD_DEVIATION_CALCULATOR = new PopulationStandardDeviationCalculator();
  /** Population variance calculator. */
  public static final PopulationVarianceCalculator POPULATION_VARIANCE_CALCULATOR = new PopulationVarianceCalculator();
  /** Quartile skewness calculator. */
  public static final QuartileSkewnessCalculator QUARTILE_SKEWNESSS_CALCULATOR = new QuartileSkewnessCalculator();
  /** Sample Fisher kurtosis calculator. */
  public static final SampleFisherKurtosisCalculator SAMPLE_FISHER_KURTOSIS_CALCULATOR = new SampleFisherKurtosisCalculator();
  /** Sample Pearson kurtosis calculator. */
  public static final SamplePearsonKurtosisCalculator SAMPLE_PEARSON_KURTOSIS_CALCULATOR = new SamplePearsonKurtosisCalculator();
  /** Sample skewness calculator. */
  public static final SampleSkewnessCalculator SAMPLE_SKEWNESS_CALCULATOR = new SampleSkewnessCalculator();
  /** Sample standard deviation calculator. */
  public static final SampleStandardDeviationCalculator SAMPLE_STANDARD_DEVIATION_CALCULATOR = new SampleStandardDeviationCalculator();
  /** Sample variance calculator. */
  public static final SampleVarianceCalculator SAMPLE_VARIANCE_CALCULATOR = new SampleVarianceCalculator();
  /** Geometric mean calculator. */
  public static final GeometricMeanCalculator GEOMETRIC_MEAN_CALCULATOR = new GeometricMeanCalculator();
  /** Sample covariance calculator. */
  public static final SampleCovarianceCalculator SAMPLE_COVARIANCE_CALCULATOR = new SampleCovarianceCalculator();

  /** A map from class to name */
  private static final Map<Class<?>, String> NAMES = new HashMap<>();

  static {
    NAMES.put(MEAN_CALCULATOR.getClass(), MEAN);
    NAMES.put(MEDIAN_CALCULATOR.getClass(), MEDIAN);
    NAMES.put(MODE_CALCULATOR.getClass(), MODE);
    NAMES.put(PEARSON_FIRST_SKEWNESS_CALCULATOR.getClass(), PEARSON_FIRST_SKEWNESS);
    NAMES.put(PEARSON_SECOND_SKEWNESS_CALCULATOR.getClass(), PEARSON_SECOND_SKEWNESS);
    NAMES.put(POPULATION_STANDARD_DEVIATION_CALCULATOR.getClass(), POPULATION_STANDARD_DEVIATION);
    NAMES.put(POPULATION_VARIANCE_CALCULATOR.getClass(), POPULATION_VARIANCE);
    NAMES.put(QUARTILE_SKEWNESSS_CALCULATOR.getClass(), QUARTILE_SKEWNESS);
    NAMES.put(SAMPLE_FISHER_KURTOSIS_CALCULATOR.getClass(), SAMPLE_FISHER_KURTOSIS);
    NAMES.put(SAMPLE_PEARSON_KURTOSIS_CALCULATOR.getClass(), SAMPLE_PEARSON_KURTOSIS);
    NAMES.put(SAMPLE_SKEWNESS_CALCULATOR.getClass(), SAMPLE_SKEWNESS);
    NAMES.put(SAMPLE_STANDARD_DEVIATION_CALCULATOR.getClass(), SAMPLE_STANDARD_DEVIATION);
    NAMES.put(SAMPLE_VARIANCE_CALCULATOR.getClass(), SAMPLE_VARIANCE);
    NAMES.put(GEOMETRIC_MEAN_CALCULATOR.getClass(), GEOMETRIC_MEAN);
    NAMES.put(SAMPLE_COVARIANCE_CALCULATOR.getClass(), SAMPLE_COVARIANCE);
  }

  /**
   * Private constructor.
   */
  private StatisticsCalculatorFactory() {
  }

  /**
   * @param name Given a name, returns the appropriate calculator
   * @return  the calculator, throws IllegalArgumentException if the calculator cannot be found
   */
  public static Function<double[], Double> getCalculator(final String name) {
    if (SAMPLE_COVARIANCE.equals(name)) {
      return SAMPLE_COVARIANCE_CALCULATOR;
    }
    return DescriptiveStatisticsFactory.of(name);
  }

  /**
   * @param calculator Given a calculator, returns the appropriate name
   * @return The calculator
   */
  public static String getCalculatorName(final Function<double[], Double> calculator) {
    if (calculator == null) {
      return null;
    }
    return NAMES.get(calculator.getClass());
  }
}
