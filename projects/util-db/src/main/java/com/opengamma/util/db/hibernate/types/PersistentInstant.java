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
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.TimestampType;
import org.hibernate.usertype.EnhancedUserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.util.db.DbDateUtils;

/**
 * Persist {@link Instant} via hibernate as a TIMESTAMP.
 */
public class PersistentInstant implements EnhancedUserType {

  /**
   * Singleton instance.
   */
  public static final PersistentInstant INSTANCE = new PersistentInstant();

  private static final Logger LOGGER = LoggerFactory.getLogger(PersistentInstant.class);

  private static final int[] SQL_TYPES = new int[] { Types.TIMESTAMP };

  @Override
  public int[] sqlTypes() {
    return SQL_TYPES;
  }

  @Override
  public Class<?> returnedClass() {
    return Instant.class;
  }

  @Override
  public boolean equals(final Object x, final Object y) throws HibernateException {
    if (x == y) {
      return true;
    }
    if (x == null || y == null) {
      return false;
    }
    final Instant ix = (Instant) x;
    final Instant iy = (Instant) y;
    return ix.equals(iy);
  }

  @Override
  public int hashCode(final Object object) throws HibernateException {
    return object.hashCode();
  }

  @Override
  public Object nullSafeGet(final ResultSet resultSet, final String[] names,
      final SharedSessionContractImplementor session, final Object owner) throws HibernateException, SQLException {
    return nullSafeGet(resultSet, names[0], session);
  }

  public Object nullSafeGet(final ResultSet resultSet, final String name, final SharedSessionContractImplementor session) throws SQLException {
    final java.sql.Timestamp value = (java.sql.Timestamp) new TimestampType().nullSafeGet(resultSet, name, session);
    if (value == null) {
      return null;
    }
    return DbDateUtils.fromSqlTimestamp(value);
  }

  @Override
  public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index,
      final SharedSessionContractImplementor session) throws HibernateException, SQLException {
    if (value == null) {
      LOGGER.debug("INSTANT -> TIMESTAMP : NULL -> NULL");
      new TimestampType().nullSafeSet(preparedStatement, null, index, session);
    } else {
      LOGGER.debug("INSTANT -> TIMESTAMP : {}   ->  {}", value, DbDateUtils.toSqlTimestamp((Instant) value));
      new TimestampType().nullSafeSet(preparedStatement, DbDateUtils.toSqlTimestamp((Instant) value), index, session);
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
    return Instant.parse(string);
  }

}
