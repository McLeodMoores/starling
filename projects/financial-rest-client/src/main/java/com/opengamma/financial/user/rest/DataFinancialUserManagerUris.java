/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * RESTful URIs for users.
 */
public class DataFinancialUserManagerUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @return the URI, not null
   */
  public static URI uriUser(URI baseUri, String userName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("users/{userName}");
    return bld.build(userName);
  }

}
