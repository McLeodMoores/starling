/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import com.google.common.collect.Multimap;
import com.opengamma.bbg.referencedata.impl.InMemoryCachingReferenceDataProvider;
import com.opengamma.financial.tool.ToolContext;

/**
 * Manages the lifecycle of components used in regression testing. This class uses an in-memory caching reference data provider,
 * which is functionality that is currently only available when Bloomberg is used as a data provider.
 */
public class BloombergRegressionTestToolContextManager extends AbstractRegressionTestToolContextManager {
  /**
   * Initialize the context using the specified db dump file.
   * @param dumpFile  the dump file to use: a zip file
   * @param toolContextPropertiesFile  a tool context, for use initializing the regression db
   * @param regressionPropertiesFile  a full engine context
   */
  public BloombergRegressionTestToolContextManager(final File dumpFile, final String toolContextPropertiesFile, final String regressionPropertiesFile) {
    super(dumpFile, toolContextPropertiesFile, regressionPropertiesFile);
  }

  @Override
  protected void restoreFromZipfile(final ToolContext toolContext, final File zipFile) throws IOException {
    final ZipFileRegressionIO io = ZipFileRegressionIO.createReader(zipFile, new FudgeXMLFormat());
    final DatabaseRestore restore = new DatabaseRestore(
        io,
        toolContext.getSecurityMaster(),
        toolContext.getPositionMaster(),
        toolContext.getPortfolioMaster(),
        toolContext.getConfigMaster(),
        toolContext.getHistoricalTimeSeriesMaster(),
        toolContext.getHolidayMaster(),
        toolContext.getExchangeMaster(),
        toolContext.getMarketDataSnapshotMaster(),
        toolContext.getLegalEntityMaster(),
        toolContext.getConventionMaster()
    );

    System.out.println("Initializing DB state...");
    restore.restoreDatabase();
    toolContext.close();

    //start toolcontext
    System.out.println("Starting full context: '" + getRegressionPropertiesFile() + "'");
    // the caching reference data provider uses the data directly, so no need to populate the underlying provider
    final InMemoryCachingReferenceDataProvider refDataProvider = new InMemoryCachingReferenceDataProvider(new MapReferenceDataProvider(Collections.<String, Multimap<String, String>>emptyMap()));

    try {
      io.beginRead();
      final RegressionReferenceData data = (RegressionReferenceData) io.read(null, RegressionUtils.REF_DATA_ACCESSES_IDENTIFIER);
      io.endRead();
      refDataProvider.addToCache(data.getReferenceData());
    } catch (final Exception e) {
      //ignore
    }
    System.out.println("Full context started");
  }

}
