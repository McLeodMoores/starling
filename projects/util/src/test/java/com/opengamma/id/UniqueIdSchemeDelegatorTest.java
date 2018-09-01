/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link UniqueIdSchemeDelegator}.
 */
@Test(groups = TestGroup.UNIT)
public class UniqueIdSchemeDelegatorTest {

  /**
   * Constructs a delegator with only the default set.
   */
  @Test
  public void testConstructorDefaultOnly() {
    final UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<>("default");
    assertEquals(test.chooseDelegate("A"), "default");
    assertEquals(test.chooseDelegate("B"), "default");
    assertEquals(test.getDefaultDelegate(), "default");
    assertEquals(test.getDelegates(), Collections.emptyMap());
  }

  /**
   * Tests that the default cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorDefaultOnlyNullDefault() {
    new UniqueIdSchemeDelegator<String>(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a delegator from a map.
   */
  @Test
  public void testConstructorDefaultAndMap() {
    final Map<String, String> map = ImmutableMap.of("A", "adapt", "B", "bootup", "C", "curve");
    final UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<>("default", map);
    assertEquals(test.chooseDelegate("A"), "adapt");
    assertEquals(test.chooseDelegate("B"), "bootup");
    assertEquals(test.chooseDelegate("C"), "curve");
    assertEquals(test.chooseDelegate("D"), "default");
    assertEquals(test.getDefaultDelegate(), "default");
    assertEquals(test.getDelegates(), map);
    assertEquals(test.getAllDelegates(), Arrays.asList("default", "adapt", "bootup", "curve"));
  }

  /**
   * Tests that the register of delegates is immutable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testImmutableDelegateRegister() {
    final Map<String, String> map = ImmutableMap.of("A", "adapt", "B", "bootup", "C", "curve");
    final UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<>("default", map);
    test.getDelegates().clear();
  }

  /**
   * Tests that the default cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorDefaultAndMapNullDefault() {
    final Map<String, String> map = ImmutableMap.of("A", "adapt", "B", "bootup", "C", "curve");
    new UniqueIdSchemeDelegator<>(null, map);
  }

  /**
   * Tests that the map of delegates cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorDefaultAndMapNullMap() {
    new UniqueIdSchemeDelegator<>("default", null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the registration and removal of delegates.
   */
  @Test
  public void testRegisterDelegateRemoveDelegate() {
    final Map<String, String> map = ImmutableMap.of("A", "adapt", "B", "bootup");
    final UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<>("default", map);
    assertEquals(test.chooseDelegate("A"), "adapt");
    assertEquals(test.chooseDelegate("B"), "bootup");
    assertEquals(test.chooseDelegate("C"), "default");
    assertEquals(test.chooseDelegate("D"), "default");
    assertTrue(test.registerDelegate("C", "curve"));
    assertFalse(test.registerDelegate("C", "curve"));
    assertEquals(test.chooseDelegate("A"), "adapt");
    assertEquals(test.chooseDelegate("B"), "bootup");
    assertEquals(test.chooseDelegate("C"), "curve");
    assertEquals(test.chooseDelegate("D"), "default");
    test.removeDelegate("C");
    assertEquals(test.chooseDelegate("A"), "adapt");
    assertEquals(test.chooseDelegate("B"), "bootup");
    assertEquals(test.chooseDelegate("C"), "default");
    assertEquals(test.chooseDelegate("D"), "default");
  }

  /**
   * Tests that the scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRegisterDelegateNullScheme() {
    final UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<>("default");
    test.registerDelegate(null, "default");
  }

  /**
   * Tests that the delegate cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRegisterDelegateNullDelegate() {
    final UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<>("default");
    test.registerDelegate("default", null);
  }

  /**
   * Tests that the null cannot be removed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRemoveDelegateNull() {
    final UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<>("default");
    test.removeDelegate(null);
  }

}
