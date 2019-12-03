/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides access to a remote time-series loader.
 * <p>
 * This is a client that connects to a time-series loader at a remote URI.
 */
public class RemoteHistoricalTimeSeriesLoader extends AbstractRemoteClient implements HistoricalTimeSeriesLoader {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteHistoricalTimeSeriesLoader(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  // delegate convenience methods to request/result method
  // code copied from AbstractHistoricalTimeSeriesLoader due to lack of multiple inheritance
  @Override
  public Map<ExternalId, UniqueId> loadTimeSeries(
      final Set<ExternalId> identifiers, final String dataProvider, final String dataField, final LocalDate startDate, final LocalDate endDate) {
    final HistoricalTimeSeriesLoaderRequest request = HistoricalTimeSeriesLoaderRequest.create(identifiers, dataProvider, dataField, startDate, endDate);
    final HistoricalTimeSeriesLoaderResult result = loadTimeSeries(request);
    return result.getResultMap();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesLoaderResult loadTimeSeries(final HistoricalTimeSeriesLoaderRequest request) {
    ArgumentChecker.notNull(request, "request");

    final URI uri = DataHistoricalTimeSeriesLoaderUris.uriGet(getBaseUri());
    return accessRemote(uri).post(HistoricalTimeSeriesLoaderResult.class, request);
  }

  @Override
  public boolean updateTimeSeries(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    final URI uri = DataHistoricalTimeSeriesLoaderUris.uriUpdate(getBaseUri(), uniqueId);
    return accessRemote(uri).post(Boolean.class);
  }

}
