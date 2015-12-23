/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Throwables;
import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Manages the life-cycle of components. This class is based on the original implementation in <code>RegressionTestToolContextManager</code>.
 * However, it does not require the use of a reference data provider, which is only available if Bloomberg is used as a data provider.
 */
public abstract class AbstractRegressionTestToolContextManager {
  /** The default logging configuration file name */
  private static String s_logbackPropertyName = "logback.configurationFile";
  /** The default logging level */
  private static String s_logbackDefaultValue = "com/opengamma/util/warn-logback.xml";

  static {
    if (System.getProperty(s_logbackPropertyName) == null) {
      //if not explicitly set, default to a quieter setting.
      System.setProperty(s_logbackPropertyName, s_logbackDefaultValue);
    }
  }
  /** The tool context */
  private ToolContext _toolContext;
  /** The tool context properties file */
  private final String _toolContextPropertiesFile;
  /** The regression properties file */
  private final String _regressionPropertiesFile;
  /** The database dump file */
  private final File _dumpFile;

  /**
   * Initialize the context using the specified db dump file.
   * @param dumpFile  the dump file to use: a zip file, not null
   * @param toolContextPropertiesFile  a tool context, for use initializing the regression db, not null
   * @param regressionPropertiesFile  a full engine context, not null
   */
  public AbstractRegressionTestToolContextManager(final File dumpFile, final String toolContextPropertiesFile, final String regressionPropertiesFile) {
    _dumpFile = ArgumentChecker.notNull(dumpFile, "dumpFile");
    _toolContextPropertiesFile = ArgumentChecker.notNull(toolContextPropertiesFile, "toolContextPropertiesFile");
    _regressionPropertiesFile = ArgumentChecker.notNull(regressionPropertiesFile, "regressionPropertiesFile");
  }

  /**
   * Initializes the databases.
   */
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
   * @throws IOException  if the files cannot be found or created
   */
  private void initialiseDB() throws IOException  {
    System.out.println("Creating empty DB...");
    EmptyDatabaseCreator.createForConfig(_toolContextPropertiesFile);

    System.out.println("Creating tool context for DB...");
    final ToolContext toolContext = ToolContextUtils.getToolContext(_toolContextPropertiesFile, ToolContext.class);

    //assume this is a zipfile:
    restoreFromZipfile(toolContext, _dumpFile);

  }

  /**
   * Closes the tool context.
   */
  public void close() {
    if (_toolContext != null) {
      _toolContext.close();
    }
    //TODO delete the tmp db?
  }

  /**
   * Restores the database from a zip file to provide before values for the regression tests.
   * @param toolContext  the tool context, not null
   * @param zipFile  the zip file to restore from, not null
   * @throws IOException  if there is a problem reading the file
   */
  protected abstract void restoreFromZipfile(final ToolContext toolContext, final File zipFile) throws IOException;

  /**
   * Creates the tool context.
   */
  public void createToolContext() {
    _toolContext = ToolContextUtils.getToolContext(getRegressionPropertiesFile(), ToolContext.class);
  }

  /**
   * Gets the tool context.
   * @return the tool context managed by this instance
   */
  public ToolContext getToolContext() {
    return _toolContext;
  }

  /**
   * Gets the name of the properties file used in creating the context.
   * @return  the name of the properties file
   */
  public String getToolContextPropertiesFile() {
    return _toolContextPropertiesFile;
  }

  /**
   * Gets the name of the properties file used for regression testing.
   * @return  the name of the properties file
   */
  public String getRegressionPropertiesFile() {
    return _regressionPropertiesFile;
  }

  /**
   * Gets the database dump file.
   * @return  the file
   */
  public File getDumpFile() {
    return _dumpFile;
  }
}