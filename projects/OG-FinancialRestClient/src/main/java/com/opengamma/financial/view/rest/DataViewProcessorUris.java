/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter.Mode;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.id.UniqueId;

/**
 * RESTful URIs for a {@link ViewProcessor}.
 */
public class DataViewProcessorUris {

  /**
   * The period after which, if a view client has not been accessed, it may be shut down.
   */
  public static final long VIEW_CLIENT_TIMEOUT_MILLIS = 30000;
  /**
   * URI path to the config source.
   */
  public static final String PATH_CONFIG_SOURCE = "configSource";
  /**
   * URI path to the market data repository.
   */
  public static final String PATH_NAMED_MARKET_DATA_SPEC_REPOSITORY = "namedMarketDataSpecRepository";
  /**
   * URI path to the name.
   */
  public static final String PATH_NAME = "name";
  /**
   * URI path to the clients.
   */
  public static final String PATH_CLIENTS = "clients";
  /**
   * URI path to the processes.
   */
  public static final String PATH_PROCESSES = "processes";
  /**
   * URI path to the cycles.
   */
  public static final String PATH_CYCLES = "cycles";
  /**
   * URI path to the snapshotter.
   */
  public static final String PATH_SNAPSHOTTER = "marketDataSnapshotter";
 
  public static URI uriViewProcess(final URI baseUri, final UniqueId viewProcessId) {
    // WARNING: '/' characters could well appear in the view name
    // There is a bug(?) in UriBuilder where, even though segment() is meant to treat the item as a single path segment
    // and therefore encode '/' characters, it does not encode '/' characters which come from a variable substitution.
    return UriBuilder.fromUri(baseUri).path("processes").segment(viewProcessId.toString()).build();
  }

  public static URI uriClient(final URI clientsBaseUri, final UniqueId viewClientId) {
    return UriBuilder.fromUri(clientsBaseUri).segment(viewClientId.toString()).build();
  }

  public static URI uriSnapshotter(final URI clientsBaseUri, final Mode mode) {
    return UriBuilder.fromUri(clientsBaseUri).path(PATH_SNAPSHOTTER).segment(mode.name()).build();
  }

  /*package*/ static URI getViewProcessorUri(final UriInfo uriInfo) {
    return uriInfo.getBaseUri().resolve(UriBuilder.fromUri(uriInfo.getMatchedURIs().get(1)).build());
  }

}
