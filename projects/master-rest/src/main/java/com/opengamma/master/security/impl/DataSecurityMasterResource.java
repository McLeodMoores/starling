/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

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
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for securities.
 * <p>
 * The securities resource receives and processes RESTful calls to the security master.
 */
@Path("securityMaster")
public class DataSecurityMasterResource extends AbstractDataResource {

  /**
   * The security master.
   */
  private final SecurityMaster _secMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param securityMaster  the underlying security master, not null
   */
  public DataSecurityMasterResource(final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _secMaster = securityMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security master.
   *
   * @return the security master, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _secMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("securities")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @GET
  @Path("metaData")
  public Response metaData(@Context final UriInfo uriInfo) {
    final SecurityMetaDataRequest request = RestUtils.decodeQueryParams(uriInfo, SecurityMetaDataRequest.class);
    final SecurityMetaDataResult result = getSecurityMaster().metaData(request);
    return responseOkObject(result);
  }

  @POST
  @Path("securitySearches")
  public Response search(final SecuritySearchRequest request) {
    final SecuritySearchResult result = getSecurityMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("securities")
  public Response add(@Context final UriInfo uriInfo, final SecurityDocument request) {
    final SecurityDocument result = getSecurityMaster().add(request);
    final URI createdUri = new DataSecurityResource().uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("securities/{securityId}")
  public DataSecurityResource findSecurity(@PathParam("securityId") final String idStr) {
    final ObjectId id = ObjectId.parse(idStr);
    return new DataSecurityResource(this, id);
  }
}
