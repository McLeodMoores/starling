/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * RESTful URIs for the function repository intended for debugging.
 */
public class DataFunctionRepositoryUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGetFunctionsByUniqueId(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/functionsByUniqueId");
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGetFunctionsByShortName(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/functionsByShortName");
    return bld.build();
  }

}
