/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.financial.temptarget.TempTargetRepository;

/**
 * RESTful URIs for a {@link TempTargetRepository}
 */
public class DataTempTargetRepositoryUris {

  public static URI uriLocateOrStore(final URI baseUri) {
    return UriBuilder.fromUri(baseUri).path("/target").build();
  }
  
}
