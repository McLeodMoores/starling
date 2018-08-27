/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.jolbox.bonecp.BoneCPDataSource;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.management.DbManagement;
import com.opengamma.util.db.management.DbManagementUtils;
import com.opengamma.util.db.script.DbScriptUtils;
import com.opengamma.util.db.tool.DbCreateOperation;
import com.opengamma.util.db.tool.DbToolContext;
import com.opengamma.util.db.tool.DbUpgradeOperation;

/**
 * Object representing a schema in OpenGamma. Provides methods for initialization
 * of schema instances.
 */
public final class OGSchema {

  private static final Logger LOGGER = LoggerFactory.getLogger(OGSchema.class);

  private final DbConnector _dbConnector;

  private final boolean _autoSchemaManagement;

  private final boolean _enforceSchemaVersion;

  private OGSchema(final DbConnector dbConnector, final boolean autoSchemaManagement, final boolean enforceSchemaVersion) {
    _dbConnector = dbConnector;
    _autoSchemaManagement = autoSchemaManagement;
    _enforceSchemaVersion = enforceSchemaVersion;
  }

  /**
   * For building OGSchema objects
   */
  public static final class Builder {
    private final DbConnector _dbConnector;
    private boolean _autoSchemaManagement;
    private boolean _enforceSchemaVersion = true;

    private Builder(final DbConnector dbConnector) {
      _dbConnector = dbConnector;
    }

    /**
     * Whether auto schema management should be used. Defaults to false.
     * @param autoSchemaManagement whether to use auto schema management
     * @return this
     */
    public Builder withAutoSchemaManagement(final boolean autoSchemaManagement) {
      _autoSchemaManagement = autoSchemaManagement; return this;
    }

    /**
     * Whether to enforce auto schema versioning. Defaults to true.
     * @param enforceSchemaVersion whether to enforce auto schema versioning
     * @return this
     */
    public Builder enforcingSchemaVersion(final boolean enforceSchemaVersion) {
      _enforceSchemaVersion = enforceSchemaVersion; return this;
    }

    /**
     * Build the {@link OGSchema} object with the specified configuration.
     * @return this
     */
    public OGSchema build() {
      return new OGSchema(_dbConnector, _autoSchemaManagement, _enforceSchemaVersion);
    }

  }

  /**
   * Build a new {@link OGSchema} on dbConnector.
   * @param dbConnector the dbConnector to use
   * @return a builder object
   */
  public static Builder on(final DbConnector dbConnector) {
    return new Builder(dbConnector);
  }


  public DbConnector getDbConnector() {
    return _dbConnector;
  }

  public boolean isAutoSchemaManagement() {
    return _autoSchemaManagement;
  }

  public boolean isEnforceSchemaVersion() {
    return _enforceSchemaVersion;
  }


  //-------------------------------------------------------------------------
  public void checkSchema(final Integer actualSchemaVersion, final String schemaName) {
    if (isAutoSchemaManagement()) {
      manageSchema(actualSchemaVersion, schemaName);
    } else {
      checkSchemaVersion(actualSchemaVersion, schemaName);
    }
  }


  @SuppressWarnings("resource")
  private void manageSchema(final Integer actualSchemaVersion, final String schemaName) {
    ArgumentChecker.notNull(schemaName, "schemaName");

    // REVIEW jonathan 2013-05-14 -- don't look at this :-)
    if (!(getDbConnector().getDataSource() instanceof BoneCPDataSource)) {
      LOGGER.warn("Unable to obtain database management instance. Database objects cannot be inspected or modified, and may be missing or out-of-date.");
      return;
    }
    final BoneCPDataSource dataSource = (BoneCPDataSource) getDbConnector().getDataSource();
    final String jdbcUrl = dataSource.getJdbcUrl();
    if (jdbcUrl == null) {
      throw new OpenGammaRuntimeException("No JDBC URL specified");
    }
    final DbManagement dbManagement = DbManagementUtils.getDbManagement(jdbcUrl);
    final int lastSlashIdx = jdbcUrl.lastIndexOf("/");
    if (lastSlashIdx == -1) {
      throw new OpenGammaRuntimeException("JDBC URL must contain '/' before the database name");
    }

    // REVIEW jonathan 2013-05-14 -- should not be doing this (PLAT-2745)
    final int lastSlash = jdbcUrl.lastIndexOf('/');
    if (lastSlash == -1 || lastSlash == jdbcUrl.length() - 1) {
      throw new OpenGammaRuntimeException("JDBC URL must contain a slash separating the server host and the database name");
    }
    final String dbServerHost = jdbcUrl.substring(0, lastSlash);
    final String catalog = jdbcUrl.substring(lastSlashIdx + 1);
    final String user = dataSource.getUsername();
    final String password = dataSource.getPassword();
    dbManagement.initialise(dbServerHost, user, password);

    final Integer expectedSchemaVersion = DbScriptUtils.getCurrentVersion(schemaName);
    if (expectedSchemaVersion == null) {
      throw new OpenGammaRuntimeException("Unable to find schema version information for " + schemaName + ". Database objects cannot be managed.");
    }
    // DbToolContext should not be closed as DbConnector needs to remain started
    final DbToolContext dbToolContext = new DbToolContext();
    dbToolContext.setDbConnector(getDbConnector());
    dbToolContext.setDbManagement(dbManagement);
    dbToolContext.setCatalog(catalog);
    dbToolContext.setSchemaNames(ImmutableSet.of(schemaName));
    if (actualSchemaVersion == null) {
      // Assume empty database, so attempt to create tables
      final DbCreateOperation createOperation = new DbCreateOperation(dbToolContext, true, null, false);
      createOperation.execute();
    } else if (actualSchemaVersion < expectedSchemaVersion) {
      // Upgrade from expected to actual
      final DbUpgradeOperation upgradeOperation = new DbUpgradeOperation(dbToolContext, true, null);
      upgradeOperation.execute();
    } else if (expectedSchemaVersion > actualSchemaVersion) {
      throw new OpenGammaRuntimeException(schemaName + " schema too new. This build of the OpenGamma Platform works with version " +
          expectedSchemaVersion + " of the " + schemaName + " schema, but the database contains version " + actualSchemaVersion +
          ". Unable to downgrade an existing database.");
    }
  }

  private void checkSchemaVersion(final Integer actualSchemaVersion, final String schemaName) {
    ArgumentChecker.notNull(schemaName, "schemaName");
    if (actualSchemaVersion == null) {
      throw new OpenGammaRuntimeException("Unable to find current " + schemaName + " schema version in database");
    }
    final Integer expectedSchemaVersion = DbScriptUtils.getCurrentVersion(schemaName);
    if (expectedSchemaVersion == null) {
      LOGGER.info("Unable to find schema version information for {}. The database schema may differ from the required version.", schemaName);
      return;
    }
    if (expectedSchemaVersion.intValue() == actualSchemaVersion) {
      LOGGER.debug("Verified " + schemaName + " schema version " + actualSchemaVersion);
      return;
    }
    final String relativeDbAge = expectedSchemaVersion.intValue() < actualSchemaVersion ? "new" : "old";
    final String message = schemaName + " schema too " + relativeDbAge + ". This build of the OpenGamma Platform works with version " +
        expectedSchemaVersion + " of the " + schemaName + " schema, but the database contains version " + actualSchemaVersion + ".";
    if (isEnforceSchemaVersion()) {
      throw new OpenGammaRuntimeException(message);
    } else {
      LOGGER.warn(message);
    }
  }



}
