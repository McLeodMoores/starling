/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.Instant;

/**
 * RESTful URIs for function costs.
 */
public class DataFunctionCostsMasterUris  {

  /**
   * Builds a URI for the load.
   * 
   * @param baseUri  the base URI, not null
   * @param configurationName  the configuration key, not null
   * @param functionId  the function id, not null
   * @param versionAsOf  the optional instant to retrieve data as of, null means latest
   * @return the URI, not null
   */
  public static URI uriLoad(URI baseUri, String configurationName, String functionId, Instant versionAsOf) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/functioncosts");
    if (configurationName != null) {
      bld.queryParam("configurationName", configurationName);
    }
    if (functionId != null) {
      bld.queryParam("functionId", functionId);
    }
    if (versionAsOf != null) {
      bld.queryParam("versionAsOf", versionAsOf.toString());
    }
    return bld.build();
  }

  /**
   * Builds a URI for the store.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriStore(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/functioncosts");
    return bld.build();
  }

}
