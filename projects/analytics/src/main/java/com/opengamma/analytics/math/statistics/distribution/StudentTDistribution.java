/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.special.InverseIncompleteBetaFunction;

import cern.jet.random.StudentT;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * Student's T-distribution.
 * <p>
 * This class is a wrapper for the <a href="http://acs.lbl.gov/software/colt/api/cern/jet/random/StudentT.html">Colt</a> implementation of Student's
 * T-distribution for cdf and pdf calculations and Student's T-distributed random number generation.
 */
public class StudentTDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final double _degFreedom;
  private final StudentT _dist;
  private final Function1D<Double, Double> _beta;

  /**
   * @param degFreedom The number of degrees of freedom, not negative or zero
   */
  public StudentTDistribution(final double degFreedom) {
    this(degFreedom, new MersenneTwister64(new Date()));
  }

  /**
   * @param degFreedom The number of degrees of freedom, not negative or zero
   * @param engine A generator of uniform random numbers, not null
   */
  public StudentTDistribution(final double degFreedom, final RandomEngine engine) {
    Validate.isTrue(degFreedom > 0, "degrees of freedom");
    Validate.notNull(engine);
    _degFreedom = degFreedom;
    _dist = new StudentT(degFreedom, engine);
    _beta = new InverseIncompleteBetaFunction(degFreedom / 2., 0.5);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return _dist.cdf(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return _dist.pdf(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    return _dist.nextDouble();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  // * The inverse cdf is given by:
  // * $$
  // * \begin{align*}
  // * F(P) &= \mathrm{sign}(p - \frac{1}{2})\sqrt{\frac{\nu}{x - 1}}\\
  // * x &= B(2 \min(p, 1-p))
  // * \end{align*}
  // * $$
  // * where $B$ is the inverse incomplete Beta function ({@link com.opengamma.analytics.math.function.special.InverseIncompleteBetaFunction}).
  public double getInverseCDF(final Double p) {
    Validate.notNull(p);
    Validate.isTrue(p >= 0 && p <= 1, "Probability must be >= 0 and <= 1");
    final double x = _beta.apply(2 * Math.min(p, 1 - p));
    return Math.signum(p - 0.5) * Math.sqrt(_degFreedom * (1. / x - 1));
  }

  /**
   * @return The number of degrees of freedom
   */
  public double getDegreesOfFreedom() {
    return _degFreedom;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_degFreedom);
    result = prime * result + (int) (temp ^ temp >>> 32);
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
    final StudentTDistribution other = (StudentTDistribution) obj;
    return Double.doubleToLongBits(_degFreedom) == Double.doubleToLongBits(other._degFreedom);
  }
}
