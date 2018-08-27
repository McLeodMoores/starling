/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.opengamma.OpenGammaRuntimeException;


/**
 * Represents the available metadata for a database schema group.
 */
public class DbSchemaGroupMetadata {

  private static final String CREATE_TYPE = "create";
  private static final String MIGRATE_TYPE = "migrate";

  private final String _schemaGroupName;
  private final String _baseResourceUrl;
  private final int _currentVersion;

  public DbSchemaGroupMetadata(final String schemaGroupName, final String baseResourceUrl, final int currentVersion) {
    _schemaGroupName = schemaGroupName;
    _baseResourceUrl = baseResourceUrl;
    _currentVersion = currentVersion;
  }

  /**
   * Gets the schema group name.
   *
   * @return the schema group name, not null
   */
  public String getSchemaGroupName() {
    return _schemaGroupName;
  }

  /**
   * Gets the current schema group version.
   *
   * @return the current version
   */
  public int getCurrentVersion() {
    return _currentVersion;
  }

  /**
   * Gets the SQL for migrating from a particular version to the next version.
   *
   * @param dbVendorName  the database vendor name, not null
   * @param fromVersion  the version to migrate from
   * @return the SQL, or null if not found for vendor or version
   */
  public DbScript getMigrateScript(final String dbVendorName, final int fromVersion) {
    return getScript(dbVendorName, MIGRATE_TYPE, fromVersion + 1);
  }

  public List<DbScript> getMigrateScripts(final String dbVendorName, final int fromVersion) {
    final List<DbScript> result = new LinkedList<>();
    for (int i = fromVersion; i < getCurrentVersion(); i++) {
      result.add(getMigrateScript(dbVendorName, i));
    }
    return result;
  }

  /**
   * Gets the SQL for creating the database objects for this schema group at the current version.
   *
   * @param dbVendorName  the database vendor name, not null
   * @return the SQL, null if not found for vendor
   */
  public DbScript getCreateScript(final String dbVendorName) {
    return getCreateScript(dbVendorName, getCurrentVersion());
  }

  /**
   * Gets the SQL for creating the database objects for this schema group at a particular version.
   *
   * @param dbVendorName  the database vendor name, not null
   * @param version  the version
   * @return the SQL, null if not found for vendor or version
   */
  public DbScript getCreateScript(final String dbVendorName, final int version) {
    return getScript(dbVendorName, CREATE_TYPE, version);
  }

  //-------------------------------------------------------------------------
  private DbScript getScript(final String dbVendorName, final String scriptType, final int version) {
    final URL sqlScriptUrl = getSqlScriptUrl(dbVendorName, scriptType, version);
    final ClasspathDbScript script = new ClasspathDbScript(sqlScriptUrl);
    return script.exists() ? script : null;
  }

  private String getBaseResourceUrl() {
    return _baseResourceUrl;
  }

  private URL getSqlScriptUrl(final String dbVendor, final String scriptType, final int version) {
    final String fileName = "V_" + version + "__" + scriptType + "_" + getSchemaGroupName() + ".sql";
    try {
      return new URL(getBaseResourceUrl() + "/" + scriptType + "/" + dbVendor + "/" + getSchemaGroupName() + "/" + fileName);
    } catch (final MalformedURLException e) {
      throw new OpenGammaRuntimeException("Unable to construct SQL script URL", e);
    }
  }

}
