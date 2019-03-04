/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcleanprice;

import static com.opengamma.engine.value.ValueRequirementNames.CONVEXITY;

import com.opengamma.analytics.financial.interestrate.bond.calculator.ConvexityFromCleanPriceCalculator;

/**
 * Calculates the convexity of a bond from the clean price.
 */
public class BondConvexityFromCleanPriceFunction extends BondFromCleanPriceFunction<Double> {

  /**
   * Sets the value requirement name to
   * {@link com.opengamma.engine.value.ValueRequirementNames#CONVEXITY} and the
   * calculator to {@link ConvexityFromCleanPriceCalculator}
   */
  public BondConvexityFromCleanPriceFunction() {
    super(CONVEXITY, ConvexityFromCleanPriceCalculator.getInstance());
  }


}
