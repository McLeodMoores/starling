/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import org.apache.commons.lang.Validate;

/**
 * Transforms a single range limit.
 */
public class SingleRangeLimitTransform implements ParameterLimitsTransform {
  private static final double EXP_MAX = 50.;
  private final double _limit;
  private final int _sign;

  /**
   * @param a The limit level
   * @param limitType Type of the limit for the parameter
   */
  public SingleRangeLimitTransform(final double a, final LimitType limitType) {
    _limit = a;
    _sign = limitType == LimitType.GREATER_THAN ? 1 : -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double inverseTransform(final double y) {
    if (y > EXP_MAX) {
      return _limit + _sign * y;
    } else if (y < -EXP_MAX) {
      return _limit;
    }
    return _limit + _sign * Math.log(Math.exp(y) + 1);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws IllegalArgumentException
   *           If the value of $x$ is not consistent with the limit (e.g. the limit is $x &gt; a$ and $x$ is less than $a$
   */
  @Override
  public double transform(final double x) {
    Validate.isTrue(_sign * x >= _sign * _limit, "x not in limit");
    if (x == _limit) {
      return -EXP_MAX;
    }
    final double r = _sign * (x - _limit);
    if (r > EXP_MAX) {
      return r;
    }
    return Math.log(Math.exp(r) - 1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double inverseTransformGradient(final double y) {
    if (y > EXP_MAX) {
      return _sign;
    }
    final double temp = Math.exp(y);
    return _sign * temp / (temp + 1);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws IllegalArgumentException
   *           If the value of $x$ is not consistent with the limit (e.g. the limit is $x &gt; a$ and $x$ is less than $a$
   */
  @Override
  public double transformGradient(final double x) {
    Validate.isTrue(_sign * x >= _sign * _limit, "x not in limit");
    final double r = _sign * (x - _limit);
    if (r > EXP_MAX) {
      return 1.0;
    }
    final double temp = Math.exp(r);
    return _sign * temp / (temp - 1);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_limit);
    result = prime * result + (int) (temp ^ temp >>> 32);
    result = prime * result + _sign;
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
    final SingleRangeLimitTransform other = (SingleRangeLimitTransform) obj;
    if (Double.doubleToLongBits(_limit) != Double.doubleToLongBits(other._limit)) {
      return false;
    }
    return _sign == other._sign;
  }

}
