/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.CONVEXITY;

import com.opengamma.analytics.financial.provider.calculator.inflation.ConvexityFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;

/**
 * Calculates the convexity of a bond from yield curves.
 */
public class InflationBondConvexityFromCurvesFunction extends InflationBondFromCurvesFunction<InflationIssuerProviderInterface, Double> {

  /**
   * Sets the value requirement name to
   * {@link com.opengamma.engine.value.ValueRequirementNames#CONVEXITY} and the
   * calculator to {@link ConvexityFromCurvesCalculator}.
   */
  public InflationBondConvexityFromCurvesFunction() {
    super(CONVEXITY, ConvexityFromCurvesCalculator.getInstance());
  }

}
