/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful URIs for snapshots.
 */
public class DataMarketDataSnapshotSourceUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("snapshots/{snapshotId}");
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
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("snapshots/{snapshotId}");
    return bld.build(objectId);
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("snapshots/{snapshotId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  /**
   * Builds a URI for snapshot search.
   *
   * @param baseUri  the base URI, not null
   * @param type  the snapshot type, not null
   * @param name  the name, not null
   * @param versionCorrection  the version to fetch, null means latest
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, Class<?> type, String name, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(name, "name");

    String vc = versionCorrection != null ?
        versionCorrection.toString() :
        VersionCorrection.LATEST.toString();

    return UriBuilder.fromUri(baseUri)
        .path("snapshotSearches/single")
        .queryParam("name", name)
        .queryParam("type", type.getName())
        .queryParam("versionCorrection", vc)
        .build();
  }
}
