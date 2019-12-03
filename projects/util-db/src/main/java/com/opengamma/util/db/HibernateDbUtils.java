/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.SQLException;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;

/**
 * Utilities for working woth the database.
 */
public class HibernateDbUtils {

  /**
   * Improves the exception message.
   *
   * @param ex  the exception to fix, not null
   * @return the original exception, not null
   */
  protected static DataAccessException fixSQLExceptionCause(final DataAccessException ex) {
    final Throwable cause = ex.getCause();
    if (cause instanceof SQLException && cause.getCause() == null) {
      final SQLException next = ((SQLException) cause).getNextException();
      if (next != null) {
        cause.initCause(next);
      }
    }
    return ex;
  }

  /**
   * Builds a Hibernate query.
   *
   * @param propertyName  the property name
   * @param value  the value
   * @return the criterion, not null
   */
  public static Criterion eqOrIsNull(final String propertyName, final Object value) {
    if (value == null) {
      return Restrictions.isNull(propertyName);
    }
    return Restrictions.eq(propertyName, value);
  }

}
