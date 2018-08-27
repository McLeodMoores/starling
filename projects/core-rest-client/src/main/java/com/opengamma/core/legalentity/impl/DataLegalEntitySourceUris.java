/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * RESTful URIs for legal entities.
 */
public class DataLegalEntitySourceUris {

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @param vc      the version-correction, null means latest
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearch(final URI baseUri, final VersionCorrection vc, final ExternalIdBundle bundle) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("legal entities");
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
   * @param uniqueId the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final UniqueId uniqueId) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("legal entities/{legalentityId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param objectId the object identifier, may be null
   * @param vc       the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final ObjectId objectId, final VersionCorrection vc) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("legal entities/{legalentityId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri   the base URI, not null
   * @param uniqueIds the unique identifiers, may be null
   * @return the URI, not null
   */
  public static URI uriBulk(final URI baseUri, final Iterable<UniqueId> uniqueIds) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitySearches/bulk");
    bld.queryParam("id", IdUtils.toStringList(uniqueIds).toArray());
    return bld.build();
  }

  // deprecated
  //-------------------------------------------------------------------------

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearchList(final URI baseUri, final ExternalIdBundle bundle) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitySearches/list");
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @param bundle  the bundle, may be null
   * @param vc      the version-correction, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(final URI baseUri, final ExternalIdBundle bundle, final VersionCorrection vc) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitySearches/single");
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
   * @param baseUri the base URI, not null
   * @param bundle  the bundle, may be null
   * @param vc      the version-correction, may be null
   * @param type    the required type, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(final URI baseUri, final ExternalIdBundle bundle, final VersionCorrection vc, final Class<?> type) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitySearches/single");
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
