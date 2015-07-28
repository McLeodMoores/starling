/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful URIs for configuration.
 */
public class DataConfigSourceUris {
  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @param name the name, may be null
   * @param versionCorrection the version to fetch, null means latest
   * @param type the config type, may be null
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final String name, final VersionCorrection versionCorrection, final Class<?> type) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(type, "type");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs");
    bld.queryParam("name", name);
    bld.queryParam("type", type.getName());
    if (versionCorrection != null) {
      bld.queryParam("versionCorrection", versionCorrection.toString());
    } else {
      bld.queryParam("versionCorrection", VersionCorrection.LATEST.toString());
    }
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final UniqueId uniqueId) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs/{uid}");
    return bld.build(uniqueId);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @param versionCorrection  the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(objectId, "objectId");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs/{oid}/{versionCorrection}");
    versionCorrection = versionCorrection != null ? versionCorrection : VersionCorrection.LATEST;
    return bld.build(objectId, versionCorrection);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param name  the name, may be null
   * @param versionCorrection  the version to fetch, null means latest
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(final URI baseUri, final String name, final VersionCorrection versionCorrection, final Class<?> type) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(type, "type");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configSearches/single");
    bld.queryParam("name", name);
    bld.queryParam("type", type.getName());
    if (versionCorrection != null) {
      bld.queryParam("versionCorrection", versionCorrection.toString());
    } else {
      bld.queryParam("versionCorrection", VersionCorrection.LATEST.toString());
    }
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param type  the config type, may be null
   * @param versionCorrection  the version to fetch, null means latest
   *
   * @return the URI, not null
   */
  public static URI uriSearch(final URI baseUri, final Class<?> type, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(type, "type");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configSearches");
    bld.queryParam("type", type.getName());
    if (versionCorrection != null) {
      bld.queryParam("versionCorrection", versionCorrection.toString());
    } else {
      bld.queryParam("versionCorrection", VersionCorrection.LATEST.toString());
    }
    return bld.build();
  }

  public static <T> URI uriPut(final URI baseUri) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("put");
    return bld.build();
  }

}
