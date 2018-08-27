/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.db.script.DbScript;

/**
 * Upgrades or Creates database objects using the installation scripts.
 */
public class DbUpgradeOrCreateOperation extends AbstractDbScriptOperation<DbToolContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbUpgradeOrCreateOperation.class);

  private boolean _upgradeRequired;

  /**
   * Constructs an instance.
   *
   * @param dbToolContext  the database tool context, not null
   * @param write  true to modify the database, false to output the commands that would be run
   * @param outputFile  the file to which the SQL should be written, null not to write to a file
   */
  public DbUpgradeOrCreateOperation(final DbToolContext dbToolContext, final boolean write, final File outputFile) {
    super(dbToolContext, write, outputFile);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets whether an upgrade was found to be required on the last execution.
   *
   * @return true if an upgrade was required, false otherwise
   */
  public boolean isUpgradeRequired() {
    return _upgradeRequired;
  }

  private void setUpgradeRequired(final boolean upgradeRequired) {
    _upgradeRequired = upgradeRequired;
  }

  //-------------------------------------------------------------------------
  @Override
  public void execute() {
    final SqlScriptWriter writer = createSqlScriptWriter();
    boolean upgradeRequired = false;
    try {
      final Set<String> schemaNames = getDbToolContext().getSchemaNames() != null ? getDbToolContext().getSchemaNames() : getAllSchemaNames();
      for (final String schema : schemaNames) {
        final Integer currentVersion = getCurrentGroupVersion(schema);
        if (currentVersion == null) {
          //craete
          LOGGER.info("Processing schema " + schema);
          final DbScript script = getCreationScript(schema);
          LOGGER.debug("Using script: " + script);
          writer.write(schema, script);
        } else {
          //update
          final List<DbScript> scripts = getMigrationScripts(schema);
          if (scripts == null) {
            LOGGER.info(schema + " does not support migration");
            continue;
          }
          if (scripts.isEmpty()) {
            LOGGER.info(schema + " already at latest version");
            continue;
          }
          upgradeRequired = true;
          LOGGER.info(schema + " is behind by " + scripts.size() + " versions");
          for (int i = 0; i < scripts.size(); i++) {
            final DbScript script = scripts.get(i);
            LOGGER.debug("Using schema migration file: " + script);
            writer.write(schema + " - " + (i + 1) + " of " + scripts.size(), script);
          }
        }
      }
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("Error processing creation/migration scripts", e);
    } finally {
      setUpgradeRequired(upgradeRequired);
      try {
        writer.close();
      } catch (final IOException e) {
        LOGGER.error("Error closing SQL script writer", e);
      }
    }
  }

}
