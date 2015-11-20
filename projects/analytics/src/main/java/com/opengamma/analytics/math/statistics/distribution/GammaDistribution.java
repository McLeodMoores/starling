/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import cern.jet.random.Gamma;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * The Gamma distribution is a continuous probability distribution with cdf
 * $$
 * \begin{align*}
 * F(x)=\frac{\gamma\left(k, \frac{x}{\theta}\right)}{\Gamma(k)}
 * \end{align*}
 * $$
 * and pdf
 * $$
 * \begin{align*}
 * f(x)=\frac{x^{k-1}e^{-\frac{x}{\theta}}}{\Gamma{k}\theta^k}
 * \end{align*}
 * $$
 * where $k$ is the shape parameter and $\theta$ is the scale parameter.
 * <p>
 * This implementation uses the CERN <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">colt</a> package for the cdf, pdf
 * and $\Gamma$-distributed random numbers.
 * 
 */
public class GammaDistribution implements ProbabilityDistribution<Double> {
  private final Gamma _gamma;
  private final double _k;
  private final double _theta;

  /**
   * @param k The shape parameter of the distribution, not negative or zero
   * @param theta The scale parameter of the distribution, not negative or zero
   */
  public GammaDistribution(final double k, final double theta) {
    this(k, theta, new MersenneTwister(new Date()));
  }

  /**
   * @param k The shape parameter of the distribution, not negative or zero
   * @param theta The scale parameter of the distribution, not negative or zero
   * @param engine A uniform random number generator, not null
   */
  public GammaDistribution(final double k, final double theta, final RandomEngine engine) {
    Validate.isTrue(k > 0, "k must be > 0");
    Validate.isTrue(theta > 0, "theta must be > 0");
    Validate.notNull(engine);
    _gamma = new Gamma(k, 1. / theta, engine);
    _k = k;
    _theta = theta;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return _gamma.cdf(x);
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws NotImplementedException
   */
  @Override
  public double getInverseCDF(final Double p) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return _gamma.pdf(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    return _gamma.nextDouble();
  }

  /**
   * @return The shape parameter
   */
  public double getK() {
    return _k;
  }

  /**
   * @return The location parameter
   */
  public double getTheta() {
    return _theta;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_k);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_theta);
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
    final GammaDistribution other = (GammaDistribution) obj;
    if (Double.doubleToLongBits(_k) != Double.doubleToLongBits(other._k)) {
      return false;
    }
    return Double.doubleToLongBits(_theta) == Double.doubleToLongBits(other._theta);
  }

}
