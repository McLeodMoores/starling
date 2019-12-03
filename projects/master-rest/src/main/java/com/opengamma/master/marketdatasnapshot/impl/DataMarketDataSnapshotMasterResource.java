/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.change.DataChangeManagerResource;
import com.opengamma.id.ObjectId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for snapshots.
 * <p>
 * The snapshots resource receives and processes RESTful calls to the snapshot master.
 */
@Path("snapshotMaster")
public class DataMarketDataSnapshotMasterResource extends AbstractDataResource {

  /**
   * The snapshot master.
   */
  private final MarketDataSnapshotMaster _snpMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param snapshotMaster  the underlying snapshot master, not null
   */
  public DataMarketDataSnapshotMasterResource(final MarketDataSnapshotMaster snapshotMaster) {
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    _snpMaster = snapshotMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the snapshot master.
   *
   * @return the snapshot master, not null
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return _snpMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("snapshots")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("snapshotSearches")
  public Response search(final MarketDataSnapshotSearchRequest request) {
    final MarketDataSnapshotSearchResult result = getMarketDataSnapshotMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("snapshots")
  public Response add(@Context final UriInfo uriInfo, final MarketDataSnapshotDocument request) {
    final MarketDataSnapshotDocument result = getMarketDataSnapshotMaster().add(request);
    final URI createdUri = new DataMarketDataSnapshotResource().uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("snapshots/{snapshotId}")
  public DataMarketDataSnapshotResource findMarketDataSnapshot(@PathParam("snapshotId") final String idStr) {
    final ObjectId id = ObjectId.parse(idStr);
    return new DataMarketDataSnapshotResource(this, id);
  }

  @Path("snapshots/changeManager")
  public DataChangeManagerResource getChangeManager() {
    return new DataChangeManagerResource(getMarketDataSnapshotMaster().changeManager());
  }

}
