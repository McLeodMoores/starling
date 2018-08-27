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
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.user.UserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserEventHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for users.
 * <p>
 * The users resource receives and processes RESTful calls to the user master.
 */
@Path("userMaster")
public class DataUserMasterResource extends AbstractDataResource {

  /**
   * The user master.
   */
  private final UserMaster _userMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param userMaster  the underlying user master, not null
   */
  public DataUserMasterResource(final UserMaster userMaster) {
    ArgumentChecker.notNull(userMaster, "userMaster");
    _userMaster = userMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the user master.
   *
   * @return the user master, not null
   */
  public UserMaster getUserMaster() {
    return _userMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("users")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("userSearches")
  public Response search(final UserSearchRequest request) {
    final UserSearchResult result = getUserMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("users")
  public Response add(@Context final UriInfo uriInfo, final ManageableUser user) {
    final UniqueId result = getUserMaster().add(user);
    final URI createdUri = DataUserMasterUris.uriUserById(uriInfo.getBaseUri(), result);
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("users/{objectId}")
  public Response getById(@PathParam("objectId") final String idStr) {
    final ObjectId id = ObjectId.parse(idStr);
    final ManageableUser result = getUserMaster().getById(id);
    return responseOkObject(result);
  }

  @PUT
  @Path("users/{objectId}")
  public Response updateById(@Context final UriInfo uriInfo, @PathParam("objectId") final String idStr, final ManageableUser user) {
    final ObjectId id = ObjectId.parse(idStr);
    if (id.equals(user.getObjectId()) == false) {
      throw new IllegalArgumentException("ObjectId of user does not match URI");
    }
    final UniqueId result = getUserMaster().update(user);
    return responseOkObject(result);
  }

  @DELETE
  @Path("users/{objectId}")
  public void removeById(@PathParam("objectId") final String idStr) {
    final ObjectId id = ObjectId.parse(idStr);
    getUserMaster().removeById(id);
  }

  @GET
  @Path("users/{objectId}/eventHistory")
  public Response eventHistoryById(@PathParam("objectId") final String idStr) {
    final ObjectId id = ObjectId.parse(idStr);
    final UserEventHistoryRequest request = new UserEventHistoryRequest(id);
    final UserEventHistoryResult result = getUserMaster().eventHistory(request);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("users/exists/{userName}")
  public Response nameExists(@PathParam("userName") final String userName) {
    final boolean exists = getUserMaster().nameExists(userName);
    return exists ? responseOk() : Response.status(Status.NOT_FOUND).build();
  }

  @GET
  @Path("users/name/{userName}")
  public Response getByName(@PathParam("userName") final String userName) {
    final ManageableUser result = getUserMaster().getByName(userName);
    return responseOkObject(result);
  }

  @PUT
  @Path("users/name/{userName}")
  public Response updateByName(@Context final UriInfo uriInfo, @PathParam("userName") final String userName, final ManageableUser user) {
    final ManageableUser current = getUserMaster().getByName(userName);
    if (current.getObjectId().equals(user.getObjectId()) == false) {
      throw new IllegalArgumentException("User does not match URI");
    }
    final UniqueId result = getUserMaster().update(user);
    return responseOkObject(result);
  }

  @DELETE
  @Path("users/name/{userName}")
  public void removeByName(@PathParam("userName") final String userName) {
    getUserMaster().removeByName(userName);
  }

  @GET
  @Path("users/name/{userName}/eventHistory")
  public Response eventHistoryByName(@PathParam("userName") final String userName) {
    final UserEventHistoryRequest request = new UserEventHistoryRequest(userName);
    final UserEventHistoryResult result = getUserMaster().eventHistory(request);
    return responseOkObject(result);
  }

  @GET
  @Path("users/name/{userName}/account")
  public Response accountByName(@PathParam("userName") final String userName) {
    final UserAccount account = getUserMaster().getAccount(userName);
    return responseOkObject(account);
  }
}
