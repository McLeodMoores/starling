/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Converts curves for display. If the curve is interpolated, then intermediate
 * values are used. If the curve is nodal, only the nodal points are added.
 */
public class CurveConverter implements ResultConverter<DoublesCurve> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final DoublesCurve value, final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();
    if (value instanceof InterpolatedDoublesCurve) {
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value;
      final List<Double[]> data = new ArrayList<>();
      final double[] xData = interpolatedCurve.getXDataAsPrimitive();
      final double[] yData = interpolatedCurve.getYDataAsPrimitive();
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      result.put("summary", data);
      if (mode == ConversionMode.FULL) {
        final List<Double[]> detailedData = getData(interpolatedCurve);
        result.put("detailed", detailedData);
      }
      return result;
    }
    if (value instanceof NodalDoublesCurve) {
      final NodalDoublesCurve nodalCurve = (NodalDoublesCurve) value;
      final List<Double[]> data = new ArrayList<>();
      final double[] xData = nodalCurve.getXDataAsPrimitive();
      final double[] yData = nodalCurve.getYDataAsPrimitive();
      for (int i = 0; i < nodalCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i] });
      }
      result.put("summary", data);
      if (mode == ConversionMode.FULL) {
        // can't interpolate values for nodal curve, thus summary == detailed
        result.put("detailed", data);
      }
      return result;
    }
    result.put("summary", "Can only display InterpolatedDoublesCurve or NodalDoublesCurve");
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final DoublesCurve value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final DoublesCurve value) {
    if (value instanceof InterpolatedDoublesCurve) {
      final StringBuilder sb = new StringBuilder();
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value;
      final double[] xData = interpolatedCurve.getXDataAsPrimitive();
      final double[] yData = interpolatedCurve.getYDataAsPrimitive();
      boolean isFirst = true;
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        if (isFirst) {
          isFirst = false;
        } else {
          sb.append("; ");
        }
        sb.append(xData[i]).append("=").append(yData[i]);
      }
      return sb.length() > 0 ? sb.toString() : null;
    }
    return value.getClass().getSimpleName();
  }

  @Override
  public String getFormatterName() {
    return "CURVE";
  }

  private static List<Double[]> getData(final InterpolatedDoublesCurve detailedCurve) {
    final List<Double[]> detailedData = new ArrayList<>();

    final Double[] xs = detailedCurve.getXData();
    final double eps = (xs[xs.length - 1] - xs[0]) / 100;
    double x = xs[0];
    for (int i = 0; i < 100; i++) {
      detailedData.add(new Double[]{x, detailedCurve.getYValue(x)});
      x += eps;
    }
    return detailedData;
  }

}
