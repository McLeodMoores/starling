/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries2.impl;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries2.HistoricalDataRequest;
import com.opengamma.core.historicaltimeseries2.HistoricalTimeSeriesSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.util.ArgumentChecker;

/**
 * This class wraps an original implementation of HistoricalTimeSeriesSource and marshalls requests to it.
 */
public final class HistoricalTimeSeriesSourceV1Adapter implements HistoricalTimeSeriesSource {

  private final com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource _originalSource;

  private HistoricalTimeSeriesSourceV1Adapter(final com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource originalSource) {
    _originalSource = ArgumentChecker.notNull(originalSource, "originalSource");
  }

  /**
   * Wraps the original source.
   *
   * @param originalSource  the original source, not null
   * @return  a wrapped source
   */
  public static HistoricalTimeSeriesSource of(final com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource originalSource) {
    return new HistoricalTimeSeriesSourceV1Adapter(originalSource);
  }

  @Override
  public ChangeManager changeManager() {
    return _originalSource.changeManager();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final HistoricalDataRequest historicalDataRequest) {
    final String field = historicalDataRequest.getField() != null ? historicalDataRequest.getField() : MarketDataRequirementNames.MARKET_VALUE;
    return _originalSource.getHistoricalTimeSeries(field,
                                                   historicalDataRequest.getBundle(),
                                                   historicalDataRequest.getResolver(),
                                                   historicalDataRequest.getFrom(),
                                                   historicalDataRequest.isFromInclusive(),
                                                   historicalDataRequest.getTo(),
                                                   historicalDataRequest.isToInclusive());
  }
}
