/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import org.fudgemsg.FudgeContext;

import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 * Context/configuration for the objects to pass around.
 */
public class FinancialUserServices {

  private FudgeContext _fudgeContext;
  private PortfolioMaster _userPortfolioMaster;
  private PositionMaster _userPositionMaster;
  private SecurityMaster _userSecurityMaster;
  private ConfigMaster _userConfigMaster;
  private MarketDataSnapshotMaster _userSnapshotMaster;

  public void setUserPortfolioMaster(final PortfolioMaster portfolioMaster) {
    _userPortfolioMaster = portfolioMaster;
  }

  public PortfolioMaster getPortfolioMaster() {
    return _userPortfolioMaster;
  }

  public void setUserPositionMaster(final PositionMaster positionMaster) {
    _userPositionMaster = positionMaster;
  }

  public PositionMaster getPositionMaster() {
    return _userPositionMaster;
  }

  public void setUserSecurityMaster(final SecurityMaster securityMaster) {
    _userSecurityMaster = securityMaster;
  }

  public SecurityMaster getSecurityMaster() {
    return _userSecurityMaster;
  }

  public void setUserConfigMaster(final ConfigMaster configMaster) {
    _userConfigMaster = configMaster;
  }

  public ConfigMaster getConfigMaster() {
    return _userConfigMaster;
  }

  public void setUserSnapshotMaster(final MarketDataSnapshotMaster snapshotMaster) {
    _userSnapshotMaster = snapshotMaster;
  }

  public MarketDataSnapshotMaster getSnapshotMaster() {
    return _userSnapshotMaster;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

}
