/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.financial.model.finitedifference.CoupledFiniteDifference;
import com.opengamma.financial.model.finitedifference.CoupledPDEDataBundle;
import com.opengamma.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.finitedifference.PDEResults1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurface;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class TwoStateMarkovChainWithLocalVolDensity {

  private final static double THETA = 0.55;

  private final CoupledPDEDataBundle _data1;
  private final CoupledPDEDataBundle _data2;

  /**
   * @param forward
   * @param vol1
   * @param deltaVol
   * @param lambda12
   * @param lambda21
   * @param probS1
   * @param beta1
   * @param beta2
   */
  public TwoStateMarkovChainWithLocalVolDensity(ForwardCurve forward, final TwoStateMarkovChainDataBundle data,
      LocalVolatilitySurface localVol) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");
    Validate.notNull(localVol, "null localVol");
    _data1 = getCoupledPDEDataBundle(forward, data.getVol1(), data.getLambda12(), data.getLambda21(), data.getP0(), data.getBeta1(), localVol);
    _data2 = getCoupledPDEDataBundle(forward, data.getVol2(), data.getLambda21(), data.getLambda12(), 1.0 - data.getP0(), data.getBeta2(), localVol);
  }

  PDEFullResults1D[] solve(PDEGrid1D grid) {

    BoundaryCondition lower = new DirichletBoundaryCondition(0.0, 0.0);//TODO for beta < 0.5 zero is accessible and thus there will be non-zero 
    //density there
    BoundaryCondition upper = new DirichletBoundaryCondition(0.0, grid.getSpaceNode(grid.getNumSpaceNodes() - 1));

    CoupledFiniteDifference solver = new CoupledFiniteDifference(THETA, true);
    PDEResults1D[] res = solver.solve(_data1, _data2, grid, lower, upper, lower, upper, null);
    //handle this with generics  
    PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    PDEFullResults1D res2 = (PDEFullResults1D) res[1];
    return new PDEFullResults1D[] {res1, res2 };
  }

  private CoupledPDEDataBundle getCoupledPDEDataBundle(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2,
      final double initialProb, final double beta, final LocalVolatilitySurface localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double t = ts[0];
        double s = ts[1];
        double sigma = localVol.getVolatility(t, s) * vol * Math.pow(s, beta);
        return -sigma * sigma / 2;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double t = ts[0];
        double s = ts[1];
        double lvDiv = getLocalVolFirstDiv(localVol, t, s);
        double lv = localVol.getVolatility(t, s);
        return s * (forward.getDrift(t) - 2 * vol * vol * lv * Math.pow(s, 2 * beta - 2) * (s * lvDiv + lv * beta));
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double t = ts[0];
        double s = ts[1];
        double lv1Div = getLocalVolFirstDiv(localVol, t, s);
        double lv2Div = getLocalVolSecondDiv(localVol, t, s);
        double lv = localVol.getVolatility(t, s);
        double temp1 = vol * Math.pow(s, beta - 1) * (beta * lv + s * lv1Div);
        double temp2 = vol * vol * lv * Math.pow(s, 2 * (beta - 1)) * (s * s * lv2Div + 2 * beta * s * lv1Div + beta * (beta - 1) * lv);

        return lambda1 + forward.getDrift(t) - temp1 * temp1 - temp2;
      }
    };

    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(Double s) {
        if (s == 0 || initialProb == 0) {
          return 0.0;
        }
        double x = Math.log(s / forward.getSpot());
        NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return initialProb * dist.getPDF(x) / s;
      }
    };

    return new CoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda2, initialCondition);
  }

  //TODO handle with a central calculator
  private double getLocalVolFirstDiv(final LocalVolatilitySurface localVol, final double t, final double s) {
    final double eps = 1e-4;
    double up = localVol.getVolatility(t, s + eps);
    double down = localVol.getVolatility(t, s - eps);
    return (up - down) / 2 / eps;
  }

  private double getLocalVolSecondDiv(final LocalVolatilitySurface localVol, final double t, final double s) {
    final double eps = 1e-4;
    double up = localVol.getVolatility(t, s + eps);
    double mid = localVol.getVolatility(t, s);
    double down = localVol.getVolatility(t, s - eps);
    return (up + down - 2 * mid) / eps / eps;
  }

}
