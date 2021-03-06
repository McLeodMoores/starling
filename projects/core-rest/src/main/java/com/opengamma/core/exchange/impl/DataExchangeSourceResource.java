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
  /**
   * Gets a HATEAOS response.
   *
   * @param uriInfo
   *          the URI, not null
   * @return the response
   */
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  /**
   * Searches for an exchange by version, correction and external identifiers.
   *
   * @param versionAsOf
   *          the version, can be null. If null, the latest is used.
   * @param correctedTo
   *          the correction, can be null. If null, the latest is used.
   * @param externalIdStrs
   *          the external ids, not null
   * @return the exchange as a Fudge message
   */
  @GET
  @Path("exchanges")
  public Response search(
      @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo,
      @QueryParam("id") final List<String> externalIdStrs) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    final Collection<? extends Exchange> result = getExchangeSource().get(bundle, vc);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  /**
   * Searches for an exchange by identifier, version, version and correction.
   *
   * @param idStr
   *          the object identifier, not null
   * @param version
   *          the version, can be null. If null, the latest is used
   * @param versionAsOf
   *          the version, can be null. If null, the latest is used.
   * @param correctedTo
   *          the correction, can be null. If null, the latest is used.
   * @return the exchange as a Fudge message
   */
  @GET
  @Path("exchanges/{exchangeId}")
  public Response get(
      @PathParam("exchangeId") final String idStr,
      @QueryParam("version") final String version,
      @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final Exchange result = getExchangeSource().get(objectId.atVersion(version));
      return responseOkObject(result);
    }
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final Exchange result = getExchangeSource().get(objectId, vc);
    return responseOkObject(result);
  }


  // deprecated
  //-------------------------------------------------------------------------
  /**
   * Searches for exchanges by identifiers.
   *
   * @param externalIdStrs
   *          the object identifier, not null
   * @return the conventions as a Fudge message
   */
  @GET
  @Path("exchangeSearches/single")
  public Response searchSingle(@QueryParam("id") final List<String> externalIdStrs) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    final Exchange result = getExchangeSource().getSingle(bundle);
    return responseOkObject(result);
  }

}
