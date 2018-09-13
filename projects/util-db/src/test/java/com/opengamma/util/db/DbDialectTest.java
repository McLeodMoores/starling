/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbDialect.
 */
@Test(groups = TestGroup.UNIT)
public class DbDialectTest {
  /** The dialect */
  private DbDialect _dialect = new MockDbDialect();

  /**
   * Gets the dialect.
   *
   * @return  the dialect
   */
  public DbDialect getDialect() {
    return _dialect;
  }

  /**
   * Sets the dialect.
   *
   * @param dialect  the dialect
   */
  public void setDialect(final DbDialect dialect) {
    _dialect = dialect;
  }

  //-------------------------------------------------------------------------
  /**
   * Tests if there is a wildcard present.
   */
  public void testIsWildcard() {
    assertEquals(true, _dialect.isWildcard("a*"));
    assertEquals(true, _dialect.isWildcard("a?"));
    assertEquals(true, _dialect.isWildcard("a*b"));
    assertEquals(true, _dialect.isWildcard("a?b"));
    assertEquals(true, _dialect.isWildcard("*b"));
    assertEquals(true, _dialect.isWildcard("?b"));

    assertEquals(false, _dialect.isWildcard("a"));
    assertEquals(false, _dialect.isWildcard(""));
    assertEquals(false, _dialect.isWildcard(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the SQL for a wildcard operator.
   */
  public void testSqlWildcardOperator() {
    assertEquals("LIKE", _dialect.sqlWildcardOperator("a*"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("a?"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("a*b"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("a?b"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("*b"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("?b"));

    assertEquals("=", _dialect.sqlWildcardOperator("a"));
    assertEquals("=", _dialect.sqlWildcardOperator(""));
    assertEquals("=", _dialect.sqlWildcardOperator(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the SQL for a wildcard adjust value.
   */
  public void testSqlWildcardAdjustValue() {
    assertEquals("a%", _dialect.sqlWildcardAdjustValue("a*"));
    assertEquals("a_", _dialect.sqlWildcardAdjustValue("a?"));
    assertEquals("a%b", _dialect.sqlWildcardAdjustValue("a*b"));
    assertEquals("a_b", _dialect.sqlWildcardAdjustValue("a?b"));
    assertEquals("%b", _dialect.sqlWildcardAdjustValue("*b"));
    assertEquals("_b", _dialect.sqlWildcardAdjustValue("?b"));

    assertEquals("a", _dialect.sqlWildcardAdjustValue("a"));
    assertEquals("", _dialect.sqlWildcardAdjustValue(""));
    assertEquals(null, _dialect.sqlWildcardAdjustValue(null));

    assertEquals("a%b\\%c", _dialect.sqlWildcardAdjustValue("a*b%c"));
    assertEquals("a_b\\_c", _dialect.sqlWildcardAdjustValue("a?b_c"));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the SQL for a wildcard query.
   */
  public void testSqlWildcardQuery() {
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a*"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a?"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a*b"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a?b"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "*b"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "?b"));

    assertEquals("AND col = :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a"));
    assertEquals("AND col = :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", ""));
    assertEquals("", _dialect.sqlWildcardQuery("AND col ", ":arg", null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the SQL when no paging is used.
   */
  public void testSqlApplyPagingNoPaging() {
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo ",
        _dialect.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", null));
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo ",
        _dialect.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", PagingRequest.ALL));
  }

  /**
   * Tests the SQL for applying the paging limit.
   */
  public void testSqlApplyPagingLimit() {
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo FETCH FIRST 20 ROWS ONLY ",
        _dialect.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", PagingRequest.ofPage(1, 20)));
  }

  /**
   * Tests the SQL for applying the paging offset limit.
   */
  public void testSqlApplyPagingOffsetLimit() {
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo OFFSET 40 ROWS FETCH NEXT 20 ROWS ONLY ",
        _dialect.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", PagingRequest.ofPage(3, 20)));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the SQL for null default.
   */
  public void testSqlNullDefault() {
    assertEquals("COALESCE(a, b)", _dialect.sqlNullDefault("a", "b"));
  }

}
