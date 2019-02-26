/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.TestChangeManager;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DelegatingHistoricalTimeSeriesMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class DelegatingHistoricalTimeSeriesMasterTest {
  private static final String UID_SCHEME_1 = "uid1";
  private static final String UID_SCHEME_2 = "uid2";
  private static final String UID_SCHEME_3 = "uid3";

  private static final String NAME_1 = "hts 1";
  private static final String NAME_2 = "hts 2";
  private static final String NAME_3 = "hts 3";
  private static final String DATA_FIELD = "close";
  private static final String DATA_SOURCE = "source";
  private static final String DATA_PROVIDER = "provider";
  private static final String OBS_TIME = "london close";
  private static final ExternalIdBundleWithDates TS_EID_1 = ExternalIdBundleWithDates.of(ExternalIdBundle.of("hts", "a"));
  private static final ExternalIdBundleWithDates TS_EID_2 = ExternalIdBundleWithDates.of(ExternalIdBundle.of("hts", "b"));
  private static final ExternalIdBundleWithDates TS_EID_3 = ExternalIdBundleWithDates.of(ExternalIdBundle.of("hts", "c"));
  private static final ExternalIdBundleWithDates TS_EID_4 = ExternalIdBundleWithDates.of(ExternalIdBundle.of("hts", "d"));
  private static final ExternalIdBundleWithDates TS_EID_5 = ExternalIdBundleWithDates.of(ExternalIdBundle.of("hts", "e"));
  private static final ExternalIdBundleWithDates TS_EID_6 = ExternalIdBundleWithDates.of(ExternalIdBundle.of("hts", "f"));

  private final TestManageableHistoricalTimeSeriesInfo _hts1Scheme1 = new TestManageableHistoricalTimeSeriesInfo();
  private final TestManageableHistoricalTimeSeriesInfo _hts2Scheme2 = new TestManageableHistoricalTimeSeriesInfo();
  private final TestManageableHistoricalTimeSeriesInfo _hts3Scheme3 = new TestManageableHistoricalTimeSeriesInfo();
  private final TestManageableHistoricalTimeSeriesInfo _hts4Scheme1 = new TestManageableHistoricalTimeSeriesInfo();
  private final TestManageableHistoricalTimeSeriesInfo _hts5Scheme2 = new TestManageableHistoricalTimeSeriesInfo();
  private final TestManageableHistoricalTimeSeriesInfo _hts6Scheme2 = new TestManageableHistoricalTimeSeriesInfo();

  private InMemoryHistoricalTimeSeriesMaster _default;
  private InMemoryHistoricalTimeSeriesMaster _delegate1;
  private InMemoryHistoricalTimeSeriesMaster _delegate2;
  private final Map<String, HistoricalTimeSeriesMaster> _delegates = new HashMap<>();

  /**
   * Sets up the masters.
   */
  @BeforeMethod
  public void setUp() {
    setFields(_hts1Scheme1, NAME_1, TS_EID_1);
    setFields(_hts2Scheme2, NAME_2, TS_EID_2);
    setFields(_hts3Scheme3, NAME_3, TS_EID_3);
    setFields(_hts4Scheme1, NAME_1, TS_EID_4);
    setFields(_hts5Scheme2, NAME_2, TS_EID_5);
    setFields(_hts6Scheme2, NAME_3, TS_EID_6);
    final ChangeManager changeManagerScheme1 = TestChangeManager.of("test-change-manager-1");
    final ChangeManager changeManagerScheme2 = TestChangeManager.of("test-change-manager-2");
    final ChangeManager changeManagerScheme3 = TestChangeManager.of("test-change-manager-3");
    _default = new InMemoryHistoricalTimeSeriesMaster(new ObjectIdSupplier(UID_SCHEME_1), changeManagerScheme1);
    _delegate1 = new InMemoryHistoricalTimeSeriesMaster(new ObjectIdSupplier(UID_SCHEME_2), changeManagerScheme2);
    _delegate2 = new InMemoryHistoricalTimeSeriesMaster(new ObjectIdSupplier(UID_SCHEME_3), changeManagerScheme3);
    _delegates.put(UID_SCHEME_2, _delegate1);
    _delegates.put(UID_SCHEME_3, _delegate2);
  }

  private static void setFields(final TestManageableHistoricalTimeSeriesInfo info, final String name, final ExternalIdBundleWithDates eids) {
    info.setName(name);
    info.setExternalIdBundle(eids);
    info.setDataField(DATA_FIELD);
    info.setDataSource(DATA_SOURCE);
    info.setDataProvider(DATA_PROVIDER);
    info.setObservationTime(OBS_TIME);
  }

  /**
   * Tears down the masters and resets stored objects.
   */
  @AfterMethod
  public void tearDown() {
    _delegates.clear();
    _hts1Scheme1.setUniqueId(null);
    _hts2Scheme2.setUniqueId(null);
    _hts3Scheme3.setUniqueId(null);
    _hts4Scheme1.setUniqueId(null);
    _hts5Scheme2.setUniqueId(null);
    _hts6Scheme2.setUniqueId(null);
  }

  /**
   * Tests the default constructor.
   */
  @Test
  public void testDefaultConstructor() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    assertTrue(master.changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests the addition of a document when only the default delegate has been added by the constructor.
   */
  public void testAddToDefaultDelegatingMaster() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default);
    final HistoricalTimeSeriesInfoDocument doc = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    assertDocument(doc, UID_SCHEME_1, _hts1Scheme1, true);
  }

  /**
   * Tests the addition of documents when no ids have been set on the documents (i.e. they will use the default master).
   */
  public void testAddToDelegatingMasterNoUidsSet() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    final HistoricalTimeSeriesInfoDocument doc5 = master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    final HistoricalTimeSeriesInfoDocument doc6 = master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    assertDocument(doc1, UID_SCHEME_1, _hts1Scheme1, true);
    assertDocument(doc2, UID_SCHEME_1, _hts2Scheme2, true);
    assertDocument(doc3, UID_SCHEME_1, _hts3Scheme3, true);
    assertDocument(doc4, UID_SCHEME_1, _hts4Scheme1, true);
    assertDocument(doc5, UID_SCHEME_1, _hts5Scheme2, true);
    assertDocument(doc6, UID_SCHEME_1, _hts6Scheme2, true);
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    // have the changes for all ids been registered
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().keySet(),
        new HashSet<>(Arrays.asList(doc1.getObjectId(), doc2.getObjectId(), doc3.getObjectId(), doc4.getObjectId(), doc5.getObjectId(), doc6.getObjectId())));
    assertTrue(changeManagerScheme2.getUniqueOidsWithEvents().isEmpty());
    assertTrue(changeManagerScheme3.getUniqueOidsWithEvents().isEmpty());
    // what are the change event types
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc6.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED));
  }

  /**
   * Tests the addition of documents when no ids have been set on the documents (i.e. they will use the appropriate master).
   */
  public void testAddToDelegatingMasterUidsSet() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(_hts4Scheme1);
    HistoricalTimeSeriesInfoDocument doc5 = new HistoricalTimeSeriesInfoDocument(_hts5Scheme2);
    HistoricalTimeSeriesInfoDocument doc6 = new HistoricalTimeSeriesInfoDocument(_hts6Scheme2);
    // set unique ids first to put them into the appropriate masters
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "2000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "3000"));
    doc4.setUniqueId(UniqueId.of(UID_SCHEME_1, "4000"));
    doc5.setUniqueId(UniqueId.of(UID_SCHEME_2, "5000"));
    doc6.setUniqueId(UniqueId.of(UID_SCHEME_2, "6000"));
    // uids will be reset
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    doc4 = master.add(doc4);
    doc5 = master.add(doc5);
    doc6 = master.add(doc6);
    assertDocument(doc1, UID_SCHEME_1, _hts1Scheme1, true);
    assertDocument(doc2, UID_SCHEME_2, _hts2Scheme2, true);
    assertDocument(doc3, UID_SCHEME_3, _hts3Scheme3, true);
    assertDocument(doc4, UID_SCHEME_1, _hts4Scheme1, true);
    assertDocument(doc5, UID_SCHEME_2, _hts5Scheme2, true);
    assertDocument(doc6, UID_SCHEME_2, _hts6Scheme2, true);
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    // have the changes for all ids been registered
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().keySet(), new HashSet<>(Arrays.asList(doc1.getObjectId(), doc4.getObjectId())));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().keySet(),
        new HashSet<>(Arrays.asList(doc2.getObjectId(), doc5.getObjectId(), doc6.getObjectId())));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().keySet(), new HashSet<>(Arrays.asList(doc3.getObjectId())));
    // what are the change event types
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED));
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullUniqueId() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.get((UniqueId) null);
  }

  /**
   * Tests getting documents by unique id.
   */
  public void testGetByUniqueId() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    final HistoricalTimeSeriesInfoDocument doc5 = master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    final HistoricalTimeSeriesInfoDocument doc6 = master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    assertEquals(master.get(doc1.getUniqueId()), doc1);
    assertEquals(master.get(doc2.getUniqueId()), doc2);
    assertEquals(master.get(doc3.getUniqueId()), doc3);
    assertEquals(master.get(doc4.getUniqueId()), doc4);
    assertEquals(master.get(doc5.getUniqueId()), doc5);
    assertEquals(master.get(doc6.getUniqueId()), doc6);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullObjectId() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.get(null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.get(_hts1Scheme1, null);
  }

  /**
   * Tests getting documents by object id / version correction. The underlying master in this case does not track versions.
   */
  public void testGetByObjectIdVersionCorrection() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    final HistoricalTimeSeriesInfoDocument doc5 = master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    final HistoricalTimeSeriesInfoDocument doc6 = master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    final VersionCorrection vc = VersionCorrection.of(Instant.ofEpochSecond(1000), Instant.ofEpochSecond(1500));
    assertEquals(master.get(doc1.getObjectId(), vc), doc1);
    assertEquals(master.get(doc2.getObjectId(), vc), doc2);
    assertEquals(master.get(doc3.getObjectId(), vc), doc3);
    assertEquals(master.get(doc4.getObjectId(), vc), doc4);
    assertEquals(master.get(doc5.getObjectId(), vc), doc5);
    assertEquals(master.get(doc6.getObjectId(), vc), doc6);
  }

  /**
   * Tests that the unique ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullCollection() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.get((Collection<UniqueId>) null);
  }

  /**
   * Tests getting documents by unique id.
   */
  public void testGetByUniqueIdCollection() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    final HistoricalTimeSeriesInfoDocument doc5 = master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    final HistoricalTimeSeriesInfoDocument doc6 = master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    final Collection<UniqueId> uids = Arrays.asList(doc1.getUniqueId(), doc2.getUniqueId(), doc3.getUniqueId(), doc4.getUniqueId(), doc5.getUniqueId(),
        doc6.getUniqueId());
    final Map<UniqueId, HistoricalTimeSeriesInfoDocument> docs = master.get(uids);
    assertEquals(docs.size(), 6);
    assertEquals(docs.get(doc1.getUniqueId()), doc1);
    assertEquals(docs.get(doc2.getUniqueId()), doc2);
    assertEquals(docs.get(doc3.getUniqueId()), doc3);
    assertEquals(docs.get(doc4.getUniqueId()), doc4);
    assertEquals(docs.get(doc5.getUniqueId()), doc5);
    assertEquals(docs.get(doc6.getUniqueId()), doc6);
  }

  /**
   * Tests updating documents.
   */
  public void testUpdate() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(_hts4Scheme1);
    HistoricalTimeSeriesInfoDocument doc5 = new HistoricalTimeSeriesInfoDocument(_hts5Scheme2);
    HistoricalTimeSeriesInfoDocument doc6 = new HistoricalTimeSeriesInfoDocument(_hts6Scheme2);
    // set unique ids first to put them into the appropriate masters
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "2000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "3000"));
    doc4.setUniqueId(UniqueId.of(UID_SCHEME_1, "4000"));
    doc5.setUniqueId(UniqueId.of(UID_SCHEME_2, "5000"));
    doc6.setUniqueId(UniqueId.of(UID_SCHEME_2, "6000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    doc4 = master.add(doc4);
    doc5 = master.add(doc5);
    doc6 = master.add(doc6);
    doc1 = master.update(doc1);
    doc2 = master.update(doc2);
    doc3 = master.update(doc3);
    doc4 = master.update(doc4);
    doc5 = master.update(doc5);
    doc6 = master.update(doc6);
    assertEquals(master.get(doc1.getUniqueId()), doc1);
    assertEquals(master.get(doc2.getUniqueId()), doc2);
    assertEquals(master.get(doc3.getUniqueId()), doc3);
    assertEquals(master.get(doc4.getUniqueId()), doc4);
    assertEquals(master.get(doc5.getUniqueId()), doc5);
    assertEquals(master.get(doc6.getUniqueId()), doc6);
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Test that the object cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRemoveNullObject() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.remove(null);
  }

  /**
   * Tests removing documents.
   */
  public void testRemove() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(_hts4Scheme1);
    HistoricalTimeSeriesInfoDocument doc5 = new HistoricalTimeSeriesInfoDocument(_hts5Scheme2);
    HistoricalTimeSeriesInfoDocument doc6 = new HistoricalTimeSeriesInfoDocument(_hts6Scheme2);
    // set unique ids first to put them into the appropriate masters
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "2000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "3000"));
    doc4.setUniqueId(UniqueId.of(UID_SCHEME_1, "4000"));
    doc5.setUniqueId(UniqueId.of(UID_SCHEME_2, "5000"));
    doc6.setUniqueId(UniqueId.of(UID_SCHEME_2, "6000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    doc4 = master.add(doc4);
    doc5 = master.add(doc5);
    doc6 = master.add(doc6);
    master.remove(doc1);
    master.remove(doc2);
    master.remove(doc3);
    master.remove(doc4);
    master.remove(doc5);
    master.remove(doc6);
    // try / catch blocks needed because of underlying master behaviour
    try {
      master.get(doc1.getUniqueId());
      fail();
    } catch (final DataNotFoundException e) {
    }
    try {
      master.get(doc2.getUniqueId());
      fail();
    } catch (final DataNotFoundException e) {
    }
    try {
      master.get(doc3.getUniqueId());
      fail();
    } catch (final DataNotFoundException e) {
    }
    try {
      master.get(doc4.getUniqueId());
      fail();
    } catch (final DataNotFoundException e) {
    }
    try {
      master.get(doc5.getUniqueId());
      fail();
    } catch (final DataNotFoundException e) {
    }
    try {
      master.get(doc6.getUniqueId());
      fail();
    } catch (final DataNotFoundException e) {
    }
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.REMOVED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.REMOVED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.REMOVED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.REMOVED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.REMOVED));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.REMOVED));
  }

  /**
   * Tests that the document cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocument() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.correct(null);
  }

  /**
   * Tests correcting documents.
   */
  public void testCorrect() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(_hts4Scheme1);
    HistoricalTimeSeriesInfoDocument doc5 = new HistoricalTimeSeriesInfoDocument(_hts5Scheme2);
    HistoricalTimeSeriesInfoDocument doc6 = new HistoricalTimeSeriesInfoDocument(_hts6Scheme2);
    // set unique ids first to put them into the appropriate masters
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "2000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "3000"));
    doc4.setUniqueId(UniqueId.of(UID_SCHEME_1, "4000"));
    doc5.setUniqueId(UniqueId.of(UID_SCHEME_2, "5000"));
    doc6.setUniqueId(UniqueId.of(UID_SCHEME_2, "6000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    doc4 = master.add(doc4);
    doc5 = master.add(doc5);
    doc6 = master.add(doc6);
    doc1 = master.correct(doc1);
    doc2 = master.correct(doc2);
    doc3 = master.correct(doc3);
    doc4 = master.correct(doc4);
    doc5 = master.correct(doc5);
    doc6 = master.correct(doc6);
    assertEquals(master.get(doc1.getUniqueId()), doc1);
    assertEquals(master.get(doc2.getUniqueId()), doc2);
    assertEquals(master.get(doc3.getUniqueId()), doc3);
    assertEquals(master.get(doc4.getUniqueId()), doc4);
    assertEquals(master.get(doc5.getUniqueId()), doc5);
    assertEquals(master.get(doc6.getUniqueId()), doc6);
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionNullUniqueId() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.replaceVersion(null, Arrays.asList(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1), new HistoricalTimeSeriesInfoDocument(_hts2Scheme2)));
  }

  /**
   * Tests that the replacement documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionNullDocuments() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.replaceVersion(UniqueId.of(UID_SCHEME_2, "1"), null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceVersionByUid() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    // set unique ids first to put them into the appropriate masters
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "2000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "3000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    // check before replace
    assertEquals(master.get(doc1.getUniqueId()), doc1);
    assertEquals(master.get(doc2.getUniqueId()), doc2);
    assertEquals(master.get(doc3.getUniqueId()), doc3);

    final HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final HistoricalTimeSeriesInfoDocument doc5 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final HistoricalTimeSeriesInfoDocument doc6 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    doc6.setVersionFromInstant(Instant.now().plusSeconds(3000));
    final List<UniqueId> uidList1 = master.replaceVersion(doc1.getUniqueId(), Collections.singletonList(doc4));
    final List<UniqueId> uidList2 = master.replaceVersion(doc2.getUniqueId(), Collections.singletonList(doc5));
    final List<UniqueId> uidList3 = master.replaceVersion(doc3.getUniqueId(), Arrays.asList(doc4, doc5, doc6));
    assertEquals(uidList1.size(), 1);
    assertEquals(uidList2.size(), 1);
    assertEquals(uidList3.size(), 1);
    assertEquals(uidList1.get(0), doc4.getUniqueId());
    assertEquals(uidList2.get(0), doc5.getUniqueId());
    assertEquals(uidList3.get(0), doc6.getUniqueId());
    // have documents been replaced
    assertEquals(master.get(doc1.getUniqueId()), doc4);
    assertEquals(master.get(doc2.getUniqueId()), doc5);
    assertEquals(master.get(doc3.getUniqueId()), doc6);
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceAllVersionsNullObjectId() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.replaceAllVersions(null, Arrays.asList(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1), new HistoricalTimeSeriesInfoDocument(_hts2Scheme2)));
  }

  /**
   * Tests that the replacement documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceAllVersionsNullDocuments() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.replaceAllVersions(ObjectId.of(UID_SCHEME_2, "1"), null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceAllVersions() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    // set unique ids first to put them into the appropriate masters
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "2000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "3000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    // check before replace
    assertEquals(master.get(doc1.getUniqueId()), doc1);
    assertEquals(master.get(doc2.getUniqueId()), doc2);
    assertEquals(master.get(doc3.getUniqueId()), doc3);

    final HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final HistoricalTimeSeriesInfoDocument doc5 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final HistoricalTimeSeriesInfoDocument doc6 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    doc6.setVersionFromInstant(Instant.now().plusSeconds(3000));
    final List<UniqueId> uidList1 = master.replaceAllVersions(doc1.getUniqueId(), Collections.singletonList(doc4));
    final List<UniqueId> uidList2 = master.replaceAllVersions(doc2.getUniqueId(), Collections.singletonList(doc5));
    final List<UniqueId> uidList3 = master.replaceAllVersions(doc3.getUniqueId(), Arrays.asList(doc4, doc5, doc6));
    assertEquals(uidList1.size(), 1);
    assertEquals(uidList2.size(), 1);
    assertEquals(uidList3.size(), 1);
    assertEquals(uidList1.get(0), doc4.getUniqueId());
    assertEquals(uidList2.get(0), doc5.getUniqueId());
    assertEquals(uidList3.get(0), doc6.getUniqueId());
    // have documents been replaced
    assertEquals(master.get(doc1.getUniqueId()), doc4);
    assertEquals(master.get(doc2.getUniqueId()), doc5);
    assertEquals(master.get(doc3.getUniqueId()), doc6);
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionsNullObjectId() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.replaceVersions(null, Arrays.asList(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1), new HistoricalTimeSeriesInfoDocument(_hts2Scheme2)));
  }

  /**
   * Tests that the replacement documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionsNullDocuments() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.replaceVersions(ObjectId.of(UID_SCHEME_2, "1"), null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceVersions() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    // set unique ids first to put them into the appropriate masters
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "2000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "3000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    // check before replace
    assertEquals(master.get(doc1.getUniqueId()), doc1);
    assertEquals(master.get(doc2.getUniqueId()), doc2);
    assertEquals(master.get(doc3.getUniqueId()), doc3);

    final HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final HistoricalTimeSeriesInfoDocument doc5 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final HistoricalTimeSeriesInfoDocument doc6 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    doc6.setVersionFromInstant(Instant.now().plusSeconds(3000));
    final List<UniqueId> uidList1 = master.replaceVersions(doc1.getUniqueId(), Collections.singletonList(doc4));
    final List<UniqueId> uidList2 = master.replaceVersions(doc2.getUniqueId(), Collections.singletonList(doc5));
    final List<UniqueId> uidList3 = master.replaceVersions(doc3.getUniqueId(), Arrays.asList(doc4, doc5, doc6));
    assertEquals(uidList1.size(), 1);
    assertEquals(uidList2.size(), 1);
    assertEquals(uidList3.size(), 1);
    assertEquals(uidList1.get(0), doc4.getUniqueId());
    assertEquals(uidList2.get(0), doc5.getUniqueId());
    assertEquals(uidList3.get(0), doc6.getUniqueId());
    // have documents been replaced
    assertEquals(master.get(doc1.getUniqueId()), doc4);
    assertEquals(master.get(doc2.getUniqueId()), doc5);
    assertEquals(master.get(doc3.getUniqueId()), doc6);
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    // documents are cloned so only change events are registered for the
    // original ids
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionsNullHistoricalTimeSeriesInfoDocument() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.replaceVersion(null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceVersionByHistoricalTimeSeriesInfoDocument() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    // set unique ids first to put them into the appropriate masters
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "2000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "3000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);

    // check before replace
    assertEquals(master.get(doc1.getUniqueId()), doc1);
    assertEquals(master.get(doc2.getUniqueId()), doc2);
    assertEquals(master.get(doc3.getUniqueId()), doc3);

    // documents are cloned on add so need to set the unique ids to match
    final HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    doc4.setUniqueId(doc1.getUniqueId());
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final HistoricalTimeSeriesInfoDocument doc5 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    doc5.setUniqueId(doc2.getUniqueId());
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final HistoricalTimeSeriesInfoDocument doc6 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    doc6.setUniqueId(doc3.getUniqueId());
    doc6.setVersionFromInstant(Instant.now().plusSeconds(3000));
    final UniqueId uid1 = master.replaceVersion(doc4);
    final UniqueId uid2 = master.replaceVersion(doc5);
    final UniqueId uid3 = master.replaceVersion(doc6);
    assertEquals(uid1, doc4.getUniqueId());
    assertEquals(uid2, doc5.getUniqueId());
    assertEquals(uid3, doc6.getUniqueId());
    // have documents been replaced
    assertEquals(master.get(doc1.getUniqueId()), doc4);
    assertEquals(master.get(doc2.getUniqueId()), doc5);
    assertEquals(master.get(doc3.getUniqueId()), doc6);
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRemoveVersionNullId() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.removeVersion(null);
  }

  /**
   * Tests the addition of documents.
   */
  public void testAddVersion() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(_hts1Scheme1);
    HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(_hts2Scheme2);
    HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(_hts3Scheme3);
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "1000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "1000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    assertDocument(doc1, UID_SCHEME_1, _hts1Scheme1, true);
    assertDocument(doc2, UID_SCHEME_2, _hts2Scheme2, true);
    assertDocument(doc3, UID_SCHEME_3, _hts3Scheme3, true);
    HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(_hts4Scheme1);
    HistoricalTimeSeriesInfoDocument doc5 = new HistoricalTimeSeriesInfoDocument(_hts5Scheme2);
    HistoricalTimeSeriesInfoDocument doc6 = new HistoricalTimeSeriesInfoDocument(_hts6Scheme2);
    doc4.setUniqueId(UniqueId.of(UID_SCHEME_1, doc1.getUniqueId().getValue()));
    doc5.setUniqueId(UniqueId.of(UID_SCHEME_2, doc2.getUniqueId().getValue()));
    doc6.setUniqueId(UniqueId.of(UID_SCHEME_2, doc3.getUniqueId().getValue()));
    final UniqueId uid1 = master.addVersion(doc1.getObjectId(), doc4);
    final UniqueId uid2 = master.addVersion(doc2.getObjectId(), doc5);
    final UniqueId uid3 = master.addVersion(doc3.getObjectId(), doc6);
    assertEquals(uid1.getScheme(), UID_SCHEME_1);
    assertEquals(uid2.getScheme(), UID_SCHEME_2);
    assertEquals(uid3.getScheme(), UID_SCHEME_2);
    doc4 = master.get(doc1.getUniqueId());
    doc5 = master.get(doc2.getUniqueId());
    doc6 = master.get(doc3.getUniqueId());
    assertDocument(doc1, UID_SCHEME_1, _hts1Scheme1, false);
    assertDocument(doc2, UID_SCHEME_2, _hts2Scheme2, false);
    assertDocument(doc3, UID_SCHEME_3, _hts3Scheme3, false);
    assertDocument(doc4, UID_SCHEME_1, _hts4Scheme1, false);
    assertDocument(doc5, UID_SCHEME_2, _hts5Scheme2, false);
    assertDocument(doc6, UID_SCHEME_2, _hts6Scheme2, false);
    final TestChangeManager changeManagerScheme1 = (TestChangeManager) _default.changeManager();
    final TestChangeManager changeManagerScheme2 = (TestChangeManager) _delegate1.changeManager();
    final TestChangeManager changeManagerScheme3 = (TestChangeManager) _delegate2.changeManager();
    // have the changes for all ids been registered
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().keySet(), new HashSet<>(Arrays.asList(doc1.getObjectId())));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().keySet(), new HashSet<>(Arrays.asList(doc2.getObjectId())));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().keySet(), new HashSet<>(Arrays.asList(doc3.getObjectId())));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().keySet(), new HashSet<>(Arrays.asList(doc4.getObjectId())));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().keySet(), new HashSet<>(Arrays.asList(doc5.getObjectId())));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().keySet(), new HashSet<>(Arrays.asList(doc6.getObjectId())));
    // what are the change event types
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Tests that the search request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSearchNullRequest() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.search(null);
  }

  /**
   * Searches for documents by name.
   */
  public void testSearchByNameExactMatch() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    final HistoricalTimeSeriesInfoDocument doc5 = master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    final HistoricalTimeSeriesInfoDocument doc6 = master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    final HistoricalTimeSeriesInfoSearchRequest request1 = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchRequest request2 = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchRequest request3 = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchRequest request4 = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchRequest request5 = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchRequest request6 = new HistoricalTimeSeriesInfoSearchRequest();
    request1.setName(_hts1Scheme1.getName());
    request2.setName(_hts2Scheme2.getName());
    request3.setName(_hts3Scheme3.getName());
    request4.setName(_hts4Scheme1.getName());
    request5.setName(_hts5Scheme2.getName());
    request6.setName(_hts6Scheme2.getName());
    assertEqualsNoOrder(master.search(request1).getInfoList(), Arrays.asList(doc1.getInfo(), doc4.getInfo()));
    assertEqualsNoOrder(master.search(request2).getInfoList(), Arrays.asList(doc2.getInfo(), doc5.getInfo()));
    assertEqualsNoOrder(master.search(request3).getInfoList(), Arrays.asList(doc3.getInfo(), doc6.getInfo()));
  }

  /**
   * Searches for documents by name.
   */
  public void testSearchByNameWildcard() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    final HistoricalTimeSeriesInfoDocument doc5 = master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    final HistoricalTimeSeriesInfoDocument doc6 = master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("h*s*");
    final HistoricalTimeSeriesInfoSearchResult result = master.search(request);
    assertEquals(result.getDocuments().size(), 6);
    assertTrue(result.getDocuments().contains(doc1));
    assertTrue(result.getDocuments().contains(doc2));
    assertTrue(result.getDocuments().contains(doc3));
    assertTrue(result.getDocuments().contains(doc4));
    assertTrue(result.getDocuments().contains(doc5));
    assertTrue(result.getDocuments().contains(doc6));
  }

  /**
   * Searches for documents by name using the default delegate.
   */
  public void testSearchDefaultDelegateByName() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    final HistoricalTimeSeriesInfoDocument doc5 = master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    final HistoricalTimeSeriesInfoDocument doc6 = master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    final HistoricalTimeSeriesInfoSearchRequest request1 = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchRequest request2 = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchRequest request3 = new HistoricalTimeSeriesInfoSearchRequest();
    request1.setName(NAME_1);
    request2.setName(NAME_2);
    request3.setName(NAME_3);
    final HistoricalTimeSeriesInfoSearchResult result1 = master.search(request1);
    final HistoricalTimeSeriesInfoSearchResult result2 = master.search(request2);
    final HistoricalTimeSeriesInfoSearchResult result3 = master.search(request3);
    assertEquals(result1.getDocuments().size(), 2);
    assertEquals(result2.getDocuments().size(), 2);
    assertEquals(result3.getDocuments().size(), 2);
    assertTrue(result1.getDocuments().contains(doc1));
    assertTrue(result1.getDocuments().contains(doc4));
    assertTrue(result2.getDocuments().contains(doc2));
    assertTrue(result2.getDocuments().contains(doc5));
    assertTrue(result3.getDocuments().contains(doc6));
    assertTrue(result3.getDocuments().contains(doc3));
  }

  /**
   * Searches for documents by data field.
   */
  public void testSearchByDataField() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    final HistoricalTimeSeriesInfoDocument doc5 = master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    final HistoricalTimeSeriesInfoDocument doc6 = master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    // setting the id
    request.setObjectIds(Collections.singleton(doc1.getObjectId()));
    request.setDataField(DATA_FIELD);
    HistoricalTimeSeriesInfoSearchResult result = master.search(request);
    assertEquals(result.getDocuments().size(), 1);
    assertEqualsNoOrder(result.getDocuments(), Arrays.asList(doc1));
    // falls back to using default delegate as a scheme isn't set
    request.setObjectIds(Collections.<ObjectId> emptySet());
    request.setDataField(DATA_FIELD);
    result = master.search(request);
    // no result as there is no matching object id
    assertTrue(result.getDocuments().isEmpty());
    // falls back to using default delegate as the scheme isn't set
    request.setObjectIds(null);
    request.setDataField(DATA_FIELD);
    result = master.search(request);
    assertEquals(result.getDocuments().size(), 6);
    assertEqualsNoOrder(result.getDocuments(), Arrays.asList(doc1, doc2, doc3, doc4, doc5, doc6));
  }

  /**
   * Searches for documents by data field.
   */
  public void testSearchDefaultDelegateByDataField() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    final HistoricalTimeSeriesInfoDocument doc5 = master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    final HistoricalTimeSeriesInfoDocument doc6 = master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    // setting the id
    request.setObjectIds(Collections.singleton(doc1.getObjectId()));
    request.setDataField(DATA_FIELD);
    HistoricalTimeSeriesInfoSearchResult result = master.search(request);
    assertEquals(result.getDocuments().size(), 1);
    assertEqualsNoOrder(result.getDocuments(), Arrays.asList(doc1));
    // falls back to using default delegate as a scheme isn't set
    request.setObjectIds(Collections.<ObjectId> emptySet());
    request.setDataField(DATA_FIELD);
    result = master.search(request);
    // no result as there is no matching object id
    assertTrue(result.getDocuments().isEmpty());
    // falls back to using default delegate as the scheme isn't set
    request.setObjectIds(null);
    request.setDataField(DATA_FIELD);
    result = master.search(request);
    assertEquals(result.getDocuments().size(), 6);
    assertEqualsNoOrder(result.getDocuments(), Arrays.asList(doc1, doc2, doc3, doc4, doc5, doc6));
  }

  /**
   * Tests that the history request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHistoryNullRequest() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.history(null);
  }

  /**
   * Tests getting the history of a document.
   */
  public void testHistory() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    final HistoricalTimeSeriesInfoHistoryRequest request1 = new HistoricalTimeSeriesInfoHistoryRequest();
    final HistoricalTimeSeriesInfoHistoryRequest request2 = new HistoricalTimeSeriesInfoHistoryRequest();
    final HistoricalTimeSeriesInfoHistoryRequest request3 = new HistoricalTimeSeriesInfoHistoryRequest();
    final HistoricalTimeSeriesInfoHistoryRequest request4 = new HistoricalTimeSeriesInfoHistoryRequest();
    request1.setObjectId(doc1.getObjectId());
    request2.setObjectId(doc2.getObjectId());
    request3.setObjectId(doc3.getObjectId());
    request4.setObjectId(ObjectId.of(UID_SCHEME_2, "1000"));
    final HistoricalTimeSeriesInfoHistoryResult result1 = master.history(request1);
    final HistoricalTimeSeriesInfoHistoryResult result2 = master.history(request2);
    final HistoricalTimeSeriesInfoHistoryResult result3 = master.history(request3);
    // underlying master throws exception
    try {
      master.history(request4);
      fail();
    } catch (final DataNotFoundException e) {
    }
    assertEquals(result1.getDocuments().size(), 1);
    assertEquals(result2.getDocuments().size(), 1);
    assertEquals(result3.getDocuments().size(), 1);
    assertTrue(result1.getDocuments().contains(doc1));
    assertTrue(result2.getDocuments().contains(doc2));
    assertTrue(result3.getDocuments().contains(doc3));
  }

  /**
   * Tests the retrieval of meta data.
   */
  @Test
  public void testMetaData() {
    final DelegatingHistoricalTimeSeriesMaster master = new DelegatingHistoricalTimeSeriesMaster(_default, _delegates);
    master.add(new HistoricalTimeSeriesInfoDocument(_hts1Scheme1));
    master.add(new HistoricalTimeSeriesInfoDocument(_hts2Scheme2));
    master.add(new HistoricalTimeSeriesInfoDocument(_hts3Scheme3));
    master.add(new HistoricalTimeSeriesInfoDocument(_hts4Scheme1));
    master.add(new HistoricalTimeSeriesInfoDocument(_hts5Scheme2));
    master.add(new HistoricalTimeSeriesInfoDocument(_hts6Scheme2));
    final HistoricalTimeSeriesInfoMetaDataRequest request = new HistoricalTimeSeriesInfoMetaDataRequest();
    request.setUniqueIdScheme(UID_SCHEME_1);
    request.setDataFields(true);
    HistoricalTimeSeriesInfoMetaDataResult metaData = master.metaData(request);
    assertEquals(metaData.getDataFields(), Arrays.asList(DATA_FIELD));
    request.setUniqueIdScheme(null);
    request.setDataFields(true);
    metaData = master.metaData(request);
    assertEquals(metaData.getDataFields(), Arrays.asList(DATA_FIELD));
  }

  private static void assertDocument(final HistoricalTimeSeriesInfoDocument actualDocument, final String expectedIdScheme,
      final ManageableHistoricalTimeSeriesInfo expectedInfo, final boolean tsObjectIdIsSet) {
    // info objects are cloned so the unique ids are set on the actual document but not the expected
    assertEquals(actualDocument.getUniqueId().getScheme(), expectedIdScheme);
    assertEquals(actualDocument.getValue().getUniqueId().getScheme(), expectedIdScheme);
    final ManageableHistoricalTimeSeriesInfo actualInfo = actualDocument.getValue();
    assertEquals(actualInfo.getUniqueId().getScheme(), expectedIdScheme);
    if (tsObjectIdIsSet) {
      assertEquals(actualInfo.getTimeSeriesObjectId().getScheme(), expectedIdScheme);
    }
    assertTrue(equalIgnoring(actualInfo, expectedInfo, ManageableHistoricalTimeSeriesInfo.meta().uniqueId(),
        ManageableHistoricalTimeSeriesInfo.meta().timeSeriesObjectId()));
  }

  /**
   *
   */
  private static class TestManageableHistoricalTimeSeriesInfo extends ManageableHistoricalTimeSeriesInfo implements ObjectIdentifiable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    TestManageableHistoricalTimeSeriesInfo() {
      super();
    }

    @Override
    public ObjectId getObjectId() {
      return getTimeSeriesObjectId();
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof TestManageableHistoricalTimeSeriesInfo)) {
        return false;
      }
      return super.equals(o);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }
  }

  // adapted from JodaBeanUtils.equalIgnoring, allowing the subclass to be
  // compared
  private static boolean equalIgnoring(final Bean bean1, final Bean bean2, final MetaProperty<?>... properties) {
    if (bean1 == bean2) {
      return true;
    }
    final Class<? extends Bean> class1 = bean1.getClass();
    final Class<? extends Bean> class2 = bean2.getClass();
    if (class1 != class2 && !class1.isAssignableFrom(class2) && !class2.isAssignableFrom(class1)) {
      return false;
    }
    switch (properties.length) {
      case 0:
        return bean1.equals(bean2);
      case 1: {
        final MetaProperty<?> ignored = properties[0];
        for (final MetaProperty<?> mp : bean1.metaBean().metaPropertyIterable()) {
          if (!ignored.equals(mp) && !JodaBeanUtils.equal(mp.get(bean1), mp.get(bean2))) {
            return false;
          }
        }
        return true;
      }
      default:
        final Set<MetaProperty<?>> ignored = new HashSet<>(Arrays.asList(properties));
        for (final MetaProperty<?> mp : bean1.metaBean().metaPropertyIterable()) {
          if (!ignored.contains(mp) && !JodaBeanUtils.equal(mp.get(bean1), mp.get(bean2))) {
            return false;
          }
        }
        return true;
    }
  }

}
