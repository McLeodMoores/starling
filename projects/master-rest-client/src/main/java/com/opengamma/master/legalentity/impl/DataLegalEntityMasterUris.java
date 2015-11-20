/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.master.legalentity.LegalEntityMetaDataRequest;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful URIs for legalEntities.
 */
public class DataLegalEntityMasterUris {
  
  /**
   * Builds a URI for security meta-data.
   *
   * @param baseUri the base URI, not null
   * @param request the request, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, LegalEntityMetaDataRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitiesearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentities");
    return bld.build();
  }

}
