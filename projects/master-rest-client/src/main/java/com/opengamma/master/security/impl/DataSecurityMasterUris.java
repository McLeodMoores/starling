/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful URIs for securities.
 */
public class DataSecurityMasterUris {

  /**
   * Builds a URI for security meta-data.
   *
   * @param baseUri  the base URI, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(final URI baseUri, final SecurityMetaDataRequest request) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
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
  public static URI uriSearch(final URI baseUri) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("securitySearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(final URI baseUri) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("securities");
    return bld.build();
  }

}
