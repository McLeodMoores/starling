/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ClassMap}.
 */
@Test(groups = TestGroup.UNIT)
public class ClassMapTest {
  private static final Class1 EX_1 = new Class1();
  private static final Interface1 EX_2 = new Class2();
  private static final Interface1 EX_3 = new Class3();

  /**
   * Tests a simple hierarchy.
   */
  @Test
  public void testSimpleHierarchy() {
    final ClassMap<Class1> map = new ClassMap<>();
    map.put(Class1.class, EX_1);
    assertEquals(map.size(), 1);
    assertTrue(map.containsKey(Class1.class));
    assertTrue(map.containsValue(EX_1));
    assertEquals(map.get(Class1.class), EX_1);
    assertFalse(map.containsKey(Class2.class));
    map.clear();
    assertTrue(map.isEmpty());
  }

  /**
   * Tests a hierarchy using the actual type.
   */
  @Test
  public void testHierarchyActualType() {
    final ClassMap<Interface1> map = new ClassMap<>();
    map.put(Class2.class, EX_2);
    map.put(Class3.class, EX_3);
    assertEquals(map.size(), 2);
    assertFalse(map.containsKey(Interface1.class));
    assertFalse(map.containsKey(Interface2.class));
    assertTrue(map.containsKey(Class2.class));
    assertTrue(map.containsKey(Class3.class));
    assertTrue(map.containsValue(EX_2));
    assertTrue(map.containsValue(EX_3));
    assertFalse(map.containsKey(Class1.class));
    map.clear();
    assertTrue(map.isEmpty());
    final ClassMap<Interface1> other = new ClassMap<>();
    other.putAll(map);
    assertEquals(map, other); // same elements
    assertFalse(map.equals(other));  // hashCode and equals not overridden
  }

  /**
   * Tests a hierarchy using the super type.
   */
  @Test
  public void testHierarchySuperType() {
    final ClassMap<Interface1> map = new ClassMap<>();
    map.put(Interface1.class, EX_2);
    map.put(Interface2.class, EX_3);
    assertEquals(map.size(), 2);
    assertTrue(map.containsKey(Interface1.class));
    assertTrue(map.containsKey(Interface2.class));
    assertTrue(map.containsKey(Class2.class));
    assertTrue(map.containsKey(Class3.class));
    assertTrue(map.containsValue(EX_2));
    assertTrue(map.containsValue(EX_3));
    assertFalse(map.containsKey(Class1.class));
    map.remove(Interface1.class);
    map.remove(Interface2.class);
    assertTrue(map.isEmpty());
    final ClassMap<Interface1> other = new ClassMap<>();
    other.putAll(map);
    assertEquals(map, other); // same elements
    assertFalse(map.equals(other));  // hashCode and equals not overridden
  }

  /**
   * Tests that the keyset is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableKeySet() {
    final ClassMap<Interface1> map = new ClassMap<>();
    map.put(Interface1.class, EX_2);
    map.put(Interface2.class, EX_3);
    map.keySet().add(Class1.class);
  }

  /**
   * Tests that the values are unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableValues() {
    final ClassMap<Interface1> map = new ClassMap<>();
    map.put(Interface1.class, EX_2);
    map.put(Interface2.class, EX_3);
    map.values().add(EX_2);
  }

  /** Test interface. */
  public interface Interface1 {
  }

  /** Test interface. */
  public interface Interface2 {
  }

  /** Test class. */
  public static class Class1 {
  }

  /** Test class. */
  public static class Class2 implements Interface1 {
  }

  /** Test class. */
  public static class Class3 extends Class2 implements Interface2 {
  }
}
