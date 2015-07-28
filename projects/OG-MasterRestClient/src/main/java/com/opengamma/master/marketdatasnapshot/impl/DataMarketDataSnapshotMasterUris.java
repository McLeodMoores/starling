/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful URIs for snapshots.
 */
public class DataMarketDataSnapshotMasterUris extends AbstractDataResource {

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("snapshotSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("snapshots");
    return bld.build();
  }

}
