/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.testng.annotations.DataProvider;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.script.DbSchemaGroupMetadata;
import com.opengamma.util.db.script.DbScriptUtils;
import com.opengamma.util.db.tool.DbDialectUtils;
import com.opengamma.util.db.tool.DbTool;

/**
 * Utilities to support database testing.
 */
public final class DbTest {

  /**
   * Creates an instance.
   */
  private DbTest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the parameters for an HSQL database.
   *
   * @return  the parameters
   */
  @DataProvider(name = "localDatabase")
  public static Object[][] dataLocalDatabase() {
    final Object[][] data = getParametersForDatabase("hsqldb");
    if (data.length == 0) {
      throw new IllegalStateException("No databases available");
    }
    return data;
  }

  /**
   * Gets the parameters for the databases set in the <code>test.database.type</code> system property.
   *
   * @return  the parameters
   */
  @DataProvider(name = "databases")
  public static Object[][] dataDatabases() {
    final Object[][] data = getParameters();
    if (data.length == 0) {
      throw new IllegalStateException("No databases available");
    }
    return data;
  }

  /**
   * Gets the parameters for previous three versions of the databases set in the <code>test.database.type</code> system property.
   *
   * @return  the parameters
   */
  @DataProvider(name = "databasesVersionsForSeparateMasters")
  public static Object[][] dataDatabasesVersionsForSeparateMasters() {
    final Object[][] data = getParametersForSeparateMasters(3);
    if (data.length == 0) {
      throw new IllegalStateException("No databases available");
    }
    return data;
  }

  //-------------------------------------------------------------------------
  private static Object[][] getParametersForSeparateMasters(final int prevVersionCount) {
    final Collection<String> databaseTypes = getAvailableDatabaseTypes(System.getProperty("test.database.type"));
    final ArrayList<Object[]> parameters = new ArrayList<>();
    for (final DbSchemaGroupMetadata schemaGroupMetadata : DbScriptUtils.getAllSchemaGroupMetadata()) {
      for (final String databaseType : databaseTypes) {
        final int max = schemaGroupMetadata.getCurrentVersion();
        int min = max;
        while (schemaGroupMetadata.getCreateScript(databaseType, min - 1) != null) {
          min--;
        }
        for (int v = max; v >= Math.max(max - prevVersionCount, min); v--) {
          parameters.add(new Object[]{databaseType, schemaGroupMetadata.getSchemaGroupName(), max /*target_version*/, v /*migrate_from_version*/});
        }
      }
    }
    final Object[][] array = new Object[parameters.size()][];
    parameters.toArray(array);
    return array;
  }

  private static Object[][] getParameters() {
    final Collection<String> databaseTypes = getAvailableDatabaseTypes(System.getProperty("test.database.type"));
    final ArrayList<Object[]> parameters = new ArrayList<>();
    for (final String databaseType : databaseTypes) {
      parameters.add(new Object[]{databaseType, "latest"});
    }
    final Object[][] array = new Object[parameters.size()][];
    parameters.toArray(array);
    return array;
  }

  private static Object[][] getParametersForDatabase(final String databaseType) {
    final ArrayList<Object[]> parameters = new ArrayList<>();
    for (final String db : getAvailableDatabaseTypes(databaseType)) {
      parameters.add(new Object[]{db, "latest"});
    }
    final Object[][] array = new Object[parameters.size()][];
    parameters.toArray(array);
    return array;
  }

  /**
   * Not all database drivers are available in some test environments.
   */
  private static Collection<String> getAvailableDatabaseTypes(final String databaseType) {
    Collection<String> databaseTypes;
    if (databaseType == null) {
      databaseTypes = Sets.newHashSet(DbDialectUtils.getAvailableDatabaseTypes());
    } else {
      if (!DbDialectUtils.getAvailableDatabaseTypes().contains(databaseType)) {
        throw new IllegalArgumentException("Unknown database: " + databaseType);
      }
      databaseTypes = Sets.newHashSet(databaseType);
    }
    return databaseTypes;
  }

  /**
   * Creates a {@code DbTool} for a specific database.
   * The connector may be passed in to share if it exists already.
   *
   * @param databaseConfigPrefix  the prefix for a database in the config file, not null
   * @param connector  the connector, null if not to be shared
   * @return the tool, not null
   */
  public static DbTool createDbTool(final String databaseConfigPrefix, final DbConnector connector) {
    ArgumentChecker.notNull(databaseConfigPrefix, "databaseConfigPrefix");
    final String dbHost = getDbHost(databaseConfigPrefix);
    final String user = getDbUsername(databaseConfigPrefix);
    final String password = getDbPassword(databaseConfigPrefix);
    final DataSource dataSource = connector != null ? connector.getDataSource() : null;
    final DbTool dbTool = new DbTool(dbHost, user, password, dataSource);
    dbTool.initialize();
    dbTool.setJdbcUrl(dbTool.getTestDatabaseUrl());
    return dbTool;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database dialect. The type is taken from the <code>[PREFIX].jdbc.type</code> system property.
   *
   * @param databaseConfigPrefix  the prefix
   * @return  the dialect
   */
  public static DbDialect getDbType(final String databaseConfigPrefix) {
    final String dbTypeProperty = databaseConfigPrefix + ".jdbc.type";
    final String dbType = TestProperties.getTestProperties().getProperty(dbTypeProperty);
    if (dbType == null) {
      throw new OpenGammaRuntimeException("Property " + dbTypeProperty + " not found");
    }
    return DbDialectUtils.getSupportedDbDialect(dbType);
  }

  /**
   * Gets the database host. The host is taken from the <code>[PREFIX].jdbc.url</code> system property.
   *
   * @param databaseConfigPrefix  the prefix
   * @return  the host
   */
  public static String getDbHost(final String databaseConfigPrefix) {
    final String dbHostProperty = databaseConfigPrefix + ".jdbc.url";
    final String dbHost = TestProperties.getTestProperties().getProperty(dbHostProperty);
    if (dbHost == null) {
      throw new OpenGammaRuntimeException("Property " + dbHostProperty + " not found");
    }
    return dbHost;
  }

  /**
   * Gets the database user name. The name is taken from the <code>[PREFIX].jdbc.username</code> system property.
   *
   * @param databaseConfigPrefix  the prefix
   * @return  the host
   */
  public static String getDbUsername(final String databaseConfigPrefix) {
    final String userProperty = databaseConfigPrefix + ".jdbc.username";
    final String user = TestProperties.getTestProperties().getProperty(userProperty);
    if (user == null) {
      throw new OpenGammaRuntimeException("Property " + userProperty + " not found");
    }
    return user;
  }

  /**
   * Gets the database password. The password is taken from the <code>[PREFIX].jdbc.password</code> system property.
   *
   * @param databaseConfigPrefix  the prefix
   * @return  the host
   */
  public static String getDbPassword(final String databaseConfigPrefix) {
    final String passwordProperty = databaseConfigPrefix + ".jdbc.password";
    final String password = TestProperties.getTestProperties().getProperty(passwordProperty);
    if (password == null) {
      throw new OpenGammaRuntimeException("Property " + passwordProperty + " not found");
    }
    return password;
  }

}
