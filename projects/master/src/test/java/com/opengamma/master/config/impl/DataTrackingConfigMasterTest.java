/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.config.impl;

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
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DataTrackingConfigMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class DataTrackingConfigMasterTest {
  private static final ExternalId CONFIG_1 = ExternalId.of("eid", "1");
  private static final ExternalId CONFIG_2 = ExternalId.of("eid", "2");
  private static final ExternalId CONFIG_3 = ExternalId.of("eid", "3");
  private static final String NAME_1 = "Name";
  private static final String NAME_2 = "Name";
  private static final String NAME_3 = "Other";
  private static final VersionCorrection VC_1 = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(1000));
  private static final VersionCorrection VC_2 = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(2000));
  private static final VersionCorrection VC_3 = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(3000));
  private static final ConfigItem<?> ITEM_1;
  private static final ConfigItem<?> ITEM_2;
  private static final ConfigItem<?> ITEM_3;
  static {
    ITEM_1 = ConfigItem.of(CONFIG_1, NAME_1);
    ITEM_2 = ConfigItem.of(CONFIG_2, NAME_2);
    ITEM_3 = ConfigItem.of(CONFIG_3, NAME_3);
    ITEM_1.setType(ExternalId.class);
    ITEM_2.setType(ExternalId.class);
    ITEM_3.setType(ExternalId.class);
  }
  private InMemoryConfigMaster _delegate;
  private DataTrackingConfigMaster _master;
  private UniqueId _uid1;
  private UniqueId _uid2;
  private UniqueId _uid3;

  /**
   * Sets up the masters and item uids before each method.
   */
  @BeforeMethod
  public void setUp() {
    _delegate = new InMemoryConfigMaster();
    _delegate.add(new ConfigDocument(ITEM_1));
    _delegate.add(new ConfigDocument(ITEM_2));
    _delegate.add(new ConfigDocument(ITEM_3));
    _master = new DataTrackingConfigMaster(_delegate);
    _uid1 = ITEM_1.getUniqueId();
    _uid2 = ITEM_2.getUniqueId();
    _uid3 = ITEM_3.getUniqueId();
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
    new DataTrackingConfigMaster(null);
  }

  /**
   * Tests tracking after getting by unique id.
   */
  public void testGetByUniqueId() {
    assertEquals(_master.get(_uid1).getValue(), ITEM_1);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1));
    assertEquals(_master.get(_uid2).getValue(), ITEM_2);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2));
    assertEquals(_master.get(_uid3).getValue(), ITEM_3);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2, _uid3));
  }

  /**
   * Tests tracking after getting by object id and version correction (ignored
   * by underling master for this test).
   */
  public void testGetByObjectIdVersionCorrection() {
    assertEquals(_master.get(_uid1.getObjectId(), VC_1).getValue(), ITEM_1);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1));
    assertEquals(_master.get(_uid2.getObjectId(), VC_2).getValue(), ITEM_2);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2));
    assertEquals(_master.get(_uid3.getObjectId(), VC_3).getValue(), ITEM_3);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2, _uid3));
  }

  /**
   * Tests getting a collection of unique ids.
   */
  public void testGetByUidCollection() {
    final Map<UniqueId, ConfigDocument> configs = _master.get(Arrays.asList(_uid1, _uid2, _uid3));
    assertEquals(configs.size(), 3);
    assertEqualsNoOrder(configs.keySet(), Arrays.asList(_uid1, _uid2, _uid3));
    final Set<ConfigItem<?>> items = new HashSet<>();
    for (final Map.Entry<UniqueId, ConfigDocument> entry : configs.entrySet()) {
      items.add(entry.getValue().getConfig());
    }
    assertEqualsNoOrder(items, Arrays.asList(ITEM_1, ITEM_2, ITEM_3));
  }

  /**
   * Tests the addition of documents.
   */
  public void testAddDocument() {
    final DataTrackingConfigMaster master = new DataTrackingConfigMaster(_delegate);
    assertTrue(master.getIdsAccessed().isEmpty());
    final ConfigDocument doc1 = master.add(new ConfigDocument(ITEM_1.clone()));
    final ConfigDocument doc2 = master.add(new ConfigDocument(ITEM_2.clone()));
    final ConfigDocument doc3 = master.add(new ConfigDocument(ITEM_3.clone()));
    assertEqualsNoOrder(master.getIdsAccessed(), Arrays.asList(doc1.getUniqueId(), doc2.getUniqueId(), doc3.getUniqueId()));
  }

  /**
   * Tests updating a document.
   */
  public void testUpdateDocument() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    final ConfigDocument doc = new ConfigDocument(ITEM_1);
    // clone because original document is updated
    final ConfigDocument updated = _master.update(doc.clone());
    assertNotEquals(doc, updated);
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(_master.getIdsAccessed().iterator().next(), doc.getUniqueId());
    assertEquals(_master.getIdsAccessed().iterator().next(), updated.getUniqueId());
  }

  /**
   * Tests removing a document using the object id. The object is removed but
   * the ids accessed will remain the same.
   */
  public void testRemove() {
    final DataTrackingConfigMaster master = new DataTrackingConfigMaster(_delegate);
    assertTrue(master.getIdsAccessed().isEmpty());
    master.add(new ConfigDocument(ITEM_1.clone()));
    master.add(new ConfigDocument(ITEM_2.clone()));
    master.add(new ConfigDocument(ITEM_3.clone()));
    assertEquals(master.getIdsAccessed().size(), 3);
    master.remove(ITEM_1);
    try {
      master.get(ITEM_1.getUniqueId());
      fail();
    } catch (final DataNotFoundException e) {
    } // behaviour specific to the delegate master used
    assertEquals(master.getIdsAccessed().size(), 3);
    assertEquals(master.getIdsAccessed().size(), 3);
    master.remove(ITEM_2);
    try {
      master.get(ITEM_2.getUniqueId());
      fail();
    } catch (final DataNotFoundException e) {
    } // behaviour specific to the delegate master used
    assertEquals(master.getIdsAccessed().size(), 3);
    assertEquals(master.getIdsAccessed().size(), 3);
    master.remove(ITEM_3);
    try {
      master.get(ITEM_3.getUniqueId());
      fail();
    } catch (final DataNotFoundException e) {
    } // behaviour specific to the delegate master used
    assertEquals(master.getIdsAccessed().size(), 3);
  }

  /**
   * Tests correcting a document.
   */
  public void testCorrectDocument() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    final ConfigDocument doc = new ConfigDocument(ITEM_1);
    // clone because original document is corrected
    final ConfigDocument corrected = _master.correct(doc.clone());
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
    final ConfigDocument replacement1 = new ConfigDocument(ITEM_2);
    final ConfigDocument replacement2 = new ConfigDocument(ITEM_3);
    replacement1.setUniqueId(UniqueId.of(InMemoryConfigMaster.DEFAULT_OID_SCHEME, "1000"));
    replacement1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    replacement2.setUniqueId(UniqueId.of(InMemoryConfigMaster.DEFAULT_OID_SCHEME, "2000"));
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
    final ConfigDocument replacement1 = new ConfigDocument(ITEM_2);
    final ConfigDocument replacement2 = new ConfigDocument(ITEM_3);
    replacement1.setUniqueId(UniqueId.of(InMemoryConfigMaster.DEFAULT_OID_SCHEME, "1000"));
    replacement1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    replacement2.setUniqueId(UniqueId.of(InMemoryConfigMaster.DEFAULT_OID_SCHEME, "2000"));
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
    final ConfigDocument replacement1 = new ConfigDocument(ITEM_2);
    final ConfigDocument replacement2 = new ConfigDocument(ITEM_3);
    replacement1.setUniqueId(UniqueId.of(InMemoryConfigMaster.DEFAULT_OID_SCHEME, "1000"));
    replacement1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    replacement2.setUniqueId(UniqueId.of(InMemoryConfigMaster.DEFAULT_OID_SCHEME, "2000"));
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
    assertEquals(_master.get(_uid3).getConfig().getValue(), CONFIG_3);
    final ConfigDocument doc = new ConfigDocument(ITEM_1);
    doc.setUniqueId(_uid3);
    final UniqueId replaced = _master.replaceVersion(doc.clone());
    assertEquals(doc.getUniqueId(), replaced);
    assertEquals(_master.getIdsAccessed().size(), 1);
    assertEquals(_master.getIdsAccessed().iterator().next(), doc.getUniqueId());
    assertEquals(_master.getIdsAccessed().iterator().next(), replaced.getUniqueId());
    // document for this uid has been replaced
    assertEquals(_master.get(_uid3).getConfig().getValue(), CONFIG_1);
  }

  /**
   * Tests removing a version.
   */
  public void testRemoveVersion() {
    assertEquals(_master.getIdsAccessed().size(), 0);
    assertEquals(_master.get(_uid3).getConfig().getValue(), CONFIG_3);
    final ConfigDocument doc = new ConfigDocument(ITEM_1);
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
    final ConfigDocument doc1 = new ConfigDocument(ITEM_1);
    final ConfigDocument doc2 = new ConfigDocument(ITEM_2);
    assertEquals(doc1.getConfig().getUniqueId(), _uid1);
    assertEquals(doc2.getConfig().getUniqueId(), _uid2);
    final UniqueId newUid = _master.addVersion(doc1, doc2);
    assertNotEquals(doc1.getUniqueId(), newUid);
    assertEquals(doc2.getUniqueId(), newUid);
    assertEquals(_master.getIdsAccessed().size(), 1);
  }

  /**
   * Tests searching by type.
   */
  public void testSearchByType() {
    final ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<>();
    request.setType(ExternalId.class);
    final ConfigSearchResult<ExternalId> result = _master.search(request);
    assertEquals(result.getDocuments().size(), 3);
    assertEquals(_master.getIdsAccessed().size(), 3);
  }

  /**
   * Tests searching by name.
   */
  public void testSearchByName() {
    final ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<>();
    request.setName(NAME_1);
    ConfigSearchResult<ExternalId> result = _master.search(request);
    assertEquals(result.getDocuments().size(), 2);
    assertEquals(_master.getIdsAccessed().size(), 2);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2));
    request.setName(NAME_2);
    result = _master.search(request);
    assertEquals(result.getDocuments().size(), 2);
    assertEquals(_master.getIdsAccessed().size(), 2);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2));
    request.setName(NAME_3);
    result = _master.search(request);
    assertEquals(result.getDocuments().size(), 1);
    assertEquals(_master.getIdsAccessed().size(), 3);
    assertEqualsNoOrder(_master.getIdsAccessed(), Arrays.asList(_uid1, _uid2, _uid3));
  }

  /**
   * Tests retrieval of the history.
   */
  // TODO fix this
  // public void testHistoryByTypeAndOid() {
  // final ConfigHistoryRequest<ExternalId> request = new
  // ConfigHistoryRequest<>();
  // request.setType(ExternalId.class);
  // request.setObjectId(_uid1.getObjectId());
  // final ConfigHistoryResult<ExternalId> result = _master.history(request);
  // assertEquals(result.getDocuments().size(), 1);
  // assertEquals(result.getSingleValue().getValue(), CONFIG_1);
  // assertEquals(_master.getIdsAccessed().size(), 1);
  // assertEquals(_master.getIdsAccessed().iterator().next(), _uid1);
  // }

}
