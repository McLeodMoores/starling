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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.engine.value.ValueSpecification;

/* package */ class BlackVolatilitySurfaceMoneynessFcnBackedByGridFormatter
    extends AbstractFormatter<BlackVolatilitySurfaceMoneynessFcnBackedByGrid> {

  /* package */ BlackVolatilitySurfaceMoneynessFcnBackedByGridFormatter() {
    super(BlackVolatilitySurfaceMoneynessFcnBackedByGrid.class);
    addFormatter(new Formatter<BlackVolatilitySurfaceMoneynessFcnBackedByGrid>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final BlackVolatilitySurfaceMoneynessFcnBackedByGrid value,
                    final ValueSpecification valueSpec,
                    final Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public Object formatCell(final BlackVolatilitySurfaceMoneynessFcnBackedByGrid value,
                           final ValueSpecification valueSpec,
                           final Object inlineKey) {
    return SurfaceFormatterUtils.formatCell(value.getSurface());
  }

  private Object formatExpanded(final BlackVolatilitySurfaceMoneynessFcnBackedByGrid value) {
    final SmileSurfaceDataBundle gridData = value.getGridData();
    final Set<Double> strikes = new TreeSet<Double>();
    for (final double[] outer : gridData.getStrikes()) {
      for (final double inner : outer) {
        strikes.add(inner);
      }
    }
    final List<Double> vol = Lists.newArrayList();
    // x values (outer loop of vol) strikes
    // y values (inner loop of vol) expiries
    final List<Double> expiries = Lists.newArrayListWithCapacity(gridData.getExpiries().length);
    for (final double expiry : gridData.getExpiries()) {
      expiries.add(expiry);
    }
    for (final Double strike : strikes) {
      for (final Double expiry : expiries) {
        vol.add(value.getVolatility(expiry, strike));
      }
    }
    final Map<String, Object> results = Maps.newHashMap();
    results.put(SurfaceFormatterUtils.X_VALUES, expiries);
    results.put(SurfaceFormatterUtils.X_LABELS, SurfaceFormatterUtils.getAxisLabels(expiries));
    results.put(SurfaceFormatterUtils.X_TITLE, "Time to Expiry");
    results.put(SurfaceFormatterUtils.Y_VALUES, strikes);
    results.put(SurfaceFormatterUtils.Y_LABELS, SurfaceFormatterUtils.getAxisLabels(strikes));
    results.put(SurfaceFormatterUtils.Y_TITLE, "Strike");
    results.put(SurfaceFormatterUtils.VOL, vol);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.SURFACE_DATA;
  }
}
