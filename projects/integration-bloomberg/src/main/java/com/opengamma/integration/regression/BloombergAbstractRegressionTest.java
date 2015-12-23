/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.regression;

import java.io.File;

/**
 *
 */
public abstract class BloombergAbstractRegressionTest extends AbstractRegressionTest {

  /**
   * Initializes the test. A valid tool context properties file is required - this cut down context is used to
   * initialize the database. Secondly, a "regressionPropertiesFile" is also required. This is used to execute
   * the views and must therefore contain a full engine configuration. Typically this can be created by using
   * the fullstack.properties/ini as a starting point, removing the enterprise services such as web and amq
   * exposure.
   *
   * @param regressionRoot  the root for this set of tests (i.e. the directory containing the dbdump zip and golden_copy folder
   * @param toolContextPropertiesFile  path to a valid tool context properties file
   * @param regressionPropertiesFile  path to a valid regression properties file
   */
  public BloombergAbstractRegressionTest(final File regressionRoot, final String toolContextPropertiesFile, final String regressionPropertiesFile) {
    super(regressionRoot, toolContextPropertiesFile, regressionPropertiesFile);
  }

  /**
   * Initializes the test using the default tool context properties file - this cut down context is used to
   * initialize the database. A "regressionPropertiesFile" is also required. This is used to execute
   * the views and must therefore contain a full engine configuration. Typically this can be created by using
   * the fullstack.properties/ini as a starting point, removing the enterprise services such as web and amq
   * exposure.
   *
   * @param regressionRoot  the root for this set of tests (i.e. the directory containing the dbdump zip and golden_copy folder
   * @param regressionPropertiesFile  path to a valid regression properties file
   */
  public BloombergAbstractRegressionTest(final File regressionRoot, final String regressionPropertiesFile) {
    super(regressionRoot, regressionPropertiesFile);
  }

  @Override
  protected AbstractRegressionTestToolContextManager createToolContextManager(final File regressionRoot, final String toolContextPropertiesFile, final String regressionPropertiesFile) {
    return new BloombergRegressionTestToolContextManager(new File(regressionRoot, AbstractGoldenCopyDumpCreator.DB_DUMP_ZIP), toolContextPropertiesFile, regressionPropertiesFile);
  }

}
