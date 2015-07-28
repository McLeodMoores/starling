/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.core.user.UserAccount;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.user.RoleEventHistoryRequest;

/**
 * RESTful URIs for roles.
 */
public class DataRoleMasterUris {

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("roleSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("roles");
    return bld.build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @return the URI, not null
   */
  public static URI uriRoleById(URI baseUri, ObjectIdentifiable objectId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/roles/{objectId}");
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param roleName  the role name, not null
   * @return the URI, not null
   */
  public static URI uriRoleByName(URI baseUri, String roleName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/roles/name/{roleName}");
    return bld.build(roleName);
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param roleName  the role name, not null
   * @return the URI, not null
   */
  public static URI uriNameExists(URI baseUri, String roleName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/roles/exists/{roleName}");
    return bld.build(roleName);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param request  the request, not null
   * @return the URI, not null
   */
  public static URI uriEventHistory(URI baseUri, RoleEventHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri);
    if (request.getObjectId() != null) {
      return bld.path("/roles/{objectId}/eventHistory").build(request.getObjectId());
    } else {
      return bld.path("/roles/name/{roleName}/eventHistory").build(request.getRoleName());
    }
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param account  the account, not null
   * @return the URI, not null
   */
  public static URI uriResolveRole(URI baseUri, UserAccount account) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/roles/account");
    return bld.build();
  }

}
