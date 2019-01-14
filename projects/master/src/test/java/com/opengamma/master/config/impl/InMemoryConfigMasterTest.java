/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.TestChangeListener;
import com.opengamma.master.TestChangeManager;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryConfigMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryConfigMasterTest {

  private static final UniqueId OTHER_UID = UniqueId.of("U", "1");
  private static final ExternalId VAL1 = ExternalId.of("Test", "config1");
  private static final ExternalId VAL2 = ExternalId.of("Test", "config2");
  private static final ExternalIdBundle VAL3 = ExternalIdBundle.of(VAL1);
  private static final ExternalIdBundle VAL4 = ExternalIdBundle.of(VAL2);

  private ConfigMaster _testEmpty;
  private ConfigMaster _testPopulated;
  private ConfigItem<Object> _item1;
  private ConfigItem<Object> _item2;
  private ConfigItem<Object> _item3;
  private ConfigItem<Object> _item4;

  /**
   * Sets up the master.
   */
  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void setUp() {
    final TestChangeListener populatedChangeListener = TestChangeListener.of();
    final TestChangeManager populatedChangeManager = TestChangeManager.of("populated-change-manager");
    populatedChangeManager.addChangeListener(populatedChangeListener);
    final TestChangeListener emptyChangeListener = TestChangeListener.of();
    final TestChangeManager emptyChangeManager = TestChangeManager.of("empty-change-manager");
    emptyChangeManager.addChangeListener(emptyChangeListener);
    _testEmpty = new InMemoryConfigMaster(new ObjectIdSupplier("Test"), emptyChangeManager);
    _testPopulated = new InMemoryConfigMaster(new ObjectIdSupplier("Test"), populatedChangeManager);
    _item1 = ConfigItem.<Object> of(VAL1);
    _item1.setName("ONE");
    _item1 = (ConfigItem<Object>) _testPopulated.add(new ConfigDocument(_item1)).getConfig();
    _item2 = ConfigItem.<Object> of(VAL2);
    _item2.setName("TWO");
    _item2 = (ConfigItem<Object>) _testPopulated.add(new ConfigDocument(_item2)).getConfig();
    _item3 = ConfigItem.<Object> of(VAL3);
    _item3.setName("THREE");
    _item3 = (ConfigItem<Object>) _testPopulated.add(new ConfigDocument(_item3)).getConfig();
    _item4 = ConfigItem.<Object> of(VAL4);
    _item4.setName("FOUR");
    _item4 = (ConfigItem<Object>) _testPopulated.add(new ConfigDocument(_item4)).getConfig();
  }

  /**
   * Tears down the master.
   */
  @AfterMethod
  public void tearDown() {
    _testEmpty = null;
    _testPopulated = null;
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the change manager cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullChangeManager1() {
    new InMemoryConfigMaster((ChangeManager) null);
  }

  /**
   * Tests that the change manager cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullChangeManager2() {
    new InMemoryConfigMaster(new ObjectIdSupplier("Test"), null);
  }

  /**
   * Tests that the object id supplier cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullSupplier1() {
    new InMemoryConfigMaster((Supplier<ObjectId>) null);
  }

  /**
   * Tests that the object id supplier cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullSupplier2() {
    new InMemoryConfigMaster(null, new BasicChangeManager());
  }

  /**
   * Tests the default object id supplier.
   */
  @SuppressWarnings("unchecked")
  public void testDefaultSupplier() {
    final InMemoryConfigMaster master = new InMemoryConfigMaster();
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setName("ONE");
    final ConfigItem<ExternalId> added = (ConfigItem<ExternalId>) master.add(new ConfigDocument(item)).getConfig();
    assertEquals(added.getUniqueId().getScheme(), "MemCfg");
  }

  /**
   * Tests that the provided object id supplier is used.
   */
  @SuppressWarnings("unchecked")
  public void testAlternateSupplier() {
    final InMemoryConfigMaster master = new InMemoryConfigMaster(new ObjectIdSupplier("Hello"));
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setName("ONE");
    final ConfigItem<ExternalId> added = (ConfigItem<ExternalId>) master.add(new ConfigDocument(item)).getConfig();
    assertEquals(added.getUniqueId().getScheme(), "Hello");
  }

  /**
   * Tests the default change manager.
   */
  public void testDefaultChangeManager() {
    final InMemoryConfigMaster master = new InMemoryConfigMaster(new ObjectIdSupplier("Test"));
    final ChangeManager changeManager = master.changeManager();
    assertTrue(changeManager instanceof BasicChangeManager);
  }

  /**
   * Tests that the provided change manager is used.
   */
  public void testChangeManager() {
    final String name = "test-change-manager";
    final ChangeManager changeManager = TestChangeManager.of(name);
    final InMemoryConfigMaster master = new InMemoryConfigMaster(changeManager);
    assertTrue(master.changeManager() instanceof TestChangeManager);
    assertEquals(((TestChangeManager) changeManager).getName(), name);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests searching for a single identifier that cannot be found in the master.
   */
  public void testSearchOneIdNoMatch() {
    final ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<>();
    request.addConfigId(ObjectId.of("A", "UNREAL"));
    final ConfigSearchResult<ExternalId> result = _testPopulated.search(request);
    assertEquals(result.getDocuments().size(), 0);
  }

  /**
   * Tests searching for a single identifier that can be found in the master.
   */
  public void testSearchOneId() {
    final ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<>();
    request.addConfigId(_item2.getObjectId());
    final ConfigSearchResult<ExternalId> result = _testPopulated.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(result.getFirstDocument().getConfig(), _item2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that searching an empty master returns empty results.
   */
  public void testSearchEmptyMaster() {
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    final ConfigSearchResult<Object> result = _testEmpty.search(request);
    assertEquals(result.getPaging().getTotalItems(), 0);
    assertEquals(result.getDocuments().size(), 0);
  }

  /**
   * Tests that an empty search will return all values in the master. The
   * document types will match because the default type for the request is
   * Object.
   */
  public void testSearchPopulatedMasterAll() {
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    final ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(result.getPaging().getTotalItems(), 4);
    final List<ConfigDocument> docs = result.getDocuments();
    final Set<ConfigItem<?>> items = new HashSet<>();
    for (final ConfigDocument doc : docs) {
      items.add(doc.getConfig());
    }
    assertEquals(items.size(), 4);
    assertTrue(items.contains(_item1));
    assertTrue(items.contains(_item2));
    assertTrue(items.contains(_item3));
    assertTrue(items.contains(_item4));
  }

  /**
   * Tests that the search results are filtered by name.
   */
  public void testSearchPopulatedMasterFilterByName() {
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    request.setName("ONE");
    final ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(result.getPaging().getTotalItems(), 1);
    assertEquals(result.getDocuments().size(), 1);
    assertTrue(result.getValues().contains(_item1));
  }

  /**
   * Tests that the search results are filtered by type.
   */
  public void testSearchPopulatedMasterFilterByType() {
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    request.setType(ExternalId.class);
    final ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(result.getPaging().getTotalItems(), 2);
    assertEquals(result.getDocuments().size(), 2);
    assertTrue(result.getValues().contains(_item1));
    assertTrue(result.getValues().contains(_item2));
  }

  /**
   * Tests that searches ignore the version of an item.
   */
  public void testSearchPopulatedMasterByVersion() {
    final ConfigItem<Object> item = _item1.clone();
    final ConfigDocument versionedFrom = new ConfigDocument(item);
    versionedFrom.setVersionFromInstant(Instant.ofEpochSecond(1000));
    final InMemoryConfigMaster master = new InMemoryConfigMaster();
    master.add(versionedFrom);
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(500)));
    master.search(request);
    final ConfigSearchResult<Object> result = master.search(request);
    assertEquals(result.getPaging().getTotalItems(), 1);
    assertEquals(result.getDocuments().size(), 1);
    assertTrue(result.getValues().contains(item));
  }

  /**
   * Tests that searches ignore the correction of an item.
   */
  public void testSearchPopulatedMasterByCorrection() {
    final ConfigItem<Object> item = _item1.clone();
    final ConfigDocument versionedFrom = new ConfigDocument(item);
    versionedFrom.setCorrectionFromInstant(Instant.ofEpochSecond(1000));
    final InMemoryConfigMaster master = new InMemoryConfigMaster();
    master.add(versionedFrom);
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    request.setVersionCorrection(VersionCorrection.ofCorrectedTo(Instant.ofEpochSecond(500)));
    master.search(request);
    final ConfigSearchResult<Object> result = master.search(request);
    assertEquals(result.getPaging().getTotalItems(), 1);
    assertEquals(result.getDocuments().size(), 1);
    assertTrue(result.getValues().contains(item));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the behaviour when there is no value for an id in the master.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetByUidEmptyMaster() {
    _testEmpty.get(OTHER_UID);
  }

  /**
   * Tests the behaviour when there is no value for an id in the master.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetByOidVersionEmptyMaster2() {
    _testEmpty.get(OTHER_UID, VersionCorrection.LATEST);
  }

  /**
   * Tests the behaviour when there is no value for any id in the master. Note
   * that an exception is not thrown if no documents are found.
   */
  @Test
  public void testGetCollectionEmptyMaster() {
    final UniqueId uid = UniqueId.of("U", "2");
    final Map<UniqueId, ConfigDocument> results = _testEmpty.get(Arrays.asList(OTHER_UID, uid));
    assertEquals(results.size(), 2);
    assertTrue(results.containsKey(OTHER_UID));
    assertTrue(results.containsKey(uid));
    assertNull(results.get(OTHER_UID));
    assertNull(results.get(uid));
  }

  /**
   * Tests getting documents by unique id.
   */
  public void testGetByUidPopulatedMaster() {
    assertSame(_testPopulated.get(_item1.getUniqueId()).getConfig(), _item1);
    assertSame(_testPopulated.get(_item2.getUniqueId()).getConfig(), _item2);
    assertSame(_testPopulated.get(_item3.getUniqueId()).getConfig(), _item3);
    assertSame(_testPopulated.get(_item4.getUniqueId()).getConfig(), _item4);
  }

  /**
   * Tests getting documents by object id and (ignored) version.
   */
  public void testGetByOidVersionPopulatedMaster() {
    final VersionCorrection vc = VersionCorrection.of(Instant.ofEpochSecond(1000), Instant.ofEpochSecond(2000));
    assertSame(_testPopulated.get(_item1.getUniqueId().getObjectId(), vc).getConfig(), _item1);
    assertSame(_testPopulated.get(_item2.getUniqueId().getObjectId(), vc).getConfig(), _item2);
    assertSame(_testPopulated.get(_item3.getUniqueId().getObjectId(), vc).getConfig(), _item3);
    assertSame(_testPopulated.get(_item4.getUniqueId().getObjectId(), vc).getConfig(), _item4);
  }

  /**
   * Tests getting multiple documents in one request.
   */
  public void testGetCollectionPopulatedMaster() {
    final Collection<UniqueId> uids = Arrays.asList(_item1.getUniqueId(), _item2.getUniqueId(), _item3.getUniqueId(), _item4.getUniqueId());
    final Map<UniqueId, ConfigDocument> result = _testPopulated.get(uids);
    assertEquals(result.size(), 4);
    assertSame(result.get(_item1.getUniqueId()).getConfig(), _item1);
    assertSame(result.get(_item2.getUniqueId()).getConfig(), _item2);
    assertSame(result.get(_item3.getUniqueId()).getConfig(), _item3);
    assertSame(result.get(_item4.getUniqueId()).getConfig(), _item4);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests adding a document to an empty master.
   */
  public void testAddEmptyMaster() {
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setName("Test");
    final ConfigDocument doc = _testEmpty.add(new ConfigDocument(item));
    assertNotNull(doc.getVersionFromInstant());
    assertEquals(doc.getUniqueId().getScheme(), "Test");
    assertSame(doc.getConfig().getValue(), VAL1);
    final TestChangeManager changeManager = (TestChangeManager) _testEmpty.changeManager();
    assertEquals(changeManager.getAllCurrentListeners().size(), 1);
    final TestChangeListener changeListener = (TestChangeListener) changeManager.getAllCurrentListeners().get(0);
    final List<ChangeType> changes = changeListener.getChangeType(doc.getObjectId());
    assertEquals(changes.size(), 1);
    assertEquals(changes.get(0), ChangeType.ADDED);
  }

  /**
   * Tests the exception when adding a new version of a document to the master.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testAddVersionEmptyMaster() {
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setName("Test");
    final ConfigDocument doc = new ConfigDocument(item);
    final ObjectId oid = ObjectId.of("oid", "5");
    doc.setUniqueId(oid.atLatestVersion());
    _testEmpty.addVersion(doc.getObjectId(), doc);
  }

  /**
   * Tests adding a document to a populated master.
   */
  public void testAddPopulatedMaster() {
    final TestChangeManager changeManager = (TestChangeManager) _testPopulated.changeManager();
    assertEquals(changeManager.getAllCurrentListeners().size(), 1);
    final TestChangeListener changeListener = (TestChangeListener) changeManager.getAllCurrentListeners().get(0);
    // 4 values have been added so there have been 4 events
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setName("Test");
    final ConfigDocument doc = _testPopulated.add(new ConfigDocument(item));
    assertNotNull(doc.getVersionFromInstant());
    assertEquals(doc.getUniqueId().getScheme(), "Test");
    assertSame(doc.getConfig().getValue(), VAL1);
    final List<ChangeType> changes = changeListener.getChangeType(doc.getObjectId());
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 5);
    assertEquals(changes.size(), 1);
    assertEquals(changes.get(0), ChangeType.ADDED);
  }

  /**
   * Tests adding a new version of a document to a populated master.
   */
  public void testAddVersionExistingPopulatedMaster() {
    final TestChangeManager changeManager = (TestChangeManager) _testPopulated.changeManager();
    assertEquals(changeManager.getAllCurrentListeners().size(), 1);
    final TestChangeListener changeListener = (TestChangeListener) changeManager.getAllCurrentListeners().get(0);
    // 4 values have been added so there have been 4 events
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setName("Test");
    final ConfigDocument doc = _testPopulated.add(new ConfigDocument(item));
    // adding with an earlier version
    final Instant versionFrom = Instant.ofEpochSecond(1000);
    final Instant versionTo = Instant.ofEpochSecond(2000);
    final Instant correctionFrom = Instant.ofEpochSecond(1500);
    final Instant correctionTo = Instant.ofEpochSecond(2000);
    doc.setVersionFromInstant(versionFrom);
    doc.setVersionToInstant(versionTo);
    doc.setCorrectionFromInstant(correctionFrom);
    doc.setCorrectionToInstant(correctionTo);
    _testPopulated.addVersion(doc.getObjectId(), doc);
    assertEquals(doc.getUniqueId().getScheme(), "Test");
    assertSame(doc.getConfig().getValue(), VAL1);
    // version is unchanged, correction is set to now() by replace
    assertEquals(doc.getVersionFromInstant(), versionFrom);
    assertEquals(doc.getVersionToInstant(), versionTo);
    assertTrue(doc.getCorrectionFromInstant().isAfter(correctionFrom));
    assertNull(doc.getCorrectionToInstant());
    final List<ChangeType> changes = changeListener.getChangeType(doc.getObjectId());
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 5);
    assertEquals(changes.size(), 1);
    assertEquals(changes.get(0), ChangeType.ADDED);
  }

  /**
   * Tests adding a document to a populated master.
   */
  public void testAddExistingPopulatedMaster() {
    final TestChangeManager changeManager = (TestChangeManager) _testPopulated.changeManager();
    assertEquals(changeManager.getAllCurrentListeners().size(), 1);
    final TestChangeListener changeListener = (TestChangeListener) changeManager.getAllCurrentListeners().get(0);
    // 4 values have been added so there have been 4 events
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    final ConfigDocument doc = _testPopulated.add(new ConfigDocument(_item1));
    assertNotNull(doc.getVersionFromInstant());
    assertEquals(doc.getUniqueId().getScheme(), "Test");
    assertSame(doc.getConfig().getValue(), VAL1);
    final List<ChangeType> changes = changeListener.getChangeType(doc.getObjectId());
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 5);
    assertEquals(changes.size(), 1);
    assertEquals(changes.get(0), ChangeType.ADDED);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the behaviour when trying to update a document that does not exist in
   * the master.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testUpdateEmptyMaster() {
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setUniqueId(OTHER_UID);
    _testEmpty.update(new ConfigDocument(item));
  }

  /**
   * Tests the update of a document.
   */
  public void testUpdatePopulatedMaster() {
    final TestChangeManager changeManager = (TestChangeManager) _testPopulated.changeManager();
    assertEquals(changeManager.getAllCurrentListeners().size(), 1);
    final TestChangeListener changeListener = (TestChangeListener) changeManager.getAllCurrentListeners().get(0);
    // 4 values have been added so there have been 4 events
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setUniqueId(_item1.getUniqueId());
    final ConfigDocument doc = new ConfigDocument(item);
    final ConfigDocument updated = _testPopulated.update(doc);
    assertTrue(_item1.getUniqueId().getScheme().equals(updated.getUniqueId().getScheme()));
    assertTrue(_item1.getUniqueId().getValue().equals(updated.getUniqueId().getValue()));
    assertFalse(_item1.getUniqueId().getVersion().equals(updated.getUniqueId().getVersion()));
    assertNotNull(updated.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
    final List<ChangeType> changes = changeListener.getChangeType(doc.getObjectId());
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    assertEquals(changes.size(), 2);
    assertEquals(changes.get(0), ChangeType.ADDED);
    assertEquals(changes.get(1), ChangeType.CHANGED);
  }

  /**
   * Tests the behaviour when trying to correct a document that does not exist
   * in the master. In this case, the method simply calls update().
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testCorrectEmptyMaster() {
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setUniqueId(OTHER_UID);
    _testEmpty.correct(new ConfigDocument(item));
  }

  /**
   * Tests the update of a document. In this case, the method simply calls
   * update().
   */
  public void testCorrectPopulatedMaster() {
    final TestChangeManager changeManager = (TestChangeManager) _testPopulated.changeManager();
    assertEquals(changeManager.getAllCurrentListeners().size(), 1);
    final TestChangeListener changeListener = (TestChangeListener) changeManager.getAllCurrentListeners().get(0);
    // 4 values have been added so there have been 4 events
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    final ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setUniqueId(_item1.getUniqueId());
    final ConfigDocument doc = new ConfigDocument(item);
    final ConfigDocument updated = _testPopulated.correct(doc);
    assertTrue(_item1.getUniqueId().getScheme().equals(updated.getUniqueId().getScheme()));
    assertTrue(_item1.getUniqueId().getValue().equals(updated.getUniqueId().getValue()));
    assertFalse(_item1.getUniqueId().getVersion().equals(updated.getUniqueId().getVersion()));
    assertNotNull(updated.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
    final List<ChangeType> changes = changeListener.getChangeType(doc.getObjectId());
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    assertEquals(changes.size(), 2);
    assertEquals(changes.get(0), ChangeType.ADDED);
    assertEquals(changes.get(1), ChangeType.CHANGED);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the removal of an item that is not stored.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testRemoveEmptyMaster() {
    _testEmpty.remove(OTHER_UID);
  }

  /**
   * Tests the removal of an item that is not stored.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testRemoveVersionEmptyMaster() {
    _testEmpty.removeVersion(OTHER_UID);
  }

  /**
   * Tests removal of an item stored in the master.
   */
  public void testRemovePopulatedMaster() {
    final TestChangeManager changeManager = (TestChangeManager) _testPopulated.changeManager();
    assertEquals(changeManager.getAllCurrentListeners().size(), 1);
    final TestChangeListener changeListener = (TestChangeListener) changeManager.getAllCurrentListeners().get(0);
    // 4 values have been added so there have been 4 events
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    _testPopulated.remove(_item1.getUniqueId());
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    final ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(result.getPaging().getTotalItems(), 3);
    final List<ConfigDocument> docs = result.getDocuments();
    final Set<ConfigItem<?>> items = new HashSet<>();
    for (final ConfigDocument doc : docs) {
      items.add(doc.getConfig());
    }
    assertEquals(items.size(), 3);
    assertTrue(items.contains(_item2));
    assertTrue(items.contains(_item3));
    assertTrue(items.contains(_item4));
    final List<ChangeType> changes = changeListener.getChangeType(_item1.getUniqueId().getObjectId());
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    assertEquals(changes.size(), 2);
    assertEquals(changes.get(0), ChangeType.ADDED);
    assertEquals(changes.get(1), ChangeType.REMOVED);
  }

  /**
   * Tests removal of a version of an item stored in the master. This will
   * remove all documents matching the unique id.
   */
  public void testRemoveVersionPopulatedMaster() {
    final TestChangeManager changeManager = (TestChangeManager) _testPopulated.changeManager();
    assertEquals(changeManager.getAllCurrentListeners().size(), 1);
    final TestChangeListener changeListener = (TestChangeListener) changeManager.getAllCurrentListeners().get(0);
    // 4 values have been added so there have been 4 events
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    // add more versions of the item to be removed
    final ConfigDocument docV1 = _testPopulated.get(_item1.getUniqueId());
    final ConfigDocument docV2 = new ConfigDocument(_item1);
    docV2.setVersionFromInstant(docV1.getVersionFromInstant().plusSeconds(1000));
    final ConfigDocument docV3 = new ConfigDocument(_item1);
    docV3.setVersionFromInstant(docV1.getVersionFromInstant().plusSeconds(3000));
    _testPopulated.addVersion(_item1.getUniqueId(), docV2);
    _testPopulated.addVersion(_item1.getUniqueId(), docV3);
    // remove all versions
    _testPopulated.removeVersion(_item1.getUniqueId());
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    final ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(result.getPaging().getTotalItems(), 3);
    final List<ConfigDocument> docs = result.getDocuments();
    final Set<ConfigItem<?>> items = new HashSet<>();
    for (final ConfigDocument doc : docs) {
      items.add(doc.getConfig());
    }
    assertEquals(items.size(), 3);
    assertTrue(items.contains(_item2));
    assertTrue(items.contains(_item3));
    assertTrue(items.contains(_item4));
    final List<ChangeType> changes = changeListener.getChangeType(_item1.getUniqueId().getObjectId());
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    assertEquals(changes.size(), 2);
    assertEquals(changes.get(0), ChangeType.ADDED);
    assertEquals(changes.get(1), ChangeType.REMOVED);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests the replacement of an item that is not stored.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testReplaceVersionEmptyMaster() {
    _testEmpty.replaceVersion(new ConfigDocument(_item1));
  }

  /**
   * Tests replacement of a version of an item stored in the master. This will
   * replace all documents matching the unique id.
   */
  public void testReplaceVersionPopulatedMaster() {
    final TestChangeManager changeManager = (TestChangeManager) _testPopulated.changeManager();
    assertEquals(changeManager.getAllCurrentListeners().size(), 1);
    final TestChangeListener changeListener = (TestChangeListener) changeManager.getAllCurrentListeners().get(0);
    // 4 values have been added so there have been 4 events
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    // add more versions of the item to be removed
    final ConfigDocument docV1 = _testPopulated.get(_item1.getUniqueId());
    final ConfigDocument docV2 = new ConfigDocument(_item1);
    docV2.setVersionFromInstant(docV1.getVersionFromInstant().plusSeconds(1000));
    final ConfigDocument docV3 = new ConfigDocument(_item1);
    docV3.setVersionFromInstant(docV1.getVersionFromInstant().plusSeconds(3000));
    _testPopulated.addVersion(_item1.getUniqueId(), docV2);
    _testPopulated.addVersion(_item1.getUniqueId(), docV3);
    // remove all versions
    final UniqueId replacedId = _testPopulated.replaceVersion(docV1);
    assertEquals(replacedId, docV1.getUniqueId());
    final ConfigSearchRequest<Object> request = new ConfigSearchRequest<>();
    final ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(result.getPaging().getTotalItems(), 4);
    final List<ConfigDocument> docs = result.getDocuments();
    final Set<ConfigItem<?>> items = new HashSet<>();
    for (final ConfigDocument doc : docs) {
      items.add(doc.getConfig());
    }
    assertTrue(items.contains(_item1));
    assertTrue(items.contains(_item2));
    assertTrue(items.contains(_item3));
    assertTrue(items.contains(_item4));
    final List<ChangeType> changes = changeListener.getChangeType(_item1.getUniqueId().getObjectId());
    assertEquals(changeManager.getUniqueOidsWithEvents().size(), 4);
    assertEquals(changes.size(), 1);
    assertEquals(changes.get(0), ChangeType.ADDED);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests the meta data for all values in the master. In this case, two objects
   * are stored by ExternalId and two by ExternalIdBundle, so the expected
   * number of types is two.
   */
  public void testMetaData() {
    final ConfigMetaDataResult test = _testPopulated.metaData(new ConfigMetaDataRequest());
    assertNotNull(test);
    assertEquals(test.getConfigTypes().size(), 2);
    assertTrue(test.getConfigTypes().contains(ExternalId.class));
    assertTrue(test.getConfigTypes().contains(ExternalIdBundle.class));
  }

  /**
   * Tests the meta data when the types are not requested. In this case, an
   * empty result is returned.
   */
  public void testMetaDataNoTypes() {
    final ConfigMetaDataRequest request = new ConfigMetaDataRequest();
    request.setConfigTypes(false);
    final ConfigMetaDataResult test = _testPopulated.metaData(request);
    assertNotNull(test);
    assertEquals(test.getConfigTypes().size(), 0);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests getting the history of a document from an empty master.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testHistoryEmptyMaster() {
    _testEmpty.history(new ConfigHistoryRequest<>(_item1, Object.class));
  }

  /**
   * Tests getting the history of a document from a populated master. Versioning
   * is ignored so requests will return a single value - the last version of the
   * document.
   */
  public void testHistoryPopulatedMaster() {
    _testPopulated.update(new ConfigDocument(_item2));
    _testPopulated.update(new ConfigDocument(_item2));
    _testPopulated.update(new ConfigDocument(_item2));
    final ConfigDocument doc4 = _testPopulated.update(new ConfigDocument(_item2));
    final ConfigHistoryRequest<Object> request = new ConfigHistoryRequest<>(_item2, Object.class);
    final ConfigHistoryRequest<Object> versionedRequest = new ConfigHistoryRequest<>(_item2.getObjectId(), Instant.ofEpochSecond(100),
        Instant.ofEpochSecond(200));
    final ConfigHistoryResult<Object> result = _testPopulated.history(request);
    assertEquals(result.getDocuments().get(0), doc4);
    assertEquals(result.getDocuments().get(0).getValue(), _item2);
    final ConfigHistoryResult<Object> versionedResult = _testPopulated.history(versionedRequest);
    assertEquals(versionedResult.getDocuments().get(0), doc4);
    assertEquals(versionedResult.getDocuments().get(0).getValue(), _item2);
  }
}
