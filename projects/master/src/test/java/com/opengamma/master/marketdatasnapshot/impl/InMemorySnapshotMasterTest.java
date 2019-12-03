/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemorySnapshotMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemorySnapshotMasterTest {

  // TODO Move the logical tests from here to the generic SnapshotMasterTestCase then we can just extend from that

  private static final UniqueId OTHER_UID = UniqueId.of("U", "1");
  private static final ManageableMarketDataSnapshot SNAP1 = new ManageableMarketDataSnapshot("Test 1", new ManageableUnstructuredMarketDataSnapshot(),
      new HashMap<YieldCurveKey, YieldCurveSnapshot>(12));
  private static final ManageableMarketDataSnapshot SNAP2 = new ManageableMarketDataSnapshot("Test 2", new ManageableUnstructuredMarketDataSnapshot(),
      new HashMap<YieldCurveKey, YieldCurveSnapshot>(12));

  private InMemorySnapshotMaster _testEmpty;
  private InMemorySnapshotMaster _testPopulated;
  private MarketDataSnapshotDocument _doc1;
  private MarketDataSnapshotDocument _doc2;

  /**
   *
   */
  @SuppressWarnings("deprecation")
  @BeforeMethod
  public void setUp() {
    _testEmpty = new InMemorySnapshotMaster(new ObjectIdSupplier("Test"));
    _testPopulated = new InMemorySnapshotMaster(new ObjectIdSupplier("Test"));
    _doc1 = new MarketDataSnapshotDocument();
    _doc1.setSnapshot(SNAP1);
    _doc1 = _testPopulated.add(_doc1);
    _doc2 = new MarketDataSnapshotDocument();
    _doc2.setSnapshot(SNAP2);
    _doc2 = _testPopulated.add(_doc2);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullSupplier() {
    new InMemorySnapshotMaster((Supplier<ObjectId>) null);
  }

  /**
   *
   */
  @SuppressWarnings("deprecation")
  public void testDefaultSupplier() {
    final InMemorySnapshotMaster master = new InMemorySnapshotMaster();
    final MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    final MarketDataSnapshotDocument added = master.add(doc);
    assertEquals("MemSnap", added.getUniqueId().getScheme());
  }

  /**
   *
   */
  @SuppressWarnings("deprecation")
  public void testAlternateSupplier() {
    final InMemorySnapshotMaster master = new InMemorySnapshotMaster(new ObjectIdSupplier("Hello"));
    final MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    final MarketDataSnapshotDocument added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchEmptyMaster() {
    final MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    final MarketDataSnapshotSearchResult result = _testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchPopulatedMasterAll() {
    final MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    final MarketDataSnapshotSearchResult result = _testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    final List<MarketDataSnapshotDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(_doc1));
    assertEquals(true, docs.contains(_doc2));
  }

  /**
   *
   */
  public void testSearchPopulatedMasterFilterByName() {
    final MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    request.setName("*est 2");
    final MarketDataSnapshotSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<MarketDataSnapshotDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc2));
  }

  /**
   *
   */
  public void testSearchPopulatedMasterFilterById() {
    final MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    request.setSnapshotIds(ImmutableSet.of(_doc1.getUniqueId().getObjectId()));
    final MarketDataSnapshotSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final MarketDataSnapshotDocument doc = result.getFirstDocument();
    assertEquals(_doc1.getUniqueId(), doc.getUniqueId());
  }

  /**
   *
   */
  public void testHistoryEmptyMaster() {
    final MarketDataSnapshotHistoryRequest request = new MarketDataSnapshotHistoryRequest();
    request.setObjectId(_doc1.getUniqueId().getObjectId());
    final MarketDataSnapshotHistoryResult result = _testEmpty.history(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testHistoryPopulatedMaster() {
    final MarketDataSnapshotHistoryRequest request = new MarketDataSnapshotHistoryRequest();
    request.setObjectId(_doc1.getUniqueId().getObjectId());
    final MarketDataSnapshotHistoryResult result = _testPopulated.history(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetEmptyMaster() {
    assertNull(_testEmpty.get(OTHER_UID));
  }

  /**
   *
   */
  public void testGetPopulatedMaster() {
    assertSame(_doc1, _testPopulated.get(_doc1.getUniqueId()));
    assertSame(_doc2, _testPopulated.get(_doc2.getUniqueId()));
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @SuppressWarnings("deprecation")
  public void testAddEmptyMaster() {
    final MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    final MarketDataSnapshotDocument added = _testEmpty.add(doc);
    assertNotNull(added.getVersionFromInstant());
    assertNotNull(added.getCorrectionFromInstant());
    assertEquals(added.getVersionFromInstant(), added.getCorrectionFromInstant());
    assertEquals("Test", added.getUniqueId().getScheme());
    assertSame(SNAP1, added.getSnapshot());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testUpdateEmptyMaster() {
    final MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    doc.setUniqueId(OTHER_UID);
    _testEmpty.update(doc);
  }

  /**
   *
   */
  @SuppressWarnings("deprecation")
  public void testUpdatePopulatedMaster() {
    final MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    doc.setUniqueId(_doc1.getUniqueId());
    final MarketDataSnapshotDocument updated = _testPopulated.update(doc);
    assertEquals(_doc1.getUniqueId(), updated.getUniqueId());
    assertNotNull(_doc1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testRemoveEmptyMaster() {
    _testEmpty.remove(OTHER_UID);
  }

  /**
   *
   */
  public void testRemovePopulatedMaster() {
    _testPopulated.remove(_doc1.getUniqueId());
    final MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    final MarketDataSnapshotSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<MarketDataSnapshotDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc2));
  }

}
