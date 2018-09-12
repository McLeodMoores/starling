/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.component;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ComponentKey}.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentKeyTest {
  private static final ComponentKey KEY = ComponentKey.of(FudgeContext.class, "FudgeContext");

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final ComponentKey other = ComponentKey.of(FudgeContext.class, "FudgeContext");
    assertEquals(KEY.hashCode(), other.hashCode());
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    assertEquals(KEY, KEY);
    assertNotEquals(null, KEY);
    assertNotEquals(new ComponentInfo(FudgeContext.class, "FudgeContext"), KEY);
    ComponentKey other;
    other = ComponentKey.of(FudgeContext.class, "FudgeContext");
    assertEquals(KEY, other);
    other = ComponentKey.of(OpenGammaFudgeContext.class, "FudgeContext");
    assertNotEquals(KEY, other);
    other = ComponentKey.of(FudgeContext.class, "Fudge-Context");
    assertNotEquals(KEY, other);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetter() {
    assertEquals(KEY.getClassifier(), "FudgeContext");
    assertEquals(KEY.getType(), FudgeContext.class);
  }
}
