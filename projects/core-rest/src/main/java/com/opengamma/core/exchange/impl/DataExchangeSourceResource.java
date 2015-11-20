/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.exchange.impl;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for exchanges.
 * <p>
 * The exchanges resource receives and processes RESTful calls to the exchange source.
 */
@Path("exchangeSource")
public class DataExchangeSourceResource extends AbstractDataResource {

  /**
   * The exchange source.
   */
  private final ExchangeSource _exgSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param exchangeSource  the underlying exchange source, not null
   */
  public DataExchangeSourceResource(final ExchangeSource exchangeSource) {
    ArgumentChecker.notNull(exchangeSource, "exchangeSource");
    _exgSource = exchangeSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange source.
   * 
   * @return the exchange source, not null
   */
  public ExchangeSource getExchangeSource() {
    return _exgSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("exchanges")
  public Response search(
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo,
      @QueryParam("id") List<String> externalIdStrs) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    Collection<? extends Exchange> result = getExchangeSource().get(bundle, vc);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  @GET
  @Path("exchanges/{exchangeId}")
  public Response get(
      @PathParam("exchangeId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final Exchange result = getExchangeSource().get(objectId.atVersion(version));
      return responseOkObject(result);
    } else {
      final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
      Exchange result = getExchangeSource().get(objectId, vc);
      return responseOkObject(result);
    }
  }

 
  // deprecated
  //-------------------------------------------------------------------------
  @GET
  @Path("exchangeSearches/single")
  public Response searchSingle(@QueryParam("id") List<String> externalIdStrs) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    Exchange result = getExchangeSource().getSingle(bundle);
    return responseOkObject(result);
  }

}
