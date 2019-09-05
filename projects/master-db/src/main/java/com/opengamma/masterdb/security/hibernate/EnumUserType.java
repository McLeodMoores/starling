/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Custom Hibernate type for trivial enums.
 * 
 * @param <E>
 *          the enum type
 */
public abstract class EnumUserType<E extends Enum<E>> implements UserType {

  private final Class<E> _clazz;
  private final Map<String, E> _stringToEnum;
  private final Map<E, String> _enumToString;

  protected EnumUserType(final Class<E> clazz, final E[] values) {
    _clazz = clazz;
    _stringToEnum = new HashMap<>();
    _enumToString = new EnumMap<>(clazz);
    for (final E value : values) {
      final String string = enumToStringNoCache(value);
      _stringToEnum.put(string, value);
      _enumToString.put(value, string);
    }
  }

  @Override
  public Object assemble(final Serializable arg0, final Object arg1) throws HibernateException {
    return arg0;
  }

  @Override
  public Object deepCopy(final Object arg0) throws HibernateException {
    return arg0;
  }

  @Override
  public Serializable disassemble(final Object arg0) throws HibernateException {
    return (Serializable) arg0;
  }

  @Override
  public boolean equals(final Object x, final Object y) throws HibernateException {
    // Check for either being null for database null semantics which ObjectUtils won't give us
    if (x == null || y == null) {
      return false;
    }
    return ObjectUtils.equals(x, y);
  }

  @Override
  public int hashCode(final Object arg0) throws HibernateException {
    return arg0.hashCode();
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  protected abstract String enumToStringNoCache(E value);

  protected E stringToEnum(final String string) {
    final E value = _stringToEnum.get(string);
    if (value == null) {
      throw new OpenGammaRuntimeException("unexpected value: " + string);
    }
    return value;
  }

  @Override
  public Object nullSafeGet(final ResultSet resultSet, final String[] columnNames, final SharedSessionContractImplementor session,
      final Object owner) throws HibernateException, SQLException {
    final String databaseValue = resultSet.getString(columnNames[0]);
    if (resultSet.wasNull()) {
      return null;
    }
    return stringToEnum(databaseValue);
  }

  protected String enumToString(final E value) {
    return _enumToString.get(value);
  }

  @SuppressWarnings({ "unchecked" })
  @Override
  public void nullSafeSet(final PreparedStatement stmt, final Object value, final int index,
      final SharedSessionContractImplementor session) throws HibernateException, SQLException {
    if (value == null) {
      stmt.setNull(index, StandardBasicTypes.STRING.sqlType());
    } else {
      stmt.setString(index, enumToString((E) value));
    }
  }

  @Override
  public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
    return original;
  }

  @Override
  public Class<?> returnedClass() {
    return _clazz;
  }

  @Override
  public int[] sqlTypes() {
    return new int[] { StandardBasicTypes.STRING.sqlType() };
  }
}
