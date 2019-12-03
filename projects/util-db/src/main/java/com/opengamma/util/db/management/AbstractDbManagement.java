/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import static com.opengamma.util.RegexUtils.matches;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.QualifiedName;
import org.hibernate.boot.model.relational.QualifiedNameImpl;
import org.hibernate.id.enhanced.SequenceStructure;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Abstract implementation of database management.
 */
public abstract class AbstractDbManagement implements DbManagement {

  /**
   * The schema version table suffix.
   */
  protected static final String SCHEMA_VERSION_TABLE_SUFFIX = "_schema_version";

  /**
   * The database server.
   */
  private String _dbServerHost;
  /**
   * The user name.
   */
  private String _user;
  /**
   * The password.
   */
  private String _password;

  //-------------------------------------------------------------------------
  @Override
  public void initialise(final String dbServerHost, final String user, final String password) {
    _dbServerHost = dbServerHost;
    _user = user;
    _password = password;
    try {
      getJDBCDriverClass().newInstance();  // load the driver
    } catch (final Exception ex) {
      throw new OpenGammaRuntimeException("Cannot load JDBC driver", ex);
    }
  }

  @Override
  public String getTestCatalog() {
    return "test_" + System.getProperty("user.name").replace('.', '_');
  }

  @Override
  public String getTestSchema() {
    return null; // use default
  }

  @Override
  public void reset(final String catalog) {
    // by default, do nothing
  }

  @Override
  public void shutdown(final String catalog) {
    // by default, do nothing
  }

  /**
   * Gets the database server.
   *
   * @return  the database server
   */
  public String getDbHost() {
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
   * @return  the password
   */
  public String getPassword() {
    return _password;
  }

  //-------------------------------------------------------------------------

  /**
   * Generic representation of a column.
   */
  protected class ColumnDefinition implements Comparable<ColumnDefinition> {
    private final String _name;
    private final String _dataType;
    private final String _defaultValue;
    private final String _allowsNull;

    protected ColumnDefinition(final String name, final String dataType, final String defaultValue, final String allowsNull) {
      _name = name;
      _dataType = dataType;
      _defaultValue = defaultValue;
      _allowsNull = allowsNull;
    }

    public String getName() {
      return _name;
    }

    public String getDataType() {
      return _dataType;
    }

    public String getDefaultValue() {
      return _defaultValue;
    }

    public String getAllowsNull() {
      return _allowsNull;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(getName().toUpperCase()).append('=').append(getDataType().toUpperCase());
      if (getAllowsNull() != null) {
        sb.append(";NULL=").append(getAllowsNull());
      }
      if (getDefaultValue() != null) {
        sb.append(";DEFAULT=").append(getDefaultValue());
      }
      return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof ColumnDefinition) {
        final ColumnDefinition c = (ColumnDefinition) obj;
        return ObjectUtils.equals(getName(), c.getName())
            && ObjectUtils.equals(getDataType(), c.getDataType())
            && ObjectUtils.equals(getAllowsNull(), c.getAllowsNull())
            && ObjectUtils.equals(getDefaultValue(), c.getDefaultValue());
      }
      return false;
    }

    @Override
    public int hashCode() {
      int hc = 1;
      hc = hc * 17 + ObjectUtils.hashCode(getName());
      hc = hc * 17 + ObjectUtils.hashCode(getDataType());
      hc = hc * 17 + ObjectUtils.hashCode(getAllowsNull());
      hc = hc * 17 + ObjectUtils.hashCode(getDefaultValue());
      return hc;
    }

    @Override
    public int compareTo(final ColumnDefinition c) {
      return getName().compareTo(c.getName());
    }
  }

  public abstract String getAllSchemasSQL(String catalog);

  public abstract String getAllTablesSQL(String catalog, String schema);

  public abstract String getAllViewsSQL(String catalog, String schema);

  public abstract String getAllColumnsSQL(String catalog, String schema, String table);

  public abstract String getAllSequencesSQL(String catalog, String schema);

  public abstract String getAllForeignKeyConstraintsSQL(String catalog, String schema);

  public abstract String getCreateSchemaSQL(String catalog, String schema);

  public abstract String getSchemaVersionTable(String schemaGroupName);

  public abstract String getSchemaVersionSQL(String catalog, String schemaGroupName);

