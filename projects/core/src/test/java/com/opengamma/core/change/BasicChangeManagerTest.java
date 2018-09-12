/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for{@link BasicChangeManager}.
 */
@Test(groups = TestGroup.UNIT)
public class BasicChangeManagerTest {

  private static final Instant NOW = Instant.now();
  private static final UniqueId UID_A_B_1 = UniqueId.of("A", "B", "1");
  private static final UniqueId UID_A_B_2 = UniqueId.of("A", "B", "2");

  private BasicChangeManager _changeManager;
  private ChangeListener _testListener;

  /**
   * Sets up the listeners.
   */
  @BeforeMethod
  public void setUp() {
    _changeManager = new BasicChangeManager();
    _testListener = new ChangeListener() {
      @Override
      public void entityChanged(final ChangeEvent event) {
      }
    };
  }

  //-------------------------------------------------------------------------
  /**
   * Tests adition and removal of change listeners.
   */
  public void testAddRemove() {
    assertEquals(0, _changeManager.getListeners().size());
    _changeManager.addChangeListener(_testListener);
    assertEquals(1, _changeManager.getListeners().size());
    assertSame(_testListener, _changeManager.getListeners().get(0));
    _changeManager.removeChangeListener(_testListener);
    assertEquals(0, _changeManager.getListeners().size());
  }

  /**
   * Tests that the change listener cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNull() {
    _changeManager.addChangeListener(null);
  }

  /**
   * Tests that null cannot be removed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRemoveNull() {
    _changeManager.removeChangeListener(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the addition of an object.
   */
  public void testFireAdd() {
    _changeManager.addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(final ChangeEvent event) {
        assertEquals(ChangeType.ADDED, event.getType());
        assertEquals(UID_A_B_1.getObjectId(), event.getObjectId());
        assertEquals(NOW, event.getVersionInstant());
      }
    });
    _changeManager.entityChanged(ChangeType.ADDED, UID_A_B_1.getObjectId(), NOW, NOW, NOW);
  }

  /**
   * Tests the removal of an object.
   */
  public void testFireRemove() {
    _changeManager.addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(final ChangeEvent event) {
        assertEquals(ChangeType.REMOVED, event.getType());
        assertEquals(UID_A_B_1.getObjectId(), event.getObjectId());
        assertEquals(NOW, event.getVersionInstant());
      }
    });
    _changeManager.entityChanged(ChangeType.REMOVED, UID_A_B_1.getObjectId(), NOW, NOW, NOW);
  }

  /**
   * Tests the update of an object.
   */
  public void testFireUpdate() {
    _changeManager.addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(final ChangeEvent event) {
        assertEquals(ChangeType.CHANGED, event.getType());
        assertEquals(UID_A_B_1.getObjectId(), event.getObjectId());
        assertEquals(UID_A_B_2.getObjectId(), event.getObjectId());
        assertEquals(NOW, event.getVersionInstant());
      }
    });
    _changeManager.entityChanged(ChangeType.CHANGED, UID_A_B_1.getObjectId(), NOW, NOW, NOW);
  }

  /**
   * Tests the correction of an object.
   */
  public void testFireCorrect() {
    _changeManager.addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(final ChangeEvent event) {
        assertEquals(ChangeType.CHANGED, event.getType());
        assertEquals(UID_A_B_1.getObjectId(), event.getObjectId());
        assertEquals(UID_A_B_2.getObjectId(), event.getObjectId());
        assertEquals(NOW, event.getVersionInstant());
      }
    });
    _changeManager.entityChanged(ChangeType.CHANGED, UID_A_B_1.getObjectId(), NOW, NOW, NOW);
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    assertEquals("BasicChangeManager", _changeManager.toString());
  }
}
