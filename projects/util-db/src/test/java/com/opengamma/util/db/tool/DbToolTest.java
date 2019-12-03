/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.hibernate.mapping.Table;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestProperties;

/**
 * Test DbTool.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbToolTest {

  private static final String TEST_TABLE = "db_tool_unit_test_table";

  private DbTool _tool;

  /**
   * Tests up the tool.
   *
   * @throws Exception  if there is a problem with the setup
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    final Properties props = TestProperties.getTestProperties();
    final String dbHost = props.getProperty("utildb.jdbc.url");
    final String user = props.getProperty("utildb.jdbc.username");
    final String password = props.getProperty("utildb.jdbc.password");
    _tool = new DbTool(dbHost, user, password);
    _tool.initialize();
  }

  /**
   * Tests dropping a table.
   *
   * @throws Exception  if there is a problem
   */
  public void testDrop() throws Exception {
    _tool.createTestSchema();
    createTestTable();
    _tool.dropTestSchema();

    // The table should no longer be there
    try {
      _tool.executeSql(_tool.getTestCatalog(), _tool.getTestSchema(), "SELECT * FROM " + TEST_TABLE);
      fail();
    } catch (final OpenGammaRuntimeException e) {
      // Ok - no table should be there!
    }
  }

  /**
   * Tests clearing a table.
   *
   * @throws Exception  if there is a problem
   */
  public void testClear() throws Exception {
    _tool.createTestSchema();
    createTestTable();
    _tool.executeSql(_tool.getTestCatalog(), _tool.getTestSchema(), "INSERT INTO " + TEST_TABLE + " (test_column) VALUES ('test')");

    _tool.clearTestTables();

    try (Connection connection = getConnection()) {
      try (Statement statement = connection.createStatement()) {
        try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + TEST_TABLE)) {
          if (rs.next()) {
            final int count = rs.getInt(1);
            assertEquals(0, count);
          } else {
            fail();
          }
        rs.close();
      }
      statement.close();
      }
    connection.close();
    }
    _tool.dropTestSchema();
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(
        _tool.getDbServerHost() + "/" + _tool.getTestCatalog(), _tool.getUser(), _tool.getPassword());
  }

  private void createTestTable() {
    final Table table = new Table(TEST_TABLE);
    try {
      final String dropSql = table.sqlDropString(_tool.getHibernateDialect(), null, _tool
          .getTestSchema());
      _tool.executeSql(_tool.getTestCatalog(), _tool.getTestSchema(), dropSql);
    } catch (final OpenGammaRuntimeException e) {
      // It might not exist, that's OK
    }
    final String createSql = "CREATE TABLE "
        + table.getQualifiedName(_tool.getHibernateDialect(), null, _tool.getTestSchema()) + " (test_column char(50))";
    _tool.executeSql(_tool.getTestCatalog(), _tool.getTestSchema(), createSql);
  }

}
