/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link ParameterizedType}.
 */
public final class ParameterizedTypeImpl implements ParameterizedType {

  // TODO: Use something public from another library - the Guava stuff is all private however

  private final Type[] _actualTypeArguments;
  private final Type _rawType;
  private final Type _ownerType;

  private ParameterizedTypeImpl(final Type[] actualTypeArguments, final Type rawType, final Type ownerType) {
    _actualTypeArguments = actualTypeArguments;
    _rawType = rawType;
    _ownerType = ownerType;
  }

  /**
   * Constructs a parameterized type from a raw type and multiple type arguments.
   *
   * @param rawType  the raw type, not null
   * @param actualTypeArguments  the type arguments, not null
   * @return  the parameterized type
   */
  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type... actualTypeArguments) {
    ArgumentChecker.notNull(rawType, "rawType");
    ArgumentChecker.noNulls(actualTypeArguments, "actualTypeArguments");
    return new ParameterizedTypeImpl(actualTypeArguments.clone(), rawType, rawType.getEnclosingClass());
  }

  /**
   * Constructs a parameterized type from a raw type and single type argument.
   *
   * @param rawType  the raw type, not null
   * @param typeArg1  the type argument, not null
   * @return  the parameterized type
   */
  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type typeArg1) {
    ArgumentChecker.notNull(rawType, "rawType");
    ArgumentChecker.notNull(typeArg1, "typeArg1");
    return new ParameterizedTypeImpl(new Type[] {typeArg1 }, rawType, rawType.getEnclosingClass());
  }

  /**
   * Constructs a parameterized type from a raw type and two type arguments.
   *
   * @param rawType  the raw type, not null
   * @param typeArg1  the type argument, not null
   * @param typeArg2  the type argument, not null
   * @return  the parameterized type
   */
  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type typeArg1, final Type typeArg2) {
    ArgumentChecker.notNull(rawType, "rawType");
    ArgumentChecker.notNull(typeArg1, "typeArg1");
    ArgumentChecker.notNull(typeArg2, "typeArg2");
    return new ParameterizedTypeImpl(new Type[] {typeArg1, typeArg2 }, rawType, rawType.getEnclosingClass());
  }

  /**
   * Constructs a parameterized type from a raw type and three type arguments.
   *
   * @param rawType  the raw type, not null
   * @param typeArg1  the type argument, not null
   * @param typeArg2  the type argument, not null
   * @param typeArg3  the type argument, not null
   * @return  the parameterized type
   */
  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type typeArg1, final Type typeArg2, final Type typeArg3) {
    ArgumentChecker.notNull(rawType, "rawType");
    ArgumentChecker.notNull(typeArg1, "typeArg1");
    ArgumentChecker.notNull(typeArg2, "typeArg2");
    ArgumentChecker.notNull(typeArg3, "typeArg3");
    return new ParameterizedTypeImpl(new Type[] {typeArg1, typeArg2, typeArg3 }, rawType, rawType.getEnclosingClass());
  }

  /**
   * Constructs a parameterized type from a raw type and four type arguments.
   *
   * @param rawType  the raw type, not null
   * @param typeArg1  the type argument, not null
   * @param typeArg2  the type argument, not null
   * @param typeArg3  the type argument, not null
   * @param typeArg4  the type argument, not null
   * @return  the parameterized type
   */
  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type typeArg1, final Type typeArg2, final Type typeArg3, final Type typeArg4) {
    ArgumentChecker.notNull(rawType, "rawType");
    ArgumentChecker.notNull(typeArg1, "typeArg1");
    ArgumentChecker.notNull(typeArg2, "typeArg2");
    ArgumentChecker.notNull(typeArg3, "typeArg3");
    ArgumentChecker.notNull(typeArg4, "typeArg4");
    return new ParameterizedTypeImpl(new Type[] {typeArg1, typeArg2, typeArg3, typeArg4 }, rawType, rawType.getEnclosingClass());
  }

  @Override
  public Type[] getActualTypeArguments() {
    return _actualTypeArguments.clone();
  }

  @Override
  public Type getRawType() {
    return _rawType;
  }

  @Override
  public Type getOwnerType() {
    return _ownerType;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ParameterizedType)) {
      return false;
    }
    final ParameterizedType other = (ParameterizedType) o;
    return ObjectUtils.equals(_rawType, other.getRawType()) && ObjectUtils.equals(_ownerType, other.getOwnerType())
        && Arrays.equals(_actualTypeArguments, other.getActualTypeArguments());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_actualTypeArguments) ^ ObjectUtils.hashCode(_ownerType) ^ ObjectUtils.hashCode(_rawType);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
