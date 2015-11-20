/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for currency pairs.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
@Path("/currencyPairsSource")
public class DataCurrencyPairsSourceResource extends AbstractDataResource {

  /**
   * The source.
   */
  private final CurrencyPairsSource _source;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param source  the underlying source, not null
   */
  public DataCurrencyPairsSourceResource(final CurrencyPairsSource source) {
    ArgumentChecker.notNull(source, "source");
    _source = source;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the source.
   * 
   * @return the source, not null
   */
  public CurrencyPairsSource getCurrencyPairsSource() {
    return _source;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("currencyPairs/{name}")
  public Response getPairs(@PathParam("name") String name) {
    CurrencyPairs result = getCurrencyPairsSource().getCurrencyPairs(name);
    return responseOkObject(result);
  }

  @GET
  @Path("currencyPairs/{name}/{currency1}/{currency2}")
  public Response getPair(@PathParam("name") String name, @PathParam("currency1") String currency1Str, @PathParam("currency2") String currency2Str) {
    Currency currency1 = Currency.parse(currency1Str);
    Currency currency2 = Currency.parse(currency2Str);
    CurrencyPair result = getCurrencyPairsSource().getCurrencyPair(name, currency1, currency2);
    return responseOkObject(result);
  }
}
