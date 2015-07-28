/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region.impl;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for regions.
 * <p>
 * The regions resource receives and processes RESTful calls to the region source.
 */
@Path("regionSource")
public class DataRegionSourceResource extends AbstractDataResource {

  /**
   * The region source.
   */
  private final RegionSource _regSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param regionSource  the underlying region source, not null
   */
  public DataRegionSourceResource(final RegionSource regionSource) {
    ArgumentChecker.notNull(regionSource, "regionSource");
    _regSource = regionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the region source.
   * 
   * @return the region source, not null
   */
  public RegionSource getRegionSource() {
    return _regSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("regions")
  public Response search(
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo,
      @QueryParam("id") List<String> externalIdStrs) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    Collection<? extends Region> result = getRegionSource().get(bundle, vc);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  @GET
  @Path("regions/{regionId}")
  public Response get(
      @PathParam("regionId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final Region result = getRegionSource().get(objectId.atVersion(version));
      return responseOkObject(result);
    } else {
      final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
      Region result = getRegionSource().get(objectId, vc);
      return responseOkObject(result);
    }
  }

  // deprecated
  //-------------------------------------------------------------------------
  @GET
  @Path("regionSearches/highest")
  public Response searchHighest(@QueryParam("id") List<String> externalIdStrs) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    Region result = getRegionSource().getHighestLevelRegion(bundle);
    return responseOkObject(result);
  }
}
