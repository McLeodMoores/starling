/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.differentiation;

import java.util.function.Function;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Differentiates a scalar function with respect to its argument using finite difference.
 * <p>
 * For a function $y = f(x)$ where $x$ and $y$ are scalars, this class produces a gradient function $g(x)$, i.e. a function that returns the gradient for each
 * point $x$, where $g$ is the scalar $\frac{dy}{dx}$.
 */
public class ScalarFirstOrderDifferentiator implements Differentiator<Double, Double, Double> {
  private static final double DEFAULT_EPS = 1e-5;
  private static final double MIN_EPS = Math.sqrt(Double.MIN_NORMAL);
  private static final FiniteDifferenceType DIFF_TYPE = FiniteDifferenceType.CENTRAL;

  private final double _eps;
  private final double _twoEps;
  private final FiniteDifferenceType _differenceType;

  /**
   * Uses the default values of differencing type (central) and eps (10<sup>-5</sup>).
   */
  public ScalarFirstOrderDifferentiator() {
    this(DIFF_TYPE, DEFAULT_EPS);
  }

  /**
   * Uses the default value of eps (10<sup>-5</sup>).
   *
   * @param differenceType
   *          The differencing type to be used in calculating the gradient function
   */
  public ScalarFirstOrderDifferentiator(final FiniteDifferenceType differenceType) {
    this(differenceType, DEFAULT_EPS);
  }

  /**
   * @param differenceType
   *          {@link FiniteDifferenceType#FORWARD}, {@link FiniteDifferenceType#BACKWARD}, or {@link FiniteDifferenceType#CENTRAL}. In most situations,
   *          {@link FiniteDifferenceType#CENTRAL} is preferable. Not null
   * @param eps
   *          The step size used to approximate the derivative. If this value is too small, the result will most likely be dominated by noise. Use around
   *          10<sup>5</sup> times the domain size.
   */
  public ScalarFirstOrderDifferentiator(final FiniteDifferenceType differenceType, final double eps) {
    Validate.notNull(differenceType);
    if (eps < MIN_EPS) {
      throw new IllegalArgumentException("eps is too small. A good value is 1e-5*size of domain. The minimum value is " + MIN_EPS);
    }
    _differenceType = differenceType;
    _eps = eps;
    _twoEps = 2 * _eps;
  }

  @Override
  public Function1D<Double, Double> differentiate(final Function<Double, Double> function) {
    Validate.notNull(function);
    switch (_differenceType) {
      case FORWARD:
        return new Function1D<Double, Double>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public Double apply(final Double x) {
            Validate.notNull(x, "x");
            return (function.apply(x + _eps) - function.apply(x)) / _eps;
          }
        };
      case CENTRAL:
        return new Function1D<Double, Double>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public Double apply(final Double x) {
            Validate.notNull(x, "x");
            return (function.apply(x + _eps) - function.apply(x - _eps)) / _twoEps;
          }
        };
      case BACKWARD:
        return new Function1D<Double, Double>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public Double apply(final Double x) {
            Validate.notNull(x, "x");
            return (function.apply(x) - function.apply(x - _eps)) / _eps;
          }
        };
      default:
        throw new IllegalArgumentException("Can only handle forward, backward and central differencing");
    }
  }

  @Override
  public Function1D<Double, Double> differentiate(final Function<Double, Double> function, final Function<Double, Boolean> domain) {
    Validate.notNull(function);
    Validate.notNull(domain);

    final double[] wFwd = new double[] { -3. / _twoEps, 4. / _twoEps, -1. / _twoEps };
    final double[] wCent = new double[] { -1. / _twoEps, 0., 1. / _twoEps };
    final double[] wBack = new double[] { 1. / _twoEps, -4. / _twoEps, 3. / _twoEps };

    return new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double apply(final Double x) {
        Validate.notNull(x, "x");
        ArgumentChecker.isTrue(domain.apply(x), "point {} is not in the function domain", x.toString());

        final double[] y = new double[3];
        double[] w;

        if (!domain.apply(x + _eps)) {
          if (!domain.apply(x - _eps)) {
            throw new MathException("cannot get derivative at point " + x.toString());
          }
          y[0] = function.apply(x - _twoEps);
          y[1] = function.apply(x - _eps);
          y[2] = function.apply(x);
          w = wBack;
        } else {
          if (!domain.apply(x - _eps)) {
            y[0] = function.apply(x);
            y[1] = function.apply(x + _eps);
            y[2] = function.apply(x + _twoEps);
            w = wFwd;
          } else {
            y[0] = function.apply(x - _eps);
            y[2] = function.apply(x + _eps);
            w = wCent;
          }
        }

        double res = y[0] * w[0] + y[2] * w[2];
        if (w[1] != 0) {
          res += y[1] * w[1];
        }
        return res;
      }
    };
  }
}