  public abstract CatalogCreationStrategy getCatalogCreationStrategy();

  public void setActiveSchema(final Connection connection, final String schema) throws SQLException {
    // override in subclasses as necessary
  }

  protected Connection connect(final String catalog) throws SQLException {
    final Connection conn = DriverManager.getConnection(getCatalogToConnectTo(catalog), _user, _password);
    conn.setAutoCommit(true);
    return conn;
  }

  @Override
  public String getCatalogToConnectTo(final String catalog) {
    return getDbHost() + "/" + catalog;
  }

  protected List<String> getAllTables(final String catalog, final String schema, final Statement statement) throws SQLException {
    final List<String> tables = new LinkedList<>();
    final ResultSet rs = statement.executeQuery(getAllTablesSQL(catalog, schema));
    while (rs.next()) {
      tables.add(rs.getString("name"));
    }
    rs.close();
    return tables;
  }

  protected List<String> getAllViews(final String catalog, final String schema, final Statement statement) throws SQLException {
    final List<String> tables = new LinkedList<>();
    final ResultSet rs = statement.executeQuery(getAllViewsSQL(catalog, schema));
    while (rs.next()) {
      tables.add(rs.getString("name"));
    }
    rs.close();
    return tables;
  }

  private List<ColumnDefinition> getAllColumns(final String catalog, final String schema, final String table, final Statement statement) throws SQLException {
    final List<ColumnDefinition> columns = new LinkedList<>();
    final ResultSet rs = statement.executeQuery(getAllColumnsSQL(catalog, schema, table));
    while (rs.next()) {
      columns.add(new ColumnDefinition(rs.getString("name"), rs.getString("datatype"), rs.getString("defaultvalue"), rs.getString("allowsnull")));
    }
    rs.close();
    return columns;
  }

