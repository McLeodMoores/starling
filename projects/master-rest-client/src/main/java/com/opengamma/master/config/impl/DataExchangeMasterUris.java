/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * RESTful URIs for exchanges.
 */
public class DataExchangeMasterUris {

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(final URI baseUri) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("exchangeSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(final URI baseUri) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("exchanges");
    return bld.build();
  }

}
