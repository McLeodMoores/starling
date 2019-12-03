/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.config.impl;

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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdOrBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.TestChangeManager;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DelegatingConfigMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class DelegatingConfigMasterTest {
  private static final String UID_SCHEME_1 = "uid1";
  private static final String UID_SCHEME_2 = "uid2";
  private static final String UID_SCHEME_3 = "uid3";

  private final ConfigItem<Object> _cfg1Scheme1 = ConfigItem.<Object> of(ExternalId.of("eid", "1"), "Test 1", ExternalId.class);
  private final ConfigItem<Object> _cfg2Scheme2 = ConfigItem.<Object> of(ExternalId.of("eid", "2"), "Test 2", ExternalId.class);
  private final ConfigItem<Object> _cfg3Scheme3 = ConfigItem.<Object> of(ExternalId.of("eid", "3"), "Test 3", ExternalId.class);
  private final ConfigItem<Object> _cfg4Scheme1 = ConfigItem.<Object> of(ExternalIdBundle.of("eid", "4"), "Test 4", ExternalIdBundle.class);
  private final ConfigItem<Object> _cfg5Scheme2 = ConfigItem.<Object> of(ExternalIdBundle.of("eid", "5"), "Test 5", ExternalIdBundle.class);
  private final ConfigItem<Object> _cfg6Scheme2 = ConfigItem.<Object> of(ExternalIdBundle.of("eid", "6"), "Test 6", ExternalIdBundle.class);

  private InMemoryConfigMaster _default;
  private InMemoryConfigMaster _delegate1;
  private InMemoryConfigMaster _delegate2;
  private final Map<String, ConfigMaster> _delegates = new HashMap<>();

  /**
   * Sets up the masters.
   */
  @BeforeMethod
  public void setUp() {
    final ChangeManager changeManagerScheme1 = TestChangeManager.of("test-change-manager-1");
    final ChangeManager changeManagerScheme2 = TestChangeManager.of("test-change-manager-2");
    final ChangeManager changeManagerScheme3 = TestChangeManager.of("test-change-manager-3");
    _default = new InMemoryConfigMaster(new ObjectIdSupplier(UID_SCHEME_1), changeManagerScheme1);
    _delegate1 = new InMemoryConfigMaster(new ObjectIdSupplier(UID_SCHEME_2), changeManagerScheme2);
    _delegate2 = new InMemoryConfigMaster(new ObjectIdSupplier(UID_SCHEME_3), changeManagerScheme3);
    _delegates.put(UID_SCHEME_2, _delegate1);
    _delegates.put(UID_SCHEME_3, _delegate2);
  }

  /**
   * Tears down the masters and resets stored objects.
   */
  @AfterMethod
  public void tearDown() {
    _delegates.clear();
    _cfg1Scheme1.setUniqueId(null);
    _cfg2Scheme2.setUniqueId(null);
    _cfg3Scheme3.setUniqueId(null);
    _cfg4Scheme1.setUniqueId(null);
    _cfg5Scheme2.setUniqueId(null);
    _cfg6Scheme2.setUniqueId(null);
  }

  /**
   * Tests the default constructor.
   */
  @Test
  public void testDefaultConstructor() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    assertTrue(master.changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests the addition of a document when only the default delegate has been
   * added by the constructor.
   */
  public void testAddToDefaultDelegatingMaster() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default);
    final ConfigDocument doc = master.add(new ConfigDocument(_cfg1Scheme1));
    assertDocument(doc, UID_SCHEME_1, _cfg1Scheme1.getValue());
  }

  /**
   * Tests the addition of documents when no ids have been set on the documents
   * (i.e. they will use the default master).
   */
  public void testAddToDelegatingMasterNoUidsSet() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    final ConfigDocument doc1 = master.add(new ConfigDocument(_cfg1Scheme1));
    final ConfigDocument doc2 = master.add(new ConfigDocument(_cfg2Scheme2));
    final ConfigDocument doc3 = master.add(new ConfigDocument(_cfg3Scheme3));
    final ConfigDocument doc4 = master.add(new ConfigDocument(_cfg4Scheme1));
    final ConfigDocument doc5 = master.add(new ConfigDocument(_cfg5Scheme2));
    final ConfigDocument doc6 = master.add(new ConfigDocument(_cfg6Scheme2));
    assertDocument(doc1, UID_SCHEME_1, _cfg1Scheme1.getValue());
    assertDocument(doc2, UID_SCHEME_1, _cfg2Scheme2.getValue());
    assertDocument(doc3, UID_SCHEME_1, _cfg3Scheme3.getValue());
    assertDocument(doc4, UID_SCHEME_1, _cfg4Scheme1.getValue());
    assertDocument(doc5, UID_SCHEME_1, _cfg5Scheme2.getValue());
    assertDocument(doc6, UID_SCHEME_1, _cfg6Scheme2.getValue());
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
   * Tests the addition of documents when no ids have been set on the documents
   * (i.e. they will use the appropriate master).
   */
  public void testAddToDelegatingMasterUidsSet() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    ConfigDocument doc1 = new ConfigDocument(_cfg1Scheme1);
    ConfigDocument doc2 = new ConfigDocument(_cfg2Scheme2);
    ConfigDocument doc3 = new ConfigDocument(_cfg3Scheme3);
    ConfigDocument doc4 = new ConfigDocument(_cfg4Scheme1);
    ConfigDocument doc5 = new ConfigDocument(_cfg5Scheme2);
    ConfigDocument doc6 = new ConfigDocument(_cfg6Scheme2);
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
    assertDocument(doc1, UID_SCHEME_1, _cfg1Scheme1.getValue());
    assertDocument(doc2, UID_SCHEME_2, _cfg2Scheme2.getValue());
    assertDocument(doc3, UID_SCHEME_3, _cfg3Scheme3.getValue());
    assertDocument(doc4, UID_SCHEME_1, _cfg4Scheme1.getValue());
    assertDocument(doc5, UID_SCHEME_2, _cfg5Scheme2.getValue());
    assertDocument(doc6, UID_SCHEME_2, _cfg6Scheme2.getValue());
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
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.get((UniqueId) null);
  }

  /**
   * Tests getting documents by unique id.
   */
  public void testGetByUniqueId() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    final ConfigDocument doc1 = master.add(new ConfigDocument(_cfg1Scheme1));
    final ConfigDocument doc2 = master.add(new ConfigDocument(_cfg2Scheme2));
    final ConfigDocument doc3 = master.add(new ConfigDocument(_cfg3Scheme3));
    final ConfigDocument doc4 = master.add(new ConfigDocument(_cfg4Scheme1));
    final ConfigDocument doc5 = master.add(new ConfigDocument(_cfg5Scheme2));
    final ConfigDocument doc6 = master.add(new ConfigDocument(_cfg6Scheme2));
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
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.get(null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.get(_cfg1Scheme1, null);
  }

  /**
   * Tests getting documents by object id / version correction. The underlying
   * master in this case does not track versions.
   */
  public void testGetByObjectIdVersionCorrection() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    final ConfigDocument doc1 = master.add(new ConfigDocument(_cfg1Scheme1));
    final ConfigDocument doc2 = master.add(new ConfigDocument(_cfg2Scheme2));
    final ConfigDocument doc3 = master.add(new ConfigDocument(_cfg3Scheme3));
    final ConfigDocument doc4 = master.add(new ConfigDocument(_cfg4Scheme1));
    final ConfigDocument doc5 = master.add(new ConfigDocument(_cfg5Scheme2));
    final ConfigDocument doc6 = master.add(new ConfigDocument(_cfg6Scheme2));
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
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.get((Collection<UniqueId>) null);
  }

  /**
   * Tests getting documents by unique id.
   */
  public void testGetByUniqueIdCollection() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    final ConfigDocument doc1 = master.add(new ConfigDocument(_cfg1Scheme1));
    final ConfigDocument doc2 = master.add(new ConfigDocument(_cfg2Scheme2));
    final ConfigDocument doc3 = master.add(new ConfigDocument(_cfg3Scheme3));
    final ConfigDocument doc4 = master.add(new ConfigDocument(_cfg4Scheme1));
    final ConfigDocument doc5 = master.add(new ConfigDocument(_cfg5Scheme2));
    final ConfigDocument doc6 = master.add(new ConfigDocument(_cfg6Scheme2));
    final Collection<UniqueId> uids = Arrays.asList(doc1.getUniqueId(), doc2.getUniqueId(), doc3.getUniqueId(), doc4.getUniqueId(),
        doc5.getUniqueId(), doc6.getUniqueId());
    final Map<UniqueId, ConfigDocument> docs = master.get(uids);
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
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    ConfigDocument doc1 = new ConfigDocument(_cfg1Scheme1);
    ConfigDocument doc2 = new ConfigDocument(_cfg2Scheme2);
    ConfigDocument doc3 = new ConfigDocument(_cfg3Scheme3);
    ConfigDocument doc4 = new ConfigDocument(_cfg4Scheme1);
    ConfigDocument doc5 = new ConfigDocument(_cfg5Scheme2);
    ConfigDocument doc6 = new ConfigDocument(_cfg6Scheme2);
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
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.remove(null);
  }

  /**
   * Tests removing documents.
   */
  public void testRemove() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    ConfigDocument doc1 = new ConfigDocument(_cfg1Scheme1);
    ConfigDocument doc2 = new ConfigDocument(_cfg2Scheme2);
    ConfigDocument doc3 = new ConfigDocument(_cfg3Scheme3);
    ConfigDocument doc4 = new ConfigDocument(_cfg4Scheme1);
    ConfigDocument doc5 = new ConfigDocument(_cfg5Scheme2);
    ConfigDocument doc6 = new ConfigDocument(_cfg6Scheme2);
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
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.correct(null);
  }

  /**
   * Tests correcting documents.
   */
  public void testCorrect() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    ConfigDocument doc1 = new ConfigDocument(_cfg1Scheme1);
    ConfigDocument doc2 = new ConfigDocument(_cfg2Scheme2);
    ConfigDocument doc3 = new ConfigDocument(_cfg3Scheme3);
    ConfigDocument doc4 = new ConfigDocument(_cfg4Scheme1);
    ConfigDocument doc5 = new ConfigDocument(_cfg5Scheme2);
    ConfigDocument doc6 = new ConfigDocument(_cfg6Scheme2);
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
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.replaceVersion(null, Arrays.asList(new ConfigDocument(_cfg1Scheme1), new ConfigDocument(_cfg2Scheme2)));
  }

  /**
   * Tests that the replacement documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionNullDocuments() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.replaceVersion(UniqueId.of(UID_SCHEME_2, "1"), null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceVersionByUid() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    ConfigDocument doc1 = new ConfigDocument(_cfg1Scheme1);
    ConfigDocument doc2 = new ConfigDocument(_cfg2Scheme2);
    ConfigDocument doc3 = new ConfigDocument(_cfg3Scheme3);
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

    final ConfigDocument doc4 = new ConfigDocument(_cfg1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final ConfigDocument doc5 = new ConfigDocument(_cfg2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final ConfigDocument doc6 = new ConfigDocument(_cfg3Scheme3);
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
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceAllVersionsNullObjectId() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.replaceAllVersions(null, Arrays.asList(new ConfigDocument(_cfg1Scheme1), new ConfigDocument(_cfg2Scheme2)));
  }

  /**
   * Tests that the replacement documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceAllVersionsNullDocuments() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.replaceAllVersions(ObjectId.of(UID_SCHEME_2, "1"), null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceAllVersions() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    ConfigDocument doc1 = new ConfigDocument(_cfg1Scheme1);
    ConfigDocument doc2 = new ConfigDocument(_cfg2Scheme2);
    ConfigDocument doc3 = new ConfigDocument(_cfg3Scheme3);
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

    final ConfigDocument doc4 = new ConfigDocument(_cfg1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final ConfigDocument doc5 = new ConfigDocument(_cfg2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final ConfigDocument doc6 = new ConfigDocument(_cfg3Scheme3);
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
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionsNullObjectId() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.replaceVersions(null, Arrays.asList(new ConfigDocument(_cfg1Scheme1), new ConfigDocument(_cfg2Scheme2)));
  }

  /**
   * Tests that the replacement documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionsNullDocuments() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.replaceVersions(ObjectId.of(UID_SCHEME_2, "1"), null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceVersions() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    ConfigDocument doc1 = new ConfigDocument(_cfg1Scheme1);
    ConfigDocument doc2 = new ConfigDocument(_cfg2Scheme2);
    ConfigDocument doc3 = new ConfigDocument(_cfg3Scheme3);
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

    final ConfigDocument doc4 = new ConfigDocument(_cfg1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final ConfigDocument doc5 = new ConfigDocument(_cfg2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final ConfigDocument doc6 = new ConfigDocument(_cfg3Scheme3);
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
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionsNullConfigDocument() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.replaceVersion(null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceVersionByConfigDocument() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    ConfigDocument doc1 = new ConfigDocument(_cfg1Scheme1);
    ConfigDocument doc2 = new ConfigDocument(_cfg2Scheme2);
    ConfigDocument doc3 = new ConfigDocument(_cfg3Scheme3);
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

    final ConfigDocument doc4 = new ConfigDocument(_cfg1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final ConfigDocument doc5 = new ConfigDocument(_cfg2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final ConfigDocument doc6 = new ConfigDocument(_cfg3Scheme3);
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
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED));
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRemoveVersionNullId() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.removeVersion(null);
  }

  /**
   * Tests the addition of documents.
   */
  public void testAddVersion() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    ConfigDocument doc1 = new ConfigDocument(_cfg1Scheme1);
    ConfigDocument doc2 = new ConfigDocument(_cfg2Scheme2);
    ConfigDocument doc3 = new ConfigDocument(_cfg3Scheme3);
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "1000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "1000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    assertDocument(doc1, UID_SCHEME_1, _cfg1Scheme1.getValue());
    assertDocument(doc2, UID_SCHEME_2, _cfg2Scheme2.getValue());
    assertDocument(doc3, UID_SCHEME_3, _cfg3Scheme3.getValue());
    ConfigDocument doc4 = new ConfigDocument(_cfg4Scheme1);
    ConfigDocument doc5 = new ConfigDocument(_cfg5Scheme2);
    ConfigDocument doc6 = new ConfigDocument(_cfg6Scheme2);
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
    assertDocument(doc1, UID_SCHEME_1, _cfg1Scheme1.getValue());
    assertDocument(doc2, UID_SCHEME_2, _cfg2Scheme2.getValue());
    assertDocument(doc3, UID_SCHEME_3, _cfg3Scheme3.getValue());
    assertDocument(doc4, UID_SCHEME_1, _cfg4Scheme1.getValue());
    assertDocument(doc5, UID_SCHEME_2, _cfg5Scheme2.getValue());
    assertDocument(doc6, UID_SCHEME_2, _cfg6Scheme2.getValue());
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
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()), Arrays.asList(ChangeType.ADDED));
  }

  /**
   * Tests that the search request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSearchNullRequest() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.search(null);
  }

  /**
   * Searches for documents by name.
   */
  public void testSearchByNameExactMatch() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    final ConfigDocument doc1 = master.add(new ConfigDocument(_cfg1Scheme1));
    final ConfigDocument doc2 = master.add(new ConfigDocument(_cfg2Scheme2));
    final ConfigDocument doc3 = master.add(new ConfigDocument(_cfg3Scheme3));
    final ConfigDocument doc4 = master.add(new ConfigDocument(_cfg4Scheme1));
    final ConfigDocument doc5 = master.add(new ConfigDocument(_cfg5Scheme2));
    final ConfigDocument doc6 = master.add(new ConfigDocument(_cfg6Scheme2));
    final ConfigSearchRequest<Object> request1 = new ConfigSearchRequest<>();
    final ConfigSearchRequest<Object> request2 = new ConfigSearchRequest<>();
    final ConfigSearchRequest<Object> request3 = new ConfigSearchRequest<>();
    final ConfigSearchRequest<Object> request4 = new ConfigSearchRequest<>();
    final ConfigSearchRequest<Object> request5 = new ConfigSearchRequest<>();
    final ConfigSearchRequest<Object> request6 = new ConfigSearchRequest<>();
    request1.setName(_cfg1Scheme1.getName());
    request2.setName(_cfg2Scheme2.getName());
    request3.setName(_cfg3Scheme3.getName());
    request4.setName(_cfg4Scheme1.getName());
    request5.setName(_cfg5Scheme2.getName());
    request6.setName(_cfg6Scheme2.getName());
    assertEquals(master.search(request1).getSingleValue(), doc1.getConfig());
    assertEquals(master.search(request2).getSingleValue(), doc2.getConfig());
    assertEquals(master.search(request3).getSingleValue(), doc3.getConfig());
    assertEquals(master.search(request4).getSingleValue(), doc4.getConfig());
    assertEquals(master.search(request5).getSingleValue(), doc5.getConfig());
    assertEquals(master.search(request6).getSingleValue(), doc6.getConfig());
  }

  /**
   * Searches for documents by name.
   */
  public void testSearchByNameWildcard() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    final ConfigDocument doc1 = master.add(new ConfigDocument(_cfg1Scheme1));
    final ConfigDocument doc2 = master.add(new ConfigDocument(_cfg2Scheme2));
    final ConfigDocument doc3 = master.add(new ConfigDocument(_cfg3Scheme3));
    final ConfigDocument doc4 = master.add(new ConfigDocument(_cfg4Scheme1));
    final ConfigDocument doc5 = master.add(new ConfigDocument(_cfg5Scheme2));
    final ConfigDocument doc6 = master.add(new ConfigDocument(_cfg6Scheme2));
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    request.setName("Test*");
    final ConfigSearchResult<Object> result = master.search(request);
    assertEquals(result.getDocuments().size(), 6);
    assertTrue(result.getDocuments().contains(doc1));
    assertTrue(result.getDocuments().contains(doc2));
    assertTrue(result.getDocuments().contains(doc3));
    assertTrue(result.getDocuments().contains(doc4));
    assertTrue(result.getDocuments().contains(doc5));
    assertTrue(result.getDocuments().contains(doc6));
  }

  /**
   * Searches for documents by name.
   */
  public void testSearchByType() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    final ConfigDocument doc1 = master.add(new ConfigDocument(_cfg1Scheme1));
    final ConfigDocument doc2 = master.add(new ConfigDocument(_cfg2Scheme2));
    final ConfigDocument doc3 = master.add(new ConfigDocument(_cfg3Scheme3));
    final ConfigDocument doc4 = master.add(new ConfigDocument(_cfg4Scheme1));
    final ConfigDocument doc5 = master.add(new ConfigDocument(_cfg5Scheme2));
    final ConfigDocument doc6 = master.add(new ConfigDocument(_cfg6Scheme2));
    final ConfigSearchRequest<Object> request1 = new ConfigSearchRequest<>();
    final ConfigSearchRequest<Object> request2 = new ConfigSearchRequest<>();
    final ConfigSearchRequest<Object> request3 = new ConfigSearchRequest<>();
    request1.setType(ExternalId.class);
    request2.setType(ExternalIdBundle.class);
    request3.setType(ExternalIdOrBundle.class);
    final ConfigSearchResult<Object> result1 = master.search(request1);
    final ConfigSearchResult<Object> result2 = master.search(request2);
    final ConfigSearchResult<Object> result3 = master.search(request3);
    assertEquals(result1.getDocuments().size(), 3);
    assertEquals(result2.getDocuments().size(), 3);
    assertEquals(result3.getDocuments().size(), 6);
    assertTrue(result1.getDocuments().contains(doc1));
    assertTrue(result1.getDocuments().contains(doc2));
    assertTrue(result1.getDocuments().contains(doc3));
    assertTrue(result2.getDocuments().contains(doc4));
    assertTrue(result2.getDocuments().contains(doc5));
    assertTrue(result2.getDocuments().contains(doc6));
    assertTrue(result3.getDocuments().contains(doc1));
    assertTrue(result3.getDocuments().contains(doc2));
    assertTrue(result3.getDocuments().contains(doc3));
    assertTrue(result3.getDocuments().contains(doc4));
    assertTrue(result3.getDocuments().contains(doc5));
    assertTrue(result3.getDocuments().contains(doc6));
  }

  /**
   * Tests that the history request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHistoryNullRequest() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.history(null);
  }

  /**
   * Tests getting the history of a document.
   */
  public void testHistory() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    final ConfigDocument doc1 = master.add(new ConfigDocument(_cfg1Scheme1));
    final ConfigDocument doc2 = master.add(new ConfigDocument(_cfg2Scheme2));
    final ConfigDocument doc3 = master.add(new ConfigDocument(_cfg3Scheme3));
    final ConfigHistoryRequest<ExternalId> request1 = new ConfigHistoryRequest<>();
    final ConfigHistoryRequest<ExternalIdBundle> request2 = new ConfigHistoryRequest<>();
    final ConfigHistoryRequest<ExternalIdOrBundle> request3 = new ConfigHistoryRequest<>();
    final ConfigHistoryRequest<ExternalIdOrBundle> request4 = new ConfigHistoryRequest<>();
    request1.setType(ExternalId.class);
    request1.setObjectId(doc1.getObjectId());
    request2.setType(ExternalIdBundle.class);
    request2.setObjectId(doc2.getObjectId());
    request3.setType(ExternalIdOrBundle.class);
    request3.setObjectId(doc3.getObjectId());
    request4.setType(ExternalIdOrBundle.class);
    request4.setObjectId(ObjectId.of(UID_SCHEME_2, "1000"));
    final ConfigHistoryResult<ExternalId> result1 = master.history(request1);
    final ConfigHistoryResult<ExternalIdBundle> result2 = master.history(request2);
    final ConfigHistoryResult<ExternalIdOrBundle> result3 = master.history(request3);
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
   * Tests that meta data is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testMetaData() {
    final DelegatingConfigMaster master = new DelegatingConfigMaster(_default, _delegates);
    master.metaData(null);
  }

  private static void assertDocument(final ConfigDocument actualDocument, final String expectedIdScheme, final Object expectedValue) {
    assertEquals(actualDocument.getUniqueId().getScheme(), expectedIdScheme);
    assertEquals(actualDocument.getConfig().getUniqueId().getScheme(), expectedIdScheme);
    assertEquals(actualDocument.getConfig().getObjectId().getScheme(), expectedIdScheme);
    assertEquals(actualDocument.getConfig().getValue(), expectedValue);
  }
}
