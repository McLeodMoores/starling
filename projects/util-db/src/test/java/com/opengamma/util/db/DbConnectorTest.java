/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.Test;

import com.jolbox.bonecp.BoneCPDataSource;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DbConnectorTest {

  //-------------------------------------------------------------------------
  /**
   * Tests that the inputs cannot be null.
   */
  @SuppressWarnings("resource")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNulls() {
    new DbConnector(null, null, null, null, null, null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the fields are set correctly.
   */
  public void testBasics() {
    try (BoneCPDataSource ds = new BoneCPDataSource()) {
      final HSQLDbDialect dialect = HSQLDbDialect.INSTANCE;
      final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(ds);
      final DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
      final DataSourceTransactionManager transMgr = new DataSourceTransactionManager();
      final TransactionTemplate transTemplate = new TransactionTemplate(transMgr, transDefn);
      try (DbConnector test = new DbConnector("Test", dialect, ds, jdbcTemplate, null, transTemplate)) {
        assertSame(ds, test.getDataSource());
        assertSame(dialect, test.getDialect());
        assertSame(jdbcTemplate, test.getJdbcTemplate());
        assertSame(jdbcTemplate.getJdbcOperations(), test.getJdbcOperations());
        assertEquals(null, test.getHibernateSessionFactory());
        assertEquals(null, test.getHibernateTemplate());
        assertSame(transMgr, test.getTransactionManager());
        assertSame(transTemplate, test.getTransactionTemplate());
        test.close();
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method.
   */
  public void testToString() {
    try (BoneCPDataSource ds = new BoneCPDataSource()) {
      final HSQLDbDialect dialect = HSQLDbDialect.INSTANCE;
      final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(ds);
      final DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
      final DataSourceTransactionManager transMgr = new DataSourceTransactionManager();
      final TransactionTemplate transTemplate = new TransactionTemplate(transMgr, transDefn);
      try (DbConnector test = new DbConnector("Test", dialect, ds, jdbcTemplate, null, transTemplate)) {
        assertEquals("DbConnector[Test]", test.toString());
        test.close();
      }
    }
  }

}
