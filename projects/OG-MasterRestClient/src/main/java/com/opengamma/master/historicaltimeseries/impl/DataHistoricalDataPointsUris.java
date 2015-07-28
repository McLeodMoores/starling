/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful URIs for accessing time-series data points.
 */
public class DataHistoricalDataPointsUris {

  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @param filter  the filter, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection vc, HistoricalTimeSeriesGetFilter filter) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    if (filter != null) {
      RestUtils.encodeQueryParams(bld, filter);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @param filter  the filter, may be null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId, HistoricalTimeSeriesGetFilter filter) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null, filter);
    }
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}/versions/{versionId}");
    if (filter != null) {
      RestUtils.encodeQueryParams(bld, filter);
    }
    return bld.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

  /**
   * Builds a URI for the corrections resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @return the URI, not null
   */
  public static URI uriUpdates(URI baseUri, ObjectIdentifiable objectId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}/updates");
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the corrections resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @return the URI, not null
   */
  public static URI uriCorrections(URI baseUri, ObjectIdentifiable objectId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}/corrections");
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the corrections resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param fromDateInclusive  the start date, may be null
   * @param toDateInclusive  the end date, may be null
   * @return the URI, not null
   */
  public static URI uriRemovals(URI baseUri, ObjectIdentifiable objectId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}/removals/{startDate}/{endDate}");
    return bld.build(objectId.getObjectId(), ObjectUtils.toString(fromDateInclusive, ""), ObjectUtils.toString(toDateInclusive, ""));
  }

}
