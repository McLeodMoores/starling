/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful URIs for the permission check provider.
 */
public class DataPermissionCheckProviderUris extends AbstractDataResource {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("permissionCheckGet");
    return bld.build();
  }

}
