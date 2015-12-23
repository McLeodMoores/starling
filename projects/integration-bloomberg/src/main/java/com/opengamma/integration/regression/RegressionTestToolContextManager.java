/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import com.opengamma.bbg.referencedata.impl.InMemoryCachingReferenceDataProvider;
import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Manages the lifecycle of components.
 */
public class RegressionTestToolContextManager {

  private ToolContext _toolContext;

  private static String s_logbackPropertyName = "logback.configurationFile";
  private static String s_logbackDefaultValue = "com/opengamma/util/warn-logback.xml";

  static {
    if (System.getProperty(s_logbackPropertyName) == null) {
      //if not explicitly set, default to a quieter setting.
      System.setProperty(s_logbackPropertyName, s_logbackDefaultValue);
    }
  }

  private final String _toolContextPropertiesFile;
  private final String _regressionPropertiesFile;

  private final File _dumpFile;

  /**
   * Initialize the context using the specified db dump file.
   * @param dumpFile the dump file to use: a zip file
   * @param toolContextPropertiesFile a tool context, for use initializing the regression db
   * @param regressionPropertiesFile a full engine context
   */
  public RegressionTestToolContextManager(final File dumpFile, final String toolContextPropertiesFile, final String regressionPropertiesFile) {
    _dumpFile = dumpFile;
    _toolContextPropertiesFile = toolContextPropertiesFile;
    _regressionPropertiesFile = regressionPropertiesFile;
  }



  public void init() {

    PlatformConfigUtils.configureSystemProperties();
    System.out.println("Initializing DB using tool context '" + _toolContextPropertiesFile + "'");
    try {
      initialiseDB();
    } catch (final IOException ex) {
      throw Throwables.propagate(ex);
    }
    System.out.println("Initialized DB");


  }


  /**
   * Create a new DB, schema, and populate tables.
   * @throws IOException
   */
  private void initialiseDB() throws IOException  {
    System.out.println("Creating empty DB...");
    EmptyDatabaseCreator.createForConfig(_toolContextPropertiesFile);

    System.out.println("Creating tool context for DB...");
    final ToolContext toolContext = ToolContextUtils.getToolContext(_toolContextPropertiesFile, ToolContext.class);

    //assume this is a zipfile:
    restoreFromZipfile(toolContext, _dumpFile);

  }

  private void restoreFromZipfile(final ToolContext toolContext, final File zipFile) throws IOException {
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
    System.out.println("Starting full context: '" + _regressionPropertiesFile + "'");
    _toolContext = ToolContextUtils.getToolContext(_regressionPropertiesFile, ToolContext.class);
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

  public void close() {
    if (_toolContext != null) {
      _toolContext.close();
    }
    //TODO delete the tmp db?
  }

  /**
   * @return the tool context managed by this instance
   */
  public ToolContext getToolContext() {
    return _toolContext;
  }


}
