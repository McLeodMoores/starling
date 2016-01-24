/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ViewKey}.
 */
@Test(groups = TestGroup.UNIT)
public class ViewKeyTest {

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    ViewKey.of((String) null);
  }

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    ViewKey.of(null, UniqueId.of("A", "B"));
  }

  /**
   * Tests the behaviour when the view definition is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullViewDefinition() {
    ViewKey.of((ViewDefinition) null);
  }

  /**
   * Tests construction of the key.
   */
  @Test
  public void testConstruction() {
    final String name = "View";
    final UniqueId uid = UniqueId.of("A", "B");
    final ViewDefinition viewDefinition = new ViewDefinition(name, "Test");
    final ViewKey keyFromName = ViewKey.of(name);
    assertEquals(keyFromName.getName(), name);
    assertNull(keyFromName.getUniqueId());
    assertFalse(keyFromName.hasUniqueId());
    final ViewKey keyFromViewNoUid = ViewKey.of(viewDefinition);
    assertEquals(keyFromViewNoUid.getName(), name);
    assertNull(keyFromViewNoUid.getUniqueId());
    assertFalse(keyFromViewNoUid.hasUniqueId());
    final ViewKey keyFromNameAndUid = ViewKey.of(name, uid);
    assertEquals(keyFromNameAndUid.getName(), name);
    assertEquals(keyFromNameAndUid.getUniqueId(), uid);
    assertTrue(keyFromNameAndUid.hasUniqueId());
    viewDefinition.setUniqueId(uid);
    final ViewKey keyFromViewWithUid = ViewKey.of(viewDefinition);
    assertEquals(keyFromViewWithUid.getName(), name);
    assertEquals(keyFromViewWithUid.getUniqueId(), uid);
    assertTrue(keyFromViewWithUid.hasUniqueId());
  }

  /**
   * Tests the hash code.
   */
  @Test
  public void testHashCode() {
    final String name = "name";
    final UniqueId uid = UniqueId.of("A", "B");
    final ViewKey key = ViewKey.of(name, uid);
    assertEquals(key.hashCode(), ViewKey.of(name, uid).hashCode());
    // uid is ignored in hash code
    assertEquals(key.hashCode(), ViewKey.of(name).hashCode());
    final ViewDefinition viewDefinition = new ViewDefinition(name, "Test");
    viewDefinition.setUniqueId(uid);
    assertEquals(key.hashCode(), ViewKey.of(viewDefinition).hashCode());
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    final String name = "name";
    final UniqueId uid = UniqueId.of("A", "B");
    assertEquals(ViewKey.of(name).toString(), "ViewKey[name]");
    assertEquals(ViewKey.of(name, uid).toString(), "ViewKey[name(A~B)]");
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final String name = "View";
    final UniqueId uid = UniqueId.of("A", "B");
    final ViewDefinition viewDefinition = new ViewDefinition(name, "Test");
    final ViewKey keyFromName = ViewKey.of(name);
    assertEquals(keyFromName, keyFromName);
    assertEquals(keyFromName, ViewKey.of(name));
    assertNotEquals(null, keyFromName);
    assertNotEquals(new Object(), keyFromName);
    assertNotEquals(keyFromName, ViewKey.of(name + "-1"));
    final ViewKey keyFromViewNoUid = ViewKey.of(viewDefinition);
    assertEquals(keyFromName, keyFromViewNoUid);
    final ViewKey keyFromNameAndUid = ViewKey.of(name, uid);
    // uid is ignored in equals
    assertEquals(keyFromName, keyFromNameAndUid);
    viewDefinition.setUniqueId(uid);
    final ViewKey keyFromViewWithUid = ViewKey.of(viewDefinition);
    assertEquals(keyFromName, keyFromViewWithUid);
  }
}
