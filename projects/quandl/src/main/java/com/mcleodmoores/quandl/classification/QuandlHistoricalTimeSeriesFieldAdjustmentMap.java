/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.classification;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustment;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;
import com.opengamma.util.ArgumentChecker;

/**
 * Extension of the historical time series field adjustment map that allows multiple Quandl fields to
 * be mapped to single OpenGamma fields (e.g. mapping {@link com.mcleodmoores.quandl.QuandlConstants#RATE_FIELD_NAME}
 * and {@link com.mcleodmoores.quandl.QuandlConstants#VALUE_FIELD_NAME} to
 * {@link com.opengamma.core.value.MarketDataRequirementNames#MARKET_VALUE}.
 */
public class QuandlHistoricalTimeSeriesFieldAdjustmentMap extends HistoricalTimeSeriesFieldAdjustmentMap {
  /** A map from requested field to field adjustments */
  private final Map<String, HistoricalTimeSeriesFieldAdjustment> _fieldAdjustments = new HashMap<>();

  /**
   * Creates an instance.
   * @param dataSource  the data source, not null
   */
  public QuandlHistoricalTimeSeriesFieldAdjustmentMap(final String dataSource) {
    super(dataSource);
  }

  /**
   * Gets any field adjustment for a given requested field.
   *
   * @param requestedField  the requested field, not null
   * @return the field adjustment, or null if no adjustment applies
   */
  @Override
  public HistoricalTimeSeriesFieldAdjustment getFieldAdjustment(final String requestedField) {
    return _fieldAdjustments.get(requestedField);
  }

  /**
   * Adds a field adjustment to the map, allowing multiple underlying fields to be mapped to a single requested
   * field.
   * @param requestedField  the requested field, not null
   * @param underlyingProvider  the underlying provider
   * @param underlyingFields  the underlying fields, not null
   * @param adjuster  the adjuster
   */
  public void addFieldAdjustment(final String requestedField, final String underlyingProvider, final List<String> underlyingFields,
      final HistoricalTimeSeriesAdjuster adjuster) {
    ArgumentChecker.notNull(requestedField, "requestedField");
    _fieldAdjustments.put(requestedField, new HistoricalTimeSeriesFieldAdjustment(underlyingProvider, underlyingFields, adjuster));
  }

  /**
   * Adds a field adjustment to the map.
   *
   * @param requestedField  the requested field, not null
   * @param underlyingProvider  the underlying provider, null for any
   * @param underlyingField  the underlying field, not null
   * @param adjuster  the adjuster, null for none
   */
  @Override
  public void addFieldAdjustment(final String requestedField, final String underlyingProvider, final String underlyingField,
      final HistoricalTimeSeriesAdjuster adjuster) {
    ArgumentChecker.notNull(requestedField, "requestedField");
    _fieldAdjustments.put(requestedField, new HistoricalTimeSeriesFieldAdjustment(underlyingProvider, Collections.singletonList(underlyingField), adjuster));
  }
}
