/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

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

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocumentDataResource;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a yield curve definition.
 */
public class DataInterpolatedYieldCurveDefinitionResource extends AbstractDocumentDataResource<YieldCurveDefinitionDocument> {

  /**
   * The parent resource.
   */
  private final DataInterpolatedYieldCurveDefinitionMasterResource _parentResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataInterpolatedYieldCurveDefinitionResource() {
    _parentResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param parentResource  the parent resource, not null
   * @param definitionId  the definition unique identifier, not null
   */
  public DataInterpolatedYieldCurveDefinitionResource(final DataInterpolatedYieldCurveDefinitionMasterResource parentResource, final ObjectId definitionId) {
    ArgumentChecker.notNull(parentResource, "definitionsResource");
    ArgumentChecker.notNull(definitionId, "definition");
    _parentResource = parentResource;
    _urlResourceId = definitionId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent resource.
   *
   * @return the parent resource, not null
   */
  public DataInterpolatedYieldCurveDefinitionMasterResource getParentResource() {
    return _parentResource;
  }

  /**
   * Gets the definition identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  @Override
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the definition master.
   *
   * @return the definition master, not null
   */
  @Override
  public InterpolatedYieldCurveDefinitionMaster getMaster() {
    return getParentResource().getInterpolatedYieldCurveDefinitionMaster();
  }

  @Override
  @GET
  public Response get(@QueryParam("versionAsOf") final String versionAsOf, @QueryParam("correctedTo") final String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @Override
  @POST
  public Response update(@Context final UriInfo uriInfo, final YieldCurveDefinitionDocument request) {
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
  public Response replaceVersion(@PathParam("versionId") final String versionId, final List<YieldCurveDefinitionDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @Override
  @PUT
  public Response replaceVersions(final List<YieldCurveDefinitionDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @Override
  @PUT
  @Path("all")
  public Response replaceAllVersions(final List<YieldCurveDefinitionDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "definitions";
  }
}
