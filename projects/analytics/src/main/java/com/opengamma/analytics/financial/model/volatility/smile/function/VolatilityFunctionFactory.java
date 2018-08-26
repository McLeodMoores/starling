/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public final class VolatilityFunctionFactory {
  /** Standard Hagan  */
  public static final String HAGAN = "Hagan";
  /** Alternative Hagan  */
  public static final String ALTERNATIVE_HAGAN = "Alternative Hagan";
  /** Berestycki  */
  public static final String BERESTYCKI = "Berestycki";
  /** Johnson  */
  public static final String JOHNSON = "Johnson";
  /** Paulot  */
  public static final String PAULOT = "Paulot";
  /** Standard Hagan formula */
  public static final SABRHaganVolatilityFunction HAGAN_FORMULA = new SABRHaganVolatilityFunction();
  /** Alternative Hagan formula */
  public static final SABRHaganAlternativeVolatilityFunction ALTERNATIVE_HAGAN_FORMULA = new SABRHaganAlternativeVolatilityFunction();
  /** Berestycki formula */
  public static final SABRBerestyckiVolatilityFunction BERESTYCKI_FORMULA = new SABRBerestyckiVolatilityFunction();
  /** Johnson formula */
  public static final SABRJohnsonVolatilityFunction JOHNSON_FORMULA = new SABRJohnsonVolatilityFunction();
  /** Paulot formula */
  public static final SABRPaulotVolatilityFunction PAULOT_FORMULA = new SABRPaulotVolatilityFunction();

  private static final Map<String, VolatilityFunctionProvider<?>> INSTANCES = new HashMap<>();
  private static final Map<Class<? extends VolatilityFunctionProvider<?>>, String> INSTANCE_NAMES = new HashMap<>();

  static {
    INSTANCES.put(ALTERNATIVE_HAGAN, ALTERNATIVE_HAGAN_FORMULA);
    INSTANCES.put(BERESTYCKI, BERESTYCKI_FORMULA);
    INSTANCES.put(HAGAN, HAGAN_FORMULA);
    INSTANCES.put(JOHNSON, JOHNSON_FORMULA);
    INSTANCES.put(PAULOT, PAULOT_FORMULA);
    INSTANCE_NAMES.put(ALTERNATIVE_HAGAN_FORMULA.getClass(), ALTERNATIVE_HAGAN);
    INSTANCE_NAMES.put(BERESTYCKI_FORMULA.getClass(), BERESTYCKI);
    INSTANCE_NAMES.put(HAGAN_FORMULA.getClass(), HAGAN);
    INSTANCE_NAMES.put(JOHNSON_FORMULA.getClass(), JOHNSON);
    INSTANCE_NAMES.put(PAULOT_FORMULA.getClass(), PAULOT);
  }

  private VolatilityFunctionFactory() {
  }

  public static VolatilityFunctionProvider<?> getCalculator(final String name) {
    final VolatilityFunctionProvider<?> calculator = INSTANCES.get(name);
    if (calculator != null) {
      return calculator;
    }
    throw new IllegalArgumentException("Could not get calculator for " + name);
  }

  public static String getCalculatorName(final VolatilityFunctionProvider<?> calculator) {
    if (calculator == null) {
      return null;
    }
    return INSTANCE_NAMES.get(calculator.getClass());
  }

}
