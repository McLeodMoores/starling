/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.HashMap;
import java.util.Map;

// CSOFF
/**
 * @deprecated The that are used by this factory are deprecated.
 */
@Deprecated
public final class FixedIncomeCalculatorFactory {
  /** Present value. */
  public static final String PRESENT_VALUE = "PresentValue";
  /** Present value coupon sensitivity. */
  public static final String PRESENT_VALUE_COUPON_SENSITIVITY = "PresentValueCouponSensitivity";
  /** Present value sensitivity. */
  public static final String PRESENT_VALUE_SENSITIVITY = "PresentValueSensitivity";
  /** PV01. */
  public static final String PV01 = "PV01";
  /** Par rate. */
  public static final String PAR_RATE = "ParRate";
  /** Par rate curve sensitivity. */
  public static final String PAR_RATE_CURVE_SENSITIVITY = "ParRateCurveSensitivity";
  /** Par rate parallel sensitivity. */
  public static final String PAR_RATE_PARALLEL_SENSITIVITY = "ParRateParallelSensitivity";
  /** Present value calculator. */
  public static final PresentValueCalculator PRESENT_VALUE_CALCULATOR = PresentValueCalculator.getInstance();
  /** Present value coupon sensitivity calculator. */
  public static final PresentValueCouponSensitivityCalculator PRESENT_VALUE_COUPON_SENSITIVITY_CALCULATOR =
      PresentValueCouponSensitivityCalculator.getInstance();
  /** Present value sensitivity calculator. */
  public static final PresentValueCurveSensitivityCalculator PRESENT_VALUE_SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  /** PV01 calculator. */
  public static final PV01Calculator PV01_CALCULATOR = PV01Calculator.getInstance();
  /** Par rate calculator. */
  public static final ParRateCalculator PAR_RATE_CALCULATOR = ParRateCalculator.getInstance();
  /** Par rate curve sensitivity calculator. */
  public static final ParRateCurveSensitivityCalculator PAR_RATE_CURVE_SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();
  /** Par rate parallel sensitivity calculator. */
  public static final ParRateParallelSensitivityCalculator PAR_RATE_PARALLEL_SENSITIVITY_CALCULATOR = ParRateParallelSensitivityCalculator.getInstance();

  /** Delta. */
  public static final String DELTA = "Delta";
  /** Delta calculator. */
  public static final DeltaBlackCalculator DELTA_CALCULATOR = DeltaBlackCalculator.getInstance();

  private static final Map<String, InstrumentDerivativeVisitor<?, ?>> INSTANCES = new HashMap<>();
  private static final Map<Class<?>, String> INSTANCE_NAMES = new HashMap<>();

  static {
    INSTANCES.put(PAR_RATE, PAR_RATE_CALCULATOR);
    INSTANCES.put(PAR_RATE_CURVE_SENSITIVITY, PAR_RATE_CURVE_SENSITIVITY_CALCULATOR);
    INSTANCES.put(PAR_RATE_PARALLEL_SENSITIVITY, PAR_RATE_PARALLEL_SENSITIVITY_CALCULATOR);
    INSTANCES.put(PRESENT_VALUE, PRESENT_VALUE_CALCULATOR);
    INSTANCES.put(PRESENT_VALUE_COUPON_SENSITIVITY, PRESENT_VALUE_COUPON_SENSITIVITY_CALCULATOR);
    INSTANCES.put(PRESENT_VALUE_SENSITIVITY, PRESENT_VALUE_SENSITIVITY_CALCULATOR);
    INSTANCES.put(PV01, PV01_CALCULATOR);
    INSTANCES.put(DELTA, DELTA_CALCULATOR);
    INSTANCE_NAMES.put(PAR_RATE_CALCULATOR.getClass(), PAR_RATE);
    INSTANCE_NAMES.put(PAR_RATE_CURVE_SENSITIVITY_CALCULATOR.getClass(), PAR_RATE_CURVE_SENSITIVITY);
    INSTANCE_NAMES.put(PAR_RATE_PARALLEL_SENSITIVITY_CALCULATOR.getClass(), PAR_RATE_PARALLEL_SENSITIVITY);
    INSTANCE_NAMES.put(PRESENT_VALUE_CALCULATOR.getClass(), PRESENT_VALUE);
    INSTANCE_NAMES.put(PRESENT_VALUE_COUPON_SENSITIVITY_CALCULATOR.getClass(), PRESENT_VALUE_COUPON_SENSITIVITY);
    INSTANCE_NAMES.put(PRESENT_VALUE_SENSITIVITY_CALCULATOR.getClass(), PRESENT_VALUE_SENSITIVITY);
    INSTANCE_NAMES.put(PV01_CALCULATOR.getClass(), PV01);
    INSTANCE_NAMES.put(DELTA_CALCULATOR.getClass(), DELTA);
  }

  private FixedIncomeCalculatorFactory() {
  }

  public static InstrumentDerivativeVisitor<?, ?> getCalculator(final String name) {
    final InstrumentDerivativeVisitor<?, ?> calculator = INSTANCES.get(name);
    if (calculator != null) {
      return calculator;
    }
    throw new IllegalArgumentException("Could not get calculator for " + name);
  }

  public static String getCalculatorName(final InstrumentDerivativeVisitor<?, ?> calculator) {
    if (calculator == null) {
      return null;
    }
    return INSTANCE_NAMES.get(calculator.getClass());
  }
}
