package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.tuple.ObjectsPair;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jim on 08/06/15.
 */
public class MarketDataSetTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() throws Exception {
    final MarketDataSet empty = MarketDataSet.of(null);
  }

  @Test
  public void testEmpty() throws Exception {
    final MarketDataSet empty = MarketDataSet.empty();
    Assert.assertEquals(empty.size(), 0);
  }

  @Test
  public void testOf() throws Exception {
    Map<MarketDataKey, Object> map = new HashMap<>();
    final MarketDataSet empty = MarketDataSet.of(map);
    Assert.assertEquals(empty.size(), 0);
    map.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    final MarketDataSet nonEmpty = MarketDataSet.of(map);
    Assert.assertEquals(nonEmpty.size(), 1);
    map.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    final MarketDataSet two = MarketDataSet.of(map);
    Assert.assertEquals(two.size(), 2);
  }

  @Test
  public void testContainsKey() throws Exception {
    Map<MarketDataKey, Object> map = new HashMap<>();
    map.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    map.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    final MarketDataSet two = MarketDataSet.of(map);
    Assert.assertTrue(two.containsKey(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertTrue(two.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
    Assert.assertFalse(two.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C")))));
  }

  @Test
  public void testPut() throws Exception {
    MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    Assert.assertTrue(set.containsKey(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertTrue(set.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
    Assert.assertFalse(set.containsKey(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C")))));
  }

  @Test
  public void testGet() throws Exception {
    MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    Assert.assertEquals(1.2d, set.get(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertEquals(0d, set.get(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
    Assert.assertNull(set.get(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C")))));
  }

  @Test
  public void testRemove() throws Exception {
    MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    set.remove(MarketDataKey.of(ExternalIdBundle.EMPTY));
    Assert.assertNull(set.get(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertEquals(0d, set.get(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
  }

  @Test
  public void testKeySet() throws Exception {
    MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    final Set<MarketDataKey> keys = set.keySet();
    Assert.assertTrue(keys.contains(MarketDataKey.of(ExternalIdBundle.EMPTY)));
    Assert.assertTrue(keys.contains(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B")))));
    Assert.assertFalse(keys.contains(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C")))));
    Assert.assertEquals(keys.size(), 2);
  }

  @Test
  public void testEntrySet() throws Exception {
    MarketDataSet set = MarketDataSet.empty();
    set.put(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d);
    set.put(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d);
    final Set<Map.Entry<MarketDataKey, Object>> entries = set.entrySet();
    Assert.assertTrue(entries.contains(ObjectsPair.of(MarketDataKey.of(ExternalIdBundle.EMPTY), 1.2d)));
    Assert.assertTrue(entries.contains(ObjectsPair.of(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"))), 0d)));
    Assert.assertFalse(entries.contains(ObjectsPair.of(MarketDataKey.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("B", "C"))), 0d)));
    Assert.assertEquals(entries.size(), 2);
  }

  @Test
  public void testSize() throws Exception {
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
}