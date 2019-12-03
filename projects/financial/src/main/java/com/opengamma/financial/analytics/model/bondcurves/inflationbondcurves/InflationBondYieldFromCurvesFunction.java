/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.YTM;

import com.opengamma.analytics.financial.provider.calculator.inflation.YieldFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;

/**
 * Calculates the yield of an inflation bond from inflation and yield curves.
 */
public class InflationBondYieldFromCurvesFunction extends InflationBondFromCurvesFunction<InflationIssuerProviderInterface, Double> {

  /**
   * Sets the value requirement name to
   * {@link com.opengamma.engine.value.ValueRequirementNames#YTM} and the
   * calculator to {@link YieldFromCurvesCalculator}.
   */
  public InflationBondYieldFromCurvesFunction() {
    super(YTM, YieldFromCurvesCalculator.getInstance());
  }
}
