/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ObjectId;

/**
 * RESTful resource for batch.
 * <p>
 * Generates URIs for the client
 */
public class DataBatchRunUris {

  /**
   * Builds a URI for all batch runs.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(final URI baseUri) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("run/search");
    return bld.build();
  }

  /**
   * Builds a URI for a specific uid of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param batchRunId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uri(final URI baseUri, final ObjectId batchRunId) {
    return UriBuilder.fromUri(baseUri).path("/run/{uid}").build(batchRunId);
  }

  /**
   * Builds a URI for getBatchValues.
   *
   * @param baseUri  the base URI, not null
   * @param batchRunId the batch id which values we want to build the uri for
   * @return the URI, not null
   */
  public static URI uriBatchValues(final URI baseUri, final ObjectId batchRunId) {
    return UriBuilder.fromUri(baseUri).path("/run/{uid}/values").build(batchRunId);
  }

}
