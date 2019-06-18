/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link MasterConfigSource}.
 */
@Test(groups = TestGroup.UNIT)
public class MasterConfigSourceTest {
  private static final ExternalId CONFIG_1 = ExternalId.of("A", "B");
  private static final ExternalId CONFIG_2 = ExternalId.of("A", "C");
  private static final ExternalId CONFIG_3 = ExternalId.of("A", "C");
  private static final String NAME_1 = "Test";
  private static final String NAME_2 = "Other";
  private static final VersionCorrection VC = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(1000));
  private static final ConfigItem<ExternalId> ITEM_1;
  private static final ConfigItem<ExternalId> ITEM_2;
  private static final ConfigItem<ExternalId> ITEM_3;
  static {
    ITEM_1 = ConfigItem.of(CONFIG_1);
    ITEM_1.setName(NAME_1);
    ITEM_2 = ConfigItem.of(CONFIG_2);
    ITEM_2.setName(NAME_1);
    ITEM_3 = ConfigItem.of(CONFIG_3);
    ITEM_3.setName(NAME_2);
  }

  private MasterConfigSource _configSource;

  /**
   * Sets up the underlying master and sets the UID of the config item.
   */
  @BeforeMethod
  public void setUp() {
    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    configMaster.add(new ConfigDocument(ITEM_1));
    configMaster.add(new ConfigDocument(ITEM_2));
    configMaster.add(new ConfigDocument(ITEM_3));
    _configSource = new MasterConfigSource(configMaster);
  }

  /**
   * Tears down the source.
   */
  @AfterMethod
  public void tearDown() {
    _configSource = null;
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the underlying master cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullMaster() {
    new MasterConfigSource(null);
  }

  /**
   * Tests that the change manager used is that from the underlying master.
   */
  @Test
  public void testChangeManager() {
    final BasicChangeManager changeManager = new BasicChangeManager();
    final InMemoryConfigMaster master = new InMemoryConfigMaster(changeManager);
    final MasterConfigSource source = new MasterConfigSource(master);
    assertSame(source.changeManager(), changeManager);
  }

  /**
   * Tests that the request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRequest() {
    _configSource.search(null);
  }

  /**
   * Tests that the type of the request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTypeInRequest() {
    final ConfigSearchRequest<?> request = new ConfigSearchRequest<>();
    request.setConfigIds(Arrays.asList(ITEM_1.getUniqueId()));
    request.setName(NAME_1);
    request.setPagingRequest(PagingRequest.ALL);
    request.setSortOrder(ConfigSearchSortOrder.NAME_ASC);
    request.setType(null);
    request.setUniqueIdScheme(InMemoryConfigMaster.DEFAULT_OID_SCHEME);
    request.setVersionCorrection(VersionCorrection.LATEST);
    _configSource.search(request);
  }

  /**
   * Tests a search using the name and type of the required item.
   */
  public void testSearchByNameAndType() {
    final ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<>();
    request.setName(NAME_1);
    request.setType(ExternalId.class);
    List<ConfigItem<ExternalId>> searchResult = _configSource.search(request);
    assertEquals(searchResult.size(), 2);
    assertEqualsNoOrder(searchResult, Arrays.asList(ITEM_1, ITEM_2));
    request.setName(NAME_2);
    searchResult = _configSource.search(request);
    assertEquals(searchResult.size(), 1);
    assertEquals(searchResult.iterator().next(), ITEM_3);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetConfigNullClass() {
    _configSource.getConfig(null, ITEM_1.getUniqueId());
  }

  /**
   * Tests getting an item by unique id.
   */
  public void getConfig() {
    final ExternalId test = _configSource.getConfig(ExternalId.class, ITEM_1.getUniqueId());
    assertEquals(test, CONFIG_1);
  }

  /**
   * Tests the behaviour when the expected type is not that returned.
   */
  public void testGetConfigWrongExpectedType() {
    assertNull(_configSource.getConfig(ExternalIdBundle.class, ITEM_1.getUniqueId()));
  }

  /**
   * Tests the behaviour when there is no document for a unique id.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testAccessInvalidDocument() {
    _configSource.getConfig(UniqueId.class, UniqueId.of("U", "1"));
  }

  /**
   * Tests getting an item by object id and version correction.
   */
  public void testGetByObjectIdVersionCorrection() {
    final ConfigItem<?> item = _configSource.get(ITEM_1.getObjectId(), VC);
    assertEquals(item, ITEM_1);
  }

  /**
   * Tests getting an item by unique id.
   */
  public void testGetByUniqueId() {
    final ConfigItem<?> item = _configSource.get(ITEM_1.getUniqueId());
    assertEquals(item, ITEM_1);
  }

  /**
   * Tests getting a single item by name and expected class type.
   */
  public void testSingleByNameAndType() {
    final ExternalId config = _configSource.getSingle(ExternalId.class, NAME_1, VC);
    assertTrue(config.equals(CONFIG_1) || config.equals(CONFIG_2));
  }

  /**
   * Tests the return value if the expected type does not match the config type.
   */
  public void testSingleByNameAndWrongExpectedType() {
    assertNull(_configSource.getSingle(ExternalIdBundle.class, NAME_1, VC));
  }

  /**
   * Tests that one item returned even with multiple results from the master.
   */
  public void testOneItemReturned() {
    final ConfigItem<ExternalId> item1 = ConfigItem.of(ExternalId.of("eid", "1"), NAME_1);
    final ConfigItem<ExternalId> item2 = ConfigItem.of(ExternalId.of("eid", "2"), NAME_1);
    final ConfigItem<ExternalId> item3 = ConfigItem.of(ExternalId.of("eid", "3"), NAME_1);
    final InMemoryConfigMaster master = new InMemoryConfigMaster();
    // also sets the uids of the items
    master.add(new ConfigDocument(item1));
    master.add(new ConfigDocument(item2));
    master.add(new ConfigDocument(item3));
    final MasterConfigSource source = new MasterConfigSource(master);
    final ExternalId config = source.getSingle(ExternalId.class, NAME_1, VC);
    assertTrue(item1.getValue().equals(config) || item2.getValue().equals(config) || item3.getValue().equals(config));
    // just to check master is returning more than one result
    final ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<>();
    request.setName(NAME_1);
    assertEquals(master.search(request).getDocuments().size(), 3);
  }

  /**
   * Tests the return value if the expected type does not match the config type.
   */
  public void testGetConfigByOidAndWrongExpectedType() {
    assertNull(_configSource.getConfig(ExternalIdBundle.class, ITEM_1.getObjectId(), VC));
  }

  /**
   * Tests getting an item by expected type and object id.
   */
  public void testGetConfigByOidAndType() {
    final ExternalId config = _configSource.getConfig(ExternalId.class, ITEM_1.getObjectId(), VC);
    assertEquals(config, CONFIG_1);
  }

  /**
   * Tests the return value if the expected type does not match the config type.
   */
  public void testGetByNameAndWrongExpectedType() {
    assertTrue(_configSource.get(ExternalIdBundle.class, NAME_1, VC).isEmpty());
  }

  /**
   * Tests getting an item by expected type and name.
   */
  public void testGetByNameAndType() {
    Collection<ConfigItem<ExternalId>> configs = _configSource.get(ExternalId.class, NAME_1, VC);
    assertEqualsNoOrder(configs, Arrays.asList(ITEM_1, ITEM_2));
    configs = _configSource.get(ExternalId.class, NAME_2, VC);
    assertEquals(configs.size(), 1);
    assertEquals(configs.iterator().next(), ITEM_3);
  }

  /**
   * Tests the return value if the expected type does not match the config type.
   */
  public void testGetAllWrongExpectedType() {
    assertTrue(_configSource.getAll(ExternalIdBundle.class, VC).isEmpty());
  }

  /**
   * Tests getting an item by expected type and object id.
   */
  public void testGetAll() {
    final Collection<ConfigItem<ExternalId>> configs = _configSource.getAll(ExternalId.class, VC);
    assertEquals(configs.size(), 3);
    assertEqualsNoOrder(configs, Arrays.asList(ITEM_1, ITEM_2, ITEM_3));
  }

  /**
   * Tests the return value if there are no results of a type.
   */
  public void testGetLatestByNameNoResults() {
    assertNull(_configSource.getLatestByName(ExternalIdBundle.class, NAME_1));
  }

  /**
   * Tests getting the latest version of a config.
   */
  public void testGetLatestByName() {
    ExternalId latest = _configSource.getLatestByName(ExternalId.class, NAME_2);
    assertEquals(latest, CONFIG_3);
    latest = _configSource.getLatestByName(ExternalId.class, NAME_1);
    // version is ignored in the master used in this tests so it could be either
    assertTrue(latest.equals(CONFIG_1) || latest.equals(CONFIG_2));
  }

  /**
   * Tests getting multiple configurations by unique id.
   */
  public void testGetUidCollection() {
    final Map<UniqueId, ConfigItem<?>> configs = _configSource.get(Arrays.asList(ITEM_1.getUniqueId(), ITEM_2.getUniqueId(), ITEM_3.getUniqueId()));
    assertEquals(configs.size(), 3);
    assertEqualsNoOrder(configs.keySet(), Arrays.asList(ITEM_1.getUniqueId(), ITEM_2.getUniqueId(), ITEM_3.getUniqueId()));
    assertEqualsNoOrder(configs.values(), Arrays.asList(ITEM_1, ITEM_2, ITEM_3));
  }

}
