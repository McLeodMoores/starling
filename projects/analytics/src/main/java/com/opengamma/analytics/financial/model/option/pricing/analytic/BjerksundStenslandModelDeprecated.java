/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Class defining an analytical approximation for American option prices as
 * derived by Bjerksund and Stensland (2002).
 *
 * @deprecated Use {@link BjerksundStenslandModel} instead.
 */
@Deprecated
public class BjerksundStenslandModelDeprecated extends AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> {

  private static final BjerksundStenslandModel MODEL = new BjerksundStenslandModel();

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final AmericanVanillaOptionDefinition definition) {
    ArgumentChecker.notNull(definition, "definition");
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        ArgumentChecker.notNull(data, "data");
        final ZonedDateTime date = data.getDate();
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(date);
        final double sigma = data.getVolatility(t, k);
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        if (!definition.isCall()) {
          if (s == 0) {
            return k;
          }
          return price(s, k, r, b, t, sigma, false);
        }
        return getCallPrice(s, k, r, b, t, sigma);
      }
    };
    return pricingFunction;
  }

  /**
   * Get the price of an American option by the Bjerksund and Stensland (2002) approximation.
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param sigma The volatility
   * @param isCall true for calls
   * @return The American option price
   */
  public double price(final double s0, final double k, final double r, final double b, final double t, final double sigma, final boolean isCall) {
    return MODEL.price(s0, k, r, b, t, sigma, isCall);
  }

  /**
   * Get the price of an American call option by the Bjerksund and Stensland (2002) approximation.
   * @param s0 The spot
   * @param k The strike
   * @param r The risk-free rate
   * @param b The cost-of-carry
   * @param t The time-to-expiry
   * @param sigma The volatility
   * @return The American option price
   */
  protected double getCallPrice(final double s0, final double k, final double r, final double b, final double t, final double sigma) {
    //minimum price condition is imposed in the end of the computation
    return MODEL.price(s0, k, r, b, t, sigma, true);
  }

  protected double getPhi(final double s, final double t, final double gamma, final double h, final double x, final double r, final double b, final double sigma) {
    return MODEL.getPhi(s, t, gamma, h, x, r, b, sigma);
  }

  protected double getPsi(final double s, final double t1, final double t2, final double gamma, final double h, final double x2, final double x1, final double r, final double b, final double sigma) {
    return MODEL.getPsi(s, t1, t2, gamma, h, x2, x1, r, b, sigma);
  }

  //**************
  //adjoint stuff

  /**
   * get the price and all the first order Greeks (i.e. delta (spot), dual-delta (strike), rho (risk-free rate), b-rho (cost-of-carry), theta (expiry), vega
   * (sigma)) of an American option with the Bjerksund &amp; Stensland (2002) approximation
   *
   * @param s0
   *          The spot
   * @param k
   *          The strike
   * @param r
   *          The risk-free rate
   * @param b
   *          The cost-of-carry
   * @param t
   *          The time-to-expiry
   * @param sigma
   *          The volatility
   * @param isCall
   *          true for calls
   * @return length 7 arrays containing the price, then the sensitivities (Greeks): delta (spot), dual-delta (strike), rho (risk-free rate), b-rho
   *         (cost-of-carry), theta (expiry), vega (sigma)
   */
  public double[] getPriceAdjoint(final double s0, final double k, final double r, final double b, final double t, final double sigma, final boolean isCall) {
    return MODEL.getPriceAdjoint(s0, k, r, b, t, sigma, isCall);
  }

  /**
   * Get the option price, plus its delta and gamma. <b>Note</b> if a put is required, the gamma is found by divided difference on the delta. For a call both
   * delta and gamma are found by Algorithmic Differentiation.
   *
   * @param s0
   *          The spot
   * @param k
   *          The strike
   * @param r
   *          The risk-free rate
   * @param b
   *          The cost-of-carry
   * @param t
   *          The time-to-expiry
   * @param sigma
   *          The volatility
   * @param isCall
   *          true for calls
   * @return length 3 array of price, delta and gamma
   */
  public double[] getPriceDeltaGamma(final double s0, final double k, final double r, final double b, final double t, final double sigma, final boolean isCall) {
    return MODEL.getPriceDeltaGamma(s0, k, r, b, t, sigma, isCall);
  }

  /**
   * Get the price and vega of an American option by the Bjerksund &amp; Stensland (2002) approximation
   *
   * @param s0
   *          The spot
   * @param k
   *          The strike
   * @param r
   *          The risk-free rate
   * @param b
   *          The cost-of-carry
   * @param t
   *          The time-to-expiry
   * @param sigma
   *          The volatility
   * @param isCall
   *          true for calls
   * @return length 2 arrays containing the price and vega
   */
  public double[] getPriceAndVega(final double s0, final double k, final double r, final double b, final double t, final double sigma, final boolean isCall) {
    return MODEL.getPriceAndVega(s0, k, r, b, t, sigma, isCall);
  }

  /**
   * Get a function for the price and vega of an American option by the Bjerksund &amp; Stensland (2002) approximation in terms of the volatility (sigma). This
   * is primarily used by the GenericImpliedVolatiltySolver to find a (Bjerksund &amp; Stensland) implied volatility for a given market price of an American
   * option
   *
   * @param s0
   *          The spot
   * @param k
   *          The strike
   * @param r
   *          The risk-free rate
   * @param b
   *          The cost-of-carry
   * @param t
   *          The time-to-expiry
   * @param isCall
   *          true for calls
   * @return A function from volatility (sigma) to price and vega
   */
  public Function1D<Double, double[]> getPriceAndVegaFunction(final double s0, final double k, final double r, final double b, final double t, final boolean isCall) {
    return MODEL.getPriceAndVegaFunction(s0, k, r, b, t, isCall);
  }

  /**
   * Get the implied volatility according to the Bjerksund &amp; Stensland (2002) approximation for the price of an American option quoted in the market. It is
   * the number that put into the Bjerksund &amp; Stensland (2002) approximation gives the market price. <b>This is not the same as the Black implied
   * volatility</b> (which is only applicable to European options), although it may be numerically close.
   *
   * @param price
   *          The market price of an American option
   * @param s0
   *          The spot
   * @param k
   *          The strike
   * @param r
   *          The risk-free rate
   * @param b
   *          The cost-of-carry
   * @param t
   *          The time-to-expiry
   * @param isCall
   *          true for calls
   * @return The (Bjerksund &amp; Stensland (2002)) implied volatility.
   */
  public double impliedVolatility(final double price, final double s0, final double k, final double r, final double b, final double t, final boolean isCall) {
    return MODEL.impliedVolatility(price, s0, k, r, b, t, isCall);
  }

  protected double[] getCallPriceAdjoint(final double s0, final double k, final double r, final double b, final double t, final double sigma) {
    return MODEL.getCallPriceAdjoint(s0, k, r, b, t, sigma);
  }

  final double[] getPutPriceAdjoint(final double s0, final double k, final double r, final double b, final double t, final double sigma) {
    return MODEL.getPutPriceAdjoint(s0, k, r, b, t, sigma);
  }

  protected double[] getCallDeltaGamma(final double s0, final double k, final double r, final double b, final double t, final double sigma) {
    return MODEL.getCallDeltaGamma(s0, k, r, b, t, sigma);
  }

  //TODO Have an AD version
  protected double[] getPutDeltaGamma(final double s0, final double k, final double r, final double b, final double t, final double sigma) {
    return MODEL.getPutDeltaGamma(s0, k, r, b, t, sigma);
  }

  /**
   * get alpha and its sensitivity to k, x (I) and beta
   * @param k The strike
   * @param x x
   * @param beta beta
   * @return The adjoints of alpha
   */
  protected double[] getAlphaAdjoint(final double k, final double x, final double beta) {
    return MODEL.getAlphaAdjoint(k, x, beta);
  }

  /**
   * Get lambda and its sensitivity to gamma, r, b and sigma-squared
   * @param gamma gamma
   * @param r the interest rate
   * @param b the cost-of-carry
   * @param sigmaSq volatility squared
   * @return length 5 array of lambda and its sensitivity to gamma, r, b and sigma-squared
   */
  protected double[] getLambdaAdjoint(final double gamma, final double r, final double b, final double sigmaSq) {
    return MODEL.getLambdaAdjoint(gamma, r, b, sigmaSq);
  }

  /**
   * Get kappa and its sensitivity to gamma, b and sigma-squared
   * @param gamma gamma
   * @param b cost-of-carry
   * @param sigmaSq volatility squared
   * @return length 4 array of kappa and its sensitivity to gamma, b and sigma-squared
   */
  protected double[] getKappaAdjoint(final double gamma, final double b, final double sigmaSq) {
    return MODEL.getKappaAdjoint(gamma, b, sigmaSq);
  }

  /**
   * get phi and its sensitivity to s, t, gamma, h, x (I), r, b &amp; sigma
   * 
   * @param s
   *          the strike
   * @param t
   *          the time to expiry
   * @param gamma
   *          gamma If this is set to 0 or 1, then the gamma sensitivity should be ignored
   * @param h
   *          h
   * @param x
   *          x
   * @param r
   *          the interest rate
   * @param b
   *          the cost-of-carry
   * @param sigma
   *          The volatility
   * @return length 9 array of phi and its sensitivity to s, t, gamma, h, x (I), r, b &amp; sigma
   */
  protected double[] getPhiAdjoint(final double s, final double t, final double gamma, final double h, final double x, final double r, final double b,
      final double sigma) {

    return MODEL.getPhiAdjoint(s, t, gamma, h, x, r, b, sigma);
  }

  /**
   * @param s The spot
   * @param t The time to expiry
   * @param gamma gamma
   * @param h h
   * @param x x
   * @param r The interest rate
   * @param b The cost-of-carry
   * @param sigma The volatility
   * @return The phi delta array
   */
  protected double[] getPhiDelta(final double s, final double t, final double gamma, final double h, final double x, final double r, final double b,
      final double sigma) {

    return MODEL.getPhiDelta(s, t, gamma, h, x, r, b, sigma);
  }

  /**
   * get Psi and its sensitivity to s, t, gamma, h, x2, x1, r, b and sigma
   * @param s The spot
   * @param t The time to expiry
   * @param gamma gamma
   * @param h h
   * @param x2 x2
   * @param x1 x1
   * @param r The interest rate
   * @param b The cost-of-carry
   * @param sigma The volatility
   * @return array of length 10 of Psi and its sensitivity to s, t, gamma, h, x2, x1, r, b and sigma
   */
  protected double[] getPsiAdjoint(final double s, final double t, final double gamma, final double h, final double x2, final double x1,
      final double r, final double b, final double sigma) {

    return MODEL.getPsiAdjoint(s, t, gamma, h, x2, x1, r, b, sigma);
  }

  /**
   * @param s The spot
   * @param t The time to expiry
   * @param gamma gamma
   * @param h h
   * @param x2 x2
   * @param x1 x1
   * @param r The interest rate
   * @param b The cost-of-carry
   * @param sigma The volatility
   * @return The array of psi delta
   */
  protected double[] getPsiDelta(final double s, final double t, final double gamma, final double h, final double x2, final double x1,
      final double r, final double b, final double sigma) {

    return MODEL.getPsiDelta(s, t, gamma, h, x2, x1, r, b, sigma);
  }

  /**
   * Get the first and second derivatives of the bi-variate normal with respect to a and b (rho is fixed)
   * @param a first coordinate
   * @param b second coordinate
   * @param posRho true if RHO used, false is -RHO used
   * @return array of length 5 in order dB/da, dB/db, d^2B/da^2, d^2B/db^2, d^2B/dadb
   */
  protected double[] bivariateNormDiv(final double a, final double b, final boolean posRho) {
    return MODEL.bivariateNormDiv(a, b, posRho);
  }

  /**
   * Get beta and its sensitivity to r, b and sigma-squared
   * @param r The interest rate
   * @param b The cost-of-carry
   * @param sigmaSq The volatility squared
   * @return length 4 array of beta and its sensitivity to r, b and sigma-squared
   */
  protected double[] getBetaAdjoint(final double r, final double b, final double sigmaSq) {
    return MODEL.getBetaAdjoint(r, b, sigmaSq);
  }

  /**
   * get I1 and its sensitivity to k, r, b, sigma &amp; t
   * 
   * @param k
   *          The strike
   * @param r
   *          The interest rate
   * @param b
   *          The cost-of-carry
   * @param sigma
   *          The volatility
   * @param t
   *          The time to expiry
   * @return length 6 array of I1 and its sensitivity to k, r, b, sigma &amp; t
   */
  protected double[] getI1Adjoint(final double k, final double r, final double b, final double sigma, final double t) {
    return MODEL.getI1Adjoint(k, r, b, sigma, t);
  }

  /**
   * get I2 and its sensitivity to k, r, b, sigma &amp; t
   * 
   * @param k
   *          The strike
   * @param r
   *          The interest rate
   * @param b
   *          The cost-of-carry
   * @param sigma
   *          The volatility
   * @param t
   *          The time to expiry
   * @return length 6 array of I2 and its sensitivity to k, r, b, sigma &amp; t
   */
  protected double[] getI2Adjoint(final double k, final double r, final double b, final double sigma, final double t) {
    return MODEL.getI2Adjoint(k, r, b, sigma, t);
  }

}
