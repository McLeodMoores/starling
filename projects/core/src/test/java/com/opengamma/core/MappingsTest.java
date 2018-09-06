/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link Mappings}.
 */
@Test(groups = TestGroup.UNIT )
public class MappingsTest {
  private static final Map<String, String> MAPPINGS = new HashMap<>();

  static {
    MAPPINGS.put("A", "N");
    MAPPINGS.put("B", "O");
    MAPPINGS.put("C", "P");
  }

  /**
   * Tests that the mappings cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap() {
    new Mappings(null);
  }

  /**
   * Tests that the underlying map is immutable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testImmutable() {
    final Mappings mappings = new Mappings(MAPPINGS);
    mappings.getMappings().put("D", "Q");
  }

  /**
   * Tests that the underlying map is copied.
   */
  @Test
  public void testCopy() {
    final Map<String, String> copy = new HashMap<>(MAPPINGS);
    final Mappings mappings = new Mappings(MAPPINGS);
    copy.put("D", "Q");
    assertEquals(mappings.getMappings(), MAPPINGS);
  }

  /**
   * Tests the hashCode() and equals() methods.
   */
  @Test
  public void testHashCodeEquals() {
    final Mappings m1 = new Mappings(MAPPINGS);
    Mappings m2 = new Mappings(MAPPINGS);
    assertEquals(m1, m1);
    assertEquals(m1, m2);
    assertEquals(m1.hashCode(), m2.hashCode());
    m2 = new Mappings(Collections.singletonMap("A", "N"));
    assertNotEquals(null, m1);
    assertNotEquals(MAPPINGS, m1);
    assertNotEquals(m1, m2);
  }
}
