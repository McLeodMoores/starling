/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.types;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.reflect.TypeToken;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Tests the {@link ParameterizedTypeImpl} class.
 */
@Test(groups = TestGroup.UNIT)
public class ParameterizedTypeImplTest {

  /**
   * A class with five type arguments.
   *
   * @param <A>  the first type
   * @param <B>  the second type
   * @param <C>  the third type
   * @param <D>  the fourth type
   * @param <E>  the fifth type
   */
  private static class FiveArg<A, B, C, D, E> {
  }

  /**
   * A class with four type arguments.
   *
   * @param <A>  the first type
   * @param <B>  the second type
   * @param <C>  the third type
   * @param <D>  the fourth type
   */
  private static class FourArg<A, B, C, D> {
  }

  /**
   * Tests construction with varargs.
   */
  public void testVararg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(FiveArg.class, Byte.class, Short.class, Integer.class, Long.class, Double.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) new TypeToken<FiveArg<Byte, Short, Integer, Long, Double>>() {
    }.getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

  /**
   * Tests construction with one type argument.
   */
  public void test1Arg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(Set.class, Integer.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) new TypeToken<Set<Integer>>() {
    }.getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

  /**
   * Tests construction with two type arguments.
   */
  public void test2Arg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(Pair.class, Integer.class, String.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) new TypeToken<Pair<Integer, String>>() {
    }.getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

  /**
   * Tests construction with three type arguments.
   */
  public void test3Arg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(Triple.class, Integer.class, Double.class, String.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) new TypeToken<Triple<Integer, Double, String>>() {
    }.getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

  /**
   * Tests construction with four type arguments.
   */
  public void test4Arg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(FourArg.class, Byte.class, Short.class, Integer.class, Long.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) new TypeToken<FourArg<Byte, Short, Integer, Long>>() {
    }.getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

  /**
   * Tests equality.
   */
  public void testObject() {
    final ParameterizedType type = ParameterizedTypeImpl.of(FourArg.class, Byte.class, Short.class, Integer.class, Long.class);
    assertEquals(type, type);
    assertNotEquals(null, type);
    assertNotEquals(Byte.class, type);
    assertEquals(type.getRawType(), FourArg.class);
    assertEquals(type.getOwnerType(), ParameterizedTypeImplTest.class);
    assertEquals(type.getActualTypeArguments(), new Type[] {Byte.class, Short.class, Integer.class, Long.class});
    ParameterizedType other = ParameterizedTypeImpl.of(FourArg.class, Byte.class, Short.class, Integer.class, Long.class);
    assertEquals(type, other);
    assertEquals(type.hashCode(), other.hashCode());
    other = ParameterizedTypeImpl.of(FiveArg.class, Byte.class, Short.class, Integer.class, Long.class);
    assertNotEquals(type, other);
    other = ParameterizedTypeImpl.of(FourArg.class, Short.class, Short.class, Integer.class, Long.class);
    assertNotEquals(type, other);
  }

  /**
   * Tests the toString method.
   */
  public void testToString() {
    final ParameterizedType type = ParameterizedTypeImpl.of(FourArg.class, Byte.class, Short.class, Integer.class, Long.class);
    assertEquals(type.toString(), "ParameterizedTypeImpl["
        + "_actualTypeArguments={class java.lang.Byte,class java.lang.Short,class java.lang.Integer,class java.lang.Long},"
        + "_rawType=class com.opengamma.util.types.ParameterizedTypeImplTest$FourArg,"
        + "_ownerType=class com.opengamma.util.types.ParameterizedTypeImplTest]");
  }
}
