/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for the yield curve master.
 * <p>
 * This resource receives and processes RESTful calls to the master.
 */
public class DataInterpolatedYieldCurveDefinitionMasterUris extends AbstractDataDocumentUris {
  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/definitions");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAddOrUpdate(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/definitions/save");
    return bld.build();
  } 

  @Override
  protected String getResourceName() {
    return "definitions";
  }

}
