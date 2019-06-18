/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.twoasset;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.twoasset.TwoAssetCorrelationOptionDefinition;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Prices a two-asset correlation option.
 */
public class TwoAssetCorrelationOptionModel extends TwoAssetAnalyticOptionModel<TwoAssetCorrelationOptionDefinition, StandardTwoAssetOptionDataBundle> {
  private static final ProbabilityDistribution<double[]> BIVARIATE = new BivariateNormalDistribution();

  /**
   * Gets the pricing function for a European-style two-asset correlation option.
   *
   * @param definition
   *          The option definition
   * @return The pricing function
   * @throws IllegalArgumentException
   *           If the definition is null
   */
  @Override
  public Function1D<StandardTwoAssetOptionDataBundle, Double> getPricingFunction(final TwoAssetCorrelationOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardTwoAssetOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardTwoAssetOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s1 = data.getFirstSpot();
        final double s2 = data.getSecondSpot();
        final double k = definition.getStrike();
        final double payout = definition.getPayoutLevel();
        final double b1 = data.getFirstCostOfCarry();
        final double b2 = data.getSecondCostOfCarry();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double sigma1 = data.getFirstVolatility(t, k);
        final double sigma2 = data.getSecondVolatility(t, k);
        final double rho = data.getCorrelation();
        final double tSqrt = Math.sqrt(t);
        final double sigmaT1 = sigma1 * tSqrt;
        final double sigmaT2 = sigma2 * tSqrt;
        final double d1 = (Math.log(s1 / k) + t * (b1 - sigma1 * sigma1 / 2)) / sigmaT1;
        final double d2 = (Math.log(s2 / payout) + t * (b2 - sigma2 * sigma2 / 2)) / sigmaT2;
        final double df1 = Math.exp(t * (b2 - r));
        final double df2 = Math.exp(-r * t);
        final int sign = definition.isCall() ? 1 : -1;
        return sign * (s2 * df1 * BIVARIATE.getCDF(new double[] { sign * (d2 + sigmaT2), sign * (d1 + rho * sigmaT2), rho })
            - payout * df2 * BIVARIATE.getCDF(new double[] { sign * d2, sign * d1, rho }));

      }

    };
  }

}
