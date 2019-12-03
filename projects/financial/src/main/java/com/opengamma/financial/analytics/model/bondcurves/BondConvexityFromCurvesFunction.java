/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.CONVEXITY;

import com.opengamma.analytics.financial.provider.calculator.issuer.ConvexityFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;

/**
 * Calculates the convexity of a bond from yield curves.
 */
public class BondConvexityFromCurvesFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, Double> {

  /**
   * Sets the value requirement name to
   * {@link com.opengamma.engine.value.ValueRequirementNames#CONVEXITY} and the
   * calculator to {@link ConvexityFromCurvesCalculator}.
   */
  public BondConvexityFromCurvesFunction() {
    super(CONVEXITY, ConvexityFromCurvesCalculator.getInstance());
  }

}
