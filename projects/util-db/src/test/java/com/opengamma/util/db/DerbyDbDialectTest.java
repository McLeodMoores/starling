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
public class DerbyDbDialectTest extends DbDialectTest {

  /**
   * Tests a Derby dialect.
   */
  public DerbyDbDialectTest() {
    setDialect(DerbyDbDialect.INSTANCE);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting the driver.
   */
  public void testGetJDBCDriver() {
    assertEquals(org.apache.derby.jdbc.EmbeddedDriver.class, getDialect().getJDBCDriverClass());
  }

  /**
   * Tests getting the Hibernate dialect.
   */
  public void testGetHibernateDialect() {
    assertEquals(org.hibernate.dialect.DerbyDialect.class, getDialect().getHibernateDialect().getClass());
  }

  /**
   * Tests getting the name.
   */
  public void testGetName() {
    assertEquals("Derby", getDialect().getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests next sequence value select.
   */
  public void testSqlNextSequenceValueSelect() {
    assertEquals("SELECT NEXT VALUE FOR MySeq FROM sysibm.sysdummy1", getDialect().sqlNextSequenceValueSelect("MySeq"));
  }

}
