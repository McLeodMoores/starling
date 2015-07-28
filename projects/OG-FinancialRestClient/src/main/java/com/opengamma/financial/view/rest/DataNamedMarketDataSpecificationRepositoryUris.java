/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;

/**
 * RESTful URIs for {@link NamedMarketDataSpecificationRepository}
 */
public class DataNamedMarketDataSpecificationRepositoryUris {

  private static final String PATH_NAMES = "names";
  private static final String PATH_SPECIFICATION = "specification";

  //-------------------------------------------------------------------------
  public static URI uriNames(final URI baseUri) {
    return UriBuilder.fromUri(baseUri).path(PATH_NAMES).build();
  }

  public static URI uriSpecification(final URI baseUri, final String name) {
    return UriBuilder.fromUri(baseUri).path(PATH_SPECIFICATION).path(name).build();
  }


}
