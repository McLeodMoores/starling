/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.DoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.engine.value.ValueSpecification;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/**
 * Converter for {@link VolatilitySurface} objects. The difference between this form and VolatilitySurfaceData is that the latter will often be full of repeated
 * values. Here we compute the distinct set of axes values, then query the surface for the z values.
 *
 * TODO PLAT-2249 Add field to allow transposing the display surface
 */
public class VolatilitySurfaceConverter implements ResultConverter<VolatilitySurface> {

  @Override
  /**
   * Put together a map of results such that VolatilitySurfaceDetail.js can render it Required outputs:
   * <p>
   * 'xs' and 'ys' - String arrays for axes labels.
   * <p>
   * 'surface' - 2D double array with size matching xs and ys
   */
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final VolatilitySurface value,
      final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();

    final Surface<Double, Double, Double> inputSurface = value.getSurface();
    if (inputSurface instanceof DoublesSurface) {

      // Compute unique X data
      final Double[] xData = inputSurface.getXData();
      final DoubleArrayList uniqueXs = new DoubleArrayList();
      for (final Double element : xData) {
        if (!uniqueXs.contains(element)) {
          uniqueXs.add(element);
        }
      }

      final int xCount = uniqueXs.size();
      result.put("xCount", xCount);

      // Compute unique Y data
      final Double[] yData = inputSurface.getYData();
      final DoubleArrayList uniqueYs = new DoubleArrayList();
      for (final Double element : yData) {
        if (!uniqueYs.contains(element)) {
          uniqueYs.add(element);
        }
      }
      Collections.sort(uniqueYs);
      final int yCount = uniqueYs.size();
      result.put("yCount", yCount);

      // Convert for display
      if (mode == ConversionMode.FULL) {
        final String[] xStrings = new String[xCount];
        for (int i = 0; i < xCount; i++) {
          xStrings[i] = uniqueXs.get(i).toString();
        }
        result.put("xs", xStrings);

        final String[] ysStrings = new String[yCount];
        for (int i = 0; i < yCount; i++) {
          ysStrings[i] = uniqueYs.get(i).toString();
        }
        result.put("ys", ysStrings);

        final double[][] outputSurface = new double[yCount][xCount];
        final boolean[][] missingValues = new boolean[yCount][xCount];
        // Summary view includes only the actual points of the surface
        for (int y = 0; y < yCount; y++) {
          for (int x = 0; x < xCount; x++) {
            final Double volatility = inputSurface.getZValue(uniqueXs.get(x), uniqueYs.get(y));
            if (volatility == null) {
              missingValues[y][x] = true;
              // Some 'obviously wrong' value in case client displays it. Can't use NaN
              outputSurface[y][x] = Double.MAX_VALUE;
            } else {
              outputSurface[y][x] = volatility;
            }
          }
        }
        result.put("surface", outputSurface);
        result.put("missingValues", missingValues);
      }

    } else {
      result.put("summary", "Converter only displays VolatilitySurface(DoublesSurface) as we require axes data");
    }
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context,
      final ValueSpecification valueSpec, final VolatilitySurface value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final VolatilitySurface value) {
    // Could actually serialise the surface to a string if this is an issue
    return "Volatility Surface (" + value.getSurface().getXData().length + " x " + value.getSurface().getYData().length + ")";
  }

  @Override
  public String getFormatterName() {
    return "SURFACE_DATA";
  }

}
