/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Unit tests for {@link MarketDataSet}.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataSetTest {

  /**
   * Tests the behaviour when a null map is supplied.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    MarketDataSet.of(null);
  }

  /**
   * Tests the creation of an empty set.
   */
  @Test
  public void testEmpty() {
    final MarketDataSet empty = MarketDataSet.empty();
    Assert.assertEquals(empty.size(), 0);
  }

  /**
   * Tests creation of the set.
   */
  @Test
  public void testOf() {
    final Map<MarketDataKey, Object> map = new HashMap<>();
    final MarketDataSet empty = MarketDataSet.of(map);
    Assert.assertEquals(empty.size(), 0);
    map.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    final MarketDataSet nonEmpty = MarketDataSet.of(map);
    Assert.assertEquals(nonEmpty.size(), 1);
    map.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    final MarketDataSet two = MarketDataSet.of(map);
    Assert.assertEquals(two.size(), 2);
  }

  /**
   * Tests that keys can be found.
   */
  @Test
  public void testContainsKey() {
    final Map<MarketDataKey, Object> map = new HashMap<>();
    map.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    map.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    final MarketDataSet two = MarketDataSet.of(map);
    Assert.assertTrue(two.containsKey(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertTrue(two.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
    Assert.assertFalse(two.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C")))));
  }

  /**
   * Tests data insertion.
   */
  @Test
  public void testPut() {
    final MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    Assert.assertTrue(set.containsKey(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertTrue(set.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
    Assert.assertFalse(set.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C")))));
    set.putAll(set);
    Assert.assertEquals(set.size(), 2);
    final MarketDataSet other = MarketDataSet.empty();
    other.put(MarketDataKey.of(ExternalId.of("C", "D").toBundle()), 3.4);
    set.putAll(other);
    Assert.assertEquals(set.size(), 3);
    Assert.assertTrue(set.containsKey(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertTrue(set.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
    Assert.assertTrue(set.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("C", "D")))));
  }

  /**
   * Tests data retrieval.
   */
  @Test
  public void testGet() {
    final MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    Assert.assertEquals(1.2d, set.get(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertEquals(0d, set.get(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
    Assert.assertNull(set.get(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C")))));
  }

  /**
   * Tests data removal.
   */
  @Test
  public void testRemove() {
    final MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    set.remove(MarketDataKey.of(ExternalIdBundle.EMPTY));
    Assert.assertNull(set.get(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertEquals(0d, set.get(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
  }

  /**
   * Tests the key set.
   */
  @Test
  public void testKeySet() {
    final MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    final Set<MarketDataKey> keys = set.keySet();
    Assert.assertTrue(keys.contains(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertTrue(keys.contains(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
    Assert.assertFalse(keys.contains(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C")))));
    Assert.assertEquals(keys.size(), 2);
  }

  /**
   * Tests the entry set.
   */
  @Test
  public void testEntrySet() {
    final MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    final Set<Map.Entry<MarketDataKey, Object>> entries = set.entrySet();
    Assert.assertTrue(entries.contains(ObjectsPair.of(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d)));
    Assert.assertTrue(entries.contains(ObjectsPair.of(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d)));
    Assert.assertFalse(entries.contains(ObjectsPair.of(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C"))), 0d)));
    Assert.assertEquals(entries.size(), 2);
  }

  /**
   * Tests the size.
   */
  @Test
  public void testSize() {
    final MarketDataSet set = MarketDataSet.empty();
    Assert.assertEquals(set.size(), 0);
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    Assert.assertEquals(set.size(), 1);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    Assert.assertEquals(set.size(), 2);
    set.remove(MarketDataKey.of(ExternalIdBundle.EMPTY));
    Assert.assertEquals(set.size(), 1);
    set.remove(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))));
    Assert.assertEquals(set.size(), 0);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    Assert.assertNotEquals(null, set);
    Assert.assertNotEquals(new Object(), set);
    Assert.assertEquals(set, set);
    Assert.assertEquals(set.hashCode(), set.hashCode());
    Assert.assertEquals(set.toString(), "MarketDataSet[{MarketDataKey{externalIdBundle=Bundle[A~B], field=Market_Value, "
        + "source=DEFAULT, provider=DEFAULT, normalizer=UnitNormalizer}=0.0}]");
    final MarketDataSet other = MarketDataSet.empty();
    other.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    Assert.assertNotEquals(set, other);
  }
}