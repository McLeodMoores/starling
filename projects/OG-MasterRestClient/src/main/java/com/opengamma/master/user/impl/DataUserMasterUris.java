/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.user.UserEventHistoryRequest;

/**
 * RESTful URIs for users.
 */
public class DataUserMasterUris {

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("userSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("users");
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
  public static URI uriUserById(URI baseUri, ObjectIdentifiable objectId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/users/{objectId}");
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @return the URI, not null
   */
  public static URI uriUserByName(URI baseUri, String userName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/users/name/{userName}");
    return bld.build(userName);
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @return the URI, not null
   */
  public static URI uriNameExists(URI baseUri, String userName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/users/exists/{userName}");
    return bld.build(userName);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param request  the request, not null
   * @return the URI, not null
   */
  public static URI uriEventHistory(URI baseUri, UserEventHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri);
    if (request.getObjectId() != null) {
      return bld.path("/users/{objectId}/eventHistory").build(request.getObjectId());
    } else {
      return bld.path("/users/name/{userName}/eventHistory").build(request.getUserName());
    }
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @return the URI, not null
   */
  public static URI uriUserAccountByName(URI baseUri, String userName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/users/name/{userName}/account");
    return bld.build(userName);
  }

}
