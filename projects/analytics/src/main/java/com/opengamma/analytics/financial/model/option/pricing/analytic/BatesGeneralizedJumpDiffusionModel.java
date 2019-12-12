/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.BatesGeneralizedJumpDiffusionModelDataBundle;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;

/**
 * The Bates generalized jump-diffusion model prices options with an underlying process: $$ \begin{align*} dS = (b - \lambda \overline{k})S
 * dt + \sigma S dz + k dq \end{align*} $$ with $S$ the spot, $b$ the cost-of-carry, $\sigma$ the volatility of the (relative) price change
 * based on no jumps, $dz$ a Brownian motion, $k$ a random percentage jump conditional on a Poisson-distributed event occurring, with
 * ($1+k$) lognormally distributed, $\overline{k}$ the expected jump size, $\lambda$ the frequency of events (the average number of events
 * per year) and $q$ a Poisson counter with intensity $\lambda$.
 */
public class BatesGeneralizedJumpDiffusionModel
    extends AnalyticOptionModel<OptionDefinition, BatesGeneralizedJumpDiffusionModelDataBundle> {
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final int N = 50;

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<BatesGeneralizedJumpDiffusionModelDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<BatesGeneralizedJumpDiffusionModelDataBundle, Double> pricingFunction = new Function1D<BatesGeneralizedJumpDiffusionModelDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final BatesGeneralizedJumpDiffusionModelDataBundle data) {
        Validate.notNull(data);
        final double s = data.getSpot();
        final YieldAndDiscountCurve discountCurve = data.getInterestRateCurve();
        final VolatilitySurface volSurface = data.getVolatilitySurface();
        final ZonedDateTime date = data.getDate();
        final double t = definition.getTimeToExpiry(date);
        final double k = definition.getStrike();
        final double sigma = data.getVolatility(t, k);
        double b = data.getCostOfCarry();
        final double lambda = data.getLambda();
        final double expectedJumpSize = data.getExpectedJumpSize();
        final double delta = data.getDelta();
        final double gamma = Math.log(1 + expectedJumpSize);
        final double sigmaSq = sigma * sigma;
        double z;
        final double lambdaT = lambda * t;
        double mult = Math.exp(-lambdaT);
        b -= lambda * expectedJumpSize;
        StandardOptionDataBundle bsmData = new StandardOptionDataBundle(discountCurve, b, volSurface, s, date);
        final Function1D<StandardOptionDataBundle, Double> bsmFunction = BSM.getPricingFunction(definition);
        double price = mult * bsmFunction.apply(bsmData);
        for (int i = 1; i < N; i++) {
          z = Math.sqrt(sigmaSq + delta * delta * i / t);
          b += gamma / t;
          bsmData = bsmData.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(z))).withCostOfCarry(b);
          mult *= lambdaT / i;
          price += mult * bsmFunction.apply(bsmData);
        }
        return price;
      }
    };
    return pricingFunction;
  }
}