  @Override
  public void clearTables(final String catalog, final String schema, final Collection<String> ignoredTables) {
    final LinkedList<String> script = new LinkedList<>();
    Connection conn = null;
    try {
      if (!getCatalogCreationStrategy().catalogExists(catalog)) {
        return; // nothing to clear
      }

      conn = connect(catalog);
      setActiveSchema(conn, schema);
      final Statement statement = conn.createStatement();

      // Clear tables SQL
      final List<String> tablesToClear = new ArrayList<>();
      for (final String name : getAllTables(catalog, schema, statement)) {
        if (!ignoredTables.contains(name.toLowerCase())) {
          tablesToClear.add(name);
        }
      }
      final List<String> clearTablesCommands = getClearTablesCommand(schema, tablesToClear);
      script.addAll(clearTablesCommands);
      for (final String name : tablesToClear) {
        final Table table = new Table(name);
        if (matches(table.getName().toLowerCase(), Pattern.compile(".*?hibernate_sequence"))) { // if it's a sequence table, reset it
          script.add("INSERT INTO " + table.getQualifiedName(getHibernateDialect(), null, schema) + " values ( 1 )");
        }
      }

      // Now execute it all. Constraints are taken into account by retrying the failed statement after all
      // dependent tables have been cleared first.
      int i = 0;
      final int maxAttempts = script.size() * 3; // make sure the loop eventually terminates. Important if there's a cycle in the table dependency graph
      SQLException latestException = null;
      while (i < maxAttempts && !script.isEmpty()) {
        final String sql = script.remove();
        try {
          statement.executeUpdate(sql);
        } catch (final SQLException e) {
          // assume it failed because of a constraint violation
          // try deleting other tables first - make this the new last statement
          latestException = e;
          script.add(sql);
        }
        i++;
      }
      statement.close();

      if (i == maxAttempts && !script.isEmpty()) {
        throw new OpenGammaRuntimeException("Failed to clear tables - is there a cycle in the table dependency graph?", latestException);
      }

    } catch (final SQLException e) {
      throw new OpenGammaRuntimeException("Failed to clear tables", e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (final SQLException e) {
      }
    }
  }

  protected List<String> getClearTablesCommand(final String schema, final List<String> tablesToClear) {
    final List<String> clearTablesCommands = new ArrayList<>();
    for (final String name : tablesToClear) {
      final Table table = new Table(name);
      clearTablesCommands.add("DELETE FROM " + table.getQualifiedName(getHibernateDialect(), null, schema));
    }
    return clearTablesCommands;
  }

  protected List<String> getAllSchemas(final String catalog, final Statement stmt) throws SQLException {
    final List<String> schemas = new LinkedList<>();
    final ResultSet rs = stmt.executeQuery(getAllSchemasSQL(catalog));
    while (rs.next()) {
      schemas.add(rs.getString("name"));
    }
    rs.close();
    return schemas;
  }

  @Override
  public void createSchema(final String catalog, final String schema) {
    Connection conn = null;
    try {
      getCatalogCreationStrategy().create(catalog);

      if (schema != null) {
        // Connect to the new catalog and create the schema
        conn = connect(catalog);
        final Statement statement = conn.createStatement();

        final Collection<String> schemas = getAllSchemas(catalog, statement);
        if (!schemas.contains(schema)) {
          final String createSchemaSql = getCreateSchemaSQL(catalog, schema);
          statement.executeUpdate(createSchemaSql);
        }

        statement.close();
      }

    } catch (final SQLException e) {
      throw new OpenGammaRuntimeException("Failed to clear tables", e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (final SQLException e) {
      }
    }
  }

  protected List<String> getAllSequences(final String catalog, final String schema, final Statement stmt) throws SQLException {
    final List<String> sequences = new LinkedList<>();
    final String sql = getAllSequencesSQL(catalog, schema);
    if (sql != null) {
      final ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        sequences.add(rs.getString("name"));
      }
      rs.close();
    }
    return sequences;
  }

  protected List<Pair<String, String>> getAllForeignKeyConstraints(final String catalog, final String schema, final Statement stmt) throws SQLException {
    final List<Pair<String, String>> sequences = new LinkedList<>();
    final String sql = getAllForeignKeyConstraintsSQL(catalog, schema);
    if (sql != null) {
      final ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        sequences.add(Pairs.of(rs.getString("name"), rs.getString("table_name")));
      }
      rs.close();
    }
    return sequences;
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
        final Identifier catalogName = Identifier.toIdentifier(catalog);
        final Identifier schemaName = Identifier.toIdentifier(schema);
        final Identifier objectName = Identifier.toIdentifier(name);
        final QualifiedName qualifiedName = new QualifiedNameImpl(catalogName, schemaName, objectName);
        final SequenceStructure sequenceStructure = new SequenceStructure(null, qualifiedName, 0, 1, Long.class);
        //final SequenceStructure sequenceStructure = new SequenceStructure(getHibernateDialect(), name, 0, 1, Long.class);
        final String[] dropSequenceStrings = sequenceStructure.sqlDropStrings(getHibernateDialect());
        script.addAll(Arrays.asList(dropSequenceStrings));
      }

