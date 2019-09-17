/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SupershareOptionDefinition;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Class for pricing supershare options (see {@link com.opengamma.analytics.financial.model.option.definition.SupershareOptionDefinition}).
 */
public class SupershareOptionModel extends AnalyticOptionModel<SupershareOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final SupershareOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double apply(final StandardOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s = data.getSpot();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        final double lower = definition.getLowerBound();
        final double upper = definition.getUpperBound();
        final double sigma = data.getVolatility(t, lower);
        final double d1 = getD1(s, lower, t, sigma, b);
        final double d2 = getD1(s, upper, t, sigma, b);
        return s * Math.exp(t * (b - r)) * (NORMAL.getCDF(d1) - NORMAL.getCDF(d2)) / lower;
      }

    };
  }
}
