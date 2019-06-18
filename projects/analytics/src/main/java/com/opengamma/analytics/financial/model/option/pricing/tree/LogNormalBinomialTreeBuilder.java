/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.GeneralLogNormalOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Builds a binomial tree where the nodes are set to locally match a log-normal process. The process that the tree is emulating is of the form df/f = mu(f,t)dt
 * + sigma(f,t)dw. From a node at (f,t) the two daughter nodes f+ and f- (at time t + dt) are set such that p*(1-p)*(ln(f+/f-))^2 = dt*sigma(f,t)^2, where p is
 * the probability of reaching f+ from f. The forwarding condition is p*f+ + (1-p)*f- = f*exp(mu(f,t)*dt). This is adapted from the paper Derman and Kani, The
 * Volatility Smile and Its Implied Tree
 * 
 * @param <T>
 *          A GeneralLogNormalOptionDataBundle or anything that extends it
 */
public class LogNormalBinomialTreeBuilder<T extends GeneralLogNormalOptionDataBundle> extends BinomialTreeBuilder<T> {

  private static final double EPS = 1e-8;
  private static final RealSingleRootFinder ROOT = new BrentSingleRootFinder();
  private static final BracketRoot BRACKET_ROOT = new BracketRoot();

  @Override
  protected double[] getForwards(final double[] spots, final T data, final double t, final double dt) {
    final int n = spots.length;
    final double[] forwards = new double[n];
    for (int i = 0; i < n; i++) {
      final double drift = data.getLocalDrift(spots[i], t);
      forwards[i] = spots[i] * Math.exp(drift * dt);
    }
    return forwards;
  }

  @Override
  protected DoublesPair getCentralNodePair(final double dt, final double sigma, final double forward, final double centreLevel) {

    final Function1D<Double, Double> func = new CentreNode(dt, sigma, forward, centreLevel);
    final double[] limits = BRACKET_ROOT.getBracketedPoints(func, forward, forward * Math.exp(sigma * Math.sqrt(dt)));

    final double upper = ROOT.getRoot(func, limits[0], limits[1]);
    final double lower = centreLevel * centreLevel / upper;
    return DoublesPair.of(lower, upper);
  }

  @Override
  protected double getNextHigherNode(final double dt, final double sigma, final double forward, final double lowerNode) {
    final Function1D<Double, Double> func = new UpperNodes(dt, sigma, forward, lowerNode);
    final double fTry = forward * Math.exp(sigma * Math.sqrt(dt));
    // ensure we do not get p = 1 and thus a divide by zero
    final double[] limits = BRACKET_ROOT.getBracketedPoints(func, (forward - lowerNode) / 0.6 + lowerNode, (forward - lowerNode) / 0.4 + lowerNode,
        forward * (1 + EPS), 10 * fTry);
    return ROOT.getRoot(func, limits[0], limits[1]);
  }

  @Override
  protected double getNextLowerNode(final double dt, final double sigma, final double forward, final double higherNode) {
    if (forward == 0.0) {
      return 0.0;
    }
    final Function1D<Double, Double> func = new LowerNodes(dt, sigma, forward, higherNode);
    final double[] limits = BRACKET_ROOT.getBracketedPoints(func, forward * Math.exp(-sigma * Math.sqrt(dt)), forward);
    return ROOT.getRoot(func, limits[0], limits[1]);
  }

  /**
   * The root of this function gives the next node above the currently know one
   */
  private class UpperNodes extends Function1D<Double, Double> {

    private final double _rootdt;
    private final double _sigma;
    private final double _f;
    private final double _s;

    UpperNodes(final double dt, final double sigma, final double forward, final double s) {
      _rootdt = Math.sqrt(dt);
      _sigma = sigma;
      _f = forward;
      _s = s;
    }

    @Override
    public Double evaluate(final Double x) {
      final double p = (_f - _s) / (x - _s);

      final double res = _s * Math.exp(_rootdt * _sigma / Math.sqrt(p * (1 - p))) - x;
      return res;
    }
  }

  private class LowerNodes extends Function1D<Double, Double> {

    private final double _rootdt;
    private final double _sigma;
    private final double _f;
    private final double _s;

    LowerNodes(final double dt, final double sigma, final double forward, final double s) {
      _rootdt = Math.sqrt(dt);
      _sigma = sigma;
      _f = forward;
      _s = s;
    }

    @Override
    public Double evaluate(final Double x) {
      final double p = (_f - x) / (_s - x);
      final double res = _s * Math.exp(-_rootdt * _sigma / Math.sqrt(p * (1 - p))) - x;
      return res;
    }
  }

  private class CentreNode extends Function1D<Double, Double> {

    private final double _rootdt;
    private final double _sigma;
    private final double _f;
    private final double _spot;

    CentreNode(final double dt, final double sigma, final double forward, final double spot) {
      _rootdt = Math.sqrt(dt);
      _sigma = sigma;
      _f = forward;
      _spot = spot;
    }

    @Override
    public Double evaluate(final Double x) {
      double p;
      if (_f == _spot) {
        p = _f / (x + _f);
      } else {
        Validate.isTrue(x != _spot, "invalide x");
        p = (x * _f - _spot * _spot) / (x * x - _spot * _spot);
      }
      final double res = _spot * _spot * Math.exp(_rootdt * _sigma / Math.sqrt(p * (1 - p))) - x * x;
      return res;
    }
  }

}
