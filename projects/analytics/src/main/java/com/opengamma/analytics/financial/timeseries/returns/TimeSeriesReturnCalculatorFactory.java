/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.util.CalculationMode;

/**
 *
 */
public class TimeSeriesReturnCalculatorFactory {
  /** Label for continuous relative return calculator, strict mode */
  public static final String CONTINUOUS_RELATIVE_STRICT = "ContinuousRelativeReturnStrict";
  /** Label for continuous return calculator, strict mode */
  public static final String CONTINUOUS_STRICT = "ContinuousReturnStrict";
  /** Label for excess continuous return calculator, strict mode */
  public static final String EXCESS_CONTINUOUS_STRICT = "ExcessContinuousReturnStrict";
  /** Label for excess simple return calculator, strict mode */
  public static final String EXCESS_SIMPLE_NET_STRICT = "ExcessSimpleNetReturnStrict";
  /** Label for simple gross return calculator, strict mode */
  public static final String SIMPLE_GROSS_STRICT = "SimpleGrossReturnStrict";
  /** Label for simple net return calculator, strict mode */
  public static final String SIMPLE_NET_STRICT = "SimpleNetReturnStrict";
  /** Label for simple net relative return calculator, strict mode */
  public static final String SIMPLE_NET_RELATIVE_STRICT = "SimpleNetRelativeReturnStrict";
  /** Label for continuous relative return calculator, lenient mode */
  public static final String CONTINUOUS_RELATIVE_LENIENT = "ContinuousRelativeReturnLenient";
  /** Label for continuous return calculator, lenient mode */
  public static final String CONTINUOUS_LENIENT = "ContinuousReturnLenient";
  /** Label for excess continuous return calculator, lenient mode */
  public static final String EXCESS_CONTINUOUS_LENIENT = "ExcessContinuousReturnLenient";
  /** Label for excess simple return calculator, lenient mode */
  public static final String EXCESS_SIMPLE_NET_LENIENT = "ExcessSimpleNetReturnLenient";
  /** Label for simple gross return calculator, lenient mode */
  public static final String SIMPLE_GROSS_LENIENT = "SimpleGrossReturnLenient";
  /** Label for simple net return calculator, lenient mode */
  public static final String SIMPLE_NET_LENIENT = "SimpleNetReturnLenient";
  /** Label for simple net relative return calculator, lenient mode */
  public static final String SIMPLE_NET_RELATIVE_LENIENT = "SimpleNetRelativeReturnLenient";
  /** Continuous relative return calculator, strict mode */
  public static final ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator CONTINUOUS_RELATIVE_STRICT_CALCULATOR = new ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator(
      CalculationMode.STRICT);
  /** Continuous return calculator, strict mode */
  public static final ContinuouslyCompoundedTimeSeriesReturnCalculator CONTINUOUS_STRICT_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Continuous return calculator, strict mode */
  public static final ExcessContinuouslyCompoundedTimeSeriesReturnCalculator EXCESS_CONTINUOUS_STRICT_CALCULATOR = new ExcessContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Excess simple return calculator, strict mode */
  public static final ExcessSimpleNetTimeSeriesReturnCalculator EXCESS_SIMPLE_NET_STRICT_CALCULATOR = new ExcessSimpleNetTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Simple gross return calculator, strict mode */
  public static final SimpleGrossTimeSeriesReturnCalculator SIMPLE_GROSS_STRICT_CALCULATOR = new SimpleGrossTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Simple net return calculator, strict mode */
  public static final SimpleNetTimeSeriesReturnCalculator SIMPLE_NET_STRICT_CALCULATOR = new SimpleNetTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Simple net relative return calculator, strict mode */
  public static final SimpleNetRelativeTimeSeriesReturnCalculator SIMPLE_NET_RELATIVE_STRICT_CALCULATOR = new SimpleNetRelativeTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Continuous relative return calculator, lenient mode */
  public static final ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator CONTINUOUS_RELATIVE_LENIENT_CALCULATOR = new ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator(
      CalculationMode.LENIENT);
  /** Continuous return calculator, lenient mode */
  public static final ContinuouslyCompoundedTimeSeriesReturnCalculator CONTINUOUS_LENIENT_CALCULATOR =
      new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Continuous return calculator, lenient mode */
  public static final ExcessContinuouslyCompoundedTimeSeriesReturnCalculator EXCESS_CONTINUOUS_LENIENT_CALCULATOR =
      new ExcessContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Excess simple return calculator, lenient mode */
  public static final ExcessSimpleNetTimeSeriesReturnCalculator EXCESS_SIMPLE_NET_LENIENT_CALCULATOR =
      new ExcessSimpleNetTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Simple gross return calculator, lenient mode */
  public static final SimpleGrossTimeSeriesReturnCalculator SIMPLE_GROSS_LENIENT_CALCULATOR =
      new SimpleGrossTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Simple net return calculator, lenient mode */
  public static final SimpleNetTimeSeriesReturnCalculator SIMPLE_NET_LENIENT_CALCULATOR =
      new SimpleNetTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Simple net relative return calculator, lenient mode */
  public static final SimpleNetRelativeTimeSeriesReturnCalculator SIMPLE_NET_RELATIVE_LENIENT_CALCULATOR =
      new SimpleNetRelativeTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  private static final Map<String, TimeSeriesReturnCalculator> STRICT_INSTANCES;
  private static final Map<Class<?>, String> INSTANCE_STRICT_NAMES;
  private static final Map<String, TimeSeriesReturnCalculator> LENIENT_INSTANCES;
  private static final Map<Class<?>, String> INSTANCE_LENIENCE_NAMES;

