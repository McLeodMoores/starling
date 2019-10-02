/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.Collection;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EHCachingFinancialSecuritySourceTest {

  private MockFinancialSecuritySource _underlyingSecuritySource = null;
  private EHCachingFinancialSecuritySource _cachingSecuritySource = null;
  private final ExternalId _secId1 = ExternalId.of("d1", "v1");
  private final ExternalId _secId2 = ExternalId.of("d1", "v2");
  private final SimpleSecurity _security1 = new SimpleSecurity("");
  private final SimpleSecurity _security1Alternate = new SimpleSecurity("alternate");
  private final SimpleSecurity _security2 = new SimpleSecurity("");
  private CacheManager _cacheManager;

  /**
   *
   */
  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  /**
   *
   */
  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  /**
   * @throws Exception
   *           if there is an unexpected problem
   */
  @BeforeMethod
  public void setUp() throws Exception {
    EHCacheUtils.clear(_cacheManager);
    _underlyingSecuritySource = new MockFinancialSecuritySource();
    _cachingSecuritySource = new EHCachingFinancialSecuritySource(_underlyingSecuritySource, _cacheManager);

    _security1.addExternalId(_secId1);
    _security1Alternate.addExternalId(_secId1);
    _security2.addExternalId(_secId2);
  }

  /**
   * @throws Exception
   *           if there is an unexpected problem
   */
  @AfterMethod
  public void tearDown() throws Exception {
    if (_cachingSecuritySource != null) {
      _cachingSecuritySource.shutdown();
    }
    _underlyingSecuritySource = null;
    _cachingSecuritySource = null;
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test
  public void getSecurityUniqueId() {
    addSecuritiesToMock(_security1, _security2);

    final UniqueId uid1 = _security1.getUniqueId();
    final Security underlyingSec = _underlyingSecuritySource.get(uid1);
    Security cachedSec = _cachingSecuritySource.get(uid1);
    assertNotNull(underlyingSec);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);

    final Cache singleSecCache = _cacheManager.getCache(EHCachingFinancialSecuritySource.class.getName() + "-uid-cache");
    assertEquals(1, singleSecCache.getSize());
    final Element element = singleSecCache.getQuiet(uid1);
    assertNotNull(element);
    for (int i = 1; i < 10; i++) {
      cachedSec = _cachingSecuritySource.get(uid1);
      assertNotNull(cachedSec);
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void getSecurityUniqueIdEmpty() {
    final Cache singleSecCache = _cacheManager.getCache(EHCachingFinancialSecuritySource.class.getName() + "-uid-cache");

    final UniqueId uid = UniqueId.of("Mock", "99");
    try {
      _cachingSecuritySource.get(uid);
    } finally {
      assertEquals(0, singleSecCache.getSize());
      final Element element = singleSecCache.get(uid);
      assertNull(element);
    }
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test
  public void getSecuritiesExternalIdBundle() {
    addSecuritiesToMock(_security1, _security2);
    final ExternalIdBundle secKey = ExternalIdBundle.of(_secId1, _secId2);

    final Collection<Security> underlyingSecurities = _underlyingSecuritySource.get(secKey);
    assertNotNull(underlyingSecurities);
    final Collection<? extends Security> cachedSecurities = _cachingSecuritySource.get(secKey);
    assertNotNull(cachedSecurities);
    assertEquals(underlyingSecurities, cachedSecurities);

    final Cache singleSecCache = _cacheManager.getCache(EHCachingFinancialSecuritySource.class.getName() + "-uid-cache");
    assertNotNull(singleSecCache);
    assertEquals(2, singleSecCache.getSize());

    final Element sec1Element = singleSecCache.getQuiet(_security1.getUniqueId());
    final Element sec2Element = singleSecCache.getQuiet(_security2.getUniqueId());
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.get(secKey);
      assertEquals(0, sec1Element.getHitCount());
      assertEquals(0, sec2Element.getHitCount());
    }
  }

  /**
   *
   */
  @Test
  public void getSecurityExternalIdBundleChanging() {
    final ExternalIdBundle secKey = ExternalIdBundle.of(_secId1, _secId2);
    addSecuritiesToMock(_security1);

    Security underlyingSecurity = _cachingSecuritySource.getSingle(secKey, VersionCorrection.LATEST);
    assertEquals(_security1, underlyingSecurity);

    _underlyingSecuritySource.removeSecurity(_security1);
    addSecuritiesToMock(_security1Alternate);

    underlyingSecurity = _cachingSecuritySource.getSingle(secKey, VersionCorrection.LATEST);
    assertEquals(_security1Alternate, underlyingSecurity);
  }

  /**
   *
   */
  @Test
  public void getSecuritiesExternalIdBundleEmpty() {
    final ExternalIdBundle secKey = ExternalIdBundle.of(_secId1);
    final Cache singleSecCache = _cacheManager.getCache(EHCachingFinancialSecuritySource.class.getName() + "-uid-cache");

    final Security cachedSec = _cachingSecuritySource.getSingle(secKey);
    assertNull(cachedSec);
    assertEquals(0, singleSecCache.getSize());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test
  public void getSecurityExternalIdBundle() {
    addSecuritiesToMock(_security1, _security2);

    final ExternalIdBundle secKey1 = ExternalIdBundle.of(_secId1);
    final Security underlyingSec = _underlyingSecuritySource.getSingle(secKey1);
    final Security cachedSec = _cachingSecuritySource.getSingle(secKey1);
    assertNotNull(underlyingSec);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);

    final Cache singleSecCache = _cacheManager.getCache(EHCachingFinancialSecuritySource.class.getName() + "-uid-cache");
    assertNotNull(singleSecCache);
    assertEquals(1, singleSecCache.getSize());

    final Element sec1Element = singleSecCache.getQuiet(_security1.getUniqueId());
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.get(secKey1);
      assertEquals(0, sec1Element.getHitCount());
    }
  }

  /**
   *
   */
  @Test
  public void getSecurityExternalIdBundleEmpty() {
    final ExternalIdBundle secKey = ExternalIdBundle.of(_secId1);
    final Cache singleSecCache = _cacheManager.getCache(EHCachingFinancialSecuritySource.class.getName() + "-uid-cache");

    final Security cachedSec = _cachingSecuritySource.getSingle(secKey);
    assertNull(cachedSec);
    assertEquals(0, singleSecCache.getSize());
  }

  private void addSecuritiesToMock(final Security... securities) {
    for (final Security security : securities) {
      _underlyingSecuritySource.addSecurity(security);
    }
  }

}
