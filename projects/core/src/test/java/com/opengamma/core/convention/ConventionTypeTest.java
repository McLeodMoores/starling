/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConventionType}.
 */
@Test(groups = TestGroup.UNIT)
public class ConventionTypeTest {
  private static final String NAME = "NAME";

  /**
   * Tests the object.
   */
  @Test
  public void test() {
    final ConventionType type = ConventionType.of(NAME);
    assertEquals(type, type);
    assertNotEquals(null, type);
    assertNotEquals(NAME, type);
    assertEquals(type.getName(), NAME);
    assertEquals(type.toString(), NAME);
    ConventionType other = ConventionType.of(NAME);
    assertEquals(type, other);
    assertEquals(type.hashCode(), other.hashCode());
    other = ConventionType.of(NAME.substring(1));
    assertNotEquals(type, other);
  }

  /**
   * Tests the compareTo method.
   */
  @Test
  public void testCompareTo() {
    final ConventionType type = ConventionType.of(NAME);
    assertEquals(type.compareTo(ConventionType.of(NAME)), 0);
    assertNotEquals(type.compareTo(ConventionType.of("a" + NAME)), 0);
    assertNotEquals(type.compareTo(ConventionType.of("z" + NAME)), 0);
  }
}
