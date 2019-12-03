/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.tuple.Pair;

/**
 * Database management for Postgres databases.
 */
public final class SqlServer2008DbManagement extends AbstractDbManagement {

  /**
   * SQL to retrieve all the columns.
   */
  private static final String GET_ALL_COLUMNS_SQL =
    "SELECT column_name AS name,data_type AS datatype,is_nullable AS allowsnull,"
    + "column_default AS defaultvalue FROM information_schema.columns WHERE table_name='";
  /**
   * The default schema.
   */
  private static final String SQLSERVER2008_DEFAULT_SCHEMA = "dbo";
  /**
   * Singleton instance.
   */
  private static final SqlServer2008DbManagement INSTANCE = new SqlServer2008DbManagement();

  /**
   * The underlying Hibernate dialect.
   */
  private SQLServerDialect _hibernateDialect;

  /**
   * Restricted constructor.
   */
  private SqlServer2008DbManagement() {
  }

  /**
   * Gets the singleton instance.
   *
   * @return the instance, not null
   */
  public static SqlServer2008DbManagement getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      // constructed lazily so we don't get log message about 'using dialect' if we're not actually using it
      _hibernateDialect = new SQLServerDialect();
    }
    return _hibernateDialect;
  }

  @Override
  public Class<?> getJDBCDriverClass() {
    try {
      return Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    } catch (final ClassNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not load the Microsoft JDBC driver: " + ex.getMessage());
    }
    // Use the MS driver...
    // return com.microsoft.sqlserver.jdbc.SQLServerDriver.class;
    // ...or the open-source driver (LGPLed)
    // return net.sourceforge.jtds.jdbc.Driver.class;
  }

  @Override
  public String getDatabaseName() {
    return "sqlserver2008";
  }

  //-------------------------------------------------------------------------
  @Override
  public String getCatalogToConnectTo(final String catalog) {
    return getDbHost() + ";databasename=" + catalog;
  }

  @Override
  public String getAllSchemasSQL(final String catalog) {
    return "SELECT SCHEMA_NAME AS name FROM INFORMATION_SCHEMA.SCHEMATA";
  }

  @Override
  public String getAllForeignKeyConstraintsSQL(final String catalog, final String schema) {
    String constraintSchema = schema;
    if (constraintSchema == null) {
      constraintSchema = SQLSERVER2008_DEFAULT_SCHEMA;
    }
    final String sql = "SELECT constraint_name AS name, table_name FROM information_schema.table_constraints WHERE "
      + "constraint_catalog = '" + catalog + "' AND constraint_schema = '" + constraintSchema + "'" + " AND constraint_type = 'FOREIGN KEY'";
    return sql;
  }

  @Override
  public String getAllSequencesSQL(final String catalog, String schema) {
    if (schema == null) {
      schema = SQLSERVER2008_DEFAULT_SCHEMA;
    }
    final String sql = "SELECT table_name AS name FROM information_schema.tables WHERE table_name LIKE '%_seq' AND "
      + "table_catalog = '" + catalog + "'" + " AND table_schema = '" + schema + "' AND table_type = 'BASE TABLE'";
    return sql;
  }

  @Override
  public String getAllTablesSQL(final String catalog, String schema) {
    if (schema == null) {
      schema = SQLSERVER2008_DEFAULT_SCHEMA;
    }
    final String sql = "SELECT table_name AS name FROM information_schema.tables WHERE NOT table_name LIKE '%_seq' AND "
      + "table_catalog = '" + catalog + "'" + " AND table_schema = '" + schema + "' AND table_type = 'BASE TABLE'";
    return sql;
  }

  @Override
  public String getAllViewsSQL(final String catalog, String schema) {
    if (schema == null) {
      schema = SQLSERVER2008_DEFAULT_SCHEMA;
    }
    final String sql = "SELECT table_name AS name FROM information_schema.tables WHERE "
      + "table_catalog = '" + catalog + "'" + " AND table_schema = '" + schema + "' AND table_type = 'VIEW'";
    return sql;
  }

  @Override
  public String getAllColumnsSQL(final String catalog, String schema, final String table) {
    if (schema == null) {
      schema = SQLSERVER2008_DEFAULT_SCHEMA;
    }
    final StringBuilder sql = new StringBuilder(GET_ALL_COLUMNS_SQL);
    sql.append(catalog).append("' AND table_schema='").append(schema).append("' AND table_name='");
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
        "SELECT name FROM sys.databases WHERE name NOT IN ('master', 'model', 'msdb', 'tempdb')",
        null);
  }

  @Override
  public void dropSchema(final String catalog, final String schema) {
    // Does not handle triggers or stored procedures yet
    final ArrayList<String> script = new ArrayList<>();

    Connection conn = null;
    try {
      if (!getCatalogCreationStrategy().catalogExists(catalog)) {
        System.out.println("Catalog " + catalog + " does not exist");
        return; // nothing to drop
      }

      conn = connect(catalog);

      if (schema != null) {
        final Statement statement = conn.createStatement();
        final Collection<String> schemas = getAllSchemas(catalog, statement);
        statement.close();

        if (!schemas.contains(schema)) {
          System.out.println("Schema " + schema + " does not exist");
          return; // nothing to drop
        }
      }

      setActiveSchema(conn, schema);
      Statement statement = conn.createStatement();

      // Drop constraints SQL
      if (getHibernateDialect().dropConstraints()) {
        for (final Pair<String, String> constraint : getAllForeignKeyConstraints(catalog, schema, statement)) {
          final String name = constraint.getFirst();
          final String table = constraint.getSecond();
          final ForeignKey fk = new ForeignKey();
          fk.setName(name);
          fk.setTable(new Table(table));

          final String dropConstraintSql = fk.sqlDropString(getHibernateDialect(), null, schema);
          script.add(dropConstraintSql);
        }
      }

      // Drop views SQL
      for (final String name : getAllViews(catalog, schema, statement)) {
        final Table table = new Table(name);
        String dropViewStr = table.sqlDropString(getHibernateDialect(), null, schema);
        dropViewStr = dropViewStr.replaceAll("drop table", "drop view");
        script.add(dropViewStr);
      }

      // Drop tables SQL
      for (final String name : getAllTables(catalog, schema, statement)) {
        final Table table = new Table(name);
        final String dropTableStr = table.sqlDropString(getHibernateDialect(), null, schema);
        script.add(dropTableStr);
      }

      // Now execute it all
      statement.close();
      statement = conn.createStatement();
      for (final String sql : script) {
        //System.out.println("Executing \"" + sql + "\"");
        statement.executeUpdate(sql);
      }

      statement.close();
      statement = conn.createStatement();

      // Drop sequences SQL
      script.clear();
      for (final String name : getAllSequences(catalog, schema, statement)) {
        final Table table = new Table(name);
        final String dropTableStr = table.sqlDropString(getHibernateDialect(), null, schema);
        script.add(dropTableStr);
      }

      //now execute drop sequence
      statement.close();
      statement = conn.createStatement();
      for (final String sql : script) {
        //System.out.println("Executing \"" + sql + "\"");
        statement.executeUpdate(sql);
      }

      statement.close();

    } catch (final SQLException e) {
      throw new OpenGammaRuntimeException("Failed to drop schema", e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (final SQLException e) {
      }
    }
  }

}
