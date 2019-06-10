/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocumentDataResource;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a convention.
 */
public class DataConventionResource extends AbstractDocumentDataResource<ConventionDocument> {

  /**
   * The conventions resource.
   */
  private final DataConventionMasterResource _conventionsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataConventionResource() {
    _conventionsResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param conventionsResource
   *          the parent resource, not null
   * @param conventionId
   *          the convention unique identifier, not null
   */
  public DataConventionResource(final DataConventionMasterResource conventionsResource, final ObjectId conventionId) {
    ArgumentChecker.notNull(conventionsResource, "conventionsResource");
    ArgumentChecker.notNull(conventionId, "convention");
    _conventionsResource = conventionsResource;
    _urlResourceId = conventionId;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the conventions resource.
   *
   * @return the conventions resource, not null
   */
  public DataConventionMasterResource getConventionsResource() {
    return _conventionsResource;
  }

  /**
   * Gets the convention identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  @Override
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the convention master.
   *
   * @return the convention master, not null
   */
  @Override
  public ConventionMaster getMaster() {
    return getConventionsResource().getConventionMaster();
  }

  @GET
  @Path("versions")
  public Response history(@Context final UriInfo uriInfo) {
    final ConventionHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, ConventionHistoryRequest.class);
    if (!getUrlId().equals(request.getObjectId())) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    final ConventionHistoryResult result = getMaster().history(request);
    return responseOkObject(result);
  }

  @Override
  @GET
  public Response get(@QueryParam("versionAsOf") final String versionAsOf, @QueryParam("correctedTo") final String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @Override
  @POST
  public Response update(@Context final UriInfo uriInfo, final ConventionDocument request) {
    return super.update(uriInfo, request);
  }

  @Override
  @DELETE
  public void remove() {
    super.remove();
  }

  @Override
  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") final String versionId) {
    return super.getVersioned(versionId);
  }

  @Override
  @PUT
  @Path("versions/{versionId}")
  public Response replaceVersion(@PathParam("versionId") final String versionId, final List<ConventionDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @Override
  @PUT
  public Response replaceVersions(final List<ConventionDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @Override
  @PUT
  @Path("all")
  public Response replaceAllVersions(final List<ConventionDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "conventions";
  }

}
