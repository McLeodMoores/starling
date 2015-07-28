/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * RESTful URIs for currency matrices.
 */
public class DataCurrencyMatrixSourceUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @param name the name, not null
   * @param versionCorrection the version/correction timestamp
   * @return the URI, not null
   */
  public static URI uriGetMatrix(URI baseUri, String name, VersionCorrection versionCorrection) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/currencyMatricesByName/{versionCorrection}/{name}");
    versionCorrection = versionCorrection != null ? versionCorrection : VersionCorrection.LATEST;
    return bld.build(versionCorrection, name);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @param identifier the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriGetMatrix(URI baseUri, UniqueId identifier) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/currencyMatrices/{identifier}");
    return bld.build(identifier);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @param identifier the object identifier, not null
   * @param versionCorrection the version/correction timestamp
   * @return the URI, not null
   */
  public static URI uriGetMatrix(URI baseUri, ObjectId identifier, VersionCorrection versionCorrection) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/currencyMatrices/{identifier}/{versionCorrection}");
    versionCorrection = versionCorrection != null ? versionCorrection : VersionCorrection.LATEST;
    return bld.build(identifier, versionCorrection);
  }

}
