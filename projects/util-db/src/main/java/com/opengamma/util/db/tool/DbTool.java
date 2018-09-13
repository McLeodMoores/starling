/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCPDataSource;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.StartupUtils;
import com.opengamma.util.db.management.DbManagement;
import com.opengamma.util.db.management.DbManagementUtils;
import com.opengamma.util.db.script.DbSchemaGroupMetadata;
import com.opengamma.util.db.script.DbScript;
import com.opengamma.util.db.script.DbScriptUtils;
import com.opengamma.util.test.DbTest;

/**
 * Command-line interface to create or clear databases.
 * <p>
 * This was originally written as an Ant Task.
 * It may still be usable from Ant, although this has not been tested.
 */
public class DbTool {

  /**
   * During installation, INFO level messages will be reported to the user as progress.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DbTool.class);

  static {
    StartupUtils.init();
  }

  // What to do - should be set once
  private String _catalog;
  private String _schema;
  private boolean _create;
  private boolean _drop;
  private boolean _clear;
  private boolean _createTestDb;
  private boolean _createTables;
  private String _testDbType;
  private String _testPropertiesDir;
  private Collection<String> _dbScriptDirs = new ArrayList<>();
  private Integer _targetVersion;
  private Integer _createVersion;

  // What to do it on - can change
  private DbManagement _dialect;
  private String _jdbcUrl;
  private String _dbServerHost;
  private String _user;
  private String _password;
  private volatile DataSource _dataSource;

  /**
   * Static as the parameterized JUnit test runner seems to create a new DbTool instance
   * for each DBTest test case. This is clearly a hack.
   * All strings will be lower case
   */
  private static final Collection<String> TABLES_THAT_SHOULD_NOT_BE_CLEARED = new HashSet<>();

  /**
   * Creates an instance.
   */
  public DbTool() {
  }

  /**
   * Creates an instance with a host, username and password.
   *
   * @param dbServerHost  the host
   * @param user  the user
   * @param password  the password
   */
  public DbTool(final String dbServerHost, final String user, final String password) {
    setDbServerHost(dbServerHost);
    setUser(user);
    setPassword(password);
  }

