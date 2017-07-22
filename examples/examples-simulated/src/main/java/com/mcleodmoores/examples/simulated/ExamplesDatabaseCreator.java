/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.examples.simulated;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.scripts.Scriptable;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.db.tool.DbTool;


/**
 * Tool class that creates and initializes the example database.
 */
@Scriptable
public class ExamplesDatabaseCreator {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExamplesDatabaseCreator.class);

  /** Shared database URL. */
  private static final String KEY_SHARED_URL = "db.standard.url";
  /** Shared database user name. */
  private static final String KEY_SHARED_USER_NAME = "db.standard.username";
  /** Shared database password. */
  private static final String KEY_SHARED_PASSWORD = "db.standard.password";
  /** Temporary user database URL. */
  private static final String KEY_USERFINANCIAL_URL = "db.userfinancial.url";
  /** Temporary user database user name. */
  private static final String KEY_USERFINANCIAL_USER_NAME = "db.userfinancial.username";
  /** Temporary user database password. */
  private static final String KEY_USERFINANCIAL_PASSWORD = "db.userfinancial.password";
  /** Catalog. */
  private static final String CATALOG = "og-financial";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool. No arguments are needed.
   * <p>
   * If the command line is empty, the "development" configuration file is started.
   * This file is intended for use with an IDE and a checked out source code tree.
   * It relies on the <code>web</code> directory being relative to <code>examples-simulated</code> in the file
   * system as per a standard checkout of Starling.
   *
   * @param args the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    if (args.length == 0) {
      // if no command line arguments, then use default arguments suitable for development in an IDE
      // the first argument is for verbose startup, to aid understanding
      // the second argument defines the start of a chain of properties files providing the configuration
      args = new String[] {"classpath:/toolcontext/toolcontext-mmexamples.properties"};
    }
    try {
      new ExamplesDatabaseCreator().run(args[0]);
      System.exit(0);
    } catch (final Exception ex) {
      s_logger.error("Caught exception", ex);
      ex.printStackTrace();
      System.exit(1);
    }
  }

  //-------------------------------------------------------------------------
  private void run(final String configFile) throws Exception {
    final Resource res = ResourceUtils.createResource(configFile);
    final Properties props = new Properties();
    try (InputStream in = res.getInputStream()) {
      if (in == null) {
        throw new FileNotFoundException(configFile);
      }
      props.load(in);
    }
    // Version Correction
    ThreadLocalServiceContext.init(ServiceContext.of(VersionCorrectionProvider.class, new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    }));

    // create main database
    s_logger.warn("Creating main database...");
    final DbTool dbTool = new DbTool();
    dbTool.setJdbcUrl(Objects.requireNonNull(props.getProperty(KEY_SHARED_URL)));
    dbTool.setUser(props.getProperty(KEY_SHARED_USER_NAME, ""));
    dbTool.setPassword(props.getProperty(KEY_SHARED_PASSWORD, ""));
    dbTool.setCatalog(CATALOG);  // ignored, as it is parsed from the url
    dbTool.setCreate(true);
    dbTool.setDrop(true);
    dbTool.setCreateTables(true);
    dbTool.execute();

    // create user database
    s_logger.warn("Creating user database...");
    final DbTool dbToolUser = new DbTool();
    dbToolUser.setJdbcUrl(Objects.requireNonNull(props.getProperty(KEY_USERFINANCIAL_URL)));
    dbToolUser.setUser(props.getProperty(KEY_USERFINANCIAL_USER_NAME, ""));
    dbToolUser.setPassword(props.getProperty(KEY_USERFINANCIAL_PASSWORD, ""));
    dbToolUser.setCatalog(CATALOG);  // ignored, as it is parsed from the url
    dbToolUser.setCreate(true);
    dbToolUser.setDrop(true);
    dbToolUser.setCreateTables(true);
    dbToolUser.execute();
    //


    // populate the database
    s_logger.warn("Populating main database...");
    new ExamplesDatabasePopulator().run(ResourceUtils.toResourceLocator(res), ToolContext.class);

    s_logger.warn("Successfully created example databases");
  }

}
