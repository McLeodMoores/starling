/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.regression;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.google.common.base.Function;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.DataTrackingConfigMaster;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.DataTrackingConventionMaster;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.impl.DataTrackingExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.DataTrackingHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.DataTrackingHolidayMaster;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.impl.DataTrackingLegalEntityMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.DataTrackingMarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.DataTrackingPortfolioMaster;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.DataTrackingPositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.DataTrackingSecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Executes a DB dump, only including the records which have been accessed.
 */
public abstract class AbstractGoldenCopyDumpCreator {
  /**
   * The name of the output file.
   */
  public static final String DB_DUMP_ZIP = "dbdump.zip";
  /** Regression I/O */
  private final RegressionIO _regressionIO;
  /** The tool context */
  private final ToolContext _tc;

  /**
   * @param regressionIO  the regression I/O, not null
   * @param tc  the tool context, not null
   */
  public AbstractGoldenCopyDumpCreator(final RegressionIO regressionIO, final ToolContext tc) {
    _regressionIO = ArgumentChecker.notNull(regressionIO, "regressionIO");
    _tc = ArgumentChecker.notNull(tc, "tc");
  }

  /**
   * Run the db dump, building appropriate filters from the passed DataTracking masters.
   * @throws IOException  if there is a problem writing the output
   */
  public void execute() throws IOException {
    final MasterQueryManager filterManager = buildFilterManager();
    _regressionIO.beginWrite();
    try {
      //dump ref data accesses first
      recordDataAccessed();
      final DatabaseDump databaseDump = new DatabaseDump(_regressionIO,
                                                  _tc.getSecurityMaster(),
                                                  _tc.getPositionMaster(),
                                                  _tc.getPortfolioMaster(),
                                                  _tc.getConfigMaster(),
                                                  _tc.getHistoricalTimeSeriesMaster(),
                                                  _tc.getHolidayMaster(),
                                                  _tc.getExchangeMaster(),
                                                  _tc.getMarketDataSnapshotMaster(),
                                                  _tc.getLegalEntityMaster(),
                                                  _tc.getConventionMaster(),
                                                  filterManager);

      databaseDump.dumpDatabase();
    } finally {
      _regressionIO.endWrite();
    }
  }

  /**
   * Gets the regression I/O.
   * @return  the regression I/O
   */
  public RegressionIO getRegressionIO() {
    return _regressionIO;
  }

  /**
   * Records the data that has been accessed.
   * @throws IOException  if there is a problem writing the output
   */
  protected abstract void recordDataAccessed() throws IOException;

  /**
   * Builds a filter manager for the ids used when creating the dump.
   * @return  the manager
   */
  private MasterQueryManager buildFilterManager() {
    return new MasterQueryManager(
        new UniqueIdentifiableQuery<SecurityDocument, SecurityMaster>(((DataTrackingSecurityMaster) _tc.getSecurityMaster()).getIdsAccessed()),
        new UniqueIdentifiableQuery<PositionDocument, PositionMaster>(((DataTrackingPositionMaster) _tc.getPositionMaster()).getIdsAccessed()),
        new UniqueIdentifiableQuery<PortfolioDocument, PortfolioMaster>(((DataTrackingPortfolioMaster) _tc.getPortfolioMaster()).getIdsAccessed()),
        new UniqueIdentifiableQuery<ConfigDocument, ConfigMaster>(((DataTrackingConfigMaster) _tc.getConfigMaster()).getIdsAccessed()),
        new UniqueIdentifiableQuery<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster>(((DataTrackingHistoricalTimeSeriesMaster) _tc.getHistoricalTimeSeriesMaster()).getIdsAccessed()),
        new UniqueIdentifiableQuery<HolidayDocument, HolidayMaster>(((DataTrackingHolidayMaster) _tc.getHolidayMaster()).getIdsAccessed()),
        new UniqueIdentifiableQuery<ExchangeDocument, ExchangeMaster>(((DataTrackingExchangeMaster) _tc.getExchangeMaster()).getIdsAccessed()),
        new UniqueIdentifiableQuery<MarketDataSnapshotDocument, MarketDataSnapshotMaster>(((DataTrackingMarketDataSnapshotMaster) _tc.getMarketDataSnapshotMaster()).getIdsAccessed()),
        new UniqueIdentifiableQuery<LegalEntityDocument, LegalEntityMaster>(((DataTrackingLegalEntityMaster) _tc.getLegalEntityMaster()).getIdsAccessed()),
        new UniqueIdentifiableQuery<ConventionDocument, ConventionMaster>(((DataTrackingConventionMaster) _tc.getConventionMaster()).getIdsAccessed()));
  }

  /**
   * Filter which checks a {@link UniqueIdentifiable} object is identified by one of
   * a set of ids.
   */
  private static class UniqueIdentifiableQuery<D extends AbstractDocument, M extends AbstractMaster<D>> implements Function<M, Collection<D>> {
    /** The ids to include */
    private final Set<UniqueId> _idsToInclude;

    /**
     * Creates a query.
     * @param uniqueId  the unique ids
     */
    public UniqueIdentifiableQuery(final Set<UniqueId> uniqueId) {
      _idsToInclude = uniqueId;
    }

    @Override
    public Collection<D> apply(final M input) {
      return input.get(_idsToInclude).values();
    }

  }

}
