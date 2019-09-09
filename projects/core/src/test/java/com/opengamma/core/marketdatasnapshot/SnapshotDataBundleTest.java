/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link SnapshotDataBundle} class.
 */
@Test(groups = TestGroup.UNIT)
public class SnapshotDataBundleTest {

  private static SnapshotDataBundle createObject() {
    final SnapshotDataBundle object = new SnapshotDataBundle();
    object.setDataPoint(ExternalId.of("Foo", "1"), 1d);
    object.setDataPoint(ExternalId.of("Foo", "2"), 2d);
    object.setDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Cow")), 3d);
    object.setDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "4"), ExternalId.of("Bar", "Dog")), 4d);
    assertEquals(object.size(), 4);
    return object;
  }

  /**
   * Tests getting data where all ids in the bunch match.
   */
  public void testGetBundleExactMatch() {
    final SnapshotDataBundle object = createObject();
    assertEquals(object.getDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "1"))), 1d, 1e-15);
    assertEquals(object.getDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Cow"))), 3d, 1e-15);
  }

  /**
   * Tests getting data where some ids in the bundle match.
   */
  public void testGetBundlePartialMatch() {
    final SnapshotDataBundle object = createObject();
    assertEquals(object.getDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "2"), ExternalId.of("Missing", "1"))), 2d, 1e-15);
  }

  /**
   * Tests getting where where none of the ids match.
   */
  public void testGetBundleNoMatch() {
    final SnapshotDataBundle object = createObject();
    assertNull(object.getDataPoint(ExternalIdBundle.of(ExternalId.of("Missing", "2"), ExternalId.of("Missing", "1"))));
  }

  /**
   * Tests getting data where the one id matches.
   */
  public void testGetSingleMatch() {
    final SnapshotDataBundle object = createObject();
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "1")), 1d, 1e-15);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "4")), 4d, 1e-15);
  }

  /**
   * Tests getting data where no id matches.
   */
  public void testGetSingleNoMatch() {
    final SnapshotDataBundle object = createObject();
    assertNull(object.getDataPoint(ExternalId.of("Missing", "1")));
  }

  /**
   * Tests overwriting a data point.
   */
  public void testSetBundleErasing() {
    final SnapshotDataBundle object = createObject();
    object.setDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "2"), ExternalId.of("Bar", "Cow")), 42d);
    assertEquals(object.size(), 3);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "1")), 1d, 1e-15);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "2")), 42d, 1e-15);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "4")), 4d, 1e-15);
  }

  /**
   * Tests overwriting a data point.
   */
  public void testSetBundleReplacing() {
    final SnapshotDataBundle object = createObject();
    object.setDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Cow")), 42d);
    assertEquals(object.size(), 4);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "3")), 42d, 1e-15);
  }

  /**
   * Tests overwriting a data point.
   */
  public void testSetSingleReplacing() {
    final SnapshotDataBundle object = createObject();
    object.setDataPoint(ExternalId.of("Foo", "3"), 42d);
    assertEquals(object.size(), 4);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "3")), 42d, 1e-15);
    assertEquals(object.getDataPoint(ExternalId.of("Bar", "Cow")), 42d, 1e-15);
  }

  /**
   * Tests removal of a point.
   */
  public void testRemoveBundleExact() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoints(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Cow")));
    assertEquals(object.size(), 3);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
  }

  /**
   * Tests removal of a point.
   */
  public void testRemoveBundlePartial() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoints(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Missing", "1")));
    assertEquals(object.size(), 3);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
  }

  /**
   * Tests removal of a point.
   */
  public void testRemoveBundleMultiple() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoints(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Dog")));
    assertEquals(object.size(), 2);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
    assertNull(object.getDataPoint(ExternalId.of("Foo", "4")));
  }

  /**
   * Tests removal of a point.
   */
  public void testRemoveSingleDirect() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoint(ExternalId.of("Foo", "2"));
    assertEquals(object.size(), 3);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "2")));
  }

  /**
   * Tests removal of a point.
   */
  public void testRemoveSingleCascade() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoint(ExternalId.of("Bar", "Cow"));
    assertEquals(object.size(), 3);
    assertNull(object.getDataPoint(ExternalId.of("Bar", "Cow")));
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
  }

  /**
   * Tests setting a data point.
   */
  public void testGetDataPointSet() {
    final SnapshotDataBundle object = new SnapshotDataBundle();
    assertTrue(object.getDataPointSet().isEmpty());
    object.setDataPoint(ExternalId.of("Foo", "Bar"), 42d);
    assertEquals(object.getDataPointSet().size(), 1);
    final Map.Entry<ExternalIdBundle, Double> e = object.getDataPointSet().iterator().next();
    assertEquals(e.getKey(), ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
    assertEquals(e.getValue(), 42d, 1e-15);
  }

  /**
   * Tests the equals method.
   */
  public void testEquals() {
    final SnapshotDataBundle snap = new SnapshotDataBundle();
    snap.setDataPoint(ExternalId.parse("Snap~Test"), 1234.56);
    final SnapshotDataBundle snap2 = new SnapshotDataBundle();
    snap2.setDataPoint(ExternalId.parse("Snap~Test"), 1234.56);
    final SnapshotDataBundle snap3 = new SnapshotDataBundle();
    snap3.setDataPoint(ExternalId.parse("Snap~Test1"), 1234);
    snap3.setDataPoint(ExternalId.parse("Snap~Test2"), 12340);
    assertEquals(snap, snap2);
    assertEquals(snap.hashCode(), snap2.hashCode());
    assertNotEquals(snap, snap3);
    assertNotEquals(snap.hashCode(), snap3.hashCode());
    assertNotEquals(null, snap);
    assertEquals(snap, snap);
    assertNotEquals(snap.getDataPoints(), snap);
  }

  /**
   * Tests the toString method.
   */
  public void testToString() {
    final SnapshotDataBundle data = new SnapshotDataBundle();
    data.setDataPoint(ExternalId.parse("Snap~Test1"), 1234.56);
    final String expected = "SnapshotDataBundle[Bundle[Snap~Test1]=1234.56]";
    assertEquals(data.toString(), expected);
  }
}
