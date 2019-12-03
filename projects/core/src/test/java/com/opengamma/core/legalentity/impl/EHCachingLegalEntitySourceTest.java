/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.legalentity.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.test.Assert;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pairs;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * Tests for {@link EHCachingLegalEntitySource}.
 */
@Test(groups = TestGroup.UNIT)
public class EHCachingLegalEntitySourceTest {
  private static final UniqueId UID_1 = UniqueId.of("uid", "1000");
  private static final UniqueId UID_2 = UniqueId.of("uid", "2000");
  private static final UniqueId UID_3 = UniqueId.of("uid", "1000", "123");
  private static final UniqueId UID_4 = UniqueId.of("uid", "4000", "234");
  private static final ExternalIdBundle EID_1 = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "2"));
  private static final ExternalIdBundle EID_2 = ExternalIdBundle.of(ExternalId.of("eid1", "10"), ExternalId.of("eid2", "20"));
  private static final ExternalIdBundle EID_3 = ExternalIdBundle.of(ExternalId.of("eid1", "100"), ExternalId.of("eid2", "200"));
  private static final SimpleLegalEntity LATEST_1 = new SimpleLegalEntity("NAME 1", EID_1);
  private static final SimpleLegalEntity LATEST_2 = new SimpleLegalEntity("NAME 2", EID_2);
  private static final SimpleLegalEntity VERSIONED_1 = new SimpleLegalEntity("NAME 1", EID_1);
  private static final SimpleLegalEntity VERSIONED_2 = new SimpleLegalEntity("NAME 3", EID_3);
  private static final VersionCorrection VC_1 = VersionCorrection.of(Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));
  private static final VersionCorrection VC_2 = VersionCorrection.of(Instant.ofEpochSecond(1000), Instant.ofEpochSecond(2000));
  static {
    LATEST_1.setUniqueId(UID_1);
    LATEST_2.setUniqueId(UID_2);
    VERSIONED_1.setUniqueId(UID_3);
    VERSIONED_2.setUniqueId(UID_4);
  }
  private CacheManager _cacheManager;
  private LegalEntitySource _underlying;
  private EHCachingLegalEntitySource _source;

  /**
   * Sets up the cache manager.
   */
  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  /**
   * Shuts down the caches.
   */
  @AfterClass
  public void tearDownClass() {
    _source.shutdown();
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  /**
   * Initialises the underlying source.
   */
  @BeforeMethod
  public void setUp() {
    EHCacheUtils.clear(_cacheManager);
    _underlying = Mockito.mock(LegalEntitySource.class);
    _source = new EHCachingLegalEntitySource(_underlying, _cacheManager);
    _source.emptyEHCache();
    _source.emptyFrontCache();
    populateByUniqueId(_underlying);
    populateByObjectId(_underlying);
    populateByExternalId(_underlying);
  }

  /**
   * Clears the caches.
   */
  @AfterMethod
  public void clearCaches() {
    EHCacheUtils.clear(_cacheManager);
    _source.emptyEHCache();
    _source.emptyFrontCache();
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testString() {
    assertTrue(_source.toString().startsWith("EHCachingLegalEntitySource["));
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullUniqueId() {
    _source.get((UniqueId) null);
  }

  /**
   * Tests that null is returned when there is no value available from the underlying source.
   */
  @Test
  public void testNullItemUniqueId() {
    assertNull(_source.get(UniqueId.of("test", "3")));
  }

  /**
   * Tests get by unique id.
   */
  @Test
  public void testGetLatestUniqueId() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(UID_1));
    assertNull(ehCache.get(UID_2));
    // getting the latest version will not put the value in cache
    assertEquals(_source.get(UID_1), LATEST_1);
    assertEquals(_source.get(UID_2), LATEST_2);
    // nothing in the cache
    assertNull(ehCache.get(UID_1));
    assertNull(ehCache.get(UID_2));
    _source.get(UID_1);
    _source.get(UID_2);
    // underlying has been called twice
    Mockito.verify(_underlying, Mockito.times(2)).get(UID_1);
    Mockito.verify(_underlying, Mockito.times(2)).get(UID_2);
  }

  /**
   * Tests get by unique id.
   */
  @Test
  public void testGetVersionedUniqueId() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(UID_3));
    assertNull(ehCache.get(UID_4));
    // getting the latest version will put the value in cache
    assertEquals(_source.get(UID_3), VERSIONED_1);
    assertEquals(_source.get(UID_4), VERSIONED_2);
    // values in cache
    assertEquals(ehCache.get(UID_3).getObjectValue(), VERSIONED_1);
    assertEquals(ehCache.get(UID_4).getObjectValue(), VERSIONED_2);
    _source.get(UID_3);
    _source.get(UID_4);
    // underlying has been called once
    Mockito.verify(_underlying, Mockito.times(1)).get(UID_3);
    Mockito.verify(_underlying, Mockito.times(1)).get(UID_4);
    // values in cache
    assertEquals(ehCache.get(UID_3).getObjectValue(), VERSIONED_1);
    assertEquals(ehCache.get(UID_4).getObjectValue(), VERSIONED_2);
  }

  /**
   * Tests that values are cached in the EH cache.
   */
  @Test
  public void testEhCacheGetByUniqueId() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in EH cache
    assertNull(ehCache.get(UID_3));
    assertNull(ehCache.get(UID_4));
    // add values to cache
    _source.get(UID_3);
    _source.get(UID_4);
    // flush front cache
    _source.emptyFrontCache();
    _source.get(UID_3);
    _source.get(UID_4);
    // values should be in EH cache so only one call to underlying
    Mockito.verify(_underlying, Mockito.times(1)).get(UID_3);
    Mockito.verify(_underlying, Mockito.times(1)).get(UID_4);
  }

  /**
   * Tests get by object id.
   */
  @Test
  public void testGetLatestObjectId() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(Pairs.of(UID_1.getObjectId(), VersionCorrection.LATEST)));
    assertNull(ehCache.get(Pairs.of(UID_2.getObjectId(), VersionCorrection.LATEST)));
    // getting the latest version will not put the value in cache
    assertEquals(_source.get(UID_1.getObjectId(), VersionCorrection.LATEST), LATEST_1);
    assertEquals(_source.get(UID_2.getObjectId(), VersionCorrection.LATEST), LATEST_2);
    // nothing in the cache
    assertNull(ehCache.get(Pairs.of(UID_1.getObjectId(), VersionCorrection.LATEST)));
    assertNull(ehCache.get(Pairs.of(UID_2.getObjectId(), VersionCorrection.LATEST)));
    _source.get(UID_1.getObjectId(), VersionCorrection.LATEST);
    _source.get(UID_2.getObjectId(), VersionCorrection.LATEST);
    // underlying has been called twice
    Mockito.verify(_underlying, Mockito.times(2)).get(UID_1.getObjectId(), VersionCorrection.LATEST);
    Mockito.verify(_underlying, Mockito.times(2)).get(UID_2.getObjectId(), VersionCorrection.LATEST);
  }

  /**
   * Tests get by object id.
   */
  @Test
  public void testGetVersionedObjectId() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(Pairs.of(UID_3.getObjectId(), VC_1)));
    assertNull(ehCache.get(Pairs.of(UID_4.getObjectId(), VC_2)));
    // getting the latest version will put the value in cache
    assertEquals(_source.get(UID_3.getObjectId(), VC_1), VERSIONED_1);
    assertEquals(_source.get(UID_4.getObjectId(), VC_2), VERSIONED_2);
    // values in cache
    assertEquals(ehCache.get(UID_3).getObjectValue(), VERSIONED_1);
    assertEquals(ehCache.get(UID_4).getObjectValue(), VERSIONED_2);
    _source.get(UID_3.getObjectId(), VC_1);
    _source.get(UID_4.getObjectId(), VC_2);
    // underlying has been called once
    Mockito.verify(_underlying, Mockito.times(1)).get(UID_3.getObjectId(), VC_1);
    Mockito.verify(_underlying, Mockito.times(1)).get(UID_4.getObjectId(), VC_2);
    // values in cache
    assertEquals(ehCache.get(Pairs.of(UID_3.getObjectId(), VC_1)).getObjectValue(), VERSIONED_1);
    assertEquals(ehCache.get(Pairs.of(UID_4.getObjectId(), VC_2)).getObjectValue(), VERSIONED_2);
  }

  /**
   * Tests that values are cached in the EH cache.
   */
  @Test
  public void testEhCacheGetByObjectId() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in EH cache
    assertNull(ehCache.get(Pairs.of(UID_3.getObjectId(), VC_1)));
    assertNull(ehCache.get(Pairs.of(UID_4.getObjectId(), VC_2)));
    // add values to cache
    _source.get(UID_3.getObjectId(), VC_1);
    _source.get(UID_4.getObjectId(), VC_2);
    // flush front cache
    _source.emptyFrontCache();
    _source.get(UID_3.getObjectId(), VC_1);
    _source.get(UID_4.getObjectId(), VC_2);
    // values should be in EH cache so only one call to underlying
    Mockito.verify(_underlying, Mockito.times(1)).get(UID_3.getObjectId(), VC_1);
    Mockito.verify(_underlying, Mockito.times(1)).get(UID_4.getObjectId(), VC_2);
  }

  /**
   * Tests get by external id.
   */
  @Test
  public void testGetSingleExternalId() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(Pairs.of(EID_1.getExternalId(ExternalScheme.of("eid1")), VersionCorrection.LATEST)));
    assertNull(ehCache.get(Pairs.of(EID_2.getExternalId(ExternalScheme.of("eid2")), VersionCorrection.LATEST)));
    // getting the latest version will not put the value in cache
    assertEquals(_source.getSingle(EID_1.getExternalId(ExternalScheme.of("eid1"))), LATEST_1);
    assertEquals(_source.getSingle(EID_2.getExternalId(ExternalScheme.of("eid2"))), LATEST_2);
    // nothing in the cache
    assertNull(ehCache.get(Pairs.of(EID_1.getExternalId(ExternalScheme.of("eid1")), VersionCorrection.LATEST)));
    assertNull(ehCache.get(Pairs.of(EID_2.getExternalId(ExternalScheme.of("eid2")), VersionCorrection.LATEST)));
    _source.getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")));
    _source.getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")));
    // underlying has been called twice
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle(), VersionCorrection.LATEST);
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle(), VersionCorrection.LATEST);
  }

  /**
   * Tests that values are not cached.
   */
  @Test
  public void testEhCacheGetSingleByExternalId() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in EH cache
    assertNull(ehCache.get(Pairs.of(EID_1.getExternalId(ExternalScheme.of("eid1")), VersionCorrection.LATEST)));
    assertNull(ehCache.get(Pairs.of(EID_2.getExternalId(ExternalScheme.of("eid2")), VersionCorrection.LATEST)));
    // add values to cache
    _source.getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")));
    _source.getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")));
    // flush the front cache
    _source.emptyFrontCache();
    _source.getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")));
    _source.getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")));
    // underlying has been called once as version is cached
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle(), VersionCorrection.LATEST);
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle(), VersionCorrection.LATEST);
  }

  /**
   * Tests get by external id bundle.
   */
  @Test
  public void testGetSingleExternalIdBundle() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(Pairs.of(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle(), VersionCorrection.LATEST)));
    assertNull(ehCache.get(Pairs.of(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle(), VersionCorrection.LATEST)));
    // getting the latest version will not put the value in cache
    assertEquals(_source.getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle()), LATEST_1);
    assertEquals(_source.getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle()), LATEST_2);
    // nothing in the cache
    assertNull(ehCache.get(Pairs.of(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle(), VersionCorrection.LATEST)));
    assertNull(ehCache.get(Pairs.of(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle(), VersionCorrection.LATEST)));
    _source.getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle());
    _source.getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle());
    // underlying has been called twice
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle(), VersionCorrection.LATEST);
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle(), VersionCorrection.LATEST);
  }

  /**
   * Tests that values are not cached.
   */
  @Test
  public void testEhCacheGetSingleByExternalIdBundle() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in EH cache
    assertNull(ehCache.get(Pairs.of(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle(), VersionCorrection.LATEST)));
    assertNull(ehCache.get(Pairs.of(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle(), VersionCorrection.LATEST)));
    // add values to cache
    _source.getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle());
    _source.getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle());
    // flush the front cache
    _source.emptyFrontCache();
    _source.getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle());
    _source.getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle());
    // underlying has been called once as version is cached
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle(), VersionCorrection.LATEST);
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle(), VersionCorrection.LATEST);
  }

  /**
   * Tests get by external id bundle.
   */
  @Test
  public void testGetSingleExternalIdBundleVersion() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(Pairs.of(EID_1, VC_1)));
    assertNull(ehCache.get(Pairs.of(EID_3, VC_2)));
    // put the values in the cache
    assertEquals(_source.getSingle(EID_1, VC_1), VERSIONED_1);
    assertEquals(_source.getSingle(EID_3, VC_2), VERSIONED_2);
    // values in the cache
    assertEquals(ehCache.get(Pairs.of(EID_1, VC_1)).getObjectValue(), VERSIONED_1);
    assertEquals(ehCache.get(Pairs.of(EID_3, VC_2)).getObjectValue(), VERSIONED_2);
    // get values from the cache
    assertEquals(_source.getSingle(EID_1, VC_1), VERSIONED_1);
    assertEquals(_source.getSingle(EID_3, VC_2), VERSIONED_2);
    // underlying has been called once
    Mockito.verify(_underlying, Mockito.times(1)).getSingle(EID_1, VC_1);
    Mockito.verify(_underlying, Mockito.times(1)).getSingle(EID_3, VC_2);
  }

  /**
   * Tests that values are not cached.
   */
  @Test
  public void testEhCacheGetSingleByExternalIdBundleVersion() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(Pairs.of(EID_1, VC_1)));
    assertNull(ehCache.get(Pairs.of(EID_3, VC_2)));
    // put the values in the cache
    assertEquals(_source.getSingle(EID_1, VC_1), VERSIONED_1);
    assertEquals(_source.getSingle(EID_3, VC_2), VERSIONED_2);
    // clear cache
    _source.emptyFrontCache();
    // values in the cache
    assertEquals(ehCache.get(Pairs.of(EID_1, VC_1)).getObjectValue(), VERSIONED_1);
    assertEquals(ehCache.get(Pairs.of(EID_3, VC_2)).getObjectValue(), VERSIONED_2);
    // get values from the cache
    assertEquals(_source.getSingle(EID_1, VC_1), VERSIONED_1);
    assertEquals(_source.getSingle(EID_3, VC_2), VERSIONED_2);
    // underlying has been called once
    Mockito.verify(_underlying, Mockito.times(1)).getSingle(EID_1, VC_1);
    Mockito.verify(_underlying, Mockito.times(1)).getSingle(EID_3, VC_2);
  }

  /**
   * Tests get by unique id collection - currently the values are not cached.
   */
  @Test
  public void testGetUniqueIdCollection() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(UID_1));
    assertNull(ehCache.get(UID_2));
    assertNull(ehCache.get(UID_3));
    assertNull(ehCache.get(UID_4));
    // caching versioned objects
    final List<UniqueId> uids = Arrays.asList(UID_1, UID_2, UID_3, UID_4);
    final Map<UniqueId, LegalEntity> mUid = _source.get(uids);
    Assert.assertEqualsNoOrder(mUid.keySet(), uids);
    Assert.assertEqualsNoOrder(mUid.values(), Arrays.asList(LATEST_1, LATEST_2, VERSIONED_1, VERSIONED_2));
    // latest not cached, versioned cached
    assertNull(ehCache.get(UID_1));
    assertNull(ehCache.get(UID_2));
    assertEquals(ehCache.get(UID_3).getObjectValue(), VERSIONED_1);
    assertEquals(ehCache.get(UID_4).getObjectValue(), VERSIONED_2);
    _source.get(uids);
    // underlying has been called twice
    Mockito.verify(_underlying, Mockito.times(2)).get(uids);
  }

  /**
   * Tests get by object id collection - currently the values are not cached.
   */
  @Test
  public void testGetObjectIdCollection() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(UID_1.getObjectId()));
    assertNull(ehCache.get(UID_2.getObjectId()));
    assertNull(ehCache.get(UID_3.getObjectId()));
    assertNull(ehCache.get(UID_4.getObjectId()));
    // caching versioned objects
    final List<ObjectId> oids = Arrays.asList(UID_1.getObjectId(), UID_2.getObjectId());
    final Map<ObjectId, LegalEntity> mOid = _source.get(oids, VersionCorrection.LATEST);
    Assert.assertEqualsNoOrder(mOid.keySet(), oids);
    Assert.assertEqualsNoOrder(mOid.values(), Arrays.asList(LATEST_1, LATEST_2));
    // not cached
    assertNull(ehCache.get(UID_1.getObjectId()));
    assertNull(ehCache.get(UID_2.getObjectId()));
    assertNull(ehCache.get(UID_3.getObjectId()));
    assertNull(ehCache.get(UID_4.getObjectId()));
    _source.get(oids, VersionCorrection.LATEST);
    // underlying has been called twice
    Mockito.verify(_underlying, Mockito.times(2)).get(oids, VersionCorrection.LATEST);
  }

  /**
   * Tests get by external id bundle collection - currently the values are not cached.
   */
  @Test
  public void testGetAllExternalIdBundleCollection() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(EID_1));
    assertNull(ehCache.get(EID_2));
    assertNull(ehCache.get(EID_3));
    // caching versioned objects
    final List<ExternalIdBundle> eids = Arrays.asList(EID_1, EID_2, EID_3);
    final Map<ExternalIdBundle, Collection<LegalEntity>> mEid = _source.getAll(eids, VC_1);
    Assert.assertEqualsNoOrder(mEid.keySet(), eids);
    // not cached
    assertNull(ehCache.get(EID_1));
    assertNull(ehCache.get(EID_2));
    assertNull(ehCache.get(EID_3));
    _source.getAll(eids, VC_1);
    // underlying has been called twice
    Mockito.verify(_underlying, Mockito.times(2)).getAll(eids, VC_1);
  }

  /**
   * Tests get by external id bundle collection - currently the values are not cached.
   */
  @Test
  public void testGetSingleExternalIdBundleCollection() {
    final Cache ehCache = _cacheManager.getCache("legalentity");
    // nothing in the cache
    assertNull(ehCache.get(EID_1));
    assertNull(ehCache.get(EID_2));
    assertNull(ehCache.get(EID_3));
    // caching versioned objects
    final List<ExternalIdBundle> eids = Arrays.asList(EID_1, EID_2, EID_3);
    final Map<ExternalIdBundle, LegalEntity> mEid = _source.getSingle(eids, VC_1);
    Assert.assertEqualsNoOrder(mEid.keySet(), eids);
    // not cached
    assertNull(ehCache.get(EID_1));
    assertNull(ehCache.get(EID_2));
    assertNull(ehCache.get(EID_3));
    _source.getSingle(eids, VC_1);
    // underlying has been called twice
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(eids, VC_1);
  }

  private static void populateByUniqueId(final LegalEntitySource source) {
    Mockito.when(source.get(UID_1)).thenReturn(LATEST_1);
    Mockito.when(source.get(UID_2)).thenReturn(LATEST_2);
    Mockito.when(source.get(UID_3)).thenReturn(VERSIONED_1);
    Mockito.when(source.get(UID_4)).thenReturn(VERSIONED_2);
    final Map<UniqueId, LegalEntity> mUid = new HashMap<>();
    mUid.put(UID_1, LATEST_1);
    mUid.put(UID_2, LATEST_2);
    mUid.put(UID_3, VERSIONED_1);
    mUid.put(UID_4, VERSIONED_2);
    Mockito.when(source.get(Arrays.asList(UID_1, UID_2, UID_3, UID_4))).thenReturn(mUid);
  }

  private static void populateByObjectId(final LegalEntitySource source) {
    Mockito.when(source.get(UID_1.getObjectId(), VersionCorrection.LATEST)).thenReturn(LATEST_1);
    Mockito.when(source.get(UID_2.getObjectId(), VersionCorrection.LATEST)).thenReturn(LATEST_2);
    Mockito.when(source.get(UID_3.getObjectId(), VC_1)).thenReturn(VERSIONED_1);
    Mockito.when(source.get(UID_4.getObjectId(), VC_2)).thenReturn(VERSIONED_2);

    final Map<ObjectId, LegalEntity> mOid = new HashMap<>();
    mOid.put(UID_1.getObjectId(), LATEST_1);
    mOid.put(UID_2.getObjectId(), LATEST_2);
    Mockito
      .when(source.get(Arrays.asList(UID_1.getObjectId(), UID_2.getObjectId()), VersionCorrection.LATEST))
      .thenReturn(mOid);
  }

  private static void populateByExternalId(final LegalEntitySource source) {
    Mockito.when(source.getSingle(EID_1.getExternalId(ExternalScheme.of("eid1")).toBundle(), VersionCorrection.LATEST)).thenReturn(LATEST_1);
    Mockito.when(source.getSingle(EID_2.getExternalId(ExternalScheme.of("eid2")).toBundle(), VersionCorrection.LATEST)).thenReturn(LATEST_2);
    Mockito.when(source.getSingle(EID_1, VersionCorrection.LATEST)).thenReturn(LATEST_1);
    Mockito.when(source.getSingle(EID_2, VersionCorrection.LATEST)).thenReturn(LATEST_2);
    Mockito.when(source.getSingle(EID_1, VC_1)).thenReturn(VERSIONED_1);
    Mockito.when(source.getSingle(EID_3, VC_2)).thenReturn(VERSIONED_2);

    final Map<ExternalIdBundle, Collection<LegalEntity>> mEid = new HashMap<>();
    mEid.put(EID_1, Arrays.<LegalEntity>asList(LATEST_1, VERSIONED_1));
    mEid.put(EID_2, Collections.<LegalEntity>singleton(LATEST_2));
    mEid.put(EID_3, Collections.<LegalEntity>singleton(VERSIONED_2));
    Mockito.when(source.getAll(Arrays.asList(EID_1, EID_2, EID_3), VC_1)).thenReturn(mEid);

    final Map<ExternalIdBundle, LegalEntity> mEidSingle = new HashMap<>();
    mEidSingle.put(EID_1, VERSIONED_1);
    mEidSingle.put(EID_2, LATEST_2);
    mEidSingle.put(EID_3, VERSIONED_2);
    Mockito.when(source.getSingle(Arrays.asList(EID_1, EID_2, EID_3), VC_1)).thenReturn(mEidSingle);
  }
}
