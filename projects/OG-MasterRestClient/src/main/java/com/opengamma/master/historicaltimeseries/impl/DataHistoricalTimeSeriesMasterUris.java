/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful URIs for time-series.
 */
public class DataHistoricalTimeSeriesMasterUris {

  /**
   * Builds a URI for info meta-data.
   *
   * @param baseUri  the base URI, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, HistoricalTimeSeriesInfoMetaDataRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("infoSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("infos");
    return bld.build();
  }

}
