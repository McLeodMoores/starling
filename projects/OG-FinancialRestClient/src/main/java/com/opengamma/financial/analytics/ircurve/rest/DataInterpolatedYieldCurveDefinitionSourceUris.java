/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.Instant;

import com.opengamma.util.money.Currency;

/**
 * RESTful URIs for the yield curve source.
 */
public class DataInterpolatedYieldCurveDefinitionSourceUris {
 /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @param versionAsOf  the version to fetch, null means latest
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, Currency currency, String name, Instant versionAsOf) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/definitions/searchSingle");
    bld.queryParam("currency", currency.toString());
    bld.queryParam("name", name);
    if (versionAsOf != null) {
      bld.queryParam("versionAsOf", versionAsOf.toString());
    }
    return bld.build();
  }

}
