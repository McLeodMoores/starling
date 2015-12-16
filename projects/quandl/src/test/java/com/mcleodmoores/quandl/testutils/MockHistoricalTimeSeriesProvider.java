/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.testutils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.provider.historicaltimeseries.impl.AbstractHistoricalTimeSeriesProvider;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * A mock {@link com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider} for testing.
 */
public class MockHistoricalTimeSeriesProvider extends AbstractHistoricalTimeSeriesProvider {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(MockHistoricalTimeSeriesProvider.class);
  /** A map from identifiers to time series */
  private final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> _timeSeries;

  /**
   * Creates an instance.
   * @param timeSeries  a map from identifiers to time series, not null
   */
  public MockHistoricalTimeSeriesProvider(final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> timeSeries) {
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    _timeSeries = new HashMap<>();
    _timeSeries.putAll(timeSeries);
  }

  @Override
  protected HistoricalTimeSeriesProviderGetResult doBulkGet(final HistoricalTimeSeriesProviderGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    final Set<ExternalIdBundle> idBundles = request.getExternalIdBundles();
    final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> results = new LinkedHashMap<>();
    final LocalDate startDate = request.getDateRange().getStartDateInclusive();
    final LocalDate endDate = request.getDateRange().getEndDateExclusive();
    for (final ExternalIdBundle idBundle : idBundles) {
      final LocalDateDoubleTimeSeries ts = _timeSeries.get(idBundle);
      if (ts != null) {
        results.put(idBundle, ts.subSeries(startDate, endDate));
      } else {
        LOGGER.info("Could not get historical time series with id bundle {}", idBundle);
      }
    }
    return new HistoricalTimeSeriesProviderGetResult(results);
  }

}
