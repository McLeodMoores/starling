/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for accessing time-series data points.
 */
public class DataHistoricalDataPointsResource extends AbstractDataResource {

  /**
   * The parent resource.
   */
  private final DataHistoricalTimeSeriesMasterResource _htsResource;
  /**
   * The identifier specified in the URI.
   */
  private final ObjectId _urlResourceId;

  /**
   * Creates the resource.
   *
   * @param htsResource  the parent resource, not null
   * @param dpId  the data-points unique identifier, not null
   */
  public DataHistoricalDataPointsResource(final DataHistoricalTimeSeriesMasterResource htsResource, final ObjectId dpId) {
    ArgumentChecker.notNull(htsResource, "htsResource");
    ArgumentChecker.notNull(dpId, "dpId");
    _htsResource = htsResource;
    _urlResourceId = dpId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent resource.
   *
   * @return the parent resource, not null
   */
  public DataHistoricalTimeSeriesMasterResource getParentResource() {
    return _htsResource;
  }

  /**
   * Gets the data-points identifier from the URL.
   *
   * @return the object identifier, not null
   */
  public ObjectId getUrlDataPointsId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series master.
   *
   * @return the time-series master, not null
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return getParentResource().getHistoricalTimeSeriesMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@Context final UriInfo uriInfo, @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final HistoricalTimeSeriesGetFilter filter = RestUtils.decodeQueryParams(uriInfo, HistoricalTimeSeriesGetFilter.class);
    if (filter != null) {
      final ManageableHistoricalTimeSeries result = getHistoricalTimeSeriesMaster().getTimeSeries(getUrlDataPointsId(), vc, filter);
      return responseOkObject(result);
    } else {
      final ManageableHistoricalTimeSeries result = getHistoricalTimeSeriesMaster().getTimeSeries(getUrlDataPointsId(), vc);
      return responseOkObject(result);
    }
  }

  @POST
  @Path("updates")
  public Response postUpdates(final LocalDateDoubleTimeSeries newPoints) {
    final UniqueId result = getHistoricalTimeSeriesMaster().updateTimeSeriesDataPoints(getUrlDataPointsId(), newPoints);
    return responseOkObject(result);
  }

  @POST
  @Path("corrections")
  public Response postCorrections(final LocalDateDoubleTimeSeries newPoints) {
    final UniqueId result = getHistoricalTimeSeriesMaster().correctTimeSeriesDataPoints(getUrlDataPointsId(), newPoints);
    return responseOkObject(result);
  }

  @DELETE
  @Path("removals/{startDate}/{endDate}")
  public Response remove(@PathParam("startDate") final String startDateStr, @PathParam("endDate") final String endDateStr) {
    final LocalDate fromDateInclusive = startDateStr != null ? LocalDate.parse(startDateStr) : null;
    final LocalDate toDateInclusive = endDateStr != null ? LocalDate.parse(endDateStr) : null;

    final UniqueId result = getHistoricalTimeSeriesMaster().removeTimeSeriesDataPoints(getUrlDataPointsId(), fromDateInclusive, toDateInclusive);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@Context final UriInfo uriInfo, @PathParam("versionId") final String versionId) {
    final HistoricalTimeSeriesGetFilter filter = RestUtils.decodeQueryParams(uriInfo, HistoricalTimeSeriesGetFilter.class);
    if (filter != null) {
      final ManageableHistoricalTimeSeries result = getHistoricalTimeSeriesMaster().getTimeSeries(getUrlDataPointsId().atVersion(versionId), filter);
      return responseOkObject(result);
    } else {
      final ManageableHistoricalTimeSeries result = getHistoricalTimeSeriesMaster().getTimeSeries(getUrlDataPointsId().atVersion(versionId));
      return responseOkObject(result);
    }
  }
}
