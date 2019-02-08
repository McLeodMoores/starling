/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.exchange.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful URIs for exchanges.
 */
public class DataExchangeSourceUris {

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   *
   * @param baseUri
   *          the base URI, not null
   * @param vc
   *          the version-correction, null means latest
   * @param bundle
   *          the bundle, not null
   * @return the URI, not null
   */
  public static URI uriSearch(final URI baseUri, final VersionCorrection vc, final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("exchanges");
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
   * @param baseUri
   *          the base URI, not null
   * @param uniqueId
   *          the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("exchanges/{exchangeId}");
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
  public static URI uriGet(final URI baseUri, final ObjectId objectId, final VersionCorrection vc) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("exchanges/{exchangeId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  // deprecated
  //-------------------------------------------------------------------------

  /**
   * Builds a URI.
   *
   * @param baseUri
   *          the base URI, not null
   * @param bundle
   *          the bundle, not null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(final URI baseUri, final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("exchangeSearches/single");
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

}
