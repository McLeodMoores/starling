/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.simulatedexamples;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.scripts.Scriptable;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.db.tool.DbTool;

@Scriptable
public class TestFinmathDatabaseCreator {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(TestFinmathDatabaseCreator.class);

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
   * It relies on the OG-Web directory being relative to Examples-Simulated in the file
   * system as per a standard checkout of OG-Platform.
   *
   * @param args the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    final String[] databaseArgs;
    if (args.length == 0) {
      databaseArgs = new String[] {"classpath:/toolcontext/toolcontext.properties"};
    } else {
      databaseArgs = args;
    }
    try {
      new TestFinmathDatabaseCreator().run(databaseArgs[0]);
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
    new TestFinmathDatabasePopulator().run(ResourceUtils.toResourceLocator(res), ToolContext.class);

    s_logger.warn("Successfully created test databases");
  }

}
