/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Converter for {@link VolatilitySurfaceData} objects.
 */
@SuppressWarnings("rawtypes")
public class VolatilitySurfaceDataConverter implements ResultConverter<VolatilitySurfaceData> {

  @Override
  // TODO PLAT-2249 Add field to allow transposing the display surface
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final VolatilitySurfaceData rawValue, final ConversionMode mode) {
    final
    VolatilitySurfaceData<Object, Object> value = rawValue;
    final Map<String, Object> result = new HashMap<>();

    result.put("xCount", value.getXs().length);
    result.put("yCount", value.getYs().length);

    if (mode == ConversionMode.FULL) {
      //TODO assuming that all surfaces are interpolated - bad
      final Object[] xs = value.getXs();
      final String[] xsStrings = new String[xs.length];
      for (int i = 0; i < xs.length; i++) {
        xsStrings[i] = LabelFormatter.format(xs[i]);
      }
      result.put("xs", xsStrings);
      final Object[] ys = value.getYs();
      final String[] ysStrings = new String[ys.length];
      for (int i = 0; i < ys.length; i++) {
        ysStrings[i] = LabelFormatter.format(ys[i]);
      }
      result.put("ys", ysStrings);


      final double[][] surface = new double[ys.length][xs.length];
      final boolean[][] missingValues = new boolean[ys.length][xs.length];
      // Summary view includes only the actual points of the surface
      for (int y = 0; y < ys.length; y++) {
        for (int x = 0; x < xs.length; x++) {
          final Object xt = xs[x];
          final Object yt = ys[y];
          final Double volatility = value.getVolatility(xt, yt);
          if (volatility == null) {
            missingValues[y][x] = true;
            //Some 'obviously wrong' value in case client displays it.  Can't use NaN
            surface[y][x] = Double.MAX_VALUE;
          } else {
            surface[y][x] = volatility;
          }
        }
      }
      result.put("surface", surface);
      result.put("missingValues", missingValues);
    }
    result.put("axesLabel", "Strike \\ Expiry");
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final VolatilitySurfaceData value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final VolatilitySurfaceData value) {
    // Could actually serialise the surface to a string if this is an issue
    return "Volatility Surface (" + value.getXs().length + " x " + value.getYs().length + ")";
  }

  @Override
  public String getFormatterName() {
    return "SURFACE_DATA";
  }

}