  static {
    STRICT_INSTANCES = new HashMap<>();
    INSTANCE_STRICT_NAMES = new HashMap<>();
    LENIENT_INSTANCES = new HashMap<>();
    INSTANCE_LENIENCE_NAMES = new HashMap<>();
    LENIENT_INSTANCES.put(CONTINUOUS_LENIENT, CONTINUOUS_LENIENT_CALCULATOR);
    INSTANCE_LENIENCE_NAMES.put(CONTINUOUS_LENIENT_CALCULATOR.getClass(), CONTINUOUS_LENIENT);
    LENIENT_INSTANCES.put(CONTINUOUS_RELATIVE_LENIENT, CONTINUOUS_RELATIVE_LENIENT_CALCULATOR);
    INSTANCE_LENIENCE_NAMES.put(CONTINUOUS_RELATIVE_LENIENT_CALCULATOR.getClass(), CONTINUOUS_RELATIVE_LENIENT);
    STRICT_INSTANCES.put(CONTINUOUS_RELATIVE_STRICT, CONTINUOUS_RELATIVE_STRICT_CALCULATOR);
    INSTANCE_STRICT_NAMES.put(CONTINUOUS_RELATIVE_STRICT_CALCULATOR.getClass(), CONTINUOUS_RELATIVE_STRICT);
    STRICT_INSTANCES.put(CONTINUOUS_STRICT, CONTINUOUS_STRICT_CALCULATOR);
    INSTANCE_STRICT_NAMES.put(CONTINUOUS_STRICT_CALCULATOR.getClass(), CONTINUOUS_STRICT);
    LENIENT_INSTANCES.put(EXCESS_CONTINUOUS_LENIENT, EXCESS_CONTINUOUS_LENIENT_CALCULATOR);
    INSTANCE_LENIENCE_NAMES.put(EXCESS_CONTINUOUS_LENIENT_CALCULATOR.getClass(), EXCESS_CONTINUOUS_LENIENT);
    STRICT_INSTANCES.put(EXCESS_CONTINUOUS_STRICT, EXCESS_CONTINUOUS_STRICT_CALCULATOR);
    INSTANCE_STRICT_NAMES.put(EXCESS_CONTINUOUS_STRICT_CALCULATOR.getClass(), EXCESS_CONTINUOUS_STRICT);
    LENIENT_INSTANCES.put(EXCESS_SIMPLE_NET_LENIENT, EXCESS_SIMPLE_NET_LENIENT_CALCULATOR);
    INSTANCE_LENIENCE_NAMES.put(EXCESS_SIMPLE_NET_LENIENT_CALCULATOR.getClass(), EXCESS_SIMPLE_NET_LENIENT);
    STRICT_INSTANCES.put(EXCESS_SIMPLE_NET_STRICT, EXCESS_SIMPLE_NET_STRICT_CALCULATOR);
    INSTANCE_STRICT_NAMES.put(EXCESS_SIMPLE_NET_STRICT_CALCULATOR.getClass(), EXCESS_SIMPLE_NET_STRICT);
    LENIENT_INSTANCES.put(SIMPLE_GROSS_LENIENT, SIMPLE_GROSS_LENIENT_CALCULATOR);
    INSTANCE_LENIENCE_NAMES.put(SIMPLE_GROSS_LENIENT_CALCULATOR.getClass(), SIMPLE_GROSS_LENIENT);
    STRICT_INSTANCES.put(SIMPLE_GROSS_STRICT, SIMPLE_GROSS_STRICT_CALCULATOR);
    INSTANCE_STRICT_NAMES.put(SIMPLE_GROSS_STRICT_CALCULATOR.getClass(), SIMPLE_GROSS_STRICT);
    LENIENT_INSTANCES.put(SIMPLE_NET_LENIENT, SIMPLE_NET_LENIENT_CALCULATOR);
    INSTANCE_LENIENCE_NAMES.put(SIMPLE_NET_LENIENT_CALCULATOR.getClass(), SIMPLE_NET_LENIENT);
    STRICT_INSTANCES.put(SIMPLE_NET_STRICT, SIMPLE_NET_STRICT_CALCULATOR);
    INSTANCE_STRICT_NAMES.put(SIMPLE_NET_STRICT_CALCULATOR.getClass(), SIMPLE_NET_STRICT);
    LENIENT_INSTANCES.put(SIMPLE_NET_RELATIVE_LENIENT, SIMPLE_NET_RELATIVE_LENIENT_CALCULATOR);
    INSTANCE_LENIENCE_NAMES.put(SIMPLE_NET_RELATIVE_LENIENT_CALCULATOR.getClass(), SIMPLE_NET_RELATIVE_LENIENT);
    STRICT_INSTANCES.put(SIMPLE_NET_RELATIVE_STRICT, SIMPLE_NET_RELATIVE_STRICT_CALCULATOR);
    INSTANCE_STRICT_NAMES.put(SIMPLE_NET_RELATIVE_STRICT_CALCULATOR.getClass(), SIMPLE_NET_RELATIVE_STRICT);
  }

