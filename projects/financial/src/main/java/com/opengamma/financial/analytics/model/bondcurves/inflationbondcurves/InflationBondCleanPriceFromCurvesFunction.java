/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;

import com.opengamma.analytics.financial.provider.calculator.inflation.CleanRealPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;

/**
 * Calculates the clean price of a bond from yield curves.
 */
public class InflationBondCleanPriceFromCurvesFunction extends InflationBondFromCurvesFunction<InflationIssuerProviderInterface, Double> {

  /**
   * Sets the value requirement name to {@link com.opengamma.engine.value.ValueRequirementNames#CLEAN_PRICE} and the calculator to
   * {@link CleanRealPriceFromCurvesCalculator}.
   */
  public InflationBondCleanPriceFromCurvesFunction() {
    super(CLEAN_PRICE, CleanRealPriceFromCurvesCalculator.getInstance());
  }

}
