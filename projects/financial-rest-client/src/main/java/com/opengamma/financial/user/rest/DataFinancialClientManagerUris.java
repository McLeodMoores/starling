/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * RESTful URIs for the clients of a single user.
 */
public class DataFinancialClientManagerUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @return the URI, not null
   */
  public static URI uriClient(URI baseUri, String userName, String clientName) {
    UriBuilder bld = UriBuilder.fromUri(DataFinancialUserManagerUris.uriUser(baseUri, userName)).path("clients/{clientName}");
    return bld.build(clientName);
  }

}
