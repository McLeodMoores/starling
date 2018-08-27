/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.user.UserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.RoleEventHistoryRequest;
import com.opengamma.master.user.RoleEventHistoryResult;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for roles.
 * <p>
 * The roles resource receives and processes RESTful calls to the role master.
 */
@Path("roleMaster")
public class DataRoleMasterResource extends AbstractDataResource {

  /**
   * The role master.
   */
  private final RoleMaster _roleMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param roleMaster  the underlying role master, not null
   */
  public DataRoleMasterResource(final RoleMaster roleMaster) {
    ArgumentChecker.notNull(roleMaster, "roleMaster");
    _roleMaster = roleMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the role master.
   *
   * @return the role master, not null
   */
  public RoleMaster getRoleMaster() {
    return _roleMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("roles")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("roleSearches")
  public Response search(final RoleSearchRequest request) {
    final RoleSearchResult result = getRoleMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("roles")
  public Response add(@Context final UriInfo uriInfo, final ManageableRole role) {
    final UniqueId result = getRoleMaster().add(role);
    final URI createdUri = DataRoleMasterUris.uriRoleById(uriInfo.getBaseUri(), result);
    return responseCreated(createdUri);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("roles/{objectId}")
  public Response getById(@PathParam("objectId") final String idStr) {
    final ObjectId id = ObjectId.parse(idStr);
    final ManageableRole result = getRoleMaster().getById(id);
    return responseOkObject(result);
  }

  @PUT
  @Path("roles/{objectId}")
  public Response updateById(@Context final UriInfo uriInfo, @PathParam("objectId") final String idStr, final ManageableRole role) {
    final ObjectId id = ObjectId.parse(idStr);
    if (id.equals(role.getObjectId()) == false) {
      throw new IllegalArgumentException("ObjectId of role does not match URI");
    }
    final UniqueId result = getRoleMaster().update(role);
    return responseOkObject(result);
  }

  @DELETE
  @Path("roles/{objectId}")
  public void removeById(@PathParam("objectId") final String idStr) {
    final ObjectId id = ObjectId.parse(idStr);
    getRoleMaster().removeById(id);
  }

  @GET
  @Path("roles/{objectId}/eventHistory")
  public Response eventHistoryById(@PathParam("objectId") final String idStr) {
    final ObjectId id = ObjectId.parse(idStr);
    final RoleEventHistoryRequest request = new RoleEventHistoryRequest(id);
    final RoleEventHistoryResult result = getRoleMaster().eventHistory(request);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("roles/exists/{roleName}")
  public Response nameExists(@PathParam("roleName") final String roleName) {
    final boolean exists = getRoleMaster().nameExists(roleName);
    return exists ? responseOk() : Response.status(Status.NOT_FOUND).build();
  }

  @GET
  @Path("roles/name/{roleName}")
  public Response getByName(@PathParam("roleName") final String roleName) {
    final ManageableRole result = getRoleMaster().getByName(roleName);
    return responseOkObject(result);
  }

  @PUT
  @Path("roles/name/{roleName}")
  public Response updateByName(@Context final UriInfo uriInfo, @PathParam("roleName") final String roleName, final ManageableRole role) {
    final ManageableRole current = getRoleMaster().getByName(roleName);
    if (current.getObjectId().equals(role.getObjectId()) == false) {
      throw new IllegalArgumentException("Role does not match URI");
    }
    final UniqueId result = getRoleMaster().update(role);
    return responseOkObject(result);
  }

  @DELETE
  @Path("roles/name/{roleName}")
  public void removeByName(@PathParam("roleName") final String roleName) {
    getRoleMaster().removeByName(roleName);
  }

  @GET
  @Path("roles/name/{roleName}/eventHistory")
  public Response eventHistoryByName(@PathParam("roleName") final String roleName) {
    final RoleEventHistoryRequest request = new RoleEventHistoryRequest(roleName);
    final RoleEventHistoryResult result = getRoleMaster().eventHistory(request);
    return responseOkObject(result);
  }

  @POST
  @Path("roles/account")
  public Response accountByName(final UserAccount account) {
    final UserAccount resolved = getRoleMaster().resolveAccount(account);
    return responseOkObject(resolved);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(final URI baseUri) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("roleSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(final URI baseUri) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("roles");
    return bld.build();
  }

}
