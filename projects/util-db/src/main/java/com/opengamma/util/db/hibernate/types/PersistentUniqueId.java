/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.hibernate.types;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.EnhancedUserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueId;

/**
 * Persist {@link com.opengamma.id.UniqueId} via hibernate as a String.
 */
public class PersistentUniqueId implements EnhancedUserType {

  /**
   * Singleton instance.
   */
  public static final PersistentUniqueId INSTANCE = new PersistentUniqueId();

  private static final Logger LOGGER = LoggerFactory.getLogger(PersistentUniqueId.class);

  private static final int[] SQL_TYPES = new int[] {Types.VARCHAR };

  @Override
  public int[] sqlTypes() {
    return SQL_TYPES;
  }

  @Override
  public Class<?> returnedClass() {
    return UniqueId.class;
  }

  @Override
  public boolean equals(final Object x, final Object y) throws HibernateException {
    if (x == y) {
      return true;
    }
    if (x == null || y == null) {
      return false;
    }
    final UniqueId ix = (UniqueId) x;
    final UniqueId iy = (UniqueId) y;
    return ix.equals(iy);
  }

  @Override
  public int hashCode(final Object object) throws HibernateException {
    return object.hashCode();
  }

  @Override
  public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SessionImplementor session,
      final Object object) throws HibernateException, SQLException {
    return nullSafeGet(resultSet, names[0], session);
  }

  public Object nullSafeGet(final ResultSet resultSet, final String name, final SessionImplementor session) throws SQLException {
    final String value = new StringType().nullSafeGet(resultSet, name, session);
    if (value == null) {
      return null;
    }
    return UniqueId.parse(value);
  }

  @Override
  public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index,
      final SessionImplementor session) throws HibernateException, SQLException {
    if (value == null) {
      LOGGER.debug("UniqueId -> String : NULL -> NULL");
      new StringType().nullSafeSet(preparedStatement, null, index, session);
    } else {
      LOGGER.debug("UniqueId -> String : {}   ->  {}", value, UniqueId.parse((String) value));
      new StringType().nullSafeSet(preparedStatement, UniqueId.parse((String) value), index, session);
    }
  }

  @Override
  public Object deepCopy(final Object value) throws HibernateException {
    return value;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(final Object value) throws HibernateException {
    return (Serializable) value;
  }

  @Override
  public Object assemble(final Serializable serializable, final Object value) throws HibernateException {
    return serializable;
  }

  @Override
  public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
    return original;
  }

  // __________ EnhancedUserType ____________________

  @Override
  public String objectToSQLString(final Object object) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toXMLString(final Object object) {
    return object.toString();
  }

  @Override
  public Object fromXMLString(final String string) {
    return UniqueId.parse(string);
  }

}