  public static String getReturnCalculatorName(final TimeSeriesReturnCalculator calculator) {
    if (calculator == null) {
      return null;
    }
    final CalculationMode mode = calculator.getMode();
    if (mode == CalculationMode.STRICT) {
      return INSTANCE_STRICT_NAMES.get(calculator.getClass());
    } else if (mode == CalculationMode.LENIENT) {
      return INSTANCE_LENIENCE_NAMES.get(calculator.getClass());
    } else {
      throw new IllegalArgumentException("Do not have calculator for " + calculator.getClass().getName() + " with calculation mode " + mode);
    }
  }

  public static String getReturnCalculatorName(final TimeSeriesReturnCalculator calculator, final CalculationMode mode) {
    if (calculator == null) {
      return null;
    }
    switch (mode) {
      case STRICT:
        return INSTANCE_STRICT_NAMES.get(calculator.getClass());
      case LENIENT:
        return INSTANCE_LENIENCE_NAMES.get(calculator.getClass());
      default:
        throw new IllegalArgumentException("Do not have name for " + calculator.getClass().getName() + " with calculation mode " + mode);
    }
  }

  public static TimeSeriesReturnCalculator getReturnCalculator(final String calculatorName) {
    if (LENIENT_INSTANCES.containsKey(calculatorName)) {
      return LENIENT_INSTANCES.get(calculatorName);
    }
    if (STRICT_INSTANCES.containsKey(calculatorName)) {
      return STRICT_INSTANCES.get(calculatorName);
    }
    throw new IllegalArgumentException("Do not have calculator for " + calculatorName);
  }

  public static TimeSeriesReturnCalculator getReturnCalculator(final String calculatorName, final CalculationMode mode) {
    TimeSeriesReturnCalculator calculator;
    switch (mode) {
      case STRICT:
        calculator = STRICT_INSTANCES.get(calculatorName);
        break;
      case LENIENT:
        calculator = LENIENT_INSTANCES.get(calculatorName);
        break;
      default:
        throw new IllegalArgumentException("Do not have calculator for " + calculatorName + " with mode " + mode);
    }
    if (calculator == null) {
      throw new IllegalArgumentException("Do not have calculator for " + calculatorName + " with mode " + mode);
    }
    return calculator;
  }
}
