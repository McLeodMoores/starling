/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.component.ComponentInfo;

/**
 * RESTful URIs for exposing managed components.
 */
public class DataComponentServerUris {
  /**
   * Builds a URI to fetch all components.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("components");
    return bld.build();
  }

  /**
   * Builds a URI for a single component.
   * 
   * @param baseUri  the base URI, not null
   * @param info  the component info, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ComponentInfo info) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("components/{type}/{classifier}");
    return bld.build(info.getType().getSimpleName(), info.getClassifier());
  }

}
