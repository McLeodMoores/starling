/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * RESTful URIs for the time-series provider.
 */
public class DataHistoricalTimeSeriesProviderUris {
  
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsGet");
    return bld.build();
  }

}
