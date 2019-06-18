/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * Data engine resource manager constants shared between Resource and Remotes.
 */
public class DataEngineResourceManagerUris {
  /**
   * The time after which unused references may be automatically released.
   */
  public static final long REFERENCE_LEASE_MILLIS = 5000;

  public static URI uriReference(final URI baseUri, final long referenceId) {
    return UriBuilder.fromUri(baseUri).segment(Long.toString(referenceId)).build();
  }
}
