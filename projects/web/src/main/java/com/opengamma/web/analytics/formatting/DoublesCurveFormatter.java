/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class DoublesCurveFormatter extends AbstractFormatter<DoublesCurve> {

  /* package */ DoublesCurveFormatter() {
    super(DoublesCurve.class);
    addFormatter(new Formatter<DoublesCurve>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final DoublesCurve value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public Object formatCell(final DoublesCurve value, final ValueSpecification valueSpec, final Object inlineKey) {
    if (value instanceof InterpolatedDoublesCurve) {
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value;
      final List<Double[]> data = new ArrayList<>();
      final double[] xData = interpolatedCurve.getXDataAsPrimitive();
      final double[] yData = interpolatedCurve.getYDataAsPrimitive();
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      return data;
    } else if (value instanceof NodalDoublesCurve) {
      final NodalDoublesCurve nodalCurve = (NodalDoublesCurve) value;
      final List<Double[]> data = new ArrayList<>();
      final double[] xData = nodalCurve.getXDataAsPrimitive();
      final double[] yData = nodalCurve.getYDataAsPrimitive();
      for (int i = 0; i < nodalCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      return data;
    }
    return FORMATTING_ERROR;
  }

  private List<Double[]> formatExpanded(final DoublesCurve value) {
    NodalDoublesCurve detailedCurve;
    if (value instanceof NodalDoublesCurve) {
      detailedCurve = (NodalDoublesCurve) value;
    } else if (value instanceof InterpolatedDoublesCurve) {
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value;
      detailedCurve = NodalDoublesCurve.from(interpolatedCurve.getXDataAsPrimitive(), interpolatedCurve.getYDataAsPrimitive());
    } else {
      throw new OpenGammaRuntimeException("Cannot handle curves of type " + value.getClass());
    }
    final List<Double[]> detailedData = new ArrayList<>();
    final Double[] xs = detailedCurve.getXData();
    final Double[] ys = detailedCurve.getYData();
    for (int i = 0; i < ys.length; i++) {
      detailedData.add(new Double[]{xs[i], ys[i]});
    }
    return detailedData;
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }
}
