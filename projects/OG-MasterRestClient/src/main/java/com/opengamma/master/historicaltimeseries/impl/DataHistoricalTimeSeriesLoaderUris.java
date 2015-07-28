/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.UniqueId;

/**
 * RESTful URIs for the time-series loader.
 */
public class DataHistoricalTimeSeriesLoaderUris {
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsLoad");
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriUpdate(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsUpdate/{uniqueId}");
    return bld.build(uniqueId);
  }

}
