/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Database management for Postgres databases.
 */
public final class PostgresDbManagement extends AbstractDbManagement {

  /**
   * SQL to retrieve all the columns.
   */
  private static final String GET_ALL_COLUMNS_SQL =
    "SELECT column_name AS name,data_type AS datatype,is_nullable AS allowsnull,"
    + "column_default AS defaultvalue FROM information_schema.columns WHERE table_catalog='";
  /**
   * The Postgres default schema.
   */
  private static final String POSTGRES_DEFAULT_SCHEMA = "public";
  /**
   * Singleton instance.
   */
  private static final PostgresDbManagement INSTANCE = new PostgresDbManagement();

  /**
   * The underlying Hibernate dialect.
   */
  private PostgreSQLDialect _hibernateDialect;

  /**
   * Restricted constructor.
   */
  private PostgresDbManagement() {
  }

  /**
   * Gets the singleton instance.
   *
   * @return the instance, not null
   */
  public static PostgresDbManagement getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      // constructed lazily so we don't get log message about 'using dialect' if we're not actually using it
      _hibernateDialect = new PostgreSQLDialect();
    }
    return _hibernateDialect;
  }

  @Override
  public Class<?> getJDBCDriverClass() {
    return org.postgresql.Driver.class;
  }

  @Override
  public String getDatabaseName() {
    return "postgres";
  }

  //-------------------------------------------------------------------------
  @Override
  public String getAllSchemasSQL(final String catalog) {
    return "SELECT nspname AS name from pg_namespace";
  }

  @Override
  public String getAllForeignKeyConstraintsSQL(final String catalog, final String schema) {
    String constraintSchema = schema;
    if (constraintSchema == null) {
      constraintSchema = POSTGRES_DEFAULT_SCHEMA;
    }
    final String sql = "SELECT constraint_name AS name, table_name FROM information_schema.table_constraints WHERE "
      + "constraint_catalog = '" + catalog + "' AND constraint_schema = '" + constraintSchema + "'" + " AND constraint_type = 'FOREIGN KEY'";
    return sql;
  }

  @Override
  public String getAllSequencesSQL(final String catalog, final String schema) {
    String sequenceSchema = schema;
    if (sequenceSchema == null) {
      sequenceSchema = POSTGRES_DEFAULT_SCHEMA;
    }
    final String sql = "SELECT sequence_name AS name FROM information_schema.sequences WHERE "
      + "sequence_catalog = '" + catalog + "'" + " AND sequence_schema = '" + sequenceSchema + "'";
    return sql;
  }

  @Override
  public String getAllTablesSQL(final String catalog, final String schema) {
    String tableSchema = schema;
    if (tableSchema == null) {
      tableSchema = POSTGRES_DEFAULT_SCHEMA;
    }
    final String sql = "SELECT table_name AS name FROM information_schema.tables WHERE "
      + "table_catalog = '" + catalog + "'" + " AND table_schema = '" + tableSchema + "' AND table_type = 'BASE TABLE'";
    return sql;
  }

  @Override
  public String getAllViewsSQL(final String catalog, final String schema) {
    String tableSchema = schema;
    if (tableSchema == null) {
      tableSchema = POSTGRES_DEFAULT_SCHEMA;
    }
    final String sql = "SELECT table_name AS name FROM information_schema.tables WHERE "
      + "table_catalog = '" + catalog + "'" + " AND table_schema = '" + tableSchema + "' AND table_type = 'VIEW'";
    return sql;
  }

  @Override
  public String getAllColumnsSQL(final String catalog, final String schema, final String table) {
    String tableSchema = schema;
    if (tableSchema == null) {
      tableSchema = POSTGRES_DEFAULT_SCHEMA;
    }
    final StringBuilder sql = new StringBuilder(GET_ALL_COLUMNS_SQL);
    sql.append(catalog).append("' AND table_schema='").append(tableSchema).append("' AND table_name='");
    sql.append(table).append("'");
    return sql.toString();
  }

  @Override
  public String getCreateSchemaSQL(final String catalog, final String schema) {
    return "CREATE SCHEMA " + schema;
  }

  @Override
  public String getSchemaVersionTable(final String schemaGroupName) {
    return (schemaGroupName + SCHEMA_VERSION_TABLE_SUFFIX).toLowerCase();
  }

  @Override
  public String getSchemaVersionSQL(final String catalog, final String schemaGroupName) {
    return "SELECT version_value FROM " + getSchemaVersionTable(schemaGroupName) + " WHERE version_key = 'schema_patch'";
  }

  @Override
  public CatalogCreationStrategy getCatalogCreationStrategy() {
    return new SQLCatalogCreationStrategy(
        this,
        getUser(),
        getPassword(),
        "SELECT datname AS name FROM pg_database",
        "template1");
  }

  @Override
  public void dropSchema(final String catalog, final String schema) {
    if (schema != null) {
      super.dropSchema(catalog, schema);
    } else {
      try {
        if (!getCatalogCreationStrategy().catalogExists(catalog)) {
          return;
        }
        final Connection conn = connect(catalog);
        final Statement statement = conn.createStatement();
        //TODO default schema
        statement.executeUpdate("DROP SCHEMA IF EXISTS public CASCADE;CREATE SCHEMA public;");
        statement.close();
        conn.close();
      } catch (final SQLException se) {
        throw new OpenGammaRuntimeException("Failed to drop the default schema", se);
      }
    }
  }
}