  /**
   * Creates an instance with a host, username, password and a pre-existing data source.
   *
   * @param dbServerHost  the host
   * @param user  the user
   * @param password  the password
   * @param dataSource  the pre-existing data source, may be null
   */
  public DbTool(final String dbServerHost, final String user, final String password, final DataSource dataSource) {
    setDbServerHost(dbServerHost);
    setUser(user);
    setPassword(password);
    _dataSource = dataSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the class.
   */
  public void initialize() {
    if (_dbServerHost == null) {
      // Parse the server host and catalog from a JDBC URL
      // REVIEW jonathan 2013-05-14 -- should not be doing this (PLAT-2745)
      if (_jdbcUrl != null) {
        final int lastSlash = _jdbcUrl.lastIndexOf('/');
        if (lastSlash == -1 || lastSlash == _jdbcUrl.length() - 1) {
          throw new OpenGammaRuntimeException("JDBC URL must contain a slash separating the server host and the database name");
        }

        _dbServerHost = _jdbcUrl.substring(0, lastSlash);
        _catalog = _jdbcUrl.substring(lastSlash + 1);

      } else {
        throw new OpenGammaRuntimeException("No DB server specified.");
      }
    }

    if (_dbServerHost == null || _user == null || _password == null) {
      throw new OpenGammaRuntimeException("Server/user/password not initialised");
    }
    _dialect = DbManagementUtils.getDbManagement(_dbServerHost);
    _dialect.initialise(_dbServerHost, _user, _password);
  }

  //-------------------------------------------------------------------------
  /**
   * The data-source is created once per instance of the tool.
   *
   * @return the data source, not null
   */
  public synchronized DataSource getDataSource() {
    DataSource dataSource = _dataSource;
    if (dataSource == null) {
      final BoneCPDataSource ds = new BoneCPDataSource();
      ds.setPoolName("DbTool-" + _dialect.getDatabaseName());
      ds.setDriverClass(_dialect.getJDBCDriverClass().getName());
      ds.setJdbcUrl(getJdbcUrl());
      ds.setUsername(getUser());
      ds.setPassword(getPassword());
      ds.setAcquireIncrement(1);
      ds.setPartitionCount(1);
      ds.setMaxConnectionsPerPartition(2);
      ds.setAcquireRetryAttempts(2);
      ds.setAcquireRetryDelayInMs(2000);
      _dataSource = dataSource = ds;  // CSIGNORE
    }
    return dataSource;
  }

  /**
   * Close the data-source if it was created.
   */
  public synchronized void close() {
    ReflectionUtils.close(_dataSource);
    _dataSource = null;
  }

  //-------------------------------------------------------------------------
  /**
   * Resets the database catalog. Used for testing.
   */
  public void resetTestCatalog() {
    _dialect.reset(getTestCatalog());
  }

  /**
   * Shuts down a database catalog.
   *
   * @param catalog  the catalog, not null
   */
  public void shutdown(final String catalog) {
    _dialect.shutdown(catalog);
  }

  /**
   * Sets the server host name.
   *
   * @param dbServerHost  the host
   */
  public void setDbServerHost(final String dbServerHost) {
    _dbServerHost = dbServerHost;
  }

  /**
   * Sets the user.
   *
   * @param user  the user
   */
  public void setUser(final String user) {
    _user = user;
  }

  /**
   * Sets the password.
   *
   * @param password  the password
   */
  public void setPassword(final String password) {
    _password = password;
  }

  /**
   * Gets the server host.
   *
   * @return  the host
   */
  public String getDbServerHost() {
    return _dbServerHost;
  }

  /**
   * Gets the user.
   *
   * @return  the user
   */
  public String getUser() {
    return _user;
  }

  /**
   * Gets the password.
   *
   * @return  the passwords
   */
  public String getPassword() {
    return _password;
  }

  /**
   * Gets the JDBC URL.
   *
   * @return  the URL
   */
  public String getJdbcUrl() {
    return _jdbcUrl;
  }

  /**
   * Sets the JDBC URL.
   *
   * @param jdbcUrl  the URL
   */
  public void setJdbcUrl(final String jdbcUrl) {
    _jdbcUrl = jdbcUrl;
  }

  /**
   * Gets the catalog.
   *
   * @return  the catalog
   */
  public String getCatalog() {
    return _catalog;
  }

  /**
   * Sets the catalog.
   *
   * @param catalog  the catalog
   */
  public void setCatalog(final String catalog) {
    _catalog = catalog;
  }

  /**
   * Gets the schema.
   *
   * @return  the schema
   */
  public String getSchema() {
    return _schema;
  }

  /**
   * Sets the schema.
   *
   * @param schema  the schema
   */
  public void setSchema(final String schema) {
    _schema = schema;
  }

  /**
   * True if the tables should be created.
   *
   * @param create  true to create tables
   */
  public void setCreate(final boolean create) {
    _create = create;
  }

  /**
   * True if the tables should be created.
   *
   * @param create  "true" (case insensitive) to create tables, false otherwise
   */
  public void setCreate(final String create) {
    setCreate(create.equalsIgnoreCase("true"));
  }

  /**
   * True if the tables should be dropped.
   *
   * @param drop  true to drop tables
   */
  public void setDrop(final boolean drop) {
    _drop = drop;
  }

  /**
   * True if the tables should be dropped.
   *
   * @param drop  "true" (case insensitive) to drop tables, false otherwise
   */
  public void setDrop(final String drop) {
    setDrop(drop.equalsIgnoreCase("true"));
  }

  /**
   * True if the tables should be cleared.
   *
   * @param clear true to clear tables
   */
  public void setClear(final boolean clear) {
    _clear = clear;
  }

  /**
   * True if the tables should be cleared.
   *
   * @param clear  "true" (case insensitive) to clear tables, false otherwise
   */
  public void setClear(final String clear) {
    setClear(clear.equalsIgnoreCase("true"));
  }

  /**
   * Creates a test database of the appropriate  type.
   *
   * @param testDbType  the test database type to create
   */
  public void setCreateTestDb(final String testDbType) {
    _createTestDb = testDbType != null;
    _testDbType = testDbType;
  }

  /**
   * Gets the test properties directory.
   *
   * @return  the directory
   */
  public String getTestPropertiesDir() {
    return _testPropertiesDir;
  }

  /**
   * Sets the test properties directory.
   *
   * @param testPropertiesDir  the directory
   */
  public void setTestPropertiesDir(final String testPropertiesDir) {
    _testPropertiesDir = testPropertiesDir;
  }

  /**
   * Gets the script directories.
   *
   * @return  the script directories
   */
  public Collection<String> getDbScriptDirs() {
    return _dbScriptDirs;
  }

  /**
   * Sets the script directories.
   *
   * @param dirs  the directories
   */
  public void setDbScriptDirs(final Collection<String> dirs) {
    _dbScriptDirs = dirs;
  }

  /**
   * Gets the working directory. Stored in the <code>"user.dir"</code> system property.
   *
   * @return  the working directory
   */
  public static String getWorkingDirectory() {
    return System.getProperty("user.dir");
  }

  /**
   * Sets the create version.
   *
   * @param createVersion  the create version, can be null
   */
  public void setCreateVersion(final String createVersion) {
    try {
      _createVersion = Integer.parseInt(createVersion);
    } catch (final NumberFormatException e) {
      _createVersion = null;
    }
  }

  /**
   * Sets the create version.
   *
   * @param createVersion  the create version
   */
  public void setCreateVersion(final Integer createVersion) {
    _createVersion = createVersion;
  }

  /**
   * Gets the create version.
   *
   * @return  the create version, can be null
   */
  public Integer getCreateVersion() {
    return _createVersion;
  }

  /**
   * Sets the target version.
   *
   * @param targetVersion  the target version, can be null
   */
  public void setTargetVersion(final String targetVersion) {
    try {
      _targetVersion = Integer.parseInt(targetVersion);
    } catch (final NumberFormatException e) {
      _targetVersion = null;
    }
  }

  /**
   * Sets the target version.
   *
   * @param targetVersion  the target version, can be null
   */
  public void setTargetVersion(final Integer targetVersion) {
    _targetVersion = targetVersion;
  }

  /**
   * Gets the target version.
   *
   * @return  the target version
   */
  public Integer getTargetVersion() {
    return _targetVersion;
  }

  /**
   * True if the tables should be created.
   *
   * @param create  true to create tables
   */
  public void setCreateTables(final boolean create) {
    _createTables = create;
  }

  /**
   * True if the tables should be created.
   *
   * @param create  "true" (case insensitive) to create tables, false otherwise
   */
  public void setCreateTables(final String create) {
    setCreateTables(create.equalsIgnoreCase("true"));
  }

  /**
   * Creates a test schema.
   */
  public void createTestSchema() {
    createSchema(getTestCatalog(), getTestSchema());
  }

  /**
   * Drops a test schema.
   */
  public void dropTestSchema() {
    dropSchema(getTestCatalog(), getTestSchema());
  }

  /**
   * Clears the test tables.
   */
  public void clearTestTables() {
    clearTables(getTestCatalog(), getTestSchema());
  }

  /**
   * Creates a schema.
   *
   * @param catalog  the catalog
   * @param schema  the schema
   */
  public void createSchema(final String catalog, final String schema) {
    _dialect.createSchema(catalog, schema);
  }

  /**
   * Drops a schema.
   *
   * @param catalog  the catalog
   * @param schema  the schema
   */
  public void dropSchema(final String catalog, final String schema) {
    _dialect.dropSchema(catalog, schema);
  }

  /**
   * Clears the tables.
   *
   * @param catalog  the catalog
   * @param schema  the schema
   */
  public void clearTables(final String catalog, final String schema) {
    _dialect.clearTables(catalog, schema, TABLES_THAT_SHOULD_NOT_BE_CLEARED);
  }

  /**
   * Returns the database structure description.
   *
   * @return  the description
   */
  public String describeDatabase() {
    return _dialect.describeDatabase(getTestCatalog());
  }

  /**
   * Returns the database structure description with a given prefix.
   *
   * @param prefix  the prefix
   * @return  the description
   */
  public String describeDatabase(final String prefix) {
    return _dialect.describeDatabase(getTestCatalog(), prefix);
  }

  /**
   * Gets the test catalog.
   *
   * @return  the test catalog
   */
  public String getTestCatalog() {
    return _dialect.getTestCatalog();
  }

  /**
   * Gets the test schema.
   *
   * @return  the test schema
   */
  public String getTestSchema() {
    return _dialect.getTestSchema();
  }

  /**
   * Gets the test database URL.
   *
   * @return  the URL
   */
  public String getTestDatabaseUrl() {
    return _dialect.getCatalogToConnectTo(getTestCatalog());
  }

  /**
   * Gets the Hibernate dialect.
   *
   * @return  the dialect
   */
  public Dialect getHibernateDialect() {
    return _dialect.getHibernateDialect();
  }

  /**
   * Gets the JDBC driver class.
   *
   * @return  the driver class
   */
  public Class<?> getJDBCDriverClass() {
    return _dialect.getJDBCDriverClass();
  }

  /**
   * Gets the dialect.
   *
   * @return  the dialect
   */
  public DbManagement getDbManagement() {
    return _dialect;
  }

  /**
   * Gets the Hibernate configuration.
   *
   * @return  the configuration
   */
  public Configuration getHibernateConfiguration() {
    final Configuration configuration = new Configuration();
    configuration.setProperty(AvailableSettings.DRIVER, getJDBCDriverClass().getName());
    configuration.setProperty(AvailableSettings.URL, getJdbcUrl());
    configuration.setProperty(AvailableSettings.USER, getUser());
    configuration.setProperty(AvailableSettings.PASS, getPassword());
    configuration.setProperty(AvailableSettings.DIALECT, getHibernateDialect().getClass().getName());
    configuration.setProperty(AvailableSettings.SHOW_SQL, "false");
    configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
    configuration.setProperty(AvailableSettings.TRANSACTION_COORDINATOR_STRATEGY,
        "org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl");
    return configuration;
  }

  /**
   * Gets the Hibernate configuration.
   *
   * @return  the configuration
   */
  public Configuration getTestHibernateConfiguration() {
    final Configuration configuration = getHibernateConfiguration();
    if (getTestSchema() != null) {
      configuration.setProperty(AvailableSettings.DEFAULT_SCHEMA, getTestSchema());
    }
    return configuration;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the test tables.
   *
   * @param callback  a callback invoked when tables are created or upgraded
   */
  public void createTestTables(final TableCreationCallback callback) {
    createTables(getTestCatalog(), getTestSchema(), callback);
  }

  /**
   * Executes the script.
   *
   * @param catalog  the catalog
   * @param schema  the schema
   * @param dbScript  the script
   */
  public void executeSQLScript(final String catalog, final String schema, final DbScript dbScript) {
    String sql;
    try {
      sql = dbScript.getScript();
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("Cannot read db script " + dbScript.getName(), e);
    }
    executeSql(catalog, schema, sql);

    // -- DBTOOLDONOTCLEAR
    // create table rsk_computation_target_type (
    //
    // -> extract rsk_computation_target_type

    final String doNotClear = "DBTOOLDONOTCLEAR";

    int doNotClearIndex = sql.indexOf(doNotClear);
    while (doNotClearIndex != -1) {
      final int createTableOpenParenthesis = sql.indexOf('(', doNotClearIndex);
      if (createTableOpenParenthesis == -1) {
        throw new IllegalArgumentException("There is no CREATE TABLE xxx ( after " + doNotClear);
      }
      final String[] createTableSqls = sql.substring(
          doNotClearIndex + doNotClear.length(),
          createTableOpenParenthesis).split("\r\n|\r|\n| ");
      final List<String> filteredCreateTableSqls = new ArrayList<>();
      for (final String createTableSql : createTableSqls) {
        if (!createTableSql.isEmpty()) {
          filteredCreateTableSqls.add(createTableSql);
        }
      }
      if (filteredCreateTableSqls.size() != 3) {
        throw new IllegalArgumentException("There is no CREATE TABLE xxx ( after " + doNotClear);
      }

      final String tableName = filteredCreateTableSqls.get(2);
      TABLES_THAT_SHOULD_NOT_BE_CLEARED.add(tableName.toLowerCase());

      doNotClearIndex = sql.indexOf(doNotClear, doNotClearIndex + doNotClear.length());
    }
  }

  /**
   * Gets a map from group names to latest schema group versions.
   *
   * @return  the map
   */
  public Map<String, Integer> getLatestVersions() {
    final Map<String, Integer> results = new HashMap<>();
    for (final DbSchemaGroupMetadata schemaGroupMetadata : DbScriptUtils.getAllSchemaGroupMetadata()) {
      results.put(schemaGroupMetadata.getSchemaGroupName(), schemaGroupMetadata.getCurrentVersion());
    }
    return results;
  }

  /**
   * Creates and upgrades tables.
   *
   * @param catalog  the catalog
   * @param schema  the schema
   * @param callback  the callback
   */
  public void createTables(final String catalog, final String schema, final TableCreationCallback callback) {
    for (final DbSchemaGroupMetadata schemaGroupMetadata : DbScriptUtils.getAllSchemaGroupMetadata()) {
      final int targetVersion = getTargetVersion() != null ? getTargetVersion() : schemaGroupMetadata.getCurrentVersion();
      final int migrateFromVersion = getCreateVersion() != null ? getCreateVersion() : targetVersion;
      createTables(schemaGroupMetadata, catalog, schema, targetVersion, migrateFromVersion, callback);
    }
  }

  /**
   * Creates and upgrades tables.
   *
   * @param schemaGroupMetadata  the group metadata
   * @param catalog  the catalog
   * @param schema  the schema
   * @param targetVersion  the target version to upgrade to
   * @param migrateFromVersion  the version to migrate from
   * @param callback  the callback
   */
  public void createTables(final DbSchemaGroupMetadata schemaGroupMetadata, final String catalog, final String schema,
      final int targetVersion, final int migrateFromVersion, final TableCreationCallback callback) {
    // create
    final String dbVendorName = _dialect.getDatabaseName();
    final DbScript createScript = schemaGroupMetadata.getCreateScript(dbVendorName, migrateFromVersion);
    if (createScript == null) {
      throw new OpenGammaRuntimeException("Missing create script for V" + migrateFromVersion + ", database "
          + dbVendorName + ", schema group " + schemaGroupMetadata.getSchemaGroupName());
    }
    LOGGER.debug("Creating {} DB version {}", schemaGroupMetadata.getSchemaGroupName(), migrateFromVersion);
    LOGGER.debug("Executing create script {}", createScript.getName());
    executeSQLScript(catalog, schema, createScript);
    if (callback != null) {
      callback.tablesCreatedOrUpgraded(migrateFromVersion, schemaGroupMetadata);
    }
    // migrates
    for (int v = migrateFromVersion; v < targetVersion; v++) {
      final DbScript migrateScript = schemaGroupMetadata.getMigrateScript(dbVendorName, v);
      if (migrateScript == null) {
        throw new OpenGammaRuntimeException("The " + v + " migrate script is missing for " + dbVendorName
            + " and schema group " + schemaGroupMetadata.getSchemaGroupName());
      }
      LOGGER.debug("Migrating DB from version {} to {}", v, v + 1);
      LOGGER.debug("Executing migrate script {}", migrateScript.getName());
      executeSQLScript(catalog, schema, migrateScript);
      if (callback != null) {
        callback.tablesCreatedOrUpgraded(v + 1, schemaGroupMetadata);
      }
    }
  }

  /**
   * Executes SQL against a database.
   *
   * @param catalog  the catalog
   * @param schema  the schema
   * @param sql  the SQL to execute
   */
  public void executeSql(final String catalog, final String schema, final String sql) {
    _dialect.executeSql(catalog, schema, sql);
  }

  /**
   * Executes the commands. The catalog field generally has to be set, but if you do not
   * set the jdbcHost (normally the case) then the catalog is overridden
   * with one derived from the URL in initialize() called from here.
   */
  public void execute() {

    if (!_createTestDb) {
      if (_catalog == null) {
        throw new OpenGammaRuntimeException("No database on the DB server specified.");
      }
    }

    if (!_create && !_drop && !_clear && !_createTestDb && !_createTables) {
      throw new OpenGammaRuntimeException("Nothing to do.");
    }

    if (_clear) {
      LOGGER.info("Clearing database tables at {}", getJdbcUrl());
      initialize();
      clearTables(_catalog, _schema);
    }

    if (_drop) {
      LOGGER.info("Dropping existing database schema at {}", getJdbcUrl());
      initialize();
      dropSchema(_catalog, _schema);
    }

    if (_create) {
      LOGGER.info("Creating new database schema at {}", getJdbcUrl());
      initialize();
      createSchema(_catalog, _schema);
    }

    if (_createTables) {
      LOGGER.info("Creating database tables at {}", getJdbcUrl());
      initialize();
      createTables(_catalog, null, null);
      shutdown(_catalog);
    }

    if (_createTestDb) {
      // used to try to use _testPropertiesDir here, but value was always ignored
      for (final String dbType : initDatabaseTypes(_testDbType)) {
        LOGGER.debug("Creating " + dbType + " test database...");

        final String dbUrl = DbTest.getDbHost(dbType);
        final String user = DbTest.getDbUsername(dbType);
        final String password = DbTest.getDbPassword(dbType);

        setDbServerHost(dbUrl);
        setUser(user);
        setPassword(password);

        initialize();
        dropTestSchema(); // make sure it's empty if it already existed
        createTestSchema();
        createTestTables(null);
        shutdown(getTestCatalog());
      }
    }
    LOGGER.info("OpenGamma database created at {}", getJdbcUrl());
  }

  /**
   * Gets the selected database types.
   *
   * @return a singleton collection containing the String passed in, except if the type is ALL
   *  (case insensitive), in which case all supported database types are returned, not null
   */
  private static Collection<String> initDatabaseTypes(final String commandLineDbType) {
    final ArrayList<String> dbTypes = new ArrayList<>();
    if (commandLineDbType.trim().equalsIgnoreCase("all")) {
      dbTypes.addAll(DbDialectUtils.getSupportedDatabaseTypes());
    } else {
      dbTypes.add(commandLineDbType);
    }
    return dbTypes;
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the tool from the command line.
   *
   * @param args  the command line arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    final Options options = new Options();
    options.addOption("jdbcUrl", "jdbcUrl", true, "DB server URL + database - for example, jdbc:postgresql://localhost:1234/OpenGammaTests. You can use"
        + " either this option or specify server and database separately.");
    options.addOption("server", "server", true, "DB server URL (no database at the end) - for example, jdbc:postgresql://localhost:1234");
    options.addOption("database", "database", true, "Name of database on the DB server - for example, OpenGammaTests");
    options.addOption("user", "user", true, "User name to the DB");
    options.addOption("password", "password", true, "Password to the DB");
    options.addOption("schema", "schema", true, "Name of schema within database. Optional. If not specified, the default schema for the database is used.");
    options.addOption("create", "create", false, "Creates the given database/schema. The database will be empty.");
    options.addOption("drop", "drop", false, "Drops all tables and sequences within the given database/schema");
    options.addOption("clear", "clear", false, "Clears all tables within the given database/schema");
    options.addOption("createtestdb", "createtestdb", true, "Drops schema in database test_<user.name> and recreates it (including tables). "
        + "{dbtype} should be one of derby, postgres, all. Connection parameters are read from test.properties so you do not need "
        + "to specify server, user, or password.");
    options.addOption("createtables", "createtables", true, "Creates database tables for all masters.");
    options.addOption("targetversion", "targetversion", true,
        "Version number for the end result database. 0 means latest. 1 means last but one etc. Optional. If not specified, assumes latest version.");
    options.addOption("createversion", "createversion", true,
        "Version number to run the creation script from. 0 means latest. 1 means last but one etc. Optional. If not specified, defaults to {targetversion}.");
    options.addOption("testpropertiesdir", "testpropertiesdir", true, "Directory for reading test.properties. Only used with the --createstdb option. "
        + "Optional. If not specified, the working directory is used.");

    final CommandLineParser parser = new PosixParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (final ParseException e) {
      e.printStackTrace();
      usage(options);
      System.exit(-1);
    }

    final DbTool tool = new DbTool();
    tool.setJdbcUrl(line.getOptionValue("jdbcUrl"));
    tool.setDbServerHost(line.getOptionValue("server"));
    tool.setUser(line.getOptionValue("user"));
    tool.setPassword(line.getOptionValue("password"));
    tool.setCatalog(line.getOptionValue("database"));
    tool.setSchema(line.getOptionValue("schema"));
    tool.setCreate(line.hasOption("create"));
    tool.setDrop(line.hasOption("drop"));
    tool.setClear(line.hasOption("clear"));
    tool.setCreateTestDb(line.getOptionValue("createtestdb"));
    tool.setCreateTables(line.getOptionValue("createtables"));
    tool.setTestPropertiesDir(line.getOptionValue("testpropertiesdir"));
    tool.setTargetVersion(line.getOptionValue("targetversion"));
    tool.setCreateVersion(line.getOptionValue("createversion"));

    try {
      tool.execute();
    } catch (final RuntimeException ex) {
      LOGGER.error(ex.getMessage());
      usage(options);
      System.exit(-1);
    }
  }

  /**
   * Print usage.
   *
   * @param options  the command line options
   */
  private static void usage(final Options options) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java com.opengamma.util.db.tool.DbTool [args]", options);
  }

  /**
   * Returns collection of table names.
   *
   * @return a list of table names, not null
   */
  public List<String> listTables() {
    initialize();
    return _dialect.listTables(getCatalog());
  }

  //-------------------------------------------------------------------------
  /**
   * Callback invoked when tables are created or upgraded.
   */
  public interface TableCreationCallback {

    /**
     * Creates or upgrades a table.
     *
     * @param version  the version
     * @param schemaGroupMetadata  the metadata
     */
    void tablesCreatedOrUpgraded(int version, DbSchemaGroupMetadata schemaGroupMetadata);

  }

}
