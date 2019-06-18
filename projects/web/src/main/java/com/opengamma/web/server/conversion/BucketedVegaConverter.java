/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Converts bucketed vega values into a labelled table for display.
 */
public class BucketedVegaConverter implements ResultConverter<BucketedGreekResultCollection> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BucketedVegaConverter.class);
  private static final DecimalFormat FORMAT = new DecimalFormat("##.###");

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final BucketedGreekResultCollection value,
      final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();
    if (value.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA) != null) {
      final double[] expiries = value.getExpiries();
      final double[][] strikes = value.getStrikes();
      final double[] uniqueStrikes = strikes[0];
      for (int i = 1; i < strikes.length; i++) {
        if (strikes[i].length != uniqueStrikes.length) {
          LOGGER.warn("Did not have a rectangular bucketed vega surface");
          return result;
        }
      }
      result.put("yCount", expiries.length);
      result.put("xCount", uniqueStrikes.length);
      if (mode == ConversionMode.FULL) {
        final double[][] surface = value.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA);
        final boolean[][] missingValues = new boolean[surface.length][surface[0].length];
        final Object[] yLabels = new Object[expiries.length];
        final Object[] xLabels = new Object[uniqueStrikes.length];
        for (int i = 0; i < expiries.length; i++) {
          yLabels[i] = FORMAT.format(expiries[i]);
        }
        for (int i = 0; i < uniqueStrikes.length; i++) {
          xLabels[i] = FORMAT.format(uniqueStrikes[i]);
        }
        result.put("xs", xLabels);
        result.put("ys", yLabels);
        result.put("surface", surface);
        result.put("missingValues", missingValues);
      }
    }
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final BucketedGreekResultCollection value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final BucketedGreekResultCollection value) {
    return "Bucketed Vega";
  }

  @Override
  public String getFormatterName() {
    return "SURFACE_DATA";
  }

}
