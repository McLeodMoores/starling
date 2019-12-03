/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

public class DataFinancialSecuritySourceUris {

  /**
   * Builds a URI.
   *
   * @param baseUri
   *          the base URI, not null
   * @param issuerName
   *          the issuer name, may be null
   * @return the URI, not null
   */
  public static URI uriSearchBonds(final URI baseUri, final String issuerName) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/securities/bonds");
    bld.queryParam("issuerName", issuerName);
    return bld.build();
  }

}
