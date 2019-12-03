/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for regions.
 * <p>
 * The regions resource receives and processes RESTful calls to the region master.
 */
@Path("regionMaster")
public class DataRegionMasterResource extends AbstractDataResource {

  /**
   * The region master.
   */
  private final RegionMaster _regMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param regionMaster  the underlying region master, not null
   */
  public DataRegionMasterResource(final RegionMaster regionMaster) {
    ArgumentChecker.notNull(regionMaster, "regionMaster");
    _regMaster = regionMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the region master.
   *
   * @return the region master, not null
   */
  public RegionMaster getRegionMaster() {
    return _regMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("regions")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("regionSearches")
  public Response search(final RegionSearchRequest request) {
    final RegionSearchResult result = getRegionMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("regions")
  public Response add(@Context final UriInfo uriInfo, final RegionDocument request) {
    final RegionDocument result = getRegionMaster().add(request);
    final URI createdUri = new DataRegionResource().uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("regions/{regionId}")
  public DataRegionResource findRegion(@PathParam("regionId") final String idStr) {
    final ObjectId id = ObjectId.parse(idStr);
    return new DataRegionResource(this, id);
  }
}
