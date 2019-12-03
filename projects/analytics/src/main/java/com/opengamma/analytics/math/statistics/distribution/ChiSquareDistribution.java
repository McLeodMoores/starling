/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.function.special.InverseIncompleteGammaFunction;

import cern.jet.random.ChiSquare;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * A $\chi^2$ distribution with $k$ degrees of freedom is the distribution of the sum of squares of $k$ independent standard normal random variables.
 * <p>
 * This implementation uses the CERN <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">colt</a> package for the cdf, pdf and $\chi^2$-distributed random
 * numbers.
 *
 */
public class ChiSquareDistribution implements ProbabilityDistribution<Double> {
  private final Function2D<Double, Double> _inverseFunction = new InverseIncompleteGammaFunction();
  private final ChiSquare _chiSquare;
  private final double _degrees;

  /**
   * @param degrees The degrees of freedom of the distribution, not less than one
   */
  public ChiSquareDistribution(final double degrees) {
    this(degrees, new MersenneTwister64(new Date()));
  }

  /**
   * @param degrees The degrees of freedom of the distribution, not less than one
   * @param engine A uniform random number generator, not null
   */
  public ChiSquareDistribution(final double degrees, final RandomEngine engine) {
    Validate.isTrue(degrees >= 1, "Degrees of freedom must be greater than or equal to one");
    Validate.notNull(engine);
    _chiSquare = new ChiSquare(degrees, engine);
    _degrees = degrees;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return _chiSquare.cdf(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return _chiSquare.pdf(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getInverseCDF(final Double p) {
    Validate.notNull(p);
    Validate.isTrue(p >= 0 && p <= 1, "Probability must lie between 0 and 1");
    return 2 * _inverseFunction.evaluate(0.5 * _degrees, p);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    return _chiSquare.nextDouble();
  }

  /**
   * @return The number of degrees of freedom
   */
  public double getDegreesOfFreedom() {
    return _degrees;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_degrees);
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
    final ChiSquareDistribution other = (ChiSquareDistribution) obj;
    return Double.doubleToLongBits(_degrees) == Double.doubleToLongBits(other._degrees);
  }

}
