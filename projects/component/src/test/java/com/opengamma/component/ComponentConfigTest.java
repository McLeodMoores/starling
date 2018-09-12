/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.component;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.test.Assert;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ComponentConfig}.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentConfigTest {
  private static final String GROUP_KEY_1 = "Group 1";
  private static final String GROUP_KEY_2 = "Group 2";
  private static final String GROUP_KEY_3 = "Group 3";
  private static final ComponentConfig CONFIG = new ComponentConfig();
  static {
    CONFIG.addGroup(GROUP_KEY_1);
    CONFIG.addGroup(GROUP_KEY_2);
    CONFIG.addGroup(GROUP_KEY_3);
    final ConfigProperties configProperties1 = CONFIG.getGroup(GROUP_KEY_1);
    configProperties1.add(ConfigProperty.of("inner 1", "inner value 1", true));
    final ConfigProperties configProperties2 = CONFIG.getGroup(GROUP_KEY_2);
    configProperties2.add(ConfigProperty.of("inner 2", "inner value 2", false));
    configProperties2.add(ConfigProperty.of("inner 3", "inner value 3", true));
  }

  /**
   * Tests the getGroups() method.
   */
  @Test
  public void testGetGroups() {
    Assert.assertEqualsNoOrder(CONFIG.getGroups(), Arrays.asList(GROUP_KEY_1, GROUP_KEY_2, GROUP_KEY_3));
  }

  /**
   * Tests the attempted retrieval of a non-existent group.
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testNonExistentGroup() {
    CONFIG.getGroup("Group 4");
  }

  /**
   * Tests the retrieval of a group.
   */
  @Test
  public void testGetGroup() {
    final ConfigProperties properties1 = new ConfigProperties();
    properties1.add(ConfigProperty.of("inner 1", "inner value 1", true));
    final ConfigProperties properties2 = new ConfigProperties();
    properties2.add(ConfigProperty.of("inner 2", "inner value 2", false));
    properties2.add(ConfigProperty.of("inner 3", "inner value 3", true));
    assertEquals(CONFIG.getGroup(GROUP_KEY_1).get("inner 1"), properties1.get("inner 1"));
    assertEquals(CONFIG.getGroup(GROUP_KEY_2).get("inner 2"), properties2.get("inner 2"));
    assertEquals(CONFIG.getGroup(GROUP_KEY_2).get("inner 3"), properties2.get("inner 3"));
    assertEquals(CONFIG.getGroup(GROUP_KEY_3).size(), 0);
  }

  /**
   * Tests that an existing group cannot be added.
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testAddExistingGroup() {
    CONFIG.addGroup(GROUP_KEY_1);
  }

  /**
   * Tests the contains() method.
   */
  @Test
  public void testContains() {
    assertTrue(CONFIG.contains(GROUP_KEY_1, "inner 1"));
    assertFalse(CONFIG.contains(GROUP_KEY_1, "inner 2"));
    assertTrue(CONFIG.contains(GROUP_KEY_2, "inner 2"));
    assertFalse(CONFIG.contains("Group 4", "inner 3"));
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    assertEquals(CONFIG.toString(), "Config[Group 1, Group 2, Group 3]");
  }
}
