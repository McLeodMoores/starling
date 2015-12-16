/**
 * Copyright (C) 2014 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

package com.mcleodmoores.quandl.historicaltimeseries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesConstants;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.AbstractHistoricalTimeSeriesLoader;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.time.LocalDateRange;

/**
 * Loads time-series data from Quandl into a {@link HistoricalTimeSeriesMaster}.
 * <p>
 * This loads missing historical time-series data from Quandl and stores it
 * into a master.
 */
public class QuandlHistoricalTimeSeriesLoader extends AbstractHistoricalTimeSeriesLoader {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlHistoricalTimeSeriesLoader.class);
  /**
   * String representing an unknown data provider.
   */
  public static final String UNKNOWN_DATA_PROVIDER = "UNKNOWN";
  /**
   * String representing the default data provider.
   */
  public static final String DEFAULT_DATA_PROVIDER = "DEFAULT";
  /**
   * The master.
   */
  private final HistoricalTimeSeriesMaster _htsMaster;
  /**
   * The provider of time-series.
   */
  private final HistoricalTimeSeriesProvider _underlyingHtsProvider;

  /**
   * Creates an instance.
   *
   * @param htsMaster  the time-series master, not null
   * @param underlyingHtsProvider  the time-series provider for the underlying data source, not null
   */
  public QuandlHistoricalTimeSeriesLoader(
      final HistoricalTimeSeriesMaster htsMaster,
      final HistoricalTimeSeriesProvider underlyingHtsProvider) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(underlyingHtsProvider, "underlyingHtsProvider");
    _htsMaster = htsMaster;
    _underlyingHtsProvider = underlyingHtsProvider;
  }

  /**
   * The observation time map.
   */
  private static final Map<String, String> OBSERVATION_TIME_MAP = ImmutableMap.<String, String>builder()
      .put(DEFAULT_DATA_PROVIDER, HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME)
      .build();

  /**
   * Resolves the data provider name.
   *
   * @param dataProvider the data provider, null returns the unknown value
   * @return the resolver data provider, not null
   */
  private static String resolveDataProvider(final String dataProvider) {
    return (dataProvider == null || dataProvider.equalsIgnoreCase(UNKNOWN_DATA_PROVIDER)
        || dataProvider.equalsIgnoreCase(DEFAULT_DATA_PROVIDER) ? DEFAULT_DATA_PROVIDER : dataProvider);
  }

  /**
   * Resolves the data provider to provide an observation time.
   *
   * @param dataProvider the data provider, null returns the unknown value
   * @return the corresponding observation time for the given data provider
   */
  public static String resolveObservationTime(final String dataProvider) {
    if (dataProvider == null || dataProvider.equalsIgnoreCase(UNKNOWN_DATA_PROVIDER) || dataProvider.equalsIgnoreCase(DEFAULT_DATA_PROVIDER)) {
      return OBSERVATION_TIME_MAP.get(DEFAULT_DATA_PROVIDER);
    }
    return OBSERVATION_TIME_MAP.get(dataProvider);
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesLoaderResult doBulkLoad(final HistoricalTimeSeriesLoaderRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getDataField(), "dataField");

    final Set<ExternalId> externalIds = request.getExternalIds();
    LocalDate startDate = request.getStartDate();
    LocalDate endDate = request.getEndDate();
    String dataProvider = request.getDataProvider();
    final String dataField = request.getDataField();
    dataProvider = resolveDataProvider(dataProvider);
    if (startDate == null) {
      startDate = LocalDate.MIN;
    }
    if (endDate == null) {
      endDate = LocalDate.MAX;
    }
    // finds the time-series that need loading
    final Map<ExternalId, UniqueId> resultMap = new HashMap<>();
    final Set<ExternalId> missingTimeseries = findTimeSeries(externalIds, dataProvider, dataField, resultMap);

    // batch in groups of 100 to avoid out-of-memory issues
    for (final List<ExternalId> partition : Iterables.partition(missingTimeseries, 100)) {
      final Set<ExternalId> subSet = Sets.newHashSet(partition);
      fetchTimeSeries(subSet, dataField, dataProvider, startDate, endDate, resultMap);
    }
    return new HistoricalTimeSeriesLoaderResult(resultMap);
  }

  /**
   * Finds those time-series that are not in the master.
   *
   * @param externalIds  the identifiers to lookup, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param result  the result map of identifiers, updated if already in database, not null
   * @return the missing identifiers, not null
   */
  protected Set<ExternalId> findTimeSeries(final Set<ExternalId> externalIds, final String dataProvider, final String dataField,
      final Map<ExternalId, UniqueId> result) {
    final HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    searchRequest.addExternalIds(externalIds);
    searchRequest.setDataField(dataField);
    if (dataProvider == null) {
      searchRequest.setDataProvider(DEFAULT_DATA_PROVIDER);
    } else {
      searchRequest.setDataProvider(dataProvider);
    }
    searchRequest.setDataSource(QuandlConstants.QUANDL_DATA_SOURCE_NAME);
    final HistoricalTimeSeriesInfoSearchResult searchResult = _htsMaster.search(searchRequest);

    final Set<ExternalId> missing = new HashSet<>(externalIds);
    for (final HistoricalTimeSeriesInfoDocument doc : searchResult.getDocuments()) {
      final Set<ExternalId> intersection = Sets.intersection(doc.getInfo().getExternalIdBundle().toBundle().getExternalIds(), externalIds).immutableCopy();
      if (intersection.size() == 1) {
        final ExternalId identifier = intersection.iterator().next();
        missing.remove(identifier);
        result.put(identifier, doc.getUniqueId());
      } else {
        throw new Quandl4OpenGammaRuntimeException("Unable to match single identifier: " + doc.getInfo().getExternalIdBundle());
      }
    }
    return missing;
  }

  /**
   * Fetches the time-series from Quandl and stores them in the master.
   *
   * @param identifiers  the identifiers to fetch, not null
   * @param dataField  the data field, not null
   * @param dataProvider  the data provider, not null
   * @param startDate  the start date to load, not null
   * @param endDate  the end date to load, not null
   * @param result  the result map of identifiers, updated if already in database, not null
   */
  protected void fetchTimeSeries(
      final Set<ExternalId> identifiers, final String dataField, final String dataProvider, final LocalDate startDate, final LocalDate endDate,
      final Map<ExternalId, UniqueId> result) {
    final Map<ExternalIdBundleWithDates, ExternalId> withDates2ExternalId = new HashMap<>();
    final Map<ExternalIdBundle, ExternalIdBundleWithDates> bundle2WithDates = new HashMap<>();

    // reverse map and normalize identifiers
    for (final ExternalId id : identifiers) {
      final ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(ExternalIdBundle.of(id));
      bundle2WithDates.put(bundle.toBundle(), bundle);
      withDates2ExternalId.put(bundle, id);
    }

    // fetch time-series and store to master
    if (bundle2WithDates.size() > 0) {
      final int identifiersSize = bundle2WithDates.keySet().size();
      if (bundle2WithDates.size() == 1) {
        LOGGER.info("Loading ts for {}: dataField: {} dataProvider: {} startDate: {} endDate: {}", Iterables.get(bundle2WithDates.keySet(), 0),
            dataField, dataProvider, startDate, endDate);
      } else {
        LOGGER.info("Loading {} ts:  dataField: {} dataProvider: {} startDate: {} endDate: {}", identifiersSize, dataField, dataProvider,
            startDate, endDate);
      }
      OperationTimer timer = new OperationTimer(LOGGER, " loading " + identifiersSize + " timeseries from Quandl");
      final HistoricalTimeSeriesProviderGetResult tsResult = provideTimeSeries(bundle2WithDates.keySet(), dataField, dataProvider, startDate, endDate);
      timer.finished();

      timer = new OperationTimer(LOGGER, " storing " + identifiersSize + " timeseries from Quandl");
      storeTimeSeries(tsResult, dataField, dataProvider, withDates2ExternalId, bundle2WithDates, result);
      timer.finished();
    }
  }

  /**
   * Loads time-series from the underlying source.
   *
   * @param externalIds  the external identifies to load, not null
   * @param dataField  the data field, not null
   * @param dataProvider  the data provider, not null
   * @param startDate  the start date to load, not null
   * @param endDate  the end date to load, not null
   * @return the map of results, not null
   */
  protected HistoricalTimeSeriesProviderGetResult provideTimeSeries(
      final Set<ExternalIdBundle> externalIds, final String dataField, final String dataProvider, final LocalDate startDate, final LocalDate endDate) {
    LOGGER.debug("Loading time series {} ({}-{}) {}: {}", new Object[] {dataField, startDate, endDate, dataProvider, externalIds });
    final LocalDateRange dateRange = LocalDateRange.of(startDate, endDate, true);

    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(externalIds,
        QuandlConstants.QUANDL_DATA_SOURCE_NAME, dataProvider, dataField, dateRange);
    return _underlyingHtsProvider.getHistoricalTimeSeries(request);
  }

  /**
   * Stores the time-series in the master.
   *
   * @param tsResult  the time-series result, not null
   * @param dataField  the data field, not null
   * @param dataProvider  the data provider, not null
   * @param bundleToIdentifier  the lookup map, not null
   * @param identifiersToBundleWithDates  the lookup map, not null
   * @param result  the result map of identifiers, updated if already in database, not null
   */
  protected void storeTimeSeries(
      final HistoricalTimeSeriesProviderGetResult tsResult,
      final String dataField, final String dataProvider,
      final Map<ExternalIdBundleWithDates, ExternalId> bundleToIdentifier,
      final Map<ExternalIdBundle, ExternalIdBundleWithDates> identifiersToBundleWithDates,
      final Map<ExternalId, UniqueId> result) {

    final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> tsMap = tsResult.getResultMap();

    // Add timeseries to data store
    for (final Entry<ExternalIdBundle, LocalDateDoubleTimeSeries> entry : tsMap.entrySet()) {
      final ExternalIdBundle identifers = entry.getKey();
      LocalDateDoubleTimeSeries timeSeries = entry.getValue();
      if (timeSeries != null && !timeSeries.isEmpty()) {
        final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
        final ExternalIdBundleWithDates bundleWithDates = identifiersToBundleWithDates.get(identifers);
        info.setExternalIdBundle(bundleWithDates);
        info.setDataField(dataField);
        info.setDataSource(QuandlConstants.QUANDL_DATA_SOURCE_NAME);
        final ExternalIdBundle bundle = bundleWithDates.toBundle(LocalDate.now(OpenGammaClock.getInstance()));
        final String idStr = Objects.firstNonNull(
            bundle.getValue(QuandlConstants.QUANDL_CODE),
            bundle.getExternalIds().iterator().next()).toString();
        info.setName(dataField + " " + idStr);
        info.setDataProvider(dataProvider);
        final String resolvedObservationTime = resolveObservationTime(dataProvider);
        if (resolvedObservationTime == null) {
          throw new OpenGammaRuntimeException("Unable to resolve observation time from given dataProvider: " + dataProvider);
        }
        info.setObservationTime(resolvedObservationTime);

        final Map<ExternalIdBundle, Set<String>> permissionsMap = tsResult.getPermissionsMap();
        if (permissionsMap != null) {
          final Set<String> permissions = permissionsMap.get(identifers);
          if (permissions != null) {
            info.getRequiredPermissions().addAll(permissions);
          }
        }

        // get time-series creating if necessary
        final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
        request.setDataField(info.getDataField());
        request.setDataSource(info.getDataSource());
        request.setDataProvider(info.getDataProvider());
        request.setObservationTime(info.getObservationTime());
        request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.EXACT, info.getExternalIdBundle().toBundle()));
        final HistoricalTimeSeriesInfoSearchResult searchResult = _htsMaster.search(request);
        if (searchResult.getDocuments().size() == 0) {
          // add new
          final HistoricalTimeSeriesInfoDocument doc = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
          final UniqueId uniqueId = _htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), timeSeries);
          result.put(bundleToIdentifier.get(bundleWithDates), uniqueId);
        } else {
          // update existing
          HistoricalTimeSeriesInfoDocument doc = searchResult.getDocuments().get(0);
          if (!info.getRequiredPermissions().equals(doc.getInfo().getRequiredPermissions())) {
            doc.setInfo(info);
            doc = _htsMaster.update(doc);
          }

          final HistoricalTimeSeries existingSeries = _htsMaster.getTimeSeries(doc.getInfo().getTimeSeriesObjectId(), VersionCorrection.LATEST,
              HistoricalTimeSeriesGetFilter.ofLatestPoint());
          if (existingSeries.getTimeSeries().size() > 0) {
            final LocalDate latestTime = existingSeries.getTimeSeries().getLatestTime();
            timeSeries = timeSeries.subSeries(latestTime, false, timeSeries.getLatestTime(), true);
          }
          UniqueId uniqueId = existingSeries.getUniqueId();
          if (timeSeries != null && timeSeries.size() > 0) {
            uniqueId = _htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), timeSeries);
          }
          result.put(bundleToIdentifier.get(bundleWithDates), uniqueId);
        }

      } else {
        LOGGER.warn("Empty historical data returned for {}", identifers);
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean updateTimeSeries(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    final HistoricalTimeSeriesInfoDocument doc = _htsMaster.get(uniqueId);
    final ManageableHistoricalTimeSeriesInfo info = doc.getInfo();
    final ExternalIdBundle externalIdBundle = info.getExternalIdBundle().toBundle();
    final String dataSource = info.getDataSource();
    final String dataProvider = info.getDataProvider();
    final String dataField = info.getDataField();
    final LocalDateDoubleTimeSeries series = _underlyingHtsProvider.getHistoricalTimeSeries(externalIdBundle, dataSource, dataProvider, dataField);
    if (series == null || series.isEmpty()) {
      return false;
    }
    _htsMaster.correctTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), series);
    return true;
  }

}

