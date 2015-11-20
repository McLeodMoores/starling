/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * RESTful URIs for users.
 */
public class DataUserSourceUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, may be null
   * @return the URI, not null
   */
  public static URI uriUserByName(URI baseUri, String userName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("users/name/{userName}");
    return bld.build(userName);
  }

}
