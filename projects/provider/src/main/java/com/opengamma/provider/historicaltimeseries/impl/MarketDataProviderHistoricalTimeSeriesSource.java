/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.tuple.Pair;

/**
 * HTS source which delegates all serviceable requests to the HTS provider.
 */
public abstract class MarketDataProviderHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataProviderHistoricalTimeSeriesSource.class);

  /**
   * The id supplier service.
   */
  private final UniqueIdSupplier _uniqueIdSupplier;
  /**
   * The provider.
   */
  private final HistoricalTimeSeriesProvider _provider;
  /**
   * The name of the provider.
   */
  private final String _providerName;

  /**
   * Constructor for the class taking the provider to be used for all requests.
   *
   * @param providerName the name of the data provider
   * @param uniqueIdSupplier the supplier for unique ids
   * @param provider the provider of HTS data
   */
  public MarketDataProviderHistoricalTimeSeriesSource(final String providerName,
      final UniqueIdSupplier uniqueIdSupplier,
      final HistoricalTimeSeriesProvider provider) {

    ArgumentChecker.notNull(providerName, "providerName");
    ArgumentChecker.notNull(uniqueIdSupplier, "uniqueIdSupplier");
    ArgumentChecker.notNull(provider, "provider");
    _providerName = providerName;
    _uniqueIdSupplier = uniqueIdSupplier;
    _provider = provider;

  }

  /**
   * Exception to be thrown if operation cannot be performed due to unique id.
   */
  private UnsupportedOperationException createUniqueIdException() {
    return new UnsupportedOperationException(
        "Unable to retrieve historical time-series from " + _providerName + " using unique identifier");
  }

  /**
   * Exception to be thrown if operation cannot be performed due to config.
   */
  private UnsupportedOperationException createConfigException() {
    return new UnsupportedOperationException(
        "Unable to retrieve historical time-series from " + _providerName + " using config");
  }

  /**
   * Exception to be thrown if operation cannot be performed due to validity date.
   */
  private UnsupportedOperationException createValidityDateException() {
    return new UnsupportedOperationException(
        "Unable to retrieve historical time-series from " + _providerName + " using identifier validity date");
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException("Change events not supported");
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      final ExternalIdBundle externalIdBundle, final String dataSource, final String dataProvider, final String dataField,
      final LocalDateRange dateRange, final Integer maxPoints) {

    LOGGER.info("Getting HistoricalTimeSeries for security {}", externalIdBundle);

    final HistoricalTimeSeriesProviderGetRequest request =
        HistoricalTimeSeriesProviderGetRequest.createGet(externalIdBundle, dataSource, dataProvider, dataField, dateRange);
    request.setMaxPoints(maxPoints);
    final HistoricalTimeSeriesProviderGetResult result = _provider.getHistoricalTimeSeries(request);
    final LocalDateDoubleTimeSeries timeSeries = result.getResultMap().get(externalIdBundle);
    if (timeSeries == null) {
      LOGGER.info("Unable to get HistoricalTimeSeries for {}", externalIdBundle);
      return null;
    }
    return new SimpleHistoricalTimeSeries(_uniqueIdSupplier.get(), timeSeries);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId) {
    throw createUniqueIdException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId, final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    throw createUniqueIdException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId, final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final int maxPoints) {
    throw createUniqueIdException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final UniqueId uniqueId) {
    throw createUniqueIdException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final UniqueId uniqueId, final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    throw createUniqueIdException();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final ExternalIdBundle identifiers, final String dataSource, final String dataProvider, final String dataField) {
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, LocalDateRange.ALL, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final ExternalIdBundle identifiers, final String dataSource, final String dataProvider, final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    LocalDate startDate = start;
    if (!includeStart && startDate != null) {
      startDate = startDate.plusDays(1);
    }
    final LocalDateRange dateRange = LocalDateRange.ofNullUnbounded(startDate, end, includeEnd);
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, dateRange, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final ExternalIdBundle identifiers, final String dataSource, final String dataProvider, final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final int maxPoints) {
    LocalDate startDate = start;
    if (!includeStart && startDate != null) {
      startDate = startDate.plusDays(1);
    }
    final LocalDateRange dateRange = LocalDateRange.ofNullUnbounded(startDate, end, includeEnd);
    final Integer maxPointsVal = maxPoints == 0 ? null : maxPoints;
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, dateRange, maxPointsVal);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final ExternalIdBundle identifiers, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider, final String dataField) {
    throw createValidityDateException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifiers,
      final LocalDate identifierValidityDate, final String dataSource, final String dataProvider, final String dataField, final LocalDate start,
      final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    throw createValidityDateException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider, final String dataField, final LocalDate start,
      final boolean includeStart, final LocalDate end, final boolean includeEnd, final int maxPoints) {
    throw createValidityDateException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider, final String dataField) {
    throw createValidityDateException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider, final String dataField, final LocalDate start,
      final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    throw createValidityDateException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider, final String dataField) {
    throw createValidityDateException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider, final String dataField, final LocalDate start, final boolean includeStart, final LocalDate end,
      final boolean includeEnd) {
    throw createValidityDateException();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final String dataField, final ExternalIdBundle identifiers, final String resolutionKey) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final String dataField, final ExternalIdBundle identifiers, final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final String dataField, final ExternalIdBundle identifiers, final LocalDate identifierValidityDate, final String resolutionKey) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final String dataField, final ExternalIdBundle identifiers, final LocalDate identifierValidityDate, final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey, final LocalDate start, final boolean includeStart, final LocalDate end,
      final boolean includeEnd, final int maxPoints) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String resolutionKey, final LocalDate start,
      final boolean includeStart, final LocalDate end, final boolean includeEnd, final int maxPoints) {
    throw createConfigException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey) {
    throw createConfigException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey, final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    throw createConfigException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String resolutionKey) {
    throw createConfigException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String resolutionKey, final LocalDate start, final boolean includeStart,
      final LocalDate end, final boolean includeEnd) {
    throw createConfigException();
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      final Set<ExternalIdBundle> externalIdBundles, final String dataSource, final String dataProvider, final String dataField, final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    LocalDate startDate = start;
    if (!includeStart && startDate != null) {
      startDate = startDate.plusDays(1);
    }
    final LocalDateRange dateRange = LocalDateRange.ofNullUnbounded(startDate, end, includeEnd);
    LOGGER.info("Getting HistoricalTimeSeries for securities {}", externalIdBundles);

    final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> map = _provider.getHistoricalTimeSeries(externalIdBundles, dataSource, dataProvider, dataField, dateRange);
    final Map<ExternalIdBundle, HistoricalTimeSeries> result = Maps.newHashMap();
    for (final ExternalIdBundle bundle : map.keySet()) {
      final LocalDateDoubleTimeSeries ts = map.get(bundle);
      HistoricalTimeSeries hts = null;
      if (ts != null) {
        hts = new SimpleHistoricalTimeSeries(_uniqueIdSupplier.get(), ts);
      }
      result.put(bundle, hts);
    }
    return result;
  }

  @Override
  public ExternalIdBundle getExternalIdBundle(final UniqueId uniqueId) {
    throw createConfigException();
  }
}
