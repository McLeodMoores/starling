/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.util.money.Currency;

/**
 * RESTful URIs for currency pairs.
 */
public class DataCurrencyPairsSourceUris {

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param name  the name, not null
   * @return the URI, not null
   */
  public static URI uriGetPairs(final URI baseUri, final String name) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/currencyPairs/{name}");
    return bld.build(name);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param name  the name, not null
   * @param currency1  the first currency, not null
   * @param currency2  the second currency, not null
   * @return the URI, not null
   */
  public static URI uriGetPair(final URI baseUri, final String name, final Currency currency1, final Currency currency2) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/currencyPairs/{name}/{currency1}/{currency2}");
    return bld.build(name, currency1, currency2);
  }

}
