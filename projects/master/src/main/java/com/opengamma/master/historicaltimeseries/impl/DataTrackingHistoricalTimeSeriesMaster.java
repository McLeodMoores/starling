/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * HistoricalTimeSeries master which tracks accesses using UniqueIds.
 */
public class DataTrackingHistoricalTimeSeriesMaster extends AbstractDataTrackingMaster<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster> implements HistoricalTimeSeriesMaster {

  private static final String DATA_POINT_PREFIX = "DP";

  public DataTrackingHistoricalTimeSeriesMaster(final HistoricalTimeSeriesMaster delegate) {
    super(delegate);
  }

  @Override
  public HistoricalTimeSeriesInfoSearchResult search(final HistoricalTimeSeriesInfoSearchRequest request) {
    final HistoricalTimeSeriesInfoSearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(final HistoricalTimeSeriesInfoHistoryRequest request) {
    final HistoricalTimeSeriesInfoHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(final HistoricalTimeSeriesInfoMetaDataRequest request) {
    return delegate().metaData(request);
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final UniqueId uniqueId) {
    final ManageableHistoricalTimeSeries timeSeries = delegate().getTimeSeries(uniqueId);
    //trackId(timeSeries.getUniqueId());
    return timeSeries;
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final UniqueId uniqueId, final HistoricalTimeSeriesGetFilter filter) {
    final ManageableHistoricalTimeSeries timeSeries = delegate().getTimeSeries(uniqueId, filter);
    trackId(timeSeries.getUniqueId());
    return timeSeries;
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    final ManageableHistoricalTimeSeries timeSeries = delegate().getTimeSeries(objectId, versionCorrection);
    trackId(timeSeries.getUniqueId());
    return timeSeries;
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection, final HistoricalTimeSeriesGetFilter filter) {
    final ManageableHistoricalTimeSeries timeSeries = delegate().getTimeSeries(objectId, versionCorrection, filter);
    trackId(timeSeries.getUniqueId());
    return timeSeries;
  }

  @Override
  public UniqueId updateTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    final UniqueId id = delegate().updateTimeSeriesDataPoints(objectId, series);
    return trackId(id);
  }

  @Override
  public UniqueId correctTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    final UniqueId id = delegate().correctTimeSeriesDataPoints(objectId, series);
    return trackId(id);
  }

  @Override
  public UniqueId removeTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDate fromDateInclusive, final LocalDate toDateInclusive) {
    final UniqueId id = delegate().removeTimeSeriesDataPoints(objectId, fromDateInclusive, toDateInclusive);
    return trackId(id);
  }


  /**
   * DP ids (internal to HTSMaster) should be ignored.
   * @param id the id
   * @return the id
   */
  @Override
  protected synchronized UniqueId trackId(final UniqueId id) {
    if (!isDPId(id)) {
      return super.trackId(id);
    }
    return id;
  }

  private boolean isDPId(final UniqueId id) {
    return id != null && id.getValue().startsWith(DATA_POINT_PREFIX);
  }


}
