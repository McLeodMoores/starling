/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * RESTful URIs for a single client of a single user.
 */
public class DataFinancialClientUris {

  /**
   * The path used to retrieve user portfolios.
   */
  public static final String PORTFOLIO_MASTER_PATH = "portfolioMaster";
  /**
   * The path used to retrieve user positions.
   */
  public static final String POSITION_MASTER_PATH = "positionMaster";
  /**
   * The path used to retrieve user securities.
   */
  public static final String SECURITY_MASTER_PATH = "securityMaster";
  /**
   * The path used to retrieve user configurations.
   */
  public static final String CONFIG_MASTER_PATH = "configMaster";
  /**
   * The path used to retrieve yield curve definitions.
   */
  public static final String INTERPOLATED_YIELD_CURVE_DEFINITION_MASTER_PATH = "interpolatedYieldCurveDefinitionMaster";
  /**
   * The path used to retrieve user snapshots.
   */
  public static final String MARKET_DATA_SNAPSHOT_MASTER_PATH = "snapshotMaster";
  /**
   * The path used to signal a heartbeat if no actual transactions are being done.
   */
  public static final String HEARTBEAT_PATH = "heartbeat";

 
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @return the URI, not null
   */
  public static URI uriSecurityMaster(URI baseUri, String userName, String clientName) {
    UriBuilder bld = UriBuilder.fromUri(DataFinancialClientManagerUris.uriClient(baseUri, userName, clientName)).path(SECURITY_MASTER_PATH);
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @return the URI, not null
   */
  public static URI uriPositionMaster(URI baseUri, String userName, String clientName) {
    UriBuilder bld = UriBuilder.fromUri(DataFinancialClientManagerUris.uriClient(baseUri, userName, clientName)).path(POSITION_MASTER_PATH);
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @return the URI, not null
   */
  public static URI uriPortfolioMaster(URI baseUri, String userName, String clientName) {
    UriBuilder bld = UriBuilder.fromUri(DataFinancialClientManagerUris.uriClient(baseUri, userName, clientName)).path(PORTFOLIO_MASTER_PATH);
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @return the URI, not null
   */
  public static URI uriConfigMaster(URI baseUri, String userName, String clientName) {
    UriBuilder bld = UriBuilder.fromUri(DataFinancialClientManagerUris.uriClient(baseUri, userName, clientName)).path(CONFIG_MASTER_PATH);
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @return the URI, not null
   */
  public static URI uriInterpolatedYieldCurveDefinitionMaster(URI baseUri, String userName, String clientName) {
    UriBuilder bld = UriBuilder.fromUri(DataFinancialClientManagerUris.uriClient(baseUri, userName, clientName)).path(INTERPOLATED_YIELD_CURVE_DEFINITION_MASTER_PATH);
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @return the URI, not null
   */
  public static URI uriSnapshotMaster(URI baseUri, String userName, String clientName) {
    UriBuilder bld = UriBuilder.fromUri(DataFinancialClientManagerUris.uriClient(baseUri, userName, clientName)).path(MARKET_DATA_SNAPSHOT_MASTER_PATH);
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @return the URI, not null
   */
  public static URI uriHeartbeat(URI baseUri, String userName, String clientName) {
    UriBuilder bld = UriBuilder.fromUri(DataFinancialClientManagerUris.uriClient(baseUri, userName, clientName)).path(HEARTBEAT_PATH);
    return bld.build();
  }

}
