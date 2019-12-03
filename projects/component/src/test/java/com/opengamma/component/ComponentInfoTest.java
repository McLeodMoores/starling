/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.component;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ComponentInfo}.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentInfoTest {
  private static final ComponentInfo INFO = new ComponentInfo(FudgeContext.class, "FudgeContext");

  /**
   * Tests the matches() method.
   */
  @Test
  public void testMatches() {
    assertTrue(INFO.matches(FudgeContext.class, "FudgeContext"));
    assertFalse(INFO.matches(OpenGammaFudgeContext.class, "FudgeContext"));
    assertFalse(INFO.matches(FudgeContext.class, "fudge-context"));
  }

  /**
   * Tests the toComponentKey() method.
   */
  @Test
  public void testToComponentKey() {
    assertEquals(INFO.toComponentKey(), ComponentKey.of(FudgeContext.class, "FudgeContext"));
  }

  /**
   * Tests that a null key cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAttributeKey() {
    INFO.addAttribute(null, "value");
  }

  /**
   * Tests that a null value cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAttributeValue() {
    INFO.addAttribute("key", null);
  }

  /**
   * Tests the addition and retrieval of attributes.
   */
  @Test
  public void testAddAttributes() {
    final ComponentInfo info = INFO.clone();
    assertTrue(info.getAttributes().isEmpty());
    info.addAttribute("key1", "value1");
    info.addAttribute("key2", "value2");
    assertEquals(info.getAttributes().size(), 2);
    assertEquals(info.getAttribute("key1"), "value1");
    assertEquals(info.getAttribute("key2"), "value2");
  }

  /**
   * Tests that the attribute must be present.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAttributePresent() {
    INFO.getAttribute("key");
  }

}
