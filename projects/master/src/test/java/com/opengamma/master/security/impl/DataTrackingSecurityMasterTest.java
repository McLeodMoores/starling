/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security.impl;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DataTrackingSecurityMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class DataTrackingSecurityMasterTest {
  private static final String NAME_1 = "Name 1";
  private static final String NAME_2 = "Name 2";
  private static final String NAME_3 = "Name 1";
  private static final String TYPE_1 = "Type";
  private static final String TYPE_2 = "Type";
  private static final String TYPE_3 = "Other";
  private static final VersionCorrection VC_1 = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(1000));
  private static final VersionCorrection VC_2 = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(2000));
  private static final VersionCorrection VC_3 = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(3000));
  private static final RawSecurity SEC_1;
  private static final RawSecurity SEC_2;
  private static final RawSecurity SEC_3;
  static {
    SEC_1 = new RawSecurity(TYPE_1);
    SEC_2 = new RawSecurity(TYPE_2);
    SEC_3 = new RawSecurity(TYPE_3);
    SEC_1.setName(NAME_1);
    SEC_2.setName(NAME_2);
    SEC_3.setName(NAME_3);
  }
  private InMemorySecurityMaster _delegate;
  private DataTrackingSecurityMaster _master;
  private UniqueId _uid1;
  private UniqueId _uid2;
  private UniqueId _uid3;

  /**
   * Sets up the masters and item uids before each method.
   */
  @BeforeMethod
  public void setUp() {
    _delegate = new InMemorySecurityMaster();
    _delegate.add(new SecurityDocument(SEC_1));
    _delegate.add(new SecurityDocument(SEC_2));
    _delegate.add(new SecurityDocument(SEC_3));
    _master = new DataTrackingSecurityMaster(_delegate);
    _uid1 = SEC_1.getUniqueId();
    _uid2 = SEC_2.getUniqueId();
    _uid3 = SEC_3.getUniqueId();
    // checks the reset
    assertNotNull(_uid1);
    assertNotNull(_uid2);
    assertNotNull(_uid3);
    assertTrue(_master.getIdsAccessed().isEmpty());
  }

  /**
   * Tears down the masters after each method.
   */
  @AfterMethod
  public void tearDown() {
    _master = null;
    _delegate = null;
    _uid1 = null;
    _uid2 = null;
    _uid3 = null;
  }

  /**
   * Tests that the delegate master cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDelegate() {
    new DataTrackingSecurityMaster(null);
  }

  /**
   * Tests tracking after getting by unique id.
   */
  public void testGetByUniqueId() {
    assertEquals(_master.get(_uid1).getValue(), SEC_1);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1));
    assertEquals(_master.get(_uid2).getValue(), SEC_2);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2));
    assertEquals(_master.get(_uid3).getValue(), SEC_3);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2, _uid3));
  }

  /**
   * Tests tracking after getting by object id and version correction (ignored
   * by underling master for this test).
   */
  public void testGetByObjectIdVersionCorrection() {
    assertEquals(_master.get(_uid1.getObjectId(), VC_1).getValue(), SEC_1);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1));
    assertEquals(_master.get(_uid2.getObjectId(), VC_2).getValue(), SEC_2);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2));
    assertEquals(_master.get(_uid3.getObjectId(), VC_3).getValue(), SEC_3);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2, _uid3));
  }

  /**
   * Tests getting a collection of unique ids.
   */
  public void testGetByUidCollection() {
    final Map<UniqueId, SecurityDocument> Securitys = _master.get(Arrays.asList(_uid1, _uid2, _uid3));
    assertEquals(Securitys.size(), 3);
    assertEqualsNoOrder(Securitys.keySet(), Arrays.asList(_uid1, _uid2, _uid3));
    final Set<Security> items = new HashSet<>();
    for (final Map.Entry<UniqueId, SecurityDocument> entry : Securitys.entrySet()) {
      items.add(entry.getValue().getSecurity());
    }
    assertEqualsNoOrder(items, Arrays.asList(SEC_1, SEC_2, SEC_3));
  }

  /**
   * Tests the addition of documents.
   */
  public void testAddDocument() {
    final DataTrackingSecurityMaster master = new DataTrackingSecurityMaster(_delegate);
    assertTrue(master.getIdsAccessed().isEmpty());
    final SecurityDocument doc1 = master.add(new SecurityDocument(SEC_1.clone()));
    final SecurityDocument doc2 = master.add(new SecurityDocument(SEC_2.clone()));
    final SecurityDocument doc3 = master.add(new SecurityDocument(SEC_3.clone()));
    assertEqualsNoOrder(master.getIdsAccessed(), Arrays.asList(doc1.getUniqueId(), doc2.getUniqueId(), doc3.getUniqueId()));
  }

  /**
   * Tests updating a document.
   */
  public void testUpdateDocument() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    final SecurityDocument doc = new SecurityDocument(SEC_1);
    // clone because original document is updated
    final SecurityDocument updated = _master.update(doc.clone());
    assertNotEquals(doc, updated);
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(_master.getIdsAccessed().iterator().next(), doc.getUniqueId());
    assertEquals(_master.getIdsAccessed().iterator().next(), updated.getUniqueId());
  }

  /**
   * Tests correcting a document.
   */
  public void testCorrectDocument() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    final SecurityDocument doc = new SecurityDocument(SEC_1);
    // clone because original document is corrected
    final SecurityDocument corrected = _master.correct(doc.clone());
    assertNotEquals(doc, corrected);
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(_master.getIdsAccessed().iterator().next(), doc.getUniqueId());
    assertEquals(_master.getIdsAccessed().iterator().next(), corrected.getUniqueId());
  }

  /**
   * Tests replacing a version by unique id.
   */
  public void testReplaceVersionUid() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    final SecurityDocument replacement1 = new SecurityDocument(SEC_2);
    final SecurityDocument replacement2 = new SecurityDocument(SEC_3);
    replacement1.setUniqueId(UniqueId.of(InMemorySecurityMaster.DEFAULT_OID_SCHEME, "1000"));
    replacement1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    replacement2.setUniqueId(UniqueId.of(InMemorySecurityMaster.DEFAULT_OID_SCHEME, "2000"));
    replacement1.setVersionFromInstant(Instant.ofEpochSecond(200000));
    final List<UniqueId> replaced = _master.replaceVersion(_uid1, Arrays.asList(replacement1, replacement2));
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(replaced.size(), 1);
    // last replacement document is stored
    assertEquals(_master.getIdsAccessed().iterator().next(), replacement2.getUniqueId());
    assertEquals(replaced.iterator().next(), replacement2.getUniqueId());
  }

  /**
   * Tests replacing all versions by object id.
   */
  public void testReplaceAllVersionsOid() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    final SecurityDocument replacement1 = new SecurityDocument(SEC_2);
    final SecurityDocument replacement2 = new SecurityDocument(SEC_3);
    replacement1.setUniqueId(UniqueId.of(InMemorySecurityMaster.DEFAULT_OID_SCHEME, "1000"));
    replacement1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    replacement2.setUniqueId(UniqueId.of(InMemorySecurityMaster.DEFAULT_OID_SCHEME, "2000"));
    replacement1.setVersionFromInstant(Instant.ofEpochSecond(200000));
    final List<UniqueId> replaced = _master.replaceAllVersions(_uid1.getObjectId(), Arrays.asList(replacement1, replacement2));
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(replaced.size(), 1);
    // last replacement document is stored
    assertEquals(_master.getIdsAccessed().iterator().next(), replacement2.getUniqueId());
    assertEquals(replaced.iterator().next(), replacement2.getUniqueId());
  }

  /**
   * Tests replacing versions by object id.
   */
  public void testReplaceVersionsOid() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    final SecurityDocument replacement1 = new SecurityDocument(SEC_2);
    final SecurityDocument replacement2 = new SecurityDocument(SEC_3);
    replacement1.setUniqueId(UniqueId.of(InMemorySecurityMaster.DEFAULT_OID_SCHEME, "1000"));
    replacement1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    replacement2.setUniqueId(UniqueId.of(InMemorySecurityMaster.DEFAULT_OID_SCHEME, "2000"));
    replacement1.setVersionFromInstant(Instant.ofEpochSecond(200000));
    final List<UniqueId> replaced = _master.replaceVersions(_uid1.getObjectId(), Arrays.asList(replacement1, replacement2));
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(replaced.size(), 1);
    // last replacement document is stored
    assertEquals(_master.getIdsAccessed().iterator().next(), replacement2.getUniqueId());
    assertEquals(replaced.iterator().next(), replacement2.getUniqueId());
  }

  /**
   * Tests replacing a version.
   */
  public void testReplaceVersion() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    assertEquals(_master.get(_uid3).getSecurity(), SEC_3);
    final SecurityDocument doc = new SecurityDocument(SEC_1);
    doc.setUniqueId(_uid3);
    final UniqueId replaced = _master.replaceVersion(doc.clone());
    assertEquals(doc.getUniqueId(), replaced);
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(_master.getIdsAccessed().iterator().next(), doc.getUniqueId());
    assertEquals(_master.getIdsAccessed().iterator().next(), replaced.getUniqueId());
    // document for this uid has been replaced
    assertEquals(_master.get(_uid3).getSecurity(), SEC_1);
  }

  /**
   * Tests removing a version.
   */
  public void testRemoveVersion() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    assertEquals(_master.get(_uid3).getSecurity(), SEC_3);
    final SecurityDocument doc = new SecurityDocument(SEC_1);
    doc.setUniqueId(_uid3);
    _master.removeVersion(_uid3);
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(_master.getIdsAccessed().iterator().next(), doc.getUniqueId());
    try {
      _master.get(_uid3);
      fail();
    } catch (final DataNotFoundException e) {
    }
  }

  /**
   * Tests adding a version.
   */
  public void testAddVersionByOid() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    final SecurityDocument doc1 = new SecurityDocument(SEC_1);
    final SecurityDocument doc2 = new SecurityDocument(SEC_2);
    assertEquals(doc1.getSecurity().getUniqueId(), _uid1);
    assertEquals(doc2.getSecurity().getUniqueId(), _uid2);
    final UniqueId newUid = _master.addVersion(doc1, doc2);
    assertNotEquals(doc1.getUniqueId(), newUid);
    assertEquals(doc2.getUniqueId(), newUid);
    assertEquals(_master.getIdsAccessed().size(), 1);
  }

  /**
   * Tests searching by type.
   */
  public void testSearchByType() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType(TYPE_1);
    final SecuritySearchResult result = _master.search(request);
    assertEquals(result.getDocuments().size(), 2);
    assertEquals(_master.getIdsAccessed().size(), 2);
    assertEqualsNoOrder(result.getSecurities(), Arrays.asList(SEC_1, SEC_2));
  }

  /**
   * Tests searching by name.
   */
  public void testSearchByName() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName(NAME_1);
    SecuritySearchResult result = _master.search(request);
    assertEquals(result.getDocuments().size(), 2);
    assertEquals(_master.getIdsAccessed().size(), 2);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid3));
    request.setName(NAME_2);
    result = _master.search(request);
    assertEquals(result.getDocuments().size(), 1);
    assertEquals(_master.getIdsAccessed().size(), 3);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2, _uid3));
    request.setName(NAME_3);
    result = _master.search(request);
    assertEquals(result.getDocuments().size(), 2);
    assertEquals(_master.getIdsAccessed().size(), 3);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2, _uid3));
  }

  /**
   * Tests retrieval of the history.
   */
  public void testHistoryByTypeAndOid() {
    final SecurityHistoryRequest request = new SecurityHistoryRequest();
    request.setObjectId(_uid1.getObjectId());
    final SecurityHistoryResult result = _master.history(request);
    assertEquals(result.getDocuments().size(), 1);
    assertEquals(result.getSingleSecurity(), SEC_1);
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(_master.getIdsAccessed().iterator().next(), _uid1);
  }

}
