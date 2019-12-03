/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PostgresDbDialectTest extends DbDialectTest {

  /**
   * Tests a Postgres dialect.
   */
  public PostgresDbDialectTest() {
    setDialect(PostgresDbDialect.INSTANCE);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting the driver.
   */
  public void testGetJDBCDriver() {
    assertEquals(org.postgresql.Driver.class, getDialect().getJDBCDriverClass());
  }

  /**
   * Tests getting the Hibernate dialect.
   */
  public void tesGetHibernateDialect() {
    assertEquals(org.hibernate.dialect.PostgreSQLDialect.class, getDialect().getHibernateDialect().getClass());
  }

  /**
   * Tests getting the name.
   */
  public void testGetName() {
    assertEquals("Postgres", getDialect().getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests next sequence value select.
   */
  public void testSqlNextSequenceValueSelect() {
    assertEquals("SELECT nextval('MySeq')", getDialect().sqlNextSequenceValueSelect("MySeq"));
  }

  /**
   * Tests next sequence value inline.
   */
  public void testSqlNextSequenceValueInline() {
    assertEquals("nextval('MySeq')", getDialect().sqlNextSequenceValueInline("MySeq"));
  }

}
