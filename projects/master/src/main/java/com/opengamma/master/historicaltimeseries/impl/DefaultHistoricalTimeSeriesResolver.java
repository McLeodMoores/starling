/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolverWithBasicChangeManager;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.PagingRequest;

/**
 * Simple implementation of {@link HistoricalTimeSeriesResolver} which backs directly onto retrieves candidates from an underlying master.
 */
public class DefaultHistoricalTimeSeriesResolver extends HistoricalTimeSeriesResolverWithBasicChangeManager {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultHistoricalTimeSeriesResolver.class);

  private final HistoricalTimeSeriesSelector _selector;
  private final HistoricalTimeSeriesMaster _master;

  public DefaultHistoricalTimeSeriesResolver(final HistoricalTimeSeriesSelector selector, final HistoricalTimeSeriesMaster master) {
    _selector = ArgumentChecker.notNull(selector, "selector");
    _master = ArgumentChecker.notNull(master, "master");

    _master.changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(final ChangeEvent event) {
        final ObjectId oid = event.getObjectId();
        final ChangeType type = event.getType();
        final Instant vFrom = event.getVersionFrom();
        final Instant vInstant = event.getVersionInstant();
        final Instant vTo = event.getVersionTo();
        DefaultHistoricalTimeSeriesResolver.this.changeManager().entityChanged(type, oid, vFrom, vTo, vInstant);
      }
    });

  }

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider,
      final String dataField, final String resolutionKey) {
    if (identifierBundle != null) {
      final Collection<ManageableHistoricalTimeSeriesInfo> timeSeriesCandidates = search(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField);
      final ManageableHistoricalTimeSeriesInfo selectedResult = select(timeSeriesCandidates, resolutionKey);
      if (selectedResult == null) {
        s_logger.warn("Resolver failed to find any time-series for {} using {}/{}", new Object[] {identifierBundle, dataField, resolutionKey });
        return null;
      }
      return new HistoricalTimeSeriesResolutionResult(selectedResult);
    }
    return search(dataSource, dataProvider, dataField);
  }

  protected ManageableHistoricalTimeSeriesInfo select(final Collection<ManageableHistoricalTimeSeriesInfo> timeSeriesCandidates, final String resolutionKey) {
    return getSelector().select(timeSeriesCandidates, resolutionKey);
  }

  protected Collection<ManageableHistoricalTimeSeriesInfo> search(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource,
      final String dataProvider, final String dataField) {
    final HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest(identifierBundle);
    searchRequest.setValidityDate(identifierValidityDate);
    searchRequest.setDataSource(dataSource);
    searchRequest.setDataProvider(dataProvider);
    searchRequest.setDataField(dataField);
    final HistoricalTimeSeriesInfoSearchResult searchResult = _master.search(searchRequest);
    return searchResult.getInfoList();
  }

  protected HistoricalTimeSeriesResolutionResult search(final String dataSource, final String dataProvider, final String dataField) {
    final HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    searchRequest.setDataSource(dataSource);
    searchRequest.setDataProvider(dataProvider);
    searchRequest.setDataField(dataField);
    searchRequest.setPagingRequest(PagingRequest.NONE);
    if (_master.search(searchRequest).getPaging().getTotalItems() > 0) {
      return new HistoricalTimeSeriesResolutionResult(null, null);
    }
    return null;
  }

  private HistoricalTimeSeriesSelector getSelector() {
    return _selector;
  }

}
