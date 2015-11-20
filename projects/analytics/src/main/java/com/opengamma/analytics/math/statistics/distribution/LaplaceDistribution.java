/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.Validate;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * The Laplace distribution is a continuous probability distribution with probability density function
 * $$
 * \begin{align*}
 * f(x)=\frac{1}{2b}e^{-\frac{|x-\mu|}{b}}
 * \end{align*}
 * $$
 * where $\mu$ is the location parameter and $b$ is the scale parameter. The
 * cumulative distribution function and its inverse are defined as:
 * $$
 * \begin{align*}
 * F(x)&=
 * \begin{cases}
 * \frac{1}{2}e^{\frac{x-\mu}{b}} & \text{if } x < \mu\\
 * 1-\frac{1}{2}e^{-\frac{x-\mu}{b}} & \text{if } x\geq \mu
 * \end{cases}\\
 * F^{-1}(p)&=\mu-b\text{ sgn}(p-0.5)\ln(1-2|p-0.5|)
 * \end{align*}
 * $$
 * Given a uniform random variable $U$ drawn from the interval $(-\frac{1}{2}, \frac{1}{2}]$,  
 * a Laplace-distributed random variable with parameters $\mu$ and $b$ is given by:
 * $$
 * \begin{align*}
 * X=\mu-b\text{ sgn}(U)\ln(1-2|U|)
 * \end{align*}
 * $$
 * 
 */
public class LaplaceDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final RandomEngine _engine;
  private final double _mu;
  private final double _b;

  /**
   * @param mu The location parameter
   * @param b The scale parameter, greater than zero
   */
  public LaplaceDistribution(final double mu, final double b) {
    this(mu, b, new MersenneTwister64(new Date()));
  }

  /**
   * @param mu The location parameter
   * @param b The scale parameter, greater than zero
   * @param engine A uniform random number generator, not null
   */
  public LaplaceDistribution(final double mu, final double b, final RandomEngine engine) {
    Validate.isTrue(b > 0, "b must be > 0");
    Validate.notNull(engine);
    _mu = mu;
    _b = b;
    _engine = engine;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return 0.5 * (1 + Math.signum(x - _mu) * (1 - Math.exp(-Math.abs(x - _mu) / _b)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getInverseCDF(final Double p) {
    Validate.notNull(p);
    Validate.isTrue(p >= 0 && p <= 1, "Probability must lie between 0 and 1 (inclusive)");
    return _mu - _b * Math.signum(p - 0.5) * Math.log(1 - 2 * Math.abs(p - 0.5));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return Math.exp(-Math.abs(x - _mu) / _b) / (2 * _b);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    final double u = _engine.nextDouble() - 0.5;
    return _mu - _b * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
  }

  /**
   * @return The location parameter
   */
  public double getMu() {
    return _mu;
  }

  /**
   * @return The scale parameter
   */
  public double getB() {
    return _b;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_b);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_mu);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final LaplaceDistribution other = (LaplaceDistribution) obj;
    if (Double.doubleToLongBits(_b) != Double.doubleToLongBits(other._b)) {
      return false;
    }
    return Double.doubleToLongBits(_mu) == Double.doubleToLongBits(other._mu);
  }

}
