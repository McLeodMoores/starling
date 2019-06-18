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

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class ForwardCurveConverter implements ResultConverter<ForwardCurve> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final ForwardCurve value, final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();
    if (value.getForwardCurve() instanceof InterpolatedDoublesCurve) {
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value.getForwardCurve();
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
    if (value.getForwardCurve() instanceof FunctionalDoublesCurve) {
      final FunctionalDoublesCurve functionalCurve = (FunctionalDoublesCurve) value.getForwardCurve();
      final List<Double[]> data = new ArrayList<>();
      for (int i = 0; i < 30; i++) {
        final double x = i;
        data.add(new Double[] {x, functionalCurve.getYValue(x)});
      }
      result.put("summary", data);
      if (mode == ConversionMode.FULL) {
        final List<Double[]> detailedData = getData(functionalCurve);
        result.put("detailed", detailedData);
      }
      return result;
    }
    result.put("summary", "Can only display InterpolatedDoublesCurve or FunctionalDoublesCurve");
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final ForwardCurve value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final ForwardCurve value) {
    if (value.getForwardCurve() instanceof InterpolatedDoublesCurve) {
      final StringBuilder sb = new StringBuilder();
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value.getForwardCurve();
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
    double x = 0;
    for (int i = 0; i < 100; i++) {
      detailedData.add(new Double[]{x, detailedCurve.getYValue(x)});
      x += eps;
    }
    return detailedData;
  }

  private static List<Double[]> getData(final FunctionalDoublesCurve detailedCurve) {
    final List<Double[]> detailedData = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      final double x = 3 * i / 10.;
      detailedData.add(new Double[]{x, detailedCurve.getYValue(x)});
    }
    return detailedData;
  }
}
