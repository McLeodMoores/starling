/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.IOException;

import com.opengamma.financial.tool.ToolContext;

/**
 * Manages the lifecycle of components used in regression testing. This class uses the functionality of {@link BloombergRegressionTestToolContextManager},
 * which should be used instead of this implementation.
 * @deprecated  Use {@link BloombergRegressionTestToolContextManager}.
 */
@Deprecated
public class RegressionTestToolContextManager extends AbstractRegressionTestToolContextManager {
  /** The delegate manager */
  private final AbstractRegressionTestToolContextManager _delegate;

  /**
   * Initialize the context using the specified db dump file.
   * @param dumpFile the dump file to use: a zip file
   * @param toolContextPropertiesFile a tool context, for use initializing the regression db
   * @param regressionPropertiesFile a full engine context
   */
  public RegressionTestToolContextManager(final File dumpFile, final String toolContextPropertiesFile, final String regressionPropertiesFile) {
    super(dumpFile, toolContextPropertiesFile, regressionPropertiesFile);
    _delegate = new BloombergRegressionTestToolContextManager(dumpFile, toolContextPropertiesFile, regressionPropertiesFile);
  }

  @Override
  protected void restoreFromZipfile(final ToolContext toolContext, final File zipFile) throws IOException {
    _delegate.restoreFromZipfile(toolContext, zipFile);
  }

}
