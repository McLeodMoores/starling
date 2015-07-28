/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.livedata.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.provider.livedata.LiveDataMetaDataProviderRequest;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful URIs for the live data provider.
 */
public class DataLiveDataMetaDataProviderUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param request  the request, not null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, LiveDataMetaDataProviderRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build();
  }

}
