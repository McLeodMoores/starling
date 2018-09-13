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
public class HSQLDbDialectTest extends DbDialectTest {

  /**
   * Tests a HSQL dialect.
   */
  public HSQLDbDialectTest() {
    setDialect(HSQLDbDialect.INSTANCE);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting the driver.
   */
  public void testGetJDBCDriver() {
    assertEquals(org.hsqldb.jdbcDriver.class, getDialect().getJDBCDriverClass());
  }

  /**
   * Tests getting the Hibernate dialect.
   */
  public void testGetHibernateDialect() {
    assertEquals(org.hibernate.dialect.HSQLDialect.class, getDialect().getHibernateDialect().getClass());
  }

  /**
   * Tests getting the name.
   */
  public void testGetName() {
    assertEquals("HSQL", getDialect().getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests next sequence value select.
   */
  public void testSqlNextSequenceValueSelect() {
    assertEquals("CALL NEXT VALUE FOR MySeq", getDialect().sqlNextSequenceValueSelect("MySeq"));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a wildcard query.
   */
  @Override
  public void testSqlWildcardQuery() {
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", getDialect().sqlWildcardQuery("AND col ", ":arg", "a*"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", getDialect().sqlWildcardQuery("AND col ", ":arg", "a?"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", getDialect().sqlWildcardQuery("AND col ", ":arg", "a*b"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", getDialect().sqlWildcardQuery("AND col ", ":arg", "a?b"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", getDialect().sqlWildcardQuery("AND col ", ":arg", "*b"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", getDialect().sqlWildcardQuery("AND col ", ":arg", "?b"));

    assertEquals("AND col = :arg ", getDialect().sqlWildcardQuery("AND col ", ":arg", "a"));
    assertEquals("AND col = :arg ", getDialect().sqlWildcardQuery("AND col ", ":arg", ""));
    assertEquals("", getDialect().sqlWildcardQuery("AND col ", ":arg", null));
  }

}
