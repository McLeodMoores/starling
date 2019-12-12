/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.greeks.GreekVisitor;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Generalized Black-Scholes-Merton option pricing.
 *
 * Depending on the data supplied, the model is:
 * <ul>
 * <li>$b=r$ Black-Scholes stock option pricing model
 * <li>$b=r-q$ Merton stock option model with continuous dividend yield $q$
 * <li>$b=0$ Black future option model
 * <li>$b=0, r=0$ Asay margined future option model
 * <li>$b=r-r_f$ Garman-Kohlhagen FX option model, with foreign risk-free rate $r_f$.
 * </ul>
 *
 */
public class BlackScholesMertonModel extends AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Returns a visitor that calculates the greeks analytically.
   *
   * @param pricingFunction
   *          The pricing function, not null
   * @param data
   *          The data, not null
   * @param definition
   *          The option definition, not null
   * @return A visitor that calculates BSM greeks analytically.
   */
  @Override
  public GreekVisitor<Double> getGreekVisitor(final Function1D<StandardOptionDataBundle, Double> pricingFunction,
      final StandardOptionDataBundle data,
      final OptionDefinition definition) {
    Validate.notNull(pricingFunction);
    Validate.notNull(data);
    Validate.notNull(definition);
    return new BlackScholesMertonGreekVisitor(data, pricingFunction, definition);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        final ZonedDateTime date = data.getDate();
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(date);
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();

        if (s == 0) {
          return definition.isCall() ? 0 : Math.exp(-r * t) * k;
        }

        final double sigma = data.getVolatility(t, k);
        final double d1 = getD1(s, k, t, sigma, b);
        final double d2 = getD2(d1, sigma, t);
        final int sign = definition.isCall() ? 1 : -1;
        return sign * Math.exp(-r * t) * (s * Math.exp(b * t) * NORMAL.getCDF(sign * d1) - k * NORMAL.getCDF(sign * d2));
      }
    };
    return pricingFunction;
  }

  /**
   * Greek visitor for this class. Analytic solutions for the greeks are used.
   */
  @SuppressWarnings("synthetic-access")
  protected class BlackScholesMertonGreekVisitor
      extends AnalyticOptionModelFiniteDifferenceGreekVisitor<StandardOptionDataBundle, OptionDefinition> {
    private final double _s;
    private final double _k;
    private final double _sigma;
    private final double _t;
    private final double _b;
    private final double _r;
    private final boolean _isCall;
    private final double _df;
    private final double _d1;
    private final double _d2;
    private final double _price;

    /**
     * @param data
     *          The data, not null
     * @param pricingFunction
     *          The pricing function, not null
     * @param definition
     *          The option definition, not null
     */
    public BlackScholesMertonGreekVisitor(final StandardOptionDataBundle data,
        final Function1D<StandardOptionDataBundle, Double> pricingFunction,
        final OptionDefinition definition) {
      super(pricingFunction, data, definition);
      _s = data.getSpot();
      _k = definition.getStrike();
      _t = definition.getTimeToExpiry(data.getDate());
      _r = data.getInterestRate(_t);
      _sigma = data.getVolatility(_t, _k);
      _b = data.getCostOfCarry();
      _isCall = definition.isCall();
      _df = getDF(_r, _b, _t);
      _d1 = getD1(_s, _k, _t, _sigma, _b);
      _d2 = getD2(_d1, _sigma, _t);
      _price = pricingFunction.apply(data);
    }

    @Override
    public Double visitCarryRho() {
      final int sign = _isCall ? 1 : -1;
      final double value = sign * _t * _s * _df * NORMAL.getCDF(sign * _d1);
      return value;
    }

    @Override
    public Double visitDelta() {
      final double value = _df * (_isCall ? NORMAL.getCDF(_d1) : NORMAL.getCDF(_d1) - 1);
      return value;
    }

    @Override
    public Double visitDeltaBleed() {
      final int sign = _isCall ? 1 : -1;
      final double value = -_df
          * (NORMAL.getPDF(_d1) * (_b / (_sigma * Math.sqrt(_t)) - _d2 / (2 * _t)) + sign * (_b - _r) * NORMAL.getCDF(sign * _d1));
      return value;
    }

    @Override
    public Double visitDriftlessTheta() {
      final double value = -_s * NORMAL.getPDF(_d1) * _sigma / (2 * Math.sqrt(_t));
      return value;
    }

    @Override
    public Double visitDVannaDVol() {
      final double value = visitVanna() * (_d1 * _d2 - _d1 / _d2 - 1) / _sigma;
      return value;
    }

    @Override
    public Double visitDZetaDVol() {
      final double value = (_isCall ? -1 : 1) * NORMAL.getPDF(_d2) * _d1 / _sigma;
      return value;
    }

    @Override
    public Double visitElasticity() {
      final double value = _df * (_isCall ? NORMAL.getCDF(_d1) : NORMAL.getCDF(_d1) - 1) * _s / _price;
      return value;
    }

    @Override
    public Double visitGamma() {
      if (_s == 0) {
        return 0.0;
      }
      final double value = _df * NORMAL.getPDF(_d1) / (_s * _sigma * Math.sqrt(_t));
      return value;
    }

    @Override
    public Double visitGammaBleed() {
      final double value = visitGamma() * (_r - _b + _b * _d1 / (_sigma * Math.sqrt(_t)) + (1 - _d1 * _d2) / (2 * _t));
      return value;
    }

    @Override
    public Double visitGammaP() {
      return visitGamma() * _s / 100;
    }

    @Override
    public Double visitGammaPBleed() {
      final double value = visitGammaP() * (_r - _b + _b * _d1 / (_sigma * Math.sqrt(_t)) + (1 - _d1 * _d2) / (2 * _t));
      return value;
    }

    @Override
    public Double visitPhi() {
      final int sign = _isCall ? 1 : -1;
      final double value = -sign * _t * _s * _df * NORMAL.getCDF(_d1 * sign);
      return value;
    }

    @Override
    public Double visitPrice() {
      return _price;
    }

    @Override
    public Double visitRho() {
      final int sign = _isCall ? 1 : -1;
      final double value = sign * _t * _k * Math.exp(-_r * _t) * NORMAL.getCDF(sign * _d2);
      return value;
    }

    @Override
    public Double visitSpeed() {
      final double value = -visitGamma() * (1 + _d1 / (_sigma * Math.sqrt(_t))) / _s;
      return value;
    }

    @Override
    public Double visitSpeedP() {
      final double value = -visitGamma() * _d1 / (100 * _sigma * Math.sqrt(_t));
      return value;
    }

    @Override
    public Double visitStrikeDelta() {
      final int sign = _isCall ? 1 : -1;
      final double value = -sign * Math.exp(-_r * _t) * NORMAL.getCDF(sign * _d2);
      return value;
    }

    @Override
    public Double visitStrikeGamma() {
      final double value = NORMAL.getPDF(_d2) * Math.exp(-_r * _t) / (_k * _sigma * Math.sqrt(_t));
      return value;
    }

    @Override
    public Double visitTheta() {
      final int sign = _isCall ? 1 : -1;
      final double value = -_s * _df * NORMAL.getPDF(_d1) * _sigma / (2 * Math.sqrt(_t))
          - sign * (_b - _r) * _s * _df * NORMAL.getCDF(sign * _d1)
          - sign * _r * _k * Math.exp(-_r * _t)
              * NORMAL.getCDF(sign * _d2);
      return value;
    }

    @Override
    public Double visitVanna() {
      final double value = -_df * _d2 * NORMAL.getPDF(_d1) / _sigma;
      return value;
    }

    @Override
    public Double visitVarianceUltima() {
      final double value = _s * _df * Math.sqrt(_t) / (8 * Math.pow(_sigma, 5)) * NORMAL.getPDF(_d1)
          * ((_d1 * _d2 - 1) * (_d1 * _d2 - 3) - (_d1 * _d1 + _d2 * _d2));
      return value;
    }

    @Override
    public Double visitVarianceVanna() {
      final double value = -_s * _df * NORMAL.getPDF(_d1) * _d2 / (2 * _sigma * _sigma);
      return value;
    }

    @Override
    public Double visitVarianceVega() {
      final double value = _s * _df * NORMAL.getPDF(_d1) * Math.sqrt(_t) / (2 * _sigma);
      return value;
    }

    @Override
    public Double visitVarianceVomma() {
      final double value = _s * _df * Math.sqrt(_t) / (4 * Math.pow(_sigma, 3)) * NORMAL.getPDF(_d1) * (_d1 * _d2 - 1);
      return value;
    }

    @Override
    public Double visitVega() {
      final double value = _s * _df * NORMAL.getPDF(_d1) * Math.sqrt(_t);
      return value;
    }

    @Override
    public Double visitVegaBleed() {
      final double value = visitVega() * (_r - _b + _b * _d1 / (_sigma * Math.sqrt(_t)) - (1 + _d1 * _d2) / (2 * _t));
      return value;
    }

    @Override
    public Double visitVegaP() {
      final double value = visitVega() * _sigma / 10;
      return value;
    }

    @Override
    public Double visitUltima() {
      final double value = visitVomma() * (_d1 * _d2 - _d1 / _d2 - _d2 / _d1 - 1) / _sigma;
      return value;
    }

    @Override
    public Double visitVomma() {
      final double value = visitVega() * _d1 * _d2 / _sigma;
      return value;
    }

    @Override
    public Double visitVommaP() {
      final double value = visitVegaP() * _d1 * _d2 / _sigma;
      return value;
    }

    @Override
    public Double visitZeta() {
      final double value = NORMAL.getCDF(_isCall ? _d2 : -_d2);
      return value;
    }

    @Override
    public Double visitZetaBleed() {
      final double value = (_isCall ? 1 : -1) * NORMAL.getPDF(_d2) * (_b / (_sigma * Math.sqrt(_t)) - _d1 / (2 * _t));
      return value;
    }

    @Override
    public Double visitZomma() {
      final double value = visitGamma() * (_d1 * _d2 - 1) / _sigma;
      return value;
    }

    @Override
    public Double visitZommaP() {
      final double value = visitGammaP() * (_d1 * _d2 - 1) / _sigma;
      return value;
    }
  }
}
