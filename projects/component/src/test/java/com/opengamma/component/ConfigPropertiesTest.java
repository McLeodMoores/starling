/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.test.TestGroup;

/**
 * Test for {@link ConfigProperties} and {@link ConfigProperty}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigPropertiesTest {

  /**
   * Tests a basic configuration with no properties.
   */
  public void testBasicsEmpty() {
    final ConfigProperties test = new ConfigProperties();
    assertEquals(test.size(), 0);
    assertNull(test.get("x"));
    assertNull(test.getValue("x"));
    assertFalse(test.containsKey("x"));
    assertFalse(test.containsKey(null));
    assertEquals(test.keySet().size(), 0);
    assertEquals(test.toMap().size(), 0);
    assertEquals(test.loggableMap().size(), 0);
    assertNull(test.loggableValue("x"));
  }

  /**
   * Tests a basic configuration.
   */
  public void testBasics() {
    final ConfigProperties test = new ConfigProperties();
    test.add(ConfigProperty.of("a", "AA", false));
    assertEquals(test.size(), 1);
    assertEquals(test.get("a"), ConfigProperty.of("a", "AA", false));
    assertEquals(test.getValue("a"), "AA");
    assertTrue(test.containsKey("a"));
    assertFalse(test.containsKey("AA"));
    assertEquals(test.toMap().size(), 1);
    assertEquals("AA", test.toMap().get("a"), "AA");
    assertEquals(test.loggableMap().size(), 1);
    assertEquals(test.loggableMap().get("a"), "AA");
    assertEquals(test.loggableValue("a"), "AA");
    assertTrue(test.toString().contains("AA"));

    test.add(ConfigProperty.of("b", "BB", false));
    assertEquals(test.size(), 2);
    assertEquals(test.get("a"), ConfigProperty.of("a", "AA", false));
    assertEquals(test.getValue("a"), "AA");
    assertTrue(test.containsKey("a"));
    assertEquals(test.get("b"), ConfigProperty.of("b", "BB", false));
    assertEquals(test.getValue("b"), "BB");
    assertTrue(test.containsKey("b"));
    assertEquals(2, test.toMap().size());
    assertEquals(test.toMap().get("a"), "AA");
    assertEquals(test.toMap().get("b"), "BB");

    test.add(ConfigProperty.of("b", "CC", false));
    assertEquals(test.size(), 2);
    assertEquals(test.getValue("a"), "AA");
    assertEquals(test.getValue("b"), "CC");

    test.addIfAbsent(ConfigProperty.of("b", "DD", false));
    assertEquals(test.size(), 2);
    assertEquals(test.getValue("a"), "AA");
    assertEquals(test.getValue("b"), "CC");

    test.addIfAbsent(ConfigProperty.of("e", "EE", false));
    assertEquals(test.size(), 3);
    assertEquals(test.getValue("a"), "AA");
    assertEquals(test.getValue("b"), "CC");
    assertEquals(test.getValue("e"), "EE");
  }

  /**
   * Tests adding a property using put() and putAll().
   */
  public void testBasicsPut() {
    final ConfigProperties test = new ConfigProperties();
    test.put("a", "AA");
    assertEquals(test.size(), 1);
    assertEquals(test.get("a"), ConfigProperty.of("a", "AA", false));

    test.put("a", "BB");
    assertEquals(test.size(), 1);
    assertEquals(test.get("a"), ConfigProperty.of("a", "BB", false));

    final Map<String, String> map = ImmutableMap.of("a", "CC", "e", "EE");
    test.putAll(map);
    assertEquals(test.size(), 2);
    assertEquals(test.get("a"), ConfigProperty.of("a", "CC", false));
    assertEquals(test.get("e"), ConfigProperty.of("e", "EE", false));
  }

  /**
   * Tests hidden properties.
   */
  public void testBasicsHidden() {
    final ConfigProperties test = new ConfigProperties();
    final ConfigProperty resolved = test.resolveProperty("password", "abc", 0);
    assertEquals(resolved, ConfigProperty.of("password", "abc", true));
    test.add(resolved);
    assertEquals(test.size(), 1);
    assertEquals(test.get("password"), ConfigProperty.of("password", "abc", true));
    assertEquals(test.getValue("password"), "abc");
    assertEquals(test.toMap().size(), 1);
    assertEquals(test.toMap().get("password"), "abc");
    assertEquals(test.loggableMap().size(), 1);
    assertEquals(test.loggableMap().get("password"), ConfigProperties.HIDDEN);
    assertEquals(test.loggableValue("password"), ConfigProperties.HIDDEN);
    assertFalse(test.toString().contains("abc"));
  }

  /**
   * Tests the resolution of properties that contain hidden values.
   */
  public void testBasicsHiddenResolve() {
    final ConfigProperties test = new ConfigProperties();
    test.add(ConfigProperty.of("password", "abc", true));
    final ConfigProperty resolved = test.resolveProperty("other", "pw:${password}", 0);
    assertEquals(resolved, ConfigProperty.of("other", "pw:abc", true));
    test.add(resolved);
    assertEquals(test.size(), 2);
    assertEquals(test.get("password"), ConfigProperty.of("password", "abc", true));
    assertEquals(test.get("other"), ConfigProperty.of("other", "pw:abc", true));
    assertEquals(test.getValue("password"), "abc");
    assertEquals(test.getValue("other"), "pw:abc");
    assertEquals(test.toMap().size(), 2);
    assertEquals(test.toMap().get("password"), "abc");
    assertEquals(test.toMap().get("other"), "pw:abc");
    assertEquals(test.loggableMap().size(), 2);
    assertEquals(test.loggableMap().get("password"), ConfigProperties.HIDDEN);
    assertEquals(test.loggableMap().get("other"), ConfigProperties.HIDDEN);
    assertEquals(test.loggableValue("password"), ConfigProperties.HIDDEN);
    assertEquals(test.loggableValue("other"), ConfigProperties.HIDDEN);
    assertFalse(test.toString().contains("abc"));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the addition of a property.
   */
  public void testPropertyWithKey() {
    final ConfigProperty base = ConfigProperty.of("a", "AA", false);
    final ConfigProperty test = base.withKey("b");
    assertNotSame(base, test);
    assertEquals(test, ConfigProperty.of("b", "AA", false));
  }

  /**
   * Tests property equality.
   */
  public void testPropertyEquals() {
    final ConfigProperty a1 = ConfigProperty.of("a", "AA", false);
    final ConfigProperty a2 = ConfigProperty.of("a", "AA", false);
    final ConfigProperty b = ConfigProperty.of("b", "AA", false);
    final ConfigProperty c = ConfigProperty.of("a", "BB", false);
    final ConfigProperty d1 = ConfigProperty.of("a", "AA", true);
    final ConfigProperty d2 = ConfigProperty.of("a", "AA", true);
    assertTrue(a1.equals(a1));
    assertTrue(a1.equals(a2));
    assertFalse(a1.equals(b));
    assertFalse(a1.equals(c));
    assertFalse(a1.equals(d1));
    assertFalse(a1.equals(null));
    assertNotEquals("", a1);

    assertTrue(a1.hashCode() == a2.hashCode());
    assertTrue(d1.hashCode() == d2.hashCode());
  }

  /**
   * Tests the toString() method.
   */
  public void testPropertyToString() {
    assertEquals("a=AA", ConfigProperty.of("a", "AA", false).toString());
    assertEquals("a=" + ConfigProperties.HIDDEN, ConfigProperty.of("a", "AA", true).toString());
  }

}
