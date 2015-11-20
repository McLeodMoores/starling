/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
import com.opengamma.id.UniqueId;

/**
 * RESTful URIs for a {@link MarketDataSnapshotter}.
 */
public class DataMarketDataSnapshotterUris {

  //CSOFF: just constants
  public static final String PATH_CREATE_SNAPSHOT = "create";
  public static final String PATH_YIELD_CURVE_SPECS = "yieldCurveSpecs";
  //CSON: just constants

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param viewClientId  the unique id of the view client, not null
   * @param viewCycleId  the unique id of the view cycle, not null
   * @return the URI, not null
   */
  public static URI uriCreateSnapshot(URI baseUri, UniqueId viewClientId, UniqueId viewCycleId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path(PATH_CREATE_SNAPSHOT + "/" + viewClientId + "/" + viewCycleId);
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param viewClientId  the unique id of the view client, not null
   * @param viewCycleId  the unique id of the view cycle, not null
   * @return the URI, not null
   */
  public static URI uriGetYieldCurveSpecs(URI baseUri, UniqueId viewClientId, UniqueId viewCycleId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path(PATH_YIELD_CURVE_SPECS + "/" + viewClientId + "/" + viewCycleId);    
    return bld.build();
  }

}
