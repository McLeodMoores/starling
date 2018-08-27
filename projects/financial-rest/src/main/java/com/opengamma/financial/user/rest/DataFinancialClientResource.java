/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.ircurve.rest.DataInterpolatedYieldCurveDefinitionMasterResource;
import com.opengamma.financial.user.FinancialClient;
import com.opengamma.master.config.impl.DataConfigMasterResource;
import com.opengamma.master.marketdatasnapshot.impl.DataMarketDataSnapshotMasterResource;
import com.opengamma.master.portfolio.impl.DataPortfolioMasterResource;
import com.opengamma.master.position.impl.DataPositionMasterResource;
import com.opengamma.master.security.impl.DataSecurityMasterResource;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a single client of a single user.
 * <p>
 * This resource receives and processes RESTful calls.
 */
public class DataFinancialClientResource extends AbstractDataResource {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DataFinancialClientResource.class);

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
   * The client.
   */
  private final FinancialClient _client;

  /**
   * Creates an instance.
   *
   * @param client  the client, not null
   */
  public DataFinancialClientResource(final FinancialClient client) {
    _client = client;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the client.
   *
   * @return the client, not null
   */
  public FinancialClient getClient() {
    return _client;
  }

  //-------------------------------------------------------------------------
  @Path(SECURITY_MASTER_PATH)
  public DataSecurityMasterResource getSecurityMaster() {
    LOGGER.debug("Accessed UserSecurityMaster for {}", getClient());
    return new DataSecurityMasterResource(getClient().getSecurityMaster());
  }

  @Path(POSITION_MASTER_PATH)
  public DataPositionMasterResource getPositionMaster() {
    LOGGER.debug("Accessed UserPositionMaster for {}", getClient());
    return new DataPositionMasterResource(getClient().getPositionMaster());
  }

  @Path(PORTFOLIO_MASTER_PATH)
  public DataPortfolioMasterResource getPortfolioMaster() {
    LOGGER.debug("Accessed UserPortfolioMaster for {}", getClient());
    return new DataPortfolioMasterResource(getClient().getPortfolioMaster());
  }

  @Path(CONFIG_MASTER_PATH)
  public DataConfigMasterResource getConfigMaster() {
    LOGGER.debug("Accessed UserViewDefinitionMaster for {}", getClient());
    return new DataConfigMasterResource(getClient().getConfigMaster());
  }

  @Path(INTERPOLATED_YIELD_CURVE_DEFINITION_MASTER_PATH)
  public DataInterpolatedYieldCurveDefinitionMasterResource getInterpolatedYieldCurveDefinitionMaster() {
    LOGGER.debug("Accessed UserYieldCurveMaster for {}", getClient());
    return new DataInterpolatedYieldCurveDefinitionMasterResource(getClient().getInterpolatedYieldCurveDefinitionMaster());
  }

  @Path(MARKET_DATA_SNAPSHOT_MASTER_PATH)
  public DataMarketDataSnapshotMasterResource getSnapshotMaster() {
    LOGGER.debug("Accessed UserSnapshotMaster for {}", getClient());
    return new DataMarketDataSnapshotMasterResource(getClient().getSnapshotMaster());
  }

  //-------------------------------------------------------------------------
  @POST
  @Path(HEARTBEAT_PATH)
  public void heartbeat() {
    LOGGER.debug("Heartbeat received from {}", getClient());
    getClient().updateLastAccessed();
  }
}
