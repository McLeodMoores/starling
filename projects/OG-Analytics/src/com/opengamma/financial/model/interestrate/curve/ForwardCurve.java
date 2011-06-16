/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import org.apache.commons.lang.Validate;

import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;

/**
 * 
 */
public class ForwardCurve {
  private static final RungeKuttaIntegrator1D INTEGRATOR = new RungeKuttaIntegrator1D();

  private final Curve<Double, Double> _fwdCurve;
  private final Curve<Double, Double> _drift;
  private final double _spot;

  @SuppressWarnings("unused")
  private ForwardCurve(final Curve<Double, Double> fwdCurve, final Curve<Double, Double> driftCurve) {
    Validate.notNull(fwdCurve, "null fwdCurve");
    Validate.notNull(driftCurve, "null driftCurve");
    _fwdCurve = fwdCurve;
    _drift = driftCurve;
    _spot = _fwdCurve.getYValue(0.0);
  }

  public ForwardCurve(final Curve<Double, Double> fwdCurve) {
    Validate.notNull(fwdCurve, "curve");
    _fwdCurve = fwdCurve;

    Function1D<Double, Double> drift = new Function1D<Double, Double>() {
      private final double _eps = 1e-5;

      @Override
      public Double evaluate(Double t) {

        double mid = _fwdCurve.getYValue(t);
        double up = _fwdCurve.getYValue(t + _eps);

        if (t < _eps) {
          return (up - mid) / mid / _eps;
        } else {
          double down = _fwdCurve.getYValue(t - _eps);
          return (up - down) / mid / 2 / _eps;
        }
      }
    };

    _drift = FunctionalDoublesCurve.from(drift);
    _spot = _fwdCurve.getYValue(0.0);
  }

  /**
   * Forward curve with zero drift (i.e. curve is constant)
   * @param spot
   */
  public ForwardCurve(final double spot) {
    _fwdCurve = ConstantDoublesCurve.from(spot);
    _drift = ConstantDoublesCurve.from(0.0);
    _spot = spot;
  }

  /**
   * Forward curve with constant drift 
   * @param spot 
   * @param drift
   */
  public ForwardCurve(final double spot, final double drift) {
    _drift = ConstantDoublesCurve.from(drift);
    Function1D<Double, Double> fwd = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        return spot * Math.exp(drift * t);
      }
    };
    _fwdCurve = new FunctionalDoublesCurve(fwd);
    _spot = spot;
  }

  /**
   * Forward curve with functional drift.
   * <b>Warning</b> This will be slow if you want access to the forward at many times 
   * @param spot
   * @param driftCurve
   */
  public ForwardCurve(final double spot, final Curve<Double, Double> driftCurve) {
    Validate.notNull(driftCurve, "null driftCurve");
    _drift = driftCurve;
    final Function1D<Double, Double> driftFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        return driftCurve.getYValue(t);
      }
    };

    //TODO cache integration results 
    Function1D<Double, Double> fwd = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        double temp = INTEGRATOR.integrate(driftFunc, 0.0, t);
        return spot * Math.exp(temp);
      }
    };
    _fwdCurve = new FunctionalDoublesCurve(fwd);
    _spot = spot;
  }

  public ForwardCurve(final Function1D<Double, Double> func) {
    this(FunctionalDoublesCurve.from(func));
  }

  public Curve<Double, Double> getForwardCurve() {
    return _fwdCurve;
  }

  public double getForward(final double t) {
    return _fwdCurve.getYValue(t);
  }

  /**
   * Gets the drift.
   * @return the drift
   */
  public Curve<Double, Double> getDriftCurve() {
    return _drift;
  }

  public double getDrift(final double t) {
    return _drift.getYValue(t);
  }

  public double getSpot() {
    return _spot;
  }

}
