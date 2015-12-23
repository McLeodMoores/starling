/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.regression;

import java.io.File;
import java.io.IOException;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.regression.AbstractRegressionTestToolContextManager;
import com.opengamma.integration.regression.DatabaseRestore;
import com.opengamma.integration.regression.FudgeXMLFormat;
import com.opengamma.integration.regression.ZipFileRegressionIO;

/**
 *
 */
public class FutureViewRegressionTestToolContextManager extends AbstractRegressionTestToolContextManager {
  /**
   * Initialize the context using the specified db dump file.
   * @param dumpFile  the dump file to use: a zip file
   * @param toolContextPropertiesFile  a tool context, for use initializing the regression db
   * @param regressionPropertiesFile  a full engine context
   */
  public FutureViewRegressionTestToolContextManager(final File dumpFile, final String toolContextPropertiesFile, final String regressionPropertiesFile) {
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
    createToolContext();
    System.out.println("Full context started");
  }
}