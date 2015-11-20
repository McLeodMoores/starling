/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * RESTful URIs for available outputs.
 */
public class DataAvailableOutputsProviderUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriPortfolio(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("portfolio");
    return bld.build();
  }

}
