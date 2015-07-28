/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.convention.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * RESTful URIs for conventions.
 */
public class DataConventionSourceUris {
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param vc  the version-correction, null means latest
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri, VersionCorrection vc, ExternalIdBundle bundle) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventions");
    if (vc != null) {
      bld.queryParam("versionAsof", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventions/{conventionId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @param vc  the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventions/{conventionId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueIds  the unique identifiers, may be null
   * @return the URI, not null
   */
  public static URI uriBulk(URI baseUri, Iterable<UniqueId> uniqueIds) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventionSearches/bulk");
    bld.queryParam("id", IdUtils.toStringList(uniqueIds).toArray());
    return bld.build();
  }

  // deprecated
  //-------------------------------------------------------------------------

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearchList(URI baseUri, ExternalIdBundle bundle) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventionSearches/list");
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param bundle  the bundle, may be null
   * @param vc  the version-correction, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, ExternalIdBundle bundle, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventionSearches/single");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param bundle  the bundle, may be null
   * @param vc  the version-correction, may be null
   * @param type  the required type, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, ExternalIdBundle bundle, VersionCorrection vc, Class<?> type) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventionSearches/single");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    if (type != null) {
      bld.queryParam("type", type.getName());
    }
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

}
