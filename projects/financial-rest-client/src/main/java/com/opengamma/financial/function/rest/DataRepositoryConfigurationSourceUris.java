/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.Instant;

/**
 * RESTful URIs for repository configuration.
 */
public class DataRepositoryConfigurationSourceUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @param version the version timestamp to query, not null
   * @return the URI, not null
   */
  public static URI uriGetAll(URI baseUri, Instant version) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/repoConfigs/all/{version}");
    return bld.build(version);
  }

}
