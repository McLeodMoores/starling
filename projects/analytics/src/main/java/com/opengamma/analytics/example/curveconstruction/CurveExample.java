/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.curveconstruction;

// @export "imports"
import java.io.PrintStream;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;

/**
 * Example for curves.
 */
public class CurveExample {

  // @export "constantDoublesCurveDemo"
  public static void constantDoublesCurveDemo(final PrintStream out) {
    final Curve<Double, Double> curve = new ConstantDoublesCurve(5.0);

    out.println(curve.getYValue(0.0));
    out.println(curve.getYValue(10.0));
    out.println(curve.getYValue(-10.0));
  }

  // @export "nodalDoublesCurveDemo"
  //    public static void nodalDoublesCurveDemo(PrintStream out) {
  //        double[] xdata = { 1.0, 2.0, 3.0 };
  //        double[] ydata = { 2.0, 4.0, 6.0 };
  //        Curve curve = new NodalDoublesCurve(xdata, ydata, true);
  //
  //        out.println(curve.getYValue(1.0));
  //        out.println(curve.getYValue(2.0));
  //        out.println(curve.getYValue(3.0));
  //
  //        try {
  //            out.println("Trying to get y value for an undefined x value...");
  //            curve.getYValue(1.5);
  //        } catch (java.lang.IllegalArgumentException  e) {
  //            out.println("IllegalArgumentException called");
  //        }
  //    }

  // @export "interpolatedDoublesCurveDemo"
  public static void interpolatedDoublesCurveDemo(final PrintStream out) {
    final double[] xdata = {1.0, 2.0, 3.0};
    final double[] ydata = {2.0, 4.0, 6.0};
    final LinearInterpolator1D interpolator = new LinearInterpolator1D();
    final Curve<Double, Double> curve = new InterpolatedDoublesCurve(xdata, ydata, interpolator, true);

    out.println(curve.getYValue(1.0));
    out.println(curve.getYValue(2.0));
    out.println(curve.getYValue(3.0));

    out.println(curve.getYValue(1.5));
    try {
      out.println("Trying to get y value for too large an x...");
      curve.getYValue(4.0);
    } catch (final java.lang.IllegalArgumentException e) {
      out.println("IllegalArgumentException called");
    }
  }

  // @export "interpolatorExtrapolatorDoublesCurveDemo"
  public static void interpolatorExtrapolatorDoublesCurveDemo(final PrintStream out) {
    final double[] xdata = {1.0, 2.0, 3.0};
    final double[] ydata = {2.0, 4.0, 6.0};

    final Interpolator1D interpolator = new LinearInterpolator1D();
    final Interpolator1D leftExtrapolator = new LinearExtrapolator1D(interpolator);
    final Interpolator1D rightExtrapolator = new LinearExtrapolator1D(interpolator);
    final Interpolator1D combined = new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);

    final Curve<Double, Double> curve = new InterpolatedDoublesCurve(xdata, ydata, combined, true);

    out.println(curve.getYValue(1.0));
    out.println(curve.getYValue(2.0));
    out.println(curve.getYValue(3.0));

    out.println(curve.getYValue(1.5));
    out.println(curve.getYValue(4.0));
  }

}