      //now execute drop sequence
      statement.close();
      statement = conn.createStatement();
      for (final String sql : script) {
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

  @Override
  public void executeSql(final String catalog, final String schema, final String sql) {
    final ArrayList<String> sqlStatements = new ArrayList<>();
    boolean inDollarQuote = false;
    boolean inComment = false;
    StringBuilder stmtBuilder = new StringBuilder();
    for (int currentIdx = 0; currentIdx < sql.length(); currentIdx++) {
      final char currentChar = sql.charAt(currentIdx);
      final char nextChar = currentIdx + 1 < sql.length() ? sql.charAt(currentIdx + 1) : 0;
      if (inDollarQuote) {
        // Add everything verbatim until the end-of-quote $$
        if (currentChar == '$' && nextChar == '$') {
          inDollarQuote = false;
        }
        stmtBuilder.append(currentChar);
        continue;
      }
      final boolean isLineEnd = currentChar == '\r' || currentChar == '\n';
      if (currentChar == '\r' && nextChar == '\n') {
        currentIdx++;
      }
      if (inComment) {
        // Ignore everything until the next new line
        if (isLineEnd) {
          inComment = false;
        }
        continue;
      }
      if (isLineEnd) {
        stmtBuilder.append(" ");
        continue;
      }
      if (currentChar == ';') {
        final String currentStmt = stmtBuilder.toString().trim();
        if (!currentStmt.isEmpty()) {
          sqlStatements.add(currentStmt);
        }
        stmtBuilder = new StringBuilder();
        continue;
      }
      if (currentChar == '-' && nextChar == '-') {
        inComment = true;
        continue;
      }
      if (currentChar == '$' && nextChar == '$') {
        inDollarQuote = true;
      }
      stmtBuilder.append(currentChar);
    }
    final String currentStmt = stmtBuilder.toString().trim();
    if (!currentStmt.isEmpty()) {
      sqlStatements.add(currentStmt);
    }

    Connection conn = null;
    try {
      conn = connect(catalog);
      setActiveSchema(conn, schema);

      final Statement statement = conn.createStatement();
      for (final String sqlStatement : sqlStatements) {
        try {
          statement.execute(sqlStatement);
        } catch (final SQLException e) {
          throw new OpenGammaRuntimeException("Failed to execute statement (" + getDbHost() + ") " + sqlStatement, e);
        }
      }
      statement.close();

    } catch (final SQLException e) {
      throw new OpenGammaRuntimeException("Failed to execute statement", e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (final SQLException e) {
      }
    }
  }

  @Override
  public String describeDatabase(final String catalog) {
    return describeDatabase(catalog, null);
  }

  @Override
  public String describeDatabase(final String catalog, final String prefix) {
    final StringBuilder description = new StringBuilder();
    Connection conn = null;
    try {
      conn = connect(catalog);
      final Statement stmt = conn.createStatement();
      final List<String> schemas = getAllSchemas(catalog, stmt);
      Collections.sort(schemas);
      if (schemas.size() == 0) {
        schemas.add(null);
      }
      for (final String schema : schemas) {
        description.append("schema: ").append(schema).append("\r\n");
        final List<String> tables = getAllTables(catalog, schema, stmt);
        Collections.sort(tables);
        for (final String table : tables) {
          description.append("table: ").append(table).append("\r\n");
          final List<ColumnDefinition> columns = getAllColumns(catalog, schema, table, stmt);
          Collections.sort(columns);
          for (final ColumnDefinition column : columns) {
            description.append("column: ").append(column).append("\r\n");
          }
        }
        final List<String> sequences = getAllSequences(catalog, schema, stmt);
        Collections.sort(sequences);
        for (final String sequence : sequences) {
          description.append("sequence: ").append(sequence).append("\r\n");
        }
        final List<Pair<String, String>> foreignKeys = getAllForeignKeyConstraints(catalog, schema, stmt);
        Collections.sort(foreignKeys, FirstThenSecondPairComparator.INSTANCE);
        for (final Pair<String, String> foreignKey : foreignKeys) {
          description.append("foreign key: ").append(foreignKey.getFirst()).append('.').append(foreignKey.getSecond()).append("\r\n");
        }
      }
    } catch (final SQLException e) {
      e.printStackTrace();
      System.err.println("e.getMessage: " + e.getMessage());
      throw new OpenGammaRuntimeException("SQL exception", e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (final SQLException e) {
      }
    }
    return description.toString();
  }

  @Override
  public List<String> listTables(final String catalog) {
    Connection conn = null;
    try {
      conn = connect(catalog);
      final Statement stmt = conn.createStatement();
      return getAllTables(catalog, null, stmt);
    } catch (final SQLException e) {
      e.printStackTrace();
      System.err.println("e.getMessage: " + e.getMessage());
      throw new OpenGammaRuntimeException("SQL exception", e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (final SQLException e) {
      }
    }
  }

  @Override
  public Integer getSchemaGroupVersion(final String catalog, final String schema, final String schemaGroupName) {
    Connection conn = null;
    try {
      conn = connect(catalog);
      setActiveSchema(conn, schema);
      final Statement statement = conn.createStatement();
      final List<String> tables = getAllTables(catalog, schema, statement);
      if (!tables.contains(getSchemaVersionTable(schemaGroupName))) {
        return null;
      }
      final ResultSet rs = statement.executeQuery(getSchemaVersionSQL(catalog, schemaGroupName));
      String version;
      try {
        rs.next();
        version = rs.getString("version_value");
        if (rs.next()) {
          throw new OpenGammaRuntimeException("Expected one schema version entry for group " + schemaGroupName + " but found multiple");
        }
      } finally {
        rs.close();
      }
      return Integer.parseInt(version);
    } catch (final SQLException e) {
      e.printStackTrace();
      System.err.println("e.getMessage: " + e.getMessage());
      throw new OpenGammaRuntimeException("SQL exception", e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (final SQLException e) {
        }
      }
    }
  }

}
