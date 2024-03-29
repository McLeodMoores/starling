/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.server.copier;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ResourceUtils;

/**
 * Single class that populates the standalone database with data from a given server URL.
 * <p>
 * It is designed to run against the HSQLDB example database.
 */
@Scriptable
public class ServerDatabasePopulator {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerDatabasePopulator.class);

  private final String _configFile;
  private final DatabasePopulatorTool _populatorTool;

  public ServerDatabasePopulator(final String configFile, final DatabasePopulatorTool populatorTool) {
    ArgumentChecker.notNull(configFile, "configFile");
    ArgumentChecker.notNull(populatorTool, "populatorTool");

    _configFile = configFile;
    _populatorTool = populatorTool;
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool. No arguments are needed.
   *
   * @param args  the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    LOGGER.info("Populating demo server database");
    try {
      final CommandLineOption option = new CommandLineOption(args, ServerDatabasePopulator.class);
      final String configFile = StringUtils.trimToNull(option.getConfigFile());
      final String serverUrl = StringUtils.trimToNull(option.getServerUrl());

      if (configFile != null && serverUrl != null) {
        final ServerDatabasePopulator populator = new ServerDatabasePopulator(configFile, new DatabasePopulatorTool(serverUrl));
        populator.run();
      }
      System.exit(0);
    } catch (final Exception ex) {
      LOGGER.error("Caught exception", ex);
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public void run() throws Exception {
    final Resource res = ResourceUtils.createResource(_configFile);
    final Properties props = new Properties();
    try (InputStream in = res.getInputStream()) {
      if (in == null) {
        throw new FileNotFoundException(_configFile);
      }
      props.load(in);
    }
    _populatorTool.run(ResourceUtils.toResourceLocator(res), ToolContext.class);
  }
}
