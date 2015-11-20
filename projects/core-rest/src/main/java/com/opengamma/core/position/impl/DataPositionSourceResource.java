/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for positions.
 * <p>
 * This resource receives and processes RESTful calls to the position source.
 */
@Path("positionSource")
public class DataPositionSourceResource extends AbstractDataResource {

  /**
   * The position source.
   */
  private final PositionSource _posSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param positionSource  the underlying position source, not null
   */
  public DataPositionSourceResource(final PositionSource positionSource) {
    ArgumentChecker.notNull(positionSource, "positionSource");
    _posSource = positionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position source.
   * 
   * @return the position source, not null
   */
  public PositionSource getPositionSource() {
    return _posSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("portfolios/{portfolioId}")
  public Response getPortfolio(
      @PathParam("portfolioId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    if (version != null) {
      final Portfolio result = getPositionSource().getPortfolio(objectId.atVersion(version), vc);
      return responseOkObject(result);
    } else {
      final Portfolio result = getPositionSource().getPortfolio(objectId, vc);
      return responseOkObject(result);
    }
  }

  @GET
  @Path("nodes/{nodeId}")
  public Response getNode(
      @PathParam("nodeId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final PortfolioNode result = getPositionSource().getPortfolioNode(objectId.atVersion(version), vc);
    return responseOkObject(result);
  }

  @GET
  @Path("positions/{positionId}")
  public Response getPosition(
      @PathParam("positionId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final Position result;
    if (version != null) {
      result = getPositionSource().getPosition(objectId.atVersion(version));
    } else {
      result = getPositionSource().getPosition(objectId, VersionCorrection.parse(versionAsOf, correctedTo));
    }
    return responseOkObject(result);
  }

  @GET
  @Path("trades/{tradeId}")
  public Response getTrade(
      @PathParam("tradeId") String idStr,
      @QueryParam("version") String version) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final Trade result = getPositionSource().getTrade(objectId.atVersion(version));
    return responseOkObject(result);
  }

}
