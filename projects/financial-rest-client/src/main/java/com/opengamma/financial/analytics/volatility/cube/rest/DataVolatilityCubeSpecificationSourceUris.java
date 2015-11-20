/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.Instant;

/**
 * RESTful URIs for the volatility cube specification source.
 */
public class DataVolatilityCubeSpecificationSourceUris {

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param name  the name, not null
   * @param versionAsOf  the version to fetch, null means latest
   * @return the URI, not null
   */
  public static URI uriSearchSingle(final URI baseUri, final String name, final Instant versionAsOf) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/specifications/searchSingle");
    bld.queryParam("name", name);
    if (versionAsOf != null) {
      bld.queryParam("versionAsOf", versionAsOf.toString());
    }
    return bld.build();
  }

}
