/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static com.opengamma.integration.regression.PropertiesUtils.createProperties;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.db.tool.DbTool;

/**
 *
 */
public class EmptyDatabaseCreator {

  private static final String MANAGER_INCLUDE = "MANAGER.INCLUDE";

  private static final Logger LOGGER = LoggerFactory.getLogger(EmptyDatabaseCreator.class);

  /** Shared database URL. */
  public static final String KEY_SHARED_URL = "db.standard.url";
  /** Shared database user name. */
  public static final String KEY_SHARED_USER_NAME = "db.standard.username";
  /** Shared database password. */
  public static final String KEY_SHARED_PASSWORD = "db.standard.password";
  /** Temporary user database URL. */
  public static final String KEY_USERFINANCIAL_URL = "db.userfinancial.url";
  /** Temporary user database user name. */
  public static final String KEY_USERFINANCIAL_USER_NAME = "db.userfinancial.username";
  /** Temporary user database password. */
  public static final String KEY_USERFINANCIAL_PASSWORD = "db.userfinancial.password";
  /** Catalog. */
  private static final String CATALOG = "og-financial";

  public static void main(final String[] args) throws IOException {
    if (args.length == 0) {
      throw new IllegalArgumentException("Argument required specifying configuration file");
    }
    EmptyDatabaseCreator.createDatabases(createProperties(args[0]));
  }

  public static void createForConfig(final String configFile) {

    final Properties allProperties = createProperties(configFile);

    //loosely adds support for includes:
    for (Properties lastProperties = allProperties; lastProperties.containsKey(MANAGER_INCLUDE); ) {
      final Properties properties = createProperties(lastProperties.getProperty(MANAGER_INCLUDE));
      allProperties.putAll(properties);
      lastProperties = properties;
    };

    createDatabases(allProperties);
  }


  public static void createDatabases(final Properties props) {
    // create main database
    LOGGER.info("Creating main database using properties {}", props);
    final DbTool dbTool = new DbTool();
    dbTool.setJdbcUrl(Objects.requireNonNull(props.getProperty(KEY_SHARED_URL)));
    dbTool.setUser(props.getProperty(KEY_SHARED_USER_NAME, ""));
    dbTool.setPassword(props.getProperty(KEY_SHARED_PASSWORD, ""));
    dbTool.setCatalog(CATALOG);  // ignored, as it is parsed from the url
    dbTool.setCreate(true);
    dbTool.setDrop(true);
    dbTool.setCreateTables(true);
    dbTool.execute();
    dbTool.resetTestCatalog();

    // create user database
    LOGGER.info("Creating user database using properties {}", props);
    final DbTool dbToolUser = new DbTool();
    dbToolUser.setJdbcUrl(Objects.requireNonNull(props.getProperty(KEY_USERFINANCIAL_URL)));
    dbToolUser.setUser(props.getProperty(KEY_USERFINANCIAL_USER_NAME, ""));
    dbToolUser.setPassword(props.getProperty(KEY_USERFINANCIAL_PASSWORD, ""));
    dbToolUser.setCatalog(CATALOG);  // ignored, as it is parsed from the url
    dbToolUser.setCreate(true);
    dbToolUser.setDrop(true);
    dbToolUser.setCreateTables(true);
    dbToolUser.execute();
    dbToolUser.resetTestCatalog();
  }
}
