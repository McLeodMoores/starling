/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.twoasset;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.twoasset.ProductOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Prices a product option.
 */
public class ProductOptionModel extends TwoAssetAnalyticOptionModel<ProductOptionDefinition, StandardTwoAssetOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Gets the pricing function for a European-style product option.
   * 
   * @param definition
   *          The option definition
   * @return The pricing function
   * @throws IllegalArgumentException
   *           If the definition is null
   */
  @Override
  public Function1D<StandardTwoAssetOptionDataBundle, Double> getPricingFunction(final ProductOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardTwoAssetOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardTwoAssetOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s1 = data.getFirstSpot();
        final double s2 = data.getSecondSpot();
        final double k = definition.getStrike();
        final double b1 = data.getFirstCostOfCarry();
        final double b2 = data.getSecondCostOfCarry();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double sigma1 = data.getFirstVolatility(t, k);
        final double sigma2 = data.getSecondVolatility(t, k);
        final double rho = data.getCorrelation();
        final double sigma = Math.sqrt(sigma1 * sigma1 + sigma2 * sigma2 + 2 * rho * sigma1 * sigma2);
        final double sigmaT = sigma * Math.sqrt(t);
        final double f = s1 * s2 * Math.exp(t * (b1 + b2 + rho * sigma1 * sigma2));
        final double d1 = (Math.log(f / k) + t * sigma * sigma / 2) / sigmaT;
        final double d2 = d1 - sigmaT;
        final int sign = definition.isCall() ? 1 : -1;
        return Math.exp(-r * t) * sign * (f * NORMAL.getCDF(sign * d1) - k * NORMAL.getCDF(sign * d2));
      }

    };
  }
}
