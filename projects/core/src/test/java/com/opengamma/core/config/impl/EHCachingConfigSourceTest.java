/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.config.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * Tests for {@link EHCachingConfigSource}.
 */
@Test(groups = TestGroup.UNIT)
public class EHCachingConfigSourceTest {
  private static final UnorderedCurrencyPair LATEST_1 = UnorderedCurrencyPair.of(Currency.AUD, Currency.BRL);
  private static final ConfigItem<UnorderedCurrencyPair> LATEST_ITEM_1 = ConfigItem.of(LATEST_1);
  private static final UnorderedCurrencyPair LATEST_2 = UnorderedCurrencyPair.of(Currency.CZK, Currency.DEM);
  private static final ConfigItem<UnorderedCurrencyPair> LATEST_ITEM_2 = ConfigItem.of(LATEST_2);
  static {
    LATEST_ITEM_1.setUniqueId(UniqueId.of("test", "1"));
    LATEST_ITEM_2.setUniqueId(UniqueId.of("test", "10"));
    LATEST_ITEM_1.setName("LATEST 1");
    LATEST_ITEM_2.setName("LATEST 2");
  }
  private static final UnorderedCurrencyPair VERSIONED_1 = UnorderedCurrencyPair.of(Currency.CAD, Currency.CHF);
  private static final ConfigItem<UnorderedCurrencyPair> VERSIONED_ITEM_1 = ConfigItem.of(VERSIONED_1);
  private static final UnorderedCurrencyPair VERSIONED_2 = UnorderedCurrencyPair.of(Currency.DKK, Currency.EUR);
  private static final ConfigItem<UnorderedCurrencyPair> VERSIONED_ITEM_2 = ConfigItem.of(VERSIONED_2);
  private static final VersionCorrection VC_1 = VersionCorrection.of(Instant.ofEpochSecond(100L), Instant.ofEpochSecond(200L));
  private static final VersionCorrection VC_2 = VersionCorrection.of(Instant.ofEpochSecond(1000L), Instant.ofEpochSecond(2000L));
  static {
    VERSIONED_ITEM_1.setUniqueId(UniqueId.of("test", "2", VC_1.toString()));
    VERSIONED_ITEM_2.setUniqueId(UniqueId.of("test", "20", VC_2.toString()));
    VERSIONED_ITEM_1.setName("VERSIONED 1");
    VERSIONED_ITEM_2.setName("VERSIONED 2");
  }
  private CacheManager _cacheManager;
  private ConfigSource _underlying;
  private EHCachingConfigSource _source;

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
    _underlying = Mockito.mock(ConfigSource.class);
    populateByUniqueId(_underlying);
    populateByObjectId(_underlying);
    populateByClassName(_underlying);
    populateByClass(_underlying);
    _source = new EHCachingConfigSource(_underlying, _cacheManager);
    _source.emptyCaches();
  }

  /**
   * Clears the caches.
   */
  @AfterMethod
  public void clearCaches() {
    EHCacheUtils.clear(_cacheManager);
    _source.emptyCaches();
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testString() {
    assertTrue(_source.toString().startsWith("EHCachingConfigSource["));
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
   * Tests the get method when nothing has been cached.
   */
  @Test
  public void testInitialGetLatestUniqueId() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(LATEST_ITEM_1.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VC_1)));

    assertEquals(_source.get(LATEST_ITEM_1.getUniqueId()), LATEST_ITEM_1);
    assertEquals(uidCache.get(LATEST_ITEM_1.getUniqueId()).getObjectValue(), LATEST_ITEM_1);
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VC_1)));
  }

  /**
   * Tests the get method when nothing has been cached.
   */
  @Test
  public void testInitialGetVersionedUniqueId() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));

    assertEquals(_source.get(VERSIONED_ITEM_1.getUniqueId()), VERSIONED_ITEM_1);
    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VC_1)));
  }

  /**
   * Tests the get method after items have been cached.
   */
  @Test
  public void testCachedGetLatestUniqueId() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(LATEST_ITEM_1.getUniqueId()));
    assertNull(oidCache.get(LATEST_ITEM_1.getUniqueId().getObjectId()));
    // cache item
    _source.get(LATEST_ITEM_1.getUniqueId());

    assertEquals(uidCache.get(LATEST_ITEM_1.getUniqueId()).getObjectValue(), LATEST_ITEM_1);
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));

    assertEquals(_source.get(LATEST_ITEM_1.getUniqueId()), LATEST_ITEM_1);
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
  }

  /**
   * Tests the get method after items have been cached.
   */
  @Test
  public void testCachedGetVersionedUniqueId() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
    // cache item
    _source.get(VERSIONED_ITEM_1.getUniqueId());

    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertNull(oidCache.get(VERSIONED_ITEM_1.getUniqueId().getObjectId()));

    assertEquals(_source.get(VERSIONED_ITEM_1.getUniqueId()), VERSIONED_ITEM_1);
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
  }

  /**
   * Tests the get method after the items have been cleared from the front cache.
   */
  @Test
  public void testEhCachedOnlyGetVersionedUniqueId() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VC_1)));
    // cache item
    _source.get(VERSIONED_ITEM_1.getUniqueId());
    // clear front cache - ordinarily done when weak references are removed
    _source.flushFrontCache();

    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));

    assertEquals(_source.get(VERSIONED_ITEM_1.getUniqueId()), VERSIONED_ITEM_1);
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
  }

  /**
   * Tests that the collection cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullUniqueIdCollection() {
    _source.get((Collection<UniqueId>) null);
  }

  /**
   * Tests that an empty collection is returned when there are no values available from the underlying source.
   */
  @Test
  public void testGetNullItemUniqueIdCollection() {
    assertTrue(_source.get(Arrays.asList(UniqueId.of("test", "3"), UniqueId.of("test", "30"))).isEmpty());
  }

  /**
   * Tests the get method when nothing has been cached.
   */
  @Test
  public void testInitialGetUniqueIdCollection() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(uidCache.get(VERSIONED_ITEM_2.getUniqueId()));
    assertNull(uidCache.get(LATEST_ITEM_1.getUniqueId()));
    assertNull(uidCache.get(LATEST_ITEM_2.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getUniqueId().getObjectId(), VC_2)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_2.getUniqueId().getObjectId(), VersionCorrection.LATEST)));

    final Map<UniqueId, ConfigItem<?>> result = _source.get(Arrays.asList(VERSIONED_ITEM_1.getUniqueId(), VERSIONED_ITEM_2.getUniqueId(),
        LATEST_ITEM_1.getUniqueId(), LATEST_ITEM_2.getUniqueId()));

    assertEquals(result.size(), 4);
    assertEquals(result.get(VERSIONED_ITEM_1.getUniqueId()), VERSIONED_ITEM_1);
    assertEquals(result.get(VERSIONED_ITEM_2.getUniqueId()), VERSIONED_ITEM_2);
    assertEquals(result.get(LATEST_ITEM_1.getUniqueId()), LATEST_ITEM_1);
    assertEquals(result.get(LATEST_ITEM_2.getUniqueId()), LATEST_ITEM_2);

    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertEquals(uidCache.get(VERSIONED_ITEM_2.getUniqueId()).getObjectValue(), VERSIONED_ITEM_2);
    assertEquals(uidCache.get(LATEST_ITEM_1.getUniqueId()).getObjectValue(), LATEST_ITEM_1);
    assertEquals(uidCache.get(LATEST_ITEM_2.getUniqueId()).getObjectValue(), LATEST_ITEM_2);
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getUniqueId().getObjectId(), VC_2)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_2.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
  }

  /**
   * Tests the get method when the items have been cached.
   */
  @Test
  public void testCachedGetUniqueIdCollection() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(uidCache.get(VERSIONED_ITEM_2.getUniqueId()));
    assertNull(uidCache.get(LATEST_ITEM_1.getUniqueId()));
    assertNull(uidCache.get(LATEST_ITEM_2.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getUniqueId().getObjectId(), VC_2)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_2.getUniqueId().getObjectId(), VersionCorrection.LATEST)));

    // cache items
    _source.get(Arrays.asList(VERSIONED_ITEM_1.getUniqueId(), VERSIONED_ITEM_2.getUniqueId(),
        LATEST_ITEM_1.getUniqueId(), LATEST_ITEM_2.getUniqueId()));
    final Map<UniqueId, ConfigItem<?>> result = _source.get(Arrays.asList(VERSIONED_ITEM_1.getUniqueId(), VERSIONED_ITEM_2.getUniqueId(),
        LATEST_ITEM_1.getUniqueId(), LATEST_ITEM_2.getUniqueId()));
    assertEquals(result.size(), 4);
    assertEquals(result.get(VERSIONED_ITEM_1.getUniqueId()), VERSIONED_ITEM_1);
    assertEquals(result.get(VERSIONED_ITEM_2.getUniqueId()), VERSIONED_ITEM_2);
    assertEquals(result.get(LATEST_ITEM_1.getUniqueId()), LATEST_ITEM_1);
    assertEquals(result.get(LATEST_ITEM_2.getUniqueId()), LATEST_ITEM_2);

    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertEquals(uidCache.get(VERSIONED_ITEM_2.getUniqueId()).getObjectValue(), VERSIONED_ITEM_2);
    assertEquals(uidCache.get(LATEST_ITEM_1.getUniqueId()).getObjectValue(), LATEST_ITEM_1);
    assertEquals(uidCache.get(LATEST_ITEM_2.getUniqueId()).getObjectValue(), LATEST_ITEM_2);
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getUniqueId().getObjectId(), VC_2)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_2.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
  }

  /**
   * Tests the get method after the items have been cleared from the front cache.
   */
  @Test
  public void testEhCachedOnlyGetUniqueIdCollection() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(uidCache.get(VERSIONED_ITEM_2.getUniqueId()));
    assertNull(uidCache.get(LATEST_ITEM_1.getUniqueId()));
    assertNull(uidCache.get(LATEST_ITEM_2.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getUniqueId().getObjectId(), VC_2)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_2.getUniqueId().getObjectId(), VersionCorrection.LATEST)));

    // cache items
    _source.get(Arrays.asList(VERSIONED_ITEM_1.getUniqueId(), VERSIONED_ITEM_2.getUniqueId(),
        LATEST_ITEM_1.getUniqueId(), LATEST_ITEM_2.getUniqueId()));
    // clear front cache - ordinarily done when weak references are removed
    _source.flushFrontCache();
    final Map<UniqueId, ConfigItem<?>> result = _source.get(Arrays.asList(VERSIONED_ITEM_1.getUniqueId(), VERSIONED_ITEM_2.getUniqueId(),
        LATEST_ITEM_1.getUniqueId(), LATEST_ITEM_2.getUniqueId()));

    assertEquals(result.size(), 4);
    assertEquals(result.get(VERSIONED_ITEM_1.getUniqueId()), VERSIONED_ITEM_1);
    assertEquals(result.get(VERSIONED_ITEM_2.getUniqueId()), VERSIONED_ITEM_2);
    assertEquals(result.get(LATEST_ITEM_1.getUniqueId()), LATEST_ITEM_1);
    assertEquals(result.get(LATEST_ITEM_2.getUniqueId()), LATEST_ITEM_2);

    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertEquals(uidCache.get(VERSIONED_ITEM_2.getUniqueId()).getObjectValue(), VERSIONED_ITEM_2);
    assertEquals(uidCache.get(LATEST_ITEM_1.getUniqueId()).getObjectValue(), LATEST_ITEM_1);
    assertEquals(uidCache.get(LATEST_ITEM_2.getUniqueId()).getObjectValue(), LATEST_ITEM_2);
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getUniqueId().getObjectId(), VC_2)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_2.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullObjectId() {
    _source.get((ObjectId) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version/correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    _source.get(VERSIONED_ITEM_1.getObjectId(), null);
  }

  /**
   * Tests that null is returned when there is no value available from the underlying source.
   */
  @Test
  public void testNullItemObjectId() {
    assertNull(_source.get(UniqueId.of("test", "3").getObjectId(), VersionCorrection.LATEST));
  }

  /**
   * Tests the get method when nothing has been cached.
   */
  @Test
  public void testInitialGetLatestObjectId() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(LATEST_ITEM_1.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));

    assertEquals(_source.get(LATEST_ITEM_1.getObjectId(), VersionCorrection.LATEST), LATEST_ITEM_1);

    assertEquals(uidCache.get(LATEST_ITEM_1.getUniqueId()).getObjectValue(), LATEST_ITEM_1);
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
  }

  /**
   * Tests the get method when nothing has been cached.
   */
  @Test
  public void testInitialGetVersionedObjectId() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(oidCache.get(VERSIONED_ITEM_1.getUniqueId().getObjectId()));

    assertEquals(_source.get(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1), VERSIONED_ITEM_1);

    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertEquals(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)).getObjectValue(),
        VERSIONED_ITEM_1.getUniqueId());
  }

  /**
   * Tests the get method after items have been cached.
   */
  @Test
  public void testCachedGetLatestObjectId() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(LATEST_ITEM_1.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));

    // cache item
    _source.get(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST);

    assertEquals(uidCache.get(LATEST_ITEM_1.getUniqueId()).getObjectValue(), LATEST_ITEM_1);
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));

    assertEquals(_source.get(VERSIONED_ITEM_1.getUniqueId()), VERSIONED_ITEM_1);
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)));
  }

  /**
   * Tests the get method after items have been cached.
   */
  @Test
  public void testCachedGetVersionedObjectId() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)));
    // cache item
    _source.get(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1);

    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertEquals(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)).getObjectValue(),
        VERSIONED_ITEM_1.getUniqueId());

    assertEquals(_source.get(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1), VERSIONED_ITEM_1);
    assertEquals(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)).getObjectValue(),
        VERSIONED_ITEM_1.getUniqueId());
  }

  /**
   * Tests that the collection cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullObjectIdCollection() {
    _source.get((Collection<ObjectId>) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that an empty collection is returned when there are no values available from the underlying source.
   */
  @Test
  public void testGetNullItemObjectIdCollection() {
    assertTrue(_source
        .get(Arrays.asList(UniqueId.of("test", "3").getObjectId(), UniqueId.of("test", "30").getObjectId()), VersionCorrection.LATEST)
        .isEmpty());
  }

  /**
   * Tests the get method when nothing has been cached.
   */
  @Test
  public void testInitialGetObjectIdCollection() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(uidCache.get(VERSIONED_ITEM_2.getUniqueId()));
    assertNull(uidCache.get(LATEST_ITEM_1.getUniqueId()));
    assertNull(uidCache.get(LATEST_ITEM_2.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getObjectId(), VC_1)));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getObjectId(), VC_2)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getObjectId(), VersionCorrection.LATEST)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_2.getObjectId(), VersionCorrection.LATEST)));

    final Map<ObjectId, ConfigItem<?>> result = _source.get(Arrays.asList(VERSIONED_ITEM_1.getObjectId(), VERSIONED_ITEM_2.getObjectId(),
        LATEST_ITEM_1.getObjectId(), LATEST_ITEM_2.getObjectId()), VC_2);

    assertEquals(result.size(), 4);
    assertEquals(result.get(VERSIONED_ITEM_1.getObjectId()), VERSIONED_ITEM_1);
    assertEquals(result.get(VERSIONED_ITEM_2.getObjectId()), VERSIONED_ITEM_2);
    assertEquals(result.get(LATEST_ITEM_1.getObjectId()), LATEST_ITEM_1);
    assertEquals(result.get(LATEST_ITEM_2.getObjectId()), LATEST_ITEM_2);

    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertEquals(uidCache.get(VERSIONED_ITEM_2.getUniqueId()).getObjectValue(), VERSIONED_ITEM_2);
    assertEquals(uidCache.get(LATEST_ITEM_1.getUniqueId()).getObjectValue(), LATEST_ITEM_1);
    assertEquals(uidCache.get(LATEST_ITEM_2.getUniqueId()).getObjectValue(), LATEST_ITEM_2);
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getObjectId(), VC_1)));
    assertEquals(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getObjectId(), VC_2)).getObjectValue(), VERSIONED_ITEM_1.getUniqueId());
    assertEquals(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getObjectId(), VC_2)).getObjectValue(), VERSIONED_ITEM_2.getUniqueId());
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getObjectId(), VersionCorrection.LATEST)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_2.getObjectId(), VersionCorrection.LATEST)));
    assertNull(oidCache.get(Pairs.of(LATEST_ITEM_1.getObjectId(), VC_1)));
    assertEquals(oidCache.get(Pairs.of(LATEST_ITEM_2.getObjectId(), VC_2)).getObjectValue(), LATEST_ITEM_2.getUniqueId());
  }

  /**
   * Tests the get method when the items have been cached.
   */
  @Test
  public void testCachedGetObjectIdCollection() {
    final Cache uidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-uid-cache");
    final Cache oidCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-oid-cache");

    assertNull(uidCache.get(VERSIONED_ITEM_1.getUniqueId()));
    assertNull(uidCache.get(VERSIONED_ITEM_2.getUniqueId()));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getObjectId(), VC_1)));
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getObjectId(), VC_2)));

    // cache items
    _source.get(Arrays.asList(VERSIONED_ITEM_1.getObjectId(), VERSIONED_ITEM_2.getObjectId()), VC_1);
    final Map<ObjectId, ConfigItem<?>> result = _source.get(Arrays.asList(VERSIONED_ITEM_1.getObjectId(), VERSIONED_ITEM_2.getObjectId()),
        VC_1);
    assertEquals(result.size(), 2);
    assertEquals(result.get(VERSIONED_ITEM_1.getObjectId()), VERSIONED_ITEM_1);
    assertEquals(result.get(VERSIONED_ITEM_2.getObjectId()), VERSIONED_ITEM_2);

    assertEquals(uidCache.get(VERSIONED_ITEM_1.getUniqueId()).getObjectValue(), VERSIONED_ITEM_1);
    assertEquals(uidCache.get(VERSIONED_ITEM_2.getUniqueId()).getObjectValue(), VERSIONED_ITEM_2);
    assertEquals(oidCache.get(Pairs.of(VERSIONED_ITEM_1.getObjectId(), VC_1)).getObjectValue(), VERSIONED_ITEM_1.getUniqueId());
    assertEquals(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getObjectId(), VC_1)).getObjectValue(), VERSIONED_ITEM_2.getUniqueId());
    assertNull(oidCache.get(Pairs.of(VERSIONED_ITEM_2.getObjectId(), VC_2)));
  }

  /**
   * Tests the get method when the item has not been cached.
   */
  @SuppressWarnings({ "unchecked" })
  @Test
  public void testInitialGetClassName() {
    final Cache nameCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-name-cache");
    final Cache classCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-class-cache");

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    assertEquals(_source.get(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST), Collections.singleton(LATEST_ITEM_1));
    assertEquals(_source.get(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1), Collections.singleton(VERSIONED_ITEM_1));
    assertTrue(_source.get(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST).isEmpty());
    assertTrue(_source.get(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1).isEmpty());
    assertEquals(_source.get(UnorderedCurrencyPair.class, "LATEST 1", null), Collections.singleton(LATEST_ITEM_1));
    assertTrue(_source.get(UnorderedCurrencyPair.class, "LATEST 3", null).isEmpty());

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)).getObjectValue(),
        Collections.singleton(VERSIONED_ITEM_1));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1))
        .getObjectValue()).isEmpty());
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));
  }

  /**
   * Tests the get method when the item has been cached.
   */
  @SuppressWarnings({ "unchecked" })
  @Test
  public void testCachedGetClassName() {
    final Cache nameCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-name-cache");
    final Cache classCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-class-cache");

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    // populate name cache
    _source.get(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST);
    _source.get(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1);
    _source.get(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST);
    _source.get(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1);

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)).getObjectValue(),
        Collections.singleton(VERSIONED_ITEM_1));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1))
        .getObjectValue()).isEmpty());
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    assertEquals(_source.get(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST), Collections.singleton(LATEST_ITEM_1));
    assertEquals(_source.get(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1), Collections.singleton(VERSIONED_ITEM_1));
    assertTrue(_source.get(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST).isEmpty());
    assertTrue(_source.get(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1).isEmpty());
    assertEquals(_source.get(UnorderedCurrencyPair.class, "LATEST 1", null), Collections.singleton(LATEST_ITEM_1));

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)).getObjectValue(),
        Collections.singleton(VERSIONED_ITEM_1));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1))
        .getObjectValue()).isEmpty());
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));
  }

  /**
   * Tests the get method when the item has been cached.
   */
  @SuppressWarnings({ "unchecked" })
  @Test
  public void testClassCachedOnlyGetClassName() {
    final Cache nameCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-name-cache");
    final Cache classCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-class-cache");

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    // populate name cache and class cache
    _source.getAll(UnorderedCurrencyPair.class, VersionCorrection.LATEST);
    _source.getAll(UnorderedCurrencyPair.class, VC_1);
    _source.getAll(UnorderedCurrencyPair.class, VC_2);
    _source.emptyNameCache();

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertEquals(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)).getObjectValue(), Collections.singleton(VERSIONED_ITEM_1));
    assertEquals(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_2)).getObjectValue(), Collections.singleton(VERSIONED_ITEM_2));

    assertEquals(_source.get(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST), Collections.singleton(LATEST_ITEM_1));
    assertEquals(_source.get(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1), Collections.singleton(VERSIONED_ITEM_1));
    assertTrue(_source.get(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST).isEmpty());
    assertTrue(_source.get(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1).isEmpty());
    assertEquals(_source.get(UnorderedCurrencyPair.class, "VERSIONED 1", null), Collections.singleton(VERSIONED_ITEM_1));

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)).getObjectValue(),
        Arrays.asList(VERSIONED_ITEM_1));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1))
        .getObjectValue()).isEmpty());
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertEquals(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)).getObjectValue(), Collections.singleton(VERSIONED_ITEM_1));
  }

  /**
   * Tests the result when there is no item in the source.
   */
  @Test
  public void testGetAllNoItem() {
    // underlying returns an empty list
    assertNull(_source.getAll(Currency.class, VC_2));
    // underlying returns null
    assertNull(_source.getAll(Currency.class, VC_1));
  }

  /**
   * Tests the getAll method when the items have not been cached.
   */
  @Test
  public void testInitialGetAllClassName() {
    final Cache nameCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-name-cache");
    final Cache classCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-class-cache");

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    assertEquals(_source.getAll(UnorderedCurrencyPair.class, VersionCorrection.LATEST), Arrays.asList(LATEST_ITEM_1, LATEST_ITEM_2));
    assertEquals(_source.getAll(UnorderedCurrencyPair.class, null), Arrays.asList(LATEST_ITEM_1, LATEST_ITEM_2));
    assertEquals(_source.getAll(UnorderedCurrencyPair.class, VC_1), Arrays.asList(VERSIONED_ITEM_1));

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertEquals(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)).getObjectValue(), Collections.singleton(VERSIONED_ITEM_1));
  }

  /**
   * Tests the getAll method when the items have been cached.
   */
  @Test
  public void testCachedGetAllClassName() {
    final Cache nameCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-name-cache");
    final Cache classCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-class-cache");

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    // populate class cache
    _source.getAll(UnorderedCurrencyPair.class, VersionCorrection.LATEST);
    _source.getAll(UnorderedCurrencyPair.class, VC_1);

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertEquals(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)).getObjectValue(), Collections.singleton(VERSIONED_ITEM_1));

    assertEquals(_source.getAll(UnorderedCurrencyPair.class, VersionCorrection.LATEST), Arrays.asList(LATEST_ITEM_1, LATEST_ITEM_2));
    assertEquals(_source.getAll(UnorderedCurrencyPair.class, VC_1), Arrays.asList(VERSIONED_ITEM_1));

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertEquals(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)).getObjectValue(), Collections.singleton(VERSIONED_ITEM_1));
  }

  /**
   * Tests the exception when the wrong class type is passed in.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetConfigUniqueIdWrongClass() {
    _source.getConfig(Currency.class, VERSIONED_ITEM_1.getUniqueId());
  }

  /**
   * Tests the getConfig method for a single unique id.
   */
  @Test
  public void testGetConfigUniqueId() {
    assertEquals(_source.getConfig(UnorderedCurrencyPair.class, VERSIONED_ITEM_1.getUniqueId()), VERSIONED_1);
    assertEquals(_source.getConfig(UnorderedCurrencyPair.class, LATEST_ITEM_1.getUniqueId()), LATEST_1);
  }

  /**
   * Tests the exception when the wrong class type is passed in.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetConfigObjecyIdWrongClass() {
    _source.getConfig(Currency.class, VERSIONED_ITEM_1.getObjectId(), VC_1);
  }

  /**
   * Tests the getConfig method for a single object id.
   */
  @Test
  public void testGetConfigObjectId() {
    assertEquals(_source.getConfig(UnorderedCurrencyPair.class, VERSIONED_ITEM_1.getObjectId(), VC_1), VERSIONED_1);
    assertEquals(_source.getConfig(UnorderedCurrencyPair.class, LATEST_ITEM_1.getObjectId(), VersionCorrection.LATEST), LATEST_1);
  }

  /**
   * Tests the getSingle method when the items have not been cached.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testInitialGetSingle() {
    final Cache nameCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-name-cache");
    final Cache classCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-class-cache");

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    assertEquals(_source.getSingle(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST), LATEST_1);
    assertEquals(_source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1), VERSIONED_1);
    assertNull(_source.getSingle(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST));
    assertNull(_source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1));
    assertNull(_source.getSingle(UnorderedCurrencyPair.class, "LATEST 3", null));

    // unique id is replaced if the version / correction doesn't match
    final ConfigItem<UnorderedCurrencyPair> latestItem1 = LATEST_ITEM_1.clone();
    latestItem1.setUniqueId(null);
    final ConfigItem<UnorderedCurrencyPair> versionedItem1 = VERSIONED_ITEM_1.clone();
    versionedItem1.setUniqueId(null);
    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)).getObjectValue(), latestItem1);
    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)).getObjectValue(), versionedItem1);
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VersionCorrection.LATEST)));
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1))
        .getObjectValue()).isEmpty());
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));
  }

  /**
   * Tests the getSingle method when the items are in cache.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCachedOnlyGetSingle() {
    final Cache nameCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-name-cache");
    final Cache classCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-class-cache");

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    _source.getSingle(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST);
    _source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1);
    _source.getSingle(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST);
    _source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1);
    _source.getSingle(UnorderedCurrencyPair.class, "LATEST 3", null);

    // unique id is replaced if the version / correction doesn't match
    final ConfigItem<UnorderedCurrencyPair> latestItem1 = LATEST_ITEM_1.clone();
    latestItem1.setUniqueId(null);
    final ConfigItem<UnorderedCurrencyPair> versionedItem1 = VERSIONED_ITEM_1.clone();
    versionedItem1.setUniqueId(null);

    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)).getObjectValue(), latestItem1);
    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)).getObjectValue(), versionedItem1);
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache
        .get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)).getObjectValue()).isEmpty());
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1))
        .getObjectValue()).isEmpty());
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    assertEquals(_source.getSingle(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST), LATEST_1);
    assertEquals(_source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1), VERSIONED_1);
    assertNull(_source.getSingle(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST));
    assertNull(_source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1));
    assertTrue(_source.get(UnorderedCurrencyPair.class, "LATEST 3", null).isEmpty());

    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)).getObjectValue(), latestItem1);
    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)).getObjectValue(), versionedItem1);
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VersionCorrection.LATEST)));
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1))
        .getObjectValue()).isEmpty());
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));
  }

  /**
   * Tests the getSingle method when only the class cache is populated.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testClassCachedOnlyGetSingle() {
    final Cache nameCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-name-cache");
    final Cache classCache = _cacheManager.getCache("com.opengamma.core.config.impl.EHCachingConfigSource-class-cache");

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)));

    _source.getAll(UnorderedCurrencyPair.class, VersionCorrection.LATEST);
    _source.getAll(UnorderedCurrencyPair.class, VC_1);
    _source.emptyNameCache();

    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)));
    assertNull(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1)));
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertEquals(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)).getObjectValue(), Collections.singleton(VERSIONED_ITEM_1));

    // unique id is replaced if the version / correction doesn't match
    final ConfigItem<UnorderedCurrencyPair> latestItem1 = LATEST_ITEM_1.clone();
    latestItem1.setUniqueId(null);
    final ConfigItem<UnorderedCurrencyPair> versionedItem1 = VERSIONED_ITEM_1.clone();
    versionedItem1.setUniqueId(null);

    assertEquals(_source.getSingle(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST), LATEST_1);
    assertEquals(_source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1), VERSIONED_1);
    assertNull(_source.getSingle(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST));
    assertNull(_source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1));
    assertTrue(_source.get(UnorderedCurrencyPair.class, "LATEST 3", null).isEmpty());

    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST)).getObjectValue(), latestItem1);
    assertEquals(nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1)).getObjectValue(), versionedItem1);
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache
        .get(Triple.of(UnorderedCurrencyPair.class, "LATEST 3", VersionCorrection.LATEST)).getObjectValue()).isEmpty());
    assertTrue(((Collection<ConfigItem<UnorderedCurrencyPair>>) nameCache.get(Triple.of(UnorderedCurrencyPair.class, "VERSIONED 3", VC_1))
        .getObjectValue()).isEmpty());
    assertNull(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VersionCorrection.LATEST)));
    assertEquals(classCache.get(Pairs.of(UnorderedCurrencyPair.class, VC_1)).getObjectValue(), Collections.singleton(VERSIONED_ITEM_1));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static void populateByUniqueId(final ConfigSource source) {
    Mockito.when(source.changeManager()).thenReturn(new BasicChangeManager());
    // get by uid
    Mockito.when(source.get(LATEST_ITEM_1.getUniqueId())).thenReturn((ConfigItem) LATEST_ITEM_1);
    Mockito.when(source.get(LATEST_ITEM_2.getUniqueId())).thenReturn((ConfigItem) LATEST_ITEM_2);
    Mockito.when(source.get(VERSIONED_ITEM_1.getUniqueId())).thenReturn((ConfigItem) VERSIONED_ITEM_1);
    Mockito.when(source.get(VERSIONED_ITEM_2.getUniqueId())).thenReturn((ConfigItem) VERSIONED_ITEM_2);
    // get by uid collection
    final Map<UniqueId, ConfigItem<?>> allUid = new HashMap<>();
    allUid.put(VERSIONED_ITEM_1.getUniqueId(), VERSIONED_ITEM_1);
    allUid.put(VERSIONED_ITEM_2.getUniqueId(), VERSIONED_ITEM_2);
    allUid.put(LATEST_ITEM_1.getUniqueId(), LATEST_ITEM_1);
    allUid.put(LATEST_ITEM_2.getUniqueId(), LATEST_ITEM_2);
    final Map<UniqueId, ConfigItem<?>> versionUid = new HashMap<>();
    versionUid.put(VERSIONED_ITEM_1.getUniqueId(), VERSIONED_ITEM_1);
    versionUid.put(VERSIONED_ITEM_2.getUniqueId(), VERSIONED_ITEM_2);
    final Map<UniqueId, ConfigItem<?>> latestUid = new HashMap<>();
    latestUid.put(LATEST_ITEM_1.getUniqueId(), LATEST_ITEM_1);
    latestUid.put(LATEST_ITEM_2.getUniqueId(), LATEST_ITEM_2);
    Mockito.when(source
        .get(Arrays.asList(VERSIONED_ITEM_1.getUniqueId(), VERSIONED_ITEM_2.getUniqueId())))
        .thenReturn(versionUid);
    Mockito.when(source
        .get(Arrays.asList(LATEST_ITEM_1.getUniqueId(), LATEST_ITEM_2.getUniqueId())))
        .thenReturn(latestUid);
    Mockito.when(source
        .get(Arrays.asList(VERSIONED_ITEM_1.getUniqueId(), VERSIONED_ITEM_2.getUniqueId(), LATEST_ITEM_1.getUniqueId(),
            LATEST_ITEM_2.getUniqueId())))
        .thenReturn(allUid);
    // add a non-existent item
    Mockito.when(source.get(UniqueId.of("test", "3"))).thenReturn(null);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static void populateByObjectId(final ConfigSource source) {
    Mockito.when(source.changeManager()).thenReturn(new BasicChangeManager());
    // get by oid
    Mockito.when(source.get(LATEST_ITEM_1.getUniqueId().getObjectId(), VersionCorrection.LATEST)).thenReturn((ConfigItem) LATEST_ITEM_1);
    Mockito.when(source.get(LATEST_ITEM_2.getUniqueId().getObjectId(), VersionCorrection.LATEST)).thenReturn((ConfigItem) LATEST_ITEM_2);
    Mockito.when(source.get(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VC_1)).thenReturn((ConfigItem) VERSIONED_ITEM_1);
    Mockito.when(source.get(VERSIONED_ITEM_2.getUniqueId().getObjectId(), VC_2)).thenReturn((ConfigItem) VERSIONED_ITEM_2);
    // get by oid collection
    final Map<ObjectId, ConfigItem<?>> allOid = new HashMap<>();
    allOid.put(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VERSIONED_ITEM_1);
    allOid.put(VERSIONED_ITEM_2.getUniqueId().getObjectId(), VERSIONED_ITEM_2);
    allOid.put(LATEST_ITEM_1.getUniqueId().getObjectId(), LATEST_ITEM_1);
    allOid.put(LATEST_ITEM_2.getUniqueId().getObjectId(), LATEST_ITEM_2);
    final Map<ObjectId, ConfigItem<?>> versionOid = new HashMap<>();
    versionOid.put(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VERSIONED_ITEM_1);
    versionOid.put(VERSIONED_ITEM_2.getUniqueId().getObjectId(), VERSIONED_ITEM_2);
    final Map<ObjectId, ConfigItem<?>> latestOid = new HashMap<>();
    latestOid.put(LATEST_ITEM_1.getUniqueId().getObjectId(), LATEST_ITEM_1);
    latestOid.put(LATEST_ITEM_2.getUniqueId().getObjectId(), LATEST_ITEM_2);
    Mockito.when(source
        .get(Arrays.asList(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VERSIONED_ITEM_2.getUniqueId().getObjectId()), VC_1))
        .thenReturn(versionOid);
    Mockito.when(source
        .get(Arrays.asList(LATEST_ITEM_1.getUniqueId().getObjectId(), LATEST_ITEM_2.getUniqueId().getObjectId()), VersionCorrection.LATEST))
        .thenReturn(latestOid);
    Mockito.when(source
        .get(Arrays.asList(VERSIONED_ITEM_1.getUniqueId().getObjectId(), VERSIONED_ITEM_2.getUniqueId().getObjectId(),
            LATEST_ITEM_1.getUniqueId().getObjectId(), LATEST_ITEM_2.getUniqueId().getObjectId()), VC_2))
        .thenReturn(allOid);
    // add a non-existent item
    Mockito.when(source.get(UniqueId.of("test", "3"))).thenReturn(null);
  }

  private static void populateByClassName(final ConfigSource source) {
    Mockito.when(source.changeManager()).thenReturn(new BasicChangeManager());

    Mockito.when(source.get(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST))
        .thenReturn(Collections.<ConfigItem<UnorderedCurrencyPair>> singleton(LATEST_ITEM_1));
    Mockito.when(source.get(UnorderedCurrencyPair.class, "LATEST 1", null))
        .thenReturn(Collections.<ConfigItem<UnorderedCurrencyPair>> singleton(LATEST_ITEM_1));
    Mockito.when(source.get(UnorderedCurrencyPair.class, "LATEST 2", VersionCorrection.LATEST))
        .thenReturn(Collections.<ConfigItem<UnorderedCurrencyPair>> singleton(LATEST_ITEM_2));
    Mockito.when(source.get(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1))
        .thenReturn(Collections.<ConfigItem<UnorderedCurrencyPair>> singleton(VERSIONED_ITEM_1));
    Mockito.when(source.get(UnorderedCurrencyPair.class, "VERSIONED 1", null))
        .thenReturn(Collections.<ConfigItem<UnorderedCurrencyPair>> singleton(VERSIONED_ITEM_1));
    Mockito.when(source.get(UnorderedCurrencyPair.class, "VERSIONED 2", VC_2))
        .thenReturn(Collections.<ConfigItem<UnorderedCurrencyPair>> singleton(VERSIONED_ITEM_2));

    Mockito.when(source.getSingle(UnorderedCurrencyPair.class, "LATEST 1", VersionCorrection.LATEST))
        .thenReturn(LATEST_1);
    Mockito.when(source.getSingle(UnorderedCurrencyPair.class, "LATEST 2", VersionCorrection.LATEST))
        .thenReturn(LATEST_2);
    Mockito.when(source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 1", VC_1))
        .thenReturn(VERSIONED_1);
    Mockito.when(source.getSingle(UnorderedCurrencyPair.class, "VERSIONED 2", VC_2))
        .thenReturn(VERSIONED_2);
  }

  private static void populateByClass(final ConfigSource source) {
    Mockito.when(source.changeManager()).thenReturn(new BasicChangeManager());

    Mockito.when(source.getAll(UnorderedCurrencyPair.class, VersionCorrection.LATEST))
        .thenReturn(Arrays.asList(LATEST_ITEM_1, LATEST_ITEM_2));
    Mockito.when(source.getAll(UnorderedCurrencyPair.class, null))
        .thenReturn(Arrays.asList(LATEST_ITEM_1, LATEST_ITEM_2));
    Mockito.when(source.getAll(UnorderedCurrencyPair.class, VC_1))
        .thenReturn(Collections.<ConfigItem<UnorderedCurrencyPair>> singleton(VERSIONED_ITEM_1));
    Mockito.when(source.getAll(UnorderedCurrencyPair.class, VC_2))
        .thenReturn(Collections.<ConfigItem<UnorderedCurrencyPair>> singleton(VERSIONED_ITEM_2));
    Mockito.when(source.getAll(Currency.class, VC_1))
        .thenReturn(null);
  }
}
