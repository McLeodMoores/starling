/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.rest.RestUtils;

public abstract class AbstractDataDocumentUris {
  protected abstract String getResourceName();
  
  //====================================================================================================================

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectIdentifiable  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public URI uri(URI baseUri, ObjectIdentifiable objectIdentifiable, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectIdentifiable.getObjectId());
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  // TODO replace URI with something better
  public URI uriAll(URI baseUri, ObjectIdentifiable objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}/all");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }


  /**
   * Builds a URI for the versions of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public URI uriVersions(URI baseUri, ObjectIdentifiable objectId, Object request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}/versions");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @return the URI, not null
   */
  public URI uriVersion(URI baseUri, UniqueId uniqueId) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null);
    }
    return UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}/versions/{versionId}")
      .build(uniqueId.toLatest(), uniqueId.getVersion());
  }
}
