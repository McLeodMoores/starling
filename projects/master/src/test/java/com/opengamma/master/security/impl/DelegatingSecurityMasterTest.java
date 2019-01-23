/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security.impl;

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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.TestChangeManager;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DelegatingSecurityMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class DelegatingSecurityMasterTest {
  private static final String UID_SCHEME_1 = "uid1";
  private static final String UID_SCHEME_2 = "uid2";
  private static final String UID_SCHEME_3 = "uid3";

  private static final String TYPE_1 = "type 1";
  private static final String TYPE_2 = "type 2";
  private static final String TYPE_3 = "type 3";

  private final TestSecurity _sec1Scheme1 = new TestSecurity(TYPE_1);
  private final TestSecurity _sec2Scheme2 = new TestSecurity(TYPE_2);
  private final TestSecurity _sec3Scheme3 = new TestSecurity(TYPE_3);
  private final TestSecurity _sec4Scheme1 = new TestSecurity(TYPE_1);
  private final TestSecurity _sec5Scheme2 = new TestSecurity(TYPE_2);
  private final TestSecurity _sec6Scheme2 = new TestSecurity(TYPE_3);

  private InMemorySecurityMaster _default;
  private InMemorySecurityMaster _delegate1;
  private InMemorySecurityMaster _delegate2;
  private final Map<String, SecurityMaster> _delegates = new HashMap<>();

  /**
   * Sets up the masters.
   */
  @BeforeMethod
  public void setUp() {
    _sec1Scheme1.addExternalId(ExternalId.of("eid", "1"));
    _sec2Scheme2.addExternalId(ExternalId.of("eid", "2"));
    _sec3Scheme3.addExternalId(ExternalId.of("eid", "3"));
    _sec4Scheme1.addExternalId(ExternalId.of("eid", "4"));
    _sec5Scheme2.addExternalId(ExternalId.of("eid", "5"));
    _sec6Scheme2.addExternalId(ExternalId.of("eid", "6"));
    _sec1Scheme1.setName("Test 1");
    _sec2Scheme2.setName("Test 2");
    _sec3Scheme3.setName("Test 3");
    _sec4Scheme1.setName("Test 4");
    _sec5Scheme2.setName("Test 5");
    _sec6Scheme2.setName("Test 6");
    final ChangeManager changeManagerScheme1 = TestChangeManager.of("test-change-manager-1");
    final ChangeManager changeManagerScheme2 = TestChangeManager.of("test-change-manager-2");
    final ChangeManager changeManagerScheme3 = TestChangeManager.of("test-change-manager-3");
    _default = new InMemorySecurityMaster(new ObjectIdSupplier(UID_SCHEME_1), changeManagerScheme1);
    _delegate1 = new InMemorySecurityMaster(new ObjectIdSupplier(UID_SCHEME_2), changeManagerScheme2);
    _delegate2 = new InMemorySecurityMaster(new ObjectIdSupplier(UID_SCHEME_3), changeManagerScheme3);
    _delegates.put(UID_SCHEME_2, _delegate1);
    _delegates.put(UID_SCHEME_3, _delegate2);
  }

  /**
   * Tears down the masters and resets stored objects.
   */
  @AfterMethod
  public void tearDown() {
    _delegates.clear();
    _sec1Scheme1.setUniqueId(null);
    _sec2Scheme2.setUniqueId(null);
    _sec3Scheme3.setUniqueId(null);
    _sec4Scheme1.setUniqueId(null);
    _sec5Scheme2.setUniqueId(null);
    _sec6Scheme2.setUniqueId(null);
  }

  /**
   * Tests the default constructor.
   */
  @Test
  public void testDefaultConstructor() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    assertTrue(master.changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests the addition of a document when only the default delegate has been
   * added by the constructor.
   */
  public void testAddToDefaultDelegatingMaster() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default);
    final SecurityDocument doc = master.add(new SecurityDocument(_sec1Scheme1));
    assertDocument(doc, UID_SCHEME_1, _sec1Scheme1);
  }

  /**
   * Tests the addition of documents when no ids have been set on the documents
   * (i.e. they will use the default master).
   */
  public void testAddToDelegatingMasterNoUidsSet() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    final SecurityDocument doc1 = master.add(new SecurityDocument(_sec1Scheme1));
    final SecurityDocument doc2 = master.add(new SecurityDocument(_sec2Scheme2));
    final SecurityDocument doc3 = master.add(new SecurityDocument(_sec3Scheme3));
    final SecurityDocument doc4 = master.add(new SecurityDocument(_sec4Scheme1));
    final SecurityDocument doc5 = master.add(new SecurityDocument(_sec5Scheme2));
    final SecurityDocument doc6 = master.add(new SecurityDocument(_sec6Scheme2));
    assertDocument(doc1, UID_SCHEME_1, _sec1Scheme1);
    assertDocument(doc2, UID_SCHEME_1, _sec2Scheme2);
    assertDocument(doc3, UID_SCHEME_1, _sec3Scheme3);
    assertDocument(doc4, UID_SCHEME_1, _sec4Scheme1);
    assertDocument(doc5, UID_SCHEME_1, _sec5Scheme2);
    assertDocument(doc6, UID_SCHEME_1, _sec6Scheme2);
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    SecurityDocument doc1 = new SecurityDocument(_sec1Scheme1);
    SecurityDocument doc2 = new SecurityDocument(_sec2Scheme2);
    SecurityDocument doc3 = new SecurityDocument(_sec3Scheme3);
    SecurityDocument doc4 = new SecurityDocument(_sec4Scheme1);
    SecurityDocument doc5 = new SecurityDocument(_sec5Scheme2);
    SecurityDocument doc6 = new SecurityDocument(_sec6Scheme2);
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
    assertDocument(doc1, UID_SCHEME_1, _sec1Scheme1);
    assertDocument(doc2, UID_SCHEME_2, _sec2Scheme2);
    assertDocument(doc3, UID_SCHEME_3, _sec3Scheme3);
    assertDocument(doc4, UID_SCHEME_1, _sec4Scheme1);
    assertDocument(doc5, UID_SCHEME_2, _sec5Scheme2);
    assertDocument(doc6, UID_SCHEME_2, _sec6Scheme2);
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.get((UniqueId) null);
  }

  /**
   * Tests getting documents by unique id.
   */
  public void testGetByUniqueId() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    final SecurityDocument doc1 = master.add(new SecurityDocument(_sec1Scheme1));
    final SecurityDocument doc2 = master.add(new SecurityDocument(_sec2Scheme2));
    final SecurityDocument doc3 = master.add(new SecurityDocument(_sec3Scheme3));
    final SecurityDocument doc4 = master.add(new SecurityDocument(_sec4Scheme1));
    final SecurityDocument doc5 = master.add(new SecurityDocument(_sec5Scheme2));
    final SecurityDocument doc6 = master.add(new SecurityDocument(_sec6Scheme2));
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.get(null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.get(_sec1Scheme1, null);
  }

  /**
   * Tests getting documents by object id / version correction. The underlying
   * master in this case does not track versions.
   */
  public void testGetByObjectIdVersionCorrection() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    final SecurityDocument doc1 = master.add(new SecurityDocument(_sec1Scheme1));
    final SecurityDocument doc2 = master.add(new SecurityDocument(_sec2Scheme2));
    final SecurityDocument doc3 = master.add(new SecurityDocument(_sec3Scheme3));
    final SecurityDocument doc4 = master.add(new SecurityDocument(_sec4Scheme1));
    final SecurityDocument doc5 = master.add(new SecurityDocument(_sec5Scheme2));
    final SecurityDocument doc6 = master.add(new SecurityDocument(_sec6Scheme2));
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.get((Collection<UniqueId>) null);
  }

  /**
   * Tests getting documents by unique id.
   */
  public void testGetByUniqueIdCollection() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    final SecurityDocument doc1 = master.add(new SecurityDocument(_sec1Scheme1));
    final SecurityDocument doc2 = master.add(new SecurityDocument(_sec2Scheme2));
    final SecurityDocument doc3 = master.add(new SecurityDocument(_sec3Scheme3));
    final SecurityDocument doc4 = master.add(new SecurityDocument(_sec4Scheme1));
    final SecurityDocument doc5 = master.add(new SecurityDocument(_sec5Scheme2));
    final SecurityDocument doc6 = master.add(new SecurityDocument(_sec6Scheme2));
    final Collection<UniqueId> uids = Arrays.asList(doc1.getUniqueId(), doc2.getUniqueId(), doc3.getUniqueId(), doc4.getUniqueId(),
        doc5.getUniqueId(), doc6.getUniqueId());
    final Map<UniqueId, SecurityDocument> docs = master.get(uids);
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    SecurityDocument doc1 = new SecurityDocument(_sec1Scheme1);
    SecurityDocument doc2 = new SecurityDocument(_sec2Scheme2);
    SecurityDocument doc3 = new SecurityDocument(_sec3Scheme3);
    SecurityDocument doc4 = new SecurityDocument(_sec4Scheme1);
    SecurityDocument doc5 = new SecurityDocument(_sec5Scheme2);
    SecurityDocument doc6 = new SecurityDocument(_sec6Scheme2);
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.remove(null);
  }

  /**
   * Tests removing documents.
   */
  public void testRemove() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    SecurityDocument doc1 = new SecurityDocument(_sec1Scheme1);
    SecurityDocument doc2 = new SecurityDocument(_sec2Scheme2);
    SecurityDocument doc3 = new SecurityDocument(_sec3Scheme3);
    SecurityDocument doc4 = new SecurityDocument(_sec4Scheme1);
    SecurityDocument doc5 = new SecurityDocument(_sec5Scheme2);
    SecurityDocument doc6 = new SecurityDocument(_sec6Scheme2);
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.correct(null);
  }

  /**
   * Tests correcting documents.
   */
  public void testCorrect() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    SecurityDocument doc1 = new SecurityDocument(_sec1Scheme1);
    SecurityDocument doc2 = new SecurityDocument(_sec2Scheme2);
    SecurityDocument doc3 = new SecurityDocument(_sec3Scheme3);
    SecurityDocument doc4 = new SecurityDocument(_sec4Scheme1);
    SecurityDocument doc5 = new SecurityDocument(_sec5Scheme2);
    SecurityDocument doc6 = new SecurityDocument(_sec6Scheme2);
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.replaceVersion(null, Arrays.asList(new SecurityDocument(_sec1Scheme1), new SecurityDocument(_sec2Scheme2)));
  }

  /**
   * Tests that the replacement documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionNullDocuments() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.replaceVersion(UniqueId.of(UID_SCHEME_2, "1"), null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceVersionByUid() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    SecurityDocument doc1 = new SecurityDocument(_sec1Scheme1);
    SecurityDocument doc2 = new SecurityDocument(_sec2Scheme2);
    SecurityDocument doc3 = new SecurityDocument(_sec3Scheme3);
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

    final SecurityDocument doc4 = new SecurityDocument(_sec1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final SecurityDocument doc5 = new SecurityDocument(_sec2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final SecurityDocument doc6 = new SecurityDocument(_sec3Scheme3);
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
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceAllVersionsNullObjectId() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.replaceAllVersions(null, Arrays.asList(new SecurityDocument(_sec1Scheme1), new SecurityDocument(_sec2Scheme2)));
  }

  /**
   * Tests that the replacement documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceAllVersionsNullDocuments() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.replaceAllVersions(ObjectId.of(UID_SCHEME_2, "1"), null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceAllVersions() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    SecurityDocument doc1 = new SecurityDocument(_sec1Scheme1);
    SecurityDocument doc2 = new SecurityDocument(_sec2Scheme2);
    SecurityDocument doc3 = new SecurityDocument(_sec3Scheme3);
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

    final SecurityDocument doc4 = new SecurityDocument(_sec1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final SecurityDocument doc5 = new SecurityDocument(_sec2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final SecurityDocument doc6 = new SecurityDocument(_sec3Scheme3);
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
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionsNullObjectId() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.replaceVersions(null, Arrays.asList(new SecurityDocument(_sec1Scheme1), new SecurityDocument(_sec2Scheme2)));
  }

  /**
   * Tests that the replacement documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionsNullDocuments() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.replaceVersions(ObjectId.of(UID_SCHEME_2, "1"), null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceVersions() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    SecurityDocument doc1 = new SecurityDocument(_sec1Scheme1);
    SecurityDocument doc2 = new SecurityDocument(_sec2Scheme2);
    SecurityDocument doc3 = new SecurityDocument(_sec3Scheme3);
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

    final SecurityDocument doc4 = new SecurityDocument(_sec1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final SecurityDocument doc5 = new SecurityDocument(_sec2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final SecurityDocument doc6 = new SecurityDocument(_sec3Scheme3);
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
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc1.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme1.getUniqueOidsWithEvents().get(doc4.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc2.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertEquals(changeManagerScheme2.getUniqueOidsWithEvents().get(doc5.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
    assertNull(changeManagerScheme2.getUniqueOidsWithEvents().get(doc6.getObjectId()));
    assertEquals(changeManagerScheme3.getUniqueOidsWithEvents().get(doc3.getObjectId()), Arrays.asList(ChangeType.ADDED, ChangeType.CHANGED));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceVersionsNullSecurityDocument() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.replaceVersion(null);
  }

  /**
   * Tests replacing documents.
   */
  public void testReplaceVersionBySecurityDocument() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    SecurityDocument doc1 = new SecurityDocument(_sec1Scheme1);
    SecurityDocument doc2 = new SecurityDocument(_sec2Scheme2);
    SecurityDocument doc3 = new SecurityDocument(_sec3Scheme3);
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

    final SecurityDocument doc4 = new SecurityDocument(_sec1Scheme1);
    doc4.setVersionFromInstant(Instant.now().plusSeconds(1000));
    final SecurityDocument doc5 = new SecurityDocument(_sec2Scheme2);
    doc5.setVersionFromInstant(Instant.now().plusSeconds(2000));
    final SecurityDocument doc6 = new SecurityDocument(_sec3Scheme3);
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.removeVersion(null);
  }

  /**
   * Tests the addition of documents.
   */
  public void testAddVersion() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    SecurityDocument doc1 = new SecurityDocument(_sec1Scheme1);
    SecurityDocument doc2 = new SecurityDocument(_sec2Scheme2);
    SecurityDocument doc3 = new SecurityDocument(_sec3Scheme3);
    doc1.setUniqueId(UniqueId.of(UID_SCHEME_1, "1000"));
    doc2.setUniqueId(UniqueId.of(UID_SCHEME_2, "1000"));
    doc3.setUniqueId(UniqueId.of(UID_SCHEME_3, "1000"));
    doc1 = master.add(doc1);
    doc2 = master.add(doc2);
    doc3 = master.add(doc3);
    assertDocument(doc1, UID_SCHEME_1, _sec1Scheme1);
    assertDocument(doc2, UID_SCHEME_2, _sec2Scheme2);
    assertDocument(doc3, UID_SCHEME_3, _sec3Scheme3);
    SecurityDocument doc4 = new SecurityDocument(_sec4Scheme1);
    SecurityDocument doc5 = new SecurityDocument(_sec5Scheme2);
    SecurityDocument doc6 = new SecurityDocument(_sec6Scheme2);
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
    assertDocument(doc1, UID_SCHEME_1, _sec1Scheme1);
    assertDocument(doc2, UID_SCHEME_2, _sec2Scheme2);
    assertDocument(doc3, UID_SCHEME_3, _sec3Scheme3);
    assertDocument(doc4, UID_SCHEME_1, _sec4Scheme1);
    assertDocument(doc5, UID_SCHEME_2, _sec5Scheme2);
    assertDocument(doc6, UID_SCHEME_2, _sec6Scheme2);
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
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.search(null);
  }

  /**
   * Searches for documents by name.
   */
  public void testSearchByNameExactMatch() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    final SecurityDocument doc1 = master.add(new SecurityDocument(_sec1Scheme1));
    final SecurityDocument doc2 = master.add(new SecurityDocument(_sec2Scheme2));
    final SecurityDocument doc3 = master.add(new SecurityDocument(_sec3Scheme3));
    final SecurityDocument doc4 = master.add(new SecurityDocument(_sec4Scheme1));
    final SecurityDocument doc5 = master.add(new SecurityDocument(_sec5Scheme2));
    final SecurityDocument doc6 = master.add(new SecurityDocument(_sec6Scheme2));
    final SecuritySearchRequest request1 = new SecuritySearchRequest();
    final SecuritySearchRequest request2 = new SecuritySearchRequest();
    final SecuritySearchRequest request3 = new SecuritySearchRequest();
    final SecuritySearchRequest request4 = new SecuritySearchRequest();
    final SecuritySearchRequest request5 = new SecuritySearchRequest();
    final SecuritySearchRequest request6 = new SecuritySearchRequest();
    request1.setName(_sec1Scheme1.getName());
    request2.setName(_sec2Scheme2.getName());
    request3.setName(_sec3Scheme3.getName());
    request4.setName(_sec4Scheme1.getName());
    request5.setName(_sec5Scheme2.getName());
    request6.setName(_sec6Scheme2.getName());
    assertEquals(master.search(request1).getSingleSecurity(), doc1.getSecurity());
    assertEquals(master.search(request2).getSingleSecurity(), doc2.getSecurity());
    assertEquals(master.search(request3).getSingleSecurity(), doc3.getSecurity());
    assertEquals(master.search(request4).getSingleSecurity(), doc4.getSecurity());
    assertEquals(master.search(request5).getSingleSecurity(), doc5.getSecurity());
    assertEquals(master.search(request6).getSingleSecurity(), doc6.getSecurity());
  }

  /**
   * Searches for documents by name.
   */
  public void testSearchByNameWildcard() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    final SecurityDocument doc1 = master.add(new SecurityDocument(_sec1Scheme1));
    final SecurityDocument doc2 = master.add(new SecurityDocument(_sec2Scheme2));
    final SecurityDocument doc3 = master.add(new SecurityDocument(_sec3Scheme3));
    final SecurityDocument doc4 = master.add(new SecurityDocument(_sec4Scheme1));
    final SecurityDocument doc5 = master.add(new SecurityDocument(_sec5Scheme2));
    final SecurityDocument doc6 = master.add(new SecurityDocument(_sec6Scheme2));
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("Test*");
    final SecuritySearchResult result = master.search(request);
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
  public void testSearchDefaultDelegateByType() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    final SecurityDocument doc1 = master.add(new SecurityDocument(_sec1Scheme1));
    final SecurityDocument doc2 = master.add(new SecurityDocument(_sec2Scheme2));
    final SecurityDocument doc3 = master.add(new SecurityDocument(_sec3Scheme3));
    final SecurityDocument doc4 = master.add(new SecurityDocument(_sec4Scheme1));
    final SecurityDocument doc5 = master.add(new SecurityDocument(_sec5Scheme2));
    final SecurityDocument doc6 = master.add(new SecurityDocument(_sec6Scheme2));
    final SecuritySearchRequest request1 = new SecuritySearchRequest();
    final SecuritySearchRequest request2 = new SecuritySearchRequest();
    final SecuritySearchRequest request3 = new SecuritySearchRequest();
    request1.setSecurityType(TYPE_1);
    request2.setSecurityType(TYPE_2);
    request3.setSecurityType(TYPE_3);
    final SecuritySearchResult result1 = master.search(request1);
    final SecuritySearchResult result2 = master.search(request2);
    final SecuritySearchResult result3 = master.search(request3);
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
   * Searches for documents by name using the appropriate delegate for each id
   * scheme.
   */
  public void testSearchByType() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    final SecurityDocument doc1 = master.add(new SecurityDocument(_sec1Scheme1));
    final SecurityDocument doc2 = master.add(new SecurityDocument(_sec2Scheme2));
    final SecurityDocument doc3 = master.add(new SecurityDocument(_sec3Scheme3));
    final SecurityDocument doc4 = master.add(new SecurityDocument(_sec4Scheme1));
    final SecurityDocument doc5 = master.add(new SecurityDocument(_sec5Scheme2));
    final SecurityDocument doc6 = master.add(new SecurityDocument(_sec6Scheme2));
    final SecuritySearchRequest request1 = new SecuritySearchRequest();
    final SecuritySearchRequest request2 = new SecuritySearchRequest();
    final SecuritySearchRequest request3 = new SecuritySearchRequest();
    request1.setSecurityType(TYPE_1);
    request1.setUniqueIdScheme(UID_SCHEME_1);
    request2.setSecurityType(TYPE_2);
    request2.setUniqueIdScheme(UID_SCHEME_2);
    request3.setSecurityType(TYPE_3);
    request3.setUniqueIdScheme(UID_SCHEME_3);
    SecuritySearchResult result1 = master.search(request1);
    SecuritySearchResult result2 = master.search(request2);
    SecuritySearchResult result3 = master.search(request3);
    assertEquals(result1.getDocuments().size(), 2);
    assertEquals(result2.getDocuments().size(), 0);
    assertEquals(result3.getDocuments().size(), 0);
    assertTrue(result1.getDocuments().contains(doc1));
    assertTrue(result1.getDocuments().contains(doc4));
    request1.setUniqueIdScheme(UID_SCHEME_2);
    request2.setUniqueIdScheme(UID_SCHEME_3);
    request3.setUniqueIdScheme(UID_SCHEME_1);
    result1 = master.search(request1);
    result2 = master.search(request2);
    result3 = master.search(request3);
    assertEquals(result1.getDocuments().size(), 0);
    assertEquals(result2.getDocuments().size(), 0);
    assertEquals(result3.getDocuments().size(), 2);
    assertTrue(result3.getDocuments().contains(doc3));
    assertTrue(result3.getDocuments().contains(doc6));
    request1.setUniqueIdScheme(UID_SCHEME_3);
    request2.setUniqueIdScheme(UID_SCHEME_1);
    request3.setUniqueIdScheme(UID_SCHEME_2);
    result1 = master.search(request1);
    result2 = master.search(request2);
    result3 = master.search(request3);
    assertEquals(result1.getDocuments().size(), 0);
    assertEquals(result2.getDocuments().size(), 2);
    assertEquals(result3.getDocuments().size(), 0);
    assertTrue(result2.getDocuments().contains(doc2));
    assertTrue(result2.getDocuments().contains(doc5));
  }

  /**
   * Tests that the history request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHistoryNullRequest() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.history(null);
  }

  /**
   * Tests getting the history of a document.
   */
  public void testHistory() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    final SecurityDocument doc1 = master.add(new SecurityDocument(_sec1Scheme1));
    final SecurityDocument doc2 = master.add(new SecurityDocument(_sec2Scheme2));
    final SecurityDocument doc3 = master.add(new SecurityDocument(_sec3Scheme3));
    final SecurityHistoryRequest request1 = new SecurityHistoryRequest();
    final SecurityHistoryRequest request2 = new SecurityHistoryRequest();
    final SecurityHistoryRequest request3 = new SecurityHistoryRequest();
    final SecurityHistoryRequest request4 = new SecurityHistoryRequest();
    request1.setObjectId(doc1.getObjectId());
    request2.setObjectId(doc2.getObjectId());
    request3.setObjectId(doc3.getObjectId());
    request4.setObjectId(ObjectId.of(UID_SCHEME_2, "1000"));
    final SecurityHistoryResult result1 = master.history(request1);
    final SecurityHistoryResult result2 = master.history(request2);
    final SecurityHistoryResult result3 = master.history(request3);
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
   * Tests the retrieval of meta data. The underlying masters only support
   * searching by type.
   */
  @Test
  public void testMetaData() {
    final DelegatingSecurityMaster master = new DelegatingSecurityMaster(_default, _delegates);
    master.add(new SecurityDocument(_sec1Scheme1));
    master.add(new SecurityDocument(_sec2Scheme2));
    master.add(new SecurityDocument(_sec3Scheme3));
    master.add(new SecurityDocument(_sec4Scheme1));
    master.add(new SecurityDocument(_sec5Scheme2));
    master.add(new SecurityDocument(_sec6Scheme2));
    final SecurityMetaDataRequest request = new SecurityMetaDataRequest();
    request.setUniqueIdScheme(UID_SCHEME_1);
    request.setSecurityTypes(false);
    SecurityMetaDataResult metaData = master.metaData(request);
    assertTrue(metaData.getSecurityTypes().isEmpty());
    request.setUniqueIdScheme(null);
    request.setSecurityTypes(true);
    metaData = master.metaData(request);
    assertEqualsNoOrder(metaData.getSecurityTypes(), Arrays.asList(TYPE_1, TYPE_2, TYPE_3));
  }

  private static void assertDocument(final SecurityDocument actualDocument, final String expectedIdScheme, final Object expectedValue) {
    assertEquals(actualDocument.getUniqueId().getScheme(), expectedIdScheme);
    assertEquals(actualDocument.getSecurity(), expectedValue);
  }

  private static class TestSecurity extends RawSecurity implements ObjectIdentifiable {
    private static final long serialVersionUID = 1L;

    public TestSecurity(final String securityType) {
      super(securityType);
    }

    @Override
    public ObjectId getObjectId() {
      return getUniqueId() == null ? null : getUniqueId().getObjectId();
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof TestSecurity)) {
        return false;
      }
      return super.equals(o);
    }
  }
}
