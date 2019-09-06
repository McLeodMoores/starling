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

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import com.opengamma.id.UniqueId;

/**
 * Persist {@link com.opengamma.id.UniqueId} via hibernate as a 3 Strings.
 */
public class PersistentCompositeUniqueId implements CompositeUserType {

  /**
   * Singleton instance.
   */
  public static final PersistentCompositeUniqueId INSTANCE = new PersistentCompositeUniqueId();

  @Override
  public String[] getPropertyNames() {
    return new String[] { "scheme", "value", "version" };
  }

  @Override
  public Type[] getPropertyTypes() {
    return new Type[] { StandardBasicTypes.STRING, StandardBasicTypes.STRING, StandardBasicTypes.STRING };
  }

  @Override
  public Object getPropertyValue(final Object component, final int property) throws HibernateException {
    final UniqueId uid = (UniqueId) component;
    if (property == 0) {
      return uid.getScheme();
    } else if (property == 2) {
      return uid.getValue();
    } else {
      return uid.getVersion();
    }
  }

  @Override
  public void setPropertyValue(final Object component, final int property, final Object value) throws HibernateException {
    throw new UnsupportedOperationException("UniqueId is immutable");
  }

  @Override
  public Class<?> returnedClass() {
    return UniqueId.class;
  }

  @Override
  public boolean equals(final Object x, final Object y) throws HibernateException {
    return x.equals(y);
  }

  @Override
  public int hashCode(final Object x) throws HibernateException {
    return x.hashCode();
  }

  @Override
  public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SharedSessionContractImplementor session, final Object owner)
      throws HibernateException, SQLException {
    final String scheme = resultSet.getString(names[0]);
    if (resultSet.wasNull()) {
      return null;
    }
    final String value = resultSet.getString(names[1]);
    final String version = resultSet.getString(names[2]);
    return UniqueId.of(scheme, value, version);
  }

  @Override
  public void nullSafeSet(final PreparedStatement statement, final Object value, final int index,
      final SharedSessionContractImplementor session) throws HibernateException, SQLException {
    if (value == null) {
      statement.setNull(index, StandardBasicTypes.STRING.sqlType());
      statement.setNull(index + 1, StandardBasicTypes.STRING.sqlType());
      statement.setNull(index + 2, StandardBasicTypes.STRING.sqlType());
    } else {
      final UniqueId uid = (UniqueId) value;
      statement.setString(index, uid.getScheme());
      statement.setString(index + 1, uid.getValue());
      if (uid.getVersion() != null) {
        statement.setString(index + 2, uid.getVersion());
      } else {
        statement.setNull(index + 2, StandardBasicTypes.STRING.sqlType());
      }
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
  public Serializable disassemble(final Object value, final SharedSessionContractImplementor session) throws HibernateException {
    return (Serializable) value;
  }

  @Override
  public Object assemble(final Serializable cached, final SharedSessionContractImplementor session, final Object owner) {
    return cached;
  }

  @Override
  public Object replace(final Object original, final Object target, final SharedSessionContractImplementor session,
      final Object owner) throws HibernateException {
    return original;
  }

}
