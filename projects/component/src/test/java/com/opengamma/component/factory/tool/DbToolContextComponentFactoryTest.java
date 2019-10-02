/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.tool;

import static com.opengamma.component.factory.tool.DbToolContextComponentFactory.getCatalog;
import static com.opengamma.component.factory.tool.DbToolContextComponentFactory.getMSSQLCatalog;
import static com.opengamma.component.factory.tool.DbToolContextComponentFactory.getStandardCatalog;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;

/** Test. */
@Test(groups = TestGroup.UNIT)
public class DbToolContextComponentFactoryTest {

  private static final String MSSQL_URL_1 = "jdbc:sqlserver://someserver:1433;integratedSecurity=true;databaseName=someDatabase";
  private static final String MSSQL_URL_2 = "jdbc:sqlserver://someserver:1433;databaseName=someDatabase;integratedSecurity=true";
  private static final String MSSQL_URL_3 = "jdbc:sqlserver://someserver:1433;databaseName=someDatabase";
  private static final String HSQL_URL = "jdbc:hsqldb:file:data/hsqldb/og-fin";
  private static final String POSTGRES_URL = "jdbc:postgresql://localhost/og_financial";

  private static final String MSSQL_DB = "someDatabase";
  private static final String HSQL_DB = "og-fin";
  private static final String POSTGRES_DB = "og_financial";

  private static final String MSSQL_BAD_INVALID_SLASH = "jdbc:sqlserver://someserver:1433;/databaseName=someDatabase";
  private static final String MSSQL_BAD_NO_DB_NAME = "jdbc:sqlserver://someserver:1433;integratedSecurity=true;databaseName=";
  private static final String MSSQL_BAD_NO_DB_ATALL = "jdbc:sqlserver://someserver:1433;integratedSecurity=true";

  private static final String COMPLETE_GARBAGE = "abcdefgh";

  /**
   *
   */
  @Test
  public void testRecognizeMSSQL() {
    Validate.isTrue(getMSSQLCatalog(MSSQL_URL_1).equals(MSSQL_DB), "url1 did not work");
    Validate.isTrue(getMSSQLCatalog(MSSQL_URL_2).equals(MSSQL_DB), "url2 did not work");
    Validate.isTrue(getMSSQLCatalog(MSSQL_URL_3).equals(MSSQL_DB), "url3 did not work");
  }

  /**
   *
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoInvalidSlash() {
    getMSSQLCatalog(MSSQL_BAD_INVALID_SLASH);
  }

  /**
   *
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoDbName() {
    getMSSQLCatalog(MSSQL_BAD_NO_DB_NAME);
  }

  /**
   *
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoDbAtAll() {
    getMSSQLCatalog(MSSQL_BAD_NO_DB_ATALL);
  }

  /**
   *
   */
  @Test
  public void testRecognizeHSQL() {
    Validate.isTrue(getStandardCatalog(HSQL_URL).equals(HSQL_DB), "url did not work");
  }

  /**
   *
   */
  @Test
  public void testRecognizePostgres() {
    Validate.isTrue(getStandardCatalog(POSTGRES_URL).equals(POSTGRES_DB), "url did not work");
  }

  /**
   *
   */
  @Test
  public void testAll() {
    Validate.isTrue(getCatalog(MSSQL_URL_1).equals(MSSQL_DB), "url1 did not work");
    Validate.isTrue(getCatalog(MSSQL_URL_2).equals(MSSQL_DB), "url2 did not work");
    Validate.isTrue(getCatalog(MSSQL_URL_3).equals(MSSQL_DB), "url3 did not work");
    Validate.isTrue(getCatalog(HSQL_URL).equals(HSQL_DB), "url did not work");
    Validate.isTrue(getCatalog(POSTGRES_URL).equals(POSTGRES_DB), "url did not work");
  }

  /**
   *
   */
  @Test
  public void testHandleNull() {
    Validate.isTrue(getCatalog(null) == null, "null did not work");
  }

  /**
   *
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testCompleteGarbage() {
    getCatalog(COMPLETE_GARBAGE);
  }

}
