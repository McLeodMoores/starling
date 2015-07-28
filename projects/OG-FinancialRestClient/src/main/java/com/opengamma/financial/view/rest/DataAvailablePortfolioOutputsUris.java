/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;

/**
 * RESTful URIs for accessing available outputs from a portfolio
 */
public class DataAvailablePortfolioOutputsUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param instant  the instant, may be null
   * @param maxNodes  the maximum nodes, may be null
   * @param maxPositions  the maximum positions, may be null
   * @param portfolioId  the portfolio identifier, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, Instant instant, Integer maxNodes, Integer maxPositions, UniqueId portfolioId) {
    UriBuilder bld = UriBuilder.fromUri(DataAvailableOutputsProviderUris.uriPortfolio(baseUri));
    bld.path(instant != null ? instant.toString() : "now");
    if (maxNodes != null && maxNodes > 0) {
      bld.path("nodes").path(Integer.toString(maxNodes));
    }
    if (maxPositions != null && maxPositions > 0) {
      bld.path("positions").path(Integer.toString(maxPositions));
    }
    if (portfolioId != null) {
      bld.path(portfolioId.toString());
    }
    return bld.build();
  }

}
