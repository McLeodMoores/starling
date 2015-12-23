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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class GoldenCopyCreationTool extends AbstractGoldenCopyCreationTool<IntegrationToolContext> {
  /** Logger */
  private static final Logger s_logger = LoggerFactory.getLogger(GoldenCopyCreationTool.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new GoldenCopyCreationTool().invokeAndTerminate(args);
  }

  @Override
  protected void createDump(final String regressionDirectory) throws IOException {
    final File dumpFile = new File(regressionDirectory, AbstractGoldenCopyDumpCreator.DB_DUMP_ZIP);
    final RegressionIO io = ZipFileRegressionIO.createWriter(dumpFile, new FudgeXMLFormat());
    final IntegrationToolContext tc = getToolContext();

    final GoldenCopyDumpCreator goldenCopyDumpCreator = new GoldenCopyDumpCreator(io, tc);

    s_logger.info("Persisting db dump with tracked data");
    goldenCopyDumpCreator.execute();
  }

}
