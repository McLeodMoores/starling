/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.UniqueId;

/**
 * RESTful URIs for a portfolio node.
 */
public class DataPortfolioNodeUris {

  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param nodeId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, UniqueId nodeId) {
    return UriBuilder.fromUri(baseUri).path("/nodes/{nodeId}")
      .build(nodeId);
  }

}
