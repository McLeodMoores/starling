/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

/**
 * Enum specifying the types of master in the system.
 */
public enum MasterType {
  /** {@link com.opengamma.master.portfolio.PortfolioMaster}. */
  PORTFOLIO,
  /** {@link com.opengamma.master.position.PositionMaster}. */
  POSITION,
  /** {@link com.opengamma.master.holiday.HolidayMaster}. */
  HOLIDAY,
  /** {@link com.opengamma.master.security.SecurityMaster}. */
  SECURITY,
  /** {@link com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster}. */
  TIME_SERIES,
  /** {@link com.opengamma.master.config.ConfigMaster}. */
  CONFIG,
  /** {@link com.opengamma.master.legalentity.LegalEntityMaster}. */
  ORGANIZATION,
  /** {@link com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster}. */
  LEGAL_ENTITY,
  /** {@link com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster}. */
  MARKET_DATA_SNAPSHOT,
  /** {@link com.opengamma.master.convention.ConventionMaster}. */
  CONVENTION,
  // TODO all the other masters
}
