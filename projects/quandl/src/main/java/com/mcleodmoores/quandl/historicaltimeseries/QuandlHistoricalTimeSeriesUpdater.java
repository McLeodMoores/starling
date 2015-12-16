/**
 * Copyright (C) 2014 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.mcleodmoores.quandl.historicaltimeseries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesConstants;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesInfoSearchIterator;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MapUtils;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PoolExecutor.CompletionListener;
import com.opengamma.util.PoolExecutor.Service;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.LocalDateRange;

/**
 * Updates Quandl timeseries for a given time series master.
 * <p>
 * This loads missing historical time-series data from Quandl.
 */
public class QuandlHistoricalTimeSeriesUpdater {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlHistoricalTimeSeriesUpdater.class);
  /** The historical time series master */
  private final HistoricalTimeSeriesMaster _timeSeriesMaster;
  /** The Quandl historical time series provider */
  private final HistoricalTimeSeriesProvider _historicalTimeSeriesProvider;
  /** The start date for time series updates */
  private LocalDate _startDate;
  /** The end date for time series updates */
  private LocalDate _endDate;
  /** True if the time series is to be reloaded */
  private boolean _reload;

  /**
   * Constructor for time series updater.
   * @param htsMaster  the master holding the historical time series, not null
   * @param underlyingHtsProvider  the time series provider, not null
   */
  public QuandlHistoricalTimeSeriesUpdater(final HistoricalTimeSeriesMaster htsMaster, final HistoricalTimeSeriesProvider underlyingHtsProvider) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(underlyingHtsProvider, "underlyingHtsProvider");
    _timeSeriesMaster = htsMaster;
    _historicalTimeSeriesProvider = underlyingHtsProvider;
  }

  /**
   * Sets the start date field.
   * @param startDate the start date
   */
  public void setStartDate(final LocalDate startDate) {
    _startDate = startDate;
  }

  /**
   * Sets the end date field.
   * @param endDate the end date
   */
  public void setEndDate(final LocalDate endDate) {
    _endDate = endDate;
  }

  /**
   * Sets the reload field.
   * @param reload the reload
   */
  public void setReload(final boolean reload) {
    _reload = reload;
  }

  /**
   * Run the update.
   */
  public void run() {
    if (_reload) {
      _startDate = resolveStartDate();
      _endDate = resolveEndDate();
    }
    updateTimeSeries();
  }

  //-------------------------------------------------------------------------
  /**
   * Check a time series entry to see if it requires updating and update request and lookup data structures.
   * @param doc  time series info document of the time series to update, not null
   * @param metaDataKeyMap  map from a meta data key to a set of object ids, not null
   * @param tsRequest  data structure containing entries for start dates, each with a chain of maps to link providers and fields to id bundles, not null
   * @return  whether to update this time series
   */
  protected boolean checkForUpdates(final HistoricalTimeSeriesInfoDocument doc, final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap,
      final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> tsRequest) {
    ArgumentChecker.notNull(doc, "doc");
    ArgumentChecker.notNull(metaDataKeyMap, "metaDataKeyMap");
    ArgumentChecker.notNull(tsRequest, "tsRequest");
    final ManageableHistoricalTimeSeriesInfo info = doc.getInfo();
    final ExternalIdBundle idBundle = info.getExternalIdBundle().toBundle();
    // select start date
    LocalDate startDate = _startDate;
    if (startDate == null) {
      // lookup start date as one day after the latest point in the series
      final UniqueId htsId = doc.getInfo().getUniqueId();
      final LocalDate latestDate = getLatestDate(htsId);
      if (isUpToDate(latestDate, doc.getInfo().getObservationTime())) {
        LOGGER.debug("Not scheduling update for up to date series {} from {}", htsId, latestDate);
        return false; // up to date, so do not fetch
      }
      LOGGER.debug("Scheduling update for series {} from {}", htsId, latestDate);
      startDate = latestDate == LocalDate.MIN ? LocalDate.MIN : DateUtils.nextWeekDay(latestDate);
    }
    final String dataProvider = info.getDataProvider();
    final String dataField = info.getDataField();
    synchronized (tsRequest) {
      final Map<String, Map<String, Set<ExternalIdBundle>>> providerFieldIdentifiers =
          MapUtils.putIfAbsentGet(tsRequest, startDate, new HashMap<String, Map<String, Set<ExternalIdBundle>>>());
      final Map<String, Set<ExternalIdBundle>> fieldIdentifiers =
          MapUtils.putIfAbsentGet(providerFieldIdentifiers, dataProvider, new HashMap<String, Set<ExternalIdBundle>>());
      final Set<ExternalIdBundle> identifiers = MapUtils.putIfAbsentGet(fieldIdentifiers, dataField, new HashSet<ExternalIdBundle>());
      identifiers.add(idBundle);
    }
    final MetaDataKey metaDataKey = new MetaDataKey(idBundle, dataProvider, dataField);
    synchronized (metaDataKeyMap) {
      // what is the purpose of this method other than to warn of duplicates?
      final ObjectId objectId = doc.getInfo().getTimeSeriesObjectId();
      final Set<ObjectId> objectIds = MapUtils.putIfAbsentGet(metaDataKeyMap, metaDataKey, Sets.newHashSet(objectId));
      if (objectIds != null) {
        LOGGER.warn("Duplicate time series for {}", metaDataKey._identifiers);
        objectIds.add(objectId);
      }
    }
    return true;
  }

  /**
   * Check for updates.  This method decorates the real method with code to report status.
   * @param documents  a list of meta-data info documents, not null
   * @param metaDataKeyMap  map from a meta data key to a set of object ids, not null
   * @param tsRequest  data structure containing entries for start dates, each with a chain of maps to link providers and fields to id bundles, not null
   */
  protected void checkForUpdates(final Collection<HistoricalTimeSeriesInfoDocument> documents,
      final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap, final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> tsRequest) {
    ArgumentChecker.notNull(documents, "documents");
    ArgumentChecker.notNull(metaDataKeyMap, "metaDataKeyMap");
    ArgumentChecker.notNull(tsRequest, "tsRequest");
    if (_startDate != null) {
      final List<RuntimeException> failures = new ArrayList<>();
      final Service<Boolean> service = new PoolExecutor(10, "HTS checker").createService(new CompletionListener<Boolean>() {

        @Override
        public void success(final Boolean result) {
          // Ignore
        }

        @Override
        public void failure(final Throwable error) {
          synchronized (failures) {
            if (error instanceof RuntimeException) {
              failures.add((RuntimeException) error);
            } else {
              failures.add(new Quandl4OpenGammaRuntimeException("Checked", error));
            }
          }
        }

      });
      for (final HistoricalTimeSeriesInfoDocument doc : documents) {
        service.execute(new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            return checkForUpdates(doc, metaDataKeyMap, tsRequest);
          }
        });
      }
      try {
        service.join();
        for (final RuntimeException failure : failures) {
          throw failure;
        }
      } catch (final InterruptedException e) {
        throw new Quandl4OpenGammaRuntimeException("Interrupted", e);
      }
    }
    for (final HistoricalTimeSeriesInfoDocument doc : documents) {
      checkForUpdates(doc, metaDataKeyMap, tsRequest);
    }
  }

  /**
   * Update the time series.
   */
  protected void updateTimeSeries() {
    // load the info documents for all Quandl series that can be updated
    LOGGER.info("Loading all time series information...");
    final List<HistoricalTimeSeriesInfoDocument> documents = getCurrentQuandlTimeSeriesDocuments();
    LOGGER.info("Loaded {} time series information documents from master", documents.size());
    // group Quandl request by dates/dataProviders/dataFields
    final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> tsRequest = Maps.newHashMap();
    // store identifier to UID map for timeseries update
    final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap = new HashMap<>();
    if (_startDate != null) {
      tsRequest.put(_startDate, new HashMap<String, Map<String, Set<ExternalIdBundle>>>());
    }
    checkForUpdates(documents, metaDataKeyMap, tsRequest);
    // select end date
    final LocalDate endDate = resolveEndDate();
    LOGGER.info("Updating {} time series to {}", metaDataKeyMap, endDate);
    // load from Quandl and store in database
    getAndUpdateHistoricalData(tsRequest, metaDataKeyMap, endDate);
  }

  /**
   * Resolves the start date, returning the minimum possible date if the start date field is null.
   * @return  the start date
   */
  private LocalDate resolveStartDate() {
    return _startDate == null ? LocalDate.MIN : _startDate;
  }

  /**
   * Resolves the end date, returning the maximum possible date if the end date field is null.
   * @return  the end date
   */
  private LocalDate resolveEndDate() {
    return _endDate == null ? LocalDate.MAX : _endDate;
  }

  /**
   * Gets the latest date of a series stored in the master. If the time series is empty, returns the minimum
   * date.
   * @param htsId  the unique id of the series
   * @return  the latest time for which there is data in the series
   */
  private LocalDate getLatestDate(final UniqueId htsId) {
    final LocalDateDoubleTimeSeries timeSeries = _timeSeriesMaster.getTimeSeries(htsId, HistoricalTimeSeriesGetFilter.ofLatestPoint()).getTimeSeries();
    if (timeSeries.isEmpty()) {
      return LocalDate.MIN;
    }
    return timeSeries.getLatestTime();
  }

  /**
   * Checks that the time series is up to date, which it is assumed to be if there is data on the previous
   * week day, unless the observation time is Tokyo close, in which case the previous week date plus one
   * day is checked.
   * @param latestDate  the latest date in the time series
   * @param observationTime  the observation time
   * @return  true if the series is up to date
   */
  private static boolean isUpToDate(final LocalDate latestDate, final String observationTime) {
    LocalDate previousWeekDay = null;
    //TODO think carefully about this hard-coding. Wouldn't it be better passed in as a parameter?
    if (observationTime.equalsIgnoreCase(HistoricalTimeSeriesConstants.TOKYO_CLOSE)) {
      previousWeekDay = DateUtils.previousWeekDay().plusDays(1);
    } else {
      previousWeekDay = DateUtils.previousWeekDay();
    }
    return previousWeekDay.isBefore(latestDate) || previousWeekDay.equals(latestDate);
  }

  //-------------------------------------------------------------------------

  /**
   * Gets all time series from the master and finds any that have expired.
   * @return  the list of time series that have not expired
   */
  private List<HistoricalTimeSeriesInfoDocument> getCurrentQuandlTimeSeriesDocuments() {
    // gets all time-series that were originally loaded from Quandl
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataSource(QuandlConstants.QUANDL_DATA_SOURCE_NAME);
    return identifyExpiredTimeSeries(HistoricalTimeSeriesInfoSearchIterator.iterable(_timeSeriesMaster, request));
  }

  /**
   * Identifies expired time series from the master and returns a list of those time series that are still valid.
   * @param searchIterable  an iterator over time series that have the data source set to Quandl
   * @return  a list of time series that have not expired
   */
  private static List<HistoricalTimeSeriesInfoDocument> identifyExpiredTimeSeries(final Iterable<HistoricalTimeSeriesInfoDocument> searchIterable) {
    final List<HistoricalTimeSeriesInfoDocument> result = Lists.newArrayList();
    final LocalDate previousWeekDay = DateUtils.previousWeekDay();

    for (final HistoricalTimeSeriesInfoDocument htsInfoDoc : searchIterable) {
      final ManageableHistoricalTimeSeriesInfo tsInfo = htsInfoDoc.getInfo();

      final boolean valid = getIsValidOn(previousWeekDay, tsInfo);
      if (valid) {
        result.add(htsInfoDoc);
      } else {
        LOGGER.debug("Time series {} is not valid on {}", tsInfo.getUniqueId(), previousWeekDay);
      }
    }
    return result;
  }

  /**
   * @param previousWeekDay  the previous week day
   * @param tsInfo  the time series info
   * @return  true if the series is valid on the previous week date
   */
  private static boolean getIsValidOn(final LocalDate previousWeekDay, final ManageableHistoricalTimeSeriesInfo tsInfo) {
    boolean anyInvalid = false;
    for (final ExternalIdWithDates id : tsInfo.getExternalIdBundle()) {
      if (id.isValidOn(previousWeekDay)) {
        if (id.getValidFrom() != null || id.getValidTo() != null) {
          // If there is a ticker with expiry, which is valid, that's ok
          return true;
        }
      } else {
        anyInvalid = true;
      }
    }
    // Otherwise be very strict, since many things have tickers with no expiry
    return !anyInvalid;
  }

  //-------------------------------------------------------------------------
  private void getAndUpdateHistoricalData(final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> quandlTsRequest,
      final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap, final LocalDate endDate) {
    // process the request
    for (final Entry<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> entry : quandlTsRequest.entrySet()) {
      LOGGER.debug("processing {}", entry);
      // if we're reloading we should get the whole ts, not just the end...
      final LocalDate startDate = _reload ? LocalDate.MIN : entry.getKey();

      for (final Entry<String, Map<String, Set<ExternalIdBundle>>> providerFieldIdentifiers : entry.getValue().entrySet()) {
        LOGGER.debug("processing {}", providerFieldIdentifiers);
        final String dataProvider = providerFieldIdentifiers.getKey();

        for (final Entry<String, Set<ExternalIdBundle>> fieldIdentifiers : providerFieldIdentifiers.getValue().entrySet()) {
          LOGGER.debug("processing {}", fieldIdentifiers);
          final String dataField = fieldIdentifiers.getKey();
          final Set<ExternalIdBundle> identifiers = fieldIdentifiers.getValue();

          final String quandlDataProvider = resolveDataProvider(dataProvider);
          final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> loadedTs = getTimeSeries(dataField, startDate, endDate, quandlDataProvider, identifiers);
          if (loadedTs.size() < identifiers.size()) {
            for (final ExternalIdBundle failure : Sets.difference(identifiers, loadedTs.keySet())) {
              LOGGER.error("Failed to load time series for {}, {}, {}", failure, quandlDataProvider, dataField);
              errorLoading(new MetaDataKey(failure, quandlDataProvider, dataField));
            }
          }
          updateTimeSeriesMaster(loadedTs, metaDataKeyMap, quandlDataProvider, dataField);
        }
      }
    }
  }

  /**
   * Resolves the data provider name.
   *
   * @param dataProvider the data provider, null returns the unknown value
   * @return the resolver data provider, not null
   */
  public static String resolveDataProvider(final String dataProvider) {
    return dataProvider == null || dataProvider.equalsIgnoreCase(QuandlHistoricalTimeSeriesLoader.UNKNOWN_DATA_PROVIDER)
        || dataProvider.equalsIgnoreCase(QuandlHistoricalTimeSeriesLoader.DEFAULT_DATA_PROVIDER)
        ? QuandlHistoricalTimeSeriesLoader.DEFAULT_DATA_PROVIDER : dataProvider;
  }

  private void updateTimeSeriesMaster(final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> loadedTs, final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap,
      final String dataProvider, final String dataField) {
    for (final Entry<ExternalIdBundle, LocalDateDoubleTimeSeries> identifierTs : loadedTs.entrySet()) {
      // ensure data points are after the last stored data point
      LocalDateDoubleTimeSeries timeSeries = identifierTs.getValue();
      if (timeSeries.isEmpty()) {
        LOGGER.info("No new data for series {} {}", dataField, identifierTs.getKey());
        continue; // avoids errors in getLatestTime()
      }
      LOGGER.info("Got {} new points for series {} {}", new Object[] {timeSeries.size(), dataField, identifierTs.getKey() });

      final LocalDate latestTime = timeSeries.getLatestTime();
      final LocalDate startDate = _startDate != null ? _startDate : LocalDate.MIN;
      timeSeries = timeSeries.subSeries(startDate, true, latestTime, true);
      if (!timeSeries.isEmpty()) {
        // metaDataKeyMap holds the object id of the series to be updated
        final ExternalIdBundle idBundle = identifierTs.getKey();
        final MetaDataKey metaDataKey = new MetaDataKey(idBundle, dataProvider, dataField);
        for (final ObjectId oid : metaDataKeyMap.get(metaDataKey)) {
          try {
            if (_reload) {
              _timeSeriesMaster.correctTimeSeriesDataPoints(oid, timeSeries);
            } else {
              _timeSeriesMaster.updateTimeSeriesDataPoints(oid, timeSeries);
            }
          } catch (final Exception ex) {
            LOGGER.error("Error writing time-series " + oid, ex);
            if (metaDataKeyMap.get(metaDataKey).size() > 1) {
              LOGGER.error("This is probably because there are multiple time series for {} with differing lengths.  Manually delete one or the other.",
                  metaDataKey._identifiers);
            }
            errorLoading(metaDataKey);
          }
        }
      }
    }
  }

  /**
   * Error loading this time series key.
   * @param timeSeries  the meta data key that caused the error
   */
  protected void errorLoading(final MetaDataKey timeSeries) {
    // No-op
  }

  protected Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getTimeSeries(
      final String dataField, final LocalDate startDate, final LocalDate endDate, final String quandlDataProvider, final Set<ExternalIdBundle> identifierSet) {
    LOGGER.debug("Loading time series {} ({}-{}) {}: {}", new Object[] {dataField, startDate, endDate, quandlDataProvider, identifierSet });
    final LocalDateRange dateRange = LocalDateRange.of(startDate, endDate, true);
    return _historicalTimeSeriesProvider.getHistoricalTimeSeries(identifierSet, QuandlConstants.QUANDL_DATA_SOURCE_NAME, quandlDataProvider,
        dataField, dateRange);
  }

  //-------------------------------------------------------------------------
  /**
   * Lookup data.
   */
  protected static final class MetaDataKey {

    private final ExternalIdBundle _identifiers;
    private final String _dataProvider;
    private final String _field;

    /**
     * Constructor for Meta data key.
     * @param identifiers  an external id bundle
     * @param dataProvider  a data provider (often DEFAULT)
     * @param field  a field name (e.g. Value, Rate)
     */
    public MetaDataKey(final ExternalIdBundle identifiers, final String dataProvider, final String field) {
      _identifiers = identifiers;
      _dataProvider = dataProvider;
      _field = field;
    }

    @Override
    public int hashCode() {
      return _identifiers.hashCode() ^ _field.hashCode();
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof MetaDataKey)) {
        return false;
      }
      final MetaDataKey other = (MetaDataKey) obj;
      if (!Objects.equals(_field, other._field)) {
        return false;
      }
      if (!Objects.equals(_identifiers, other._identifiers)) {
        return false;
      }
      if (!Objects.equals(_dataProvider, other._dataProvider)) {
        return false;
      }
      return true;
    }
  }

}
