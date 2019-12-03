/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful URIs for a config.
 */
public class DataConfigUris {

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public static URI uri(final URI baseUri, final ObjectIdentifiable objectId, final VersionCorrection vc) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs/{configId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  // TODO replace it with something better
  public static URI uriAll(final URI baseUri, final ObjectIdentifiable objectId, final VersionCorrection vc, final Class<?> type) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs/{configId}/all");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    if (type != null) {
      bld.queryParam("type", type.getName());
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param request  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(final URI baseUri, final ObjectIdentifiable objectId, final ConfigHistoryRequest<?> request) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs/{configId}/versions");
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
  public static URI uriVersion(final URI baseUri, final UniqueId uniqueId) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null);
    }
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs/{configId}/versions/{versionId}");
    return bld.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
