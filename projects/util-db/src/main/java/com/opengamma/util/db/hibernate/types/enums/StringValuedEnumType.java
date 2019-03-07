/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.hibernate.types.enums;

import static com.opengamma.util.db.hibernate.types.enums.StringValuedEnumReflect.getNameFromValue;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;

//Please notice the calls to getNameFromValue *************************
/**
 * An enum type for Hibernate.
 *
 * @param <T> the enum type
 */
public class StringValuedEnumType<T extends Enum<T> & StringValuedEnum>  // CSIGNORE
implements EnhancedUserType, ParameterizedType {

  /**
   * Enum class for this particular user type.
   */
  private Class<T> _enumClass;

  /**
   * Value to use if null.
   */
  private String _defaultValue;

  /** Creates a new instance of ActiveStateEnumType */
  public StringValuedEnumType() {
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setParameterValues(final Properties parameters) {
    final String enumClassName = parameters.getProperty("enum");
    try {
      _enumClass = (Class<T>) Class.forName(enumClassName).asSubclass(Enum.class).
          asSubclass(StringValuedEnum.class); //Validates the class but does not eliminate the cast
    } catch (final ClassNotFoundException cnfe) {
      throw new HibernateException("Enum class not found", cnfe);
    }
    setDefaultValue(parameters.getProperty("defaultValue"));
  }

  public String getDefaultValue() {
    return _defaultValue;
  }

  public void setDefaultValue(final String defaultValue) {
    this._defaultValue = defaultValue;
  }

  /**
   * The class returned by <tt>nullSafeGet()</tt>.
   * @return Class
   */
  @Override
  public Class<?> returnedClass() {
    return _enumClass;
  }

  @Override
  public int[] sqlTypes() {
    return new int[] {Types.VARCHAR };
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  /**
   * Retrieve an instance of the mapped class from a JDBC resultset. Implementors should handle possibility of null values.
   *
   * @param rs
   *          a JDBC result set
   * @param names
   *          the column names
   * @param owner
   *          the containing entity
   * @return Object
   * @throws HibernateException
   *           if there is a problem
   * @throws SQLException
   *           if there is a problem
   */
  @Override
  public Object nullSafeGet(final ResultSet rs, final String[] names, final SharedSessionContractImplementor session,
      final Object owner) throws HibernateException, SQLException {
    String value = rs.getString(names[0]);
    if (value == null) {
      value = getDefaultValue();
      if (value == null) { //no default value
        return null;
      }
    }
    final String name = getNameFromValue(_enumClass, value);
    final Object res = rs.wasNull() ? null : Enum.valueOf(_enumClass, name);

    return res;
  }

  /**
   * Write an instance of the mapped class to a prepared statement. Implementors should handle possibility of null values. A multi-column type should be written
   * to parameters starting from <tt>index</tt>.
   *
   * @param st
   *          a JDBC prepared statement
   * @param value
   *          the object to write
   * @param index
   *          statement parameter index
   * @throws HibernateException
   *           if there is a problem
   * @throws SQLException
   *           if there is a problem
   */
  @Override
  @SuppressWarnings("unchecked")
  public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
      final SharedSessionContractImplementor session) throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, Types.VARCHAR);
    } else {
      st.setString(index, ((T) value).getValue());
    }
  }

  @Override
  public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
    return cached;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Serializable disassemble(final Object value) throws HibernateException {
    return (Enum) value;
  }

  @Override
  public Object deepCopy(final Object value) throws HibernateException {
    return value;
  }

  @Override
  public boolean equals(final Object x, final Object y) throws HibernateException {
    return x == y;
  }

  @Override
  public int hashCode(final Object x) throws HibernateException {
    return x.hashCode();
  }

  @Override
  public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
    return original;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String objectToSQLString(final Object value) {
    return '\'' + ((T) value).getValue() + '\'';
  }

  @Override
  @SuppressWarnings("unchecked")
  public String toXMLString(final Object value) {
    return ((T) value).getValue();
  }

  @Override
  public Object fromXMLString(final String xmlValue) {
    final String name = getNameFromValue(_enumClass, xmlValue);
    return Enum.valueOf(_enumClass, name);
  }

}
