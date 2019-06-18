/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.volatility.surface.FunctionalVolatilitySurfaceData;

/**
 *
 */
public class FunctionalVolatilitySurfaceDataConverter implements ResultConverter<FunctionalVolatilitySurfaceData> {
  private static final DecimalFormat LABEL_FORMAT = new DecimalFormat("##.##");

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final FunctionalVolatilitySurfaceData value,
      final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();
    final VolatilitySurface surface = value.getSurface();
    final int nX = value.getNXSamples();
    final int nY = value.getNYSamples();
    result.put("xCount", nX);
    result.put("yCount", nY);
    if (mode == ConversionMode.FULL) {
      final Object[] xs = new Object[nX];
      final Object[] ys = new Object[nY];
      final double[][] values = new double[nX][nY];
      final boolean[][] missingValues = new boolean[nX][nY];
      final double xStep = (value.getXMaximum() - value.getXMinimum()) / nX;
      final double yStep = (value.getYMaximum() - value.getYMinimum()) / nY;
      double x = value.getXMinimum();
      for (int i = 0; i < nX; i++) {
        xs[i] = LABEL_FORMAT.format(x);
        double y = value.getYMinimum();
        for (int j = 0; j < nY; j++) {
          if (i == 0) {
            ys[j] = LABEL_FORMAT.format(y);
          }
          values[i][j] = 100 * surface.getVolatility(x, y);
          y += yStep;
        }
        x += xStep;
      }
      result.put("xs", xs);
      result.put("ys", ys);
      result.put("surface", values);
      result.put("missingValues", missingValues);
    }
    result.put("axesLabel", value.getYLabel() + "\\" + value.getXLabel());
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final FunctionalVolatilitySurfaceData value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final FunctionalVolatilitySurfaceData value) {
    return "Functional Volatility Surface";
  }

  @Override
  public String getFormatterName() {
    return "SURFACE_DATA";
  }

}
