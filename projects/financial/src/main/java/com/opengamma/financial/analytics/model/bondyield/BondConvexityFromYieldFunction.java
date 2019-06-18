/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondyield;

import static com.opengamma.engine.value.ValueRequirementNames.CONVEXITY;

import com.opengamma.analytics.financial.interestrate.bond.calculator.ConvexityFromYieldCalculator;

/**
 * Calculates the convexity of a bond from the yield.
 */
public class BondConvexityFromYieldFunction extends BondFromYieldFunction<Double> {

  /**
   * Sets the value requirement name to {@link com.opengamma.engine.value.ValueRequirementNames#CONVEXITY} and the calculator to
   * {@link ConvexityFromYieldCalculator}.
   */
  public BondConvexityFromYieldFunction() {
    super(CONVEXITY, ConvexityFromYieldCalculator.getInstance());
  }

}
