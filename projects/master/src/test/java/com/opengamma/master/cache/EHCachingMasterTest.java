/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

/**
 * Test EHCaching master behaviour that is common across all masters, using dummy TestDocument and TestMaster classes.
 */
@Test(groups = TestGroup.UNIT)
public class EHCachingMasterTest extends AbstractEHCachingMasterTest<CacheTestMaster, CacheTestDocument> {

  {
    // Initialise security documents

    // Document A
    _docA100V1999to2010Cto2011 = new CacheTestDocument(A100_UID);
    _docA200V2010to = new CacheTestDocument(A200_UID);
    _docA300V1999to2010C2011to = new CacheTestDocument(A300_UID);

    // Document B
    _docB200V2000to2009 = new CacheTestDocument(B200_UID);
    _docB400V2009to2011 = new CacheTestDocument(B400_UID);
    _docB500V2011to = new CacheTestDocument(B500_UID);

    // Document C
    _docC100Vto2011 = new CacheTestDocument(C100_UID);
    _docC300V2011to = new CacheTestDocument(C300_UID);

    // Document to add
    _docToAdd = new CacheTestDocument(null);
    _docAdded = new CacheTestDocument(ADDED_UID);
  }

  /**
   *
   */
  class EHCachingTestMaster extends AbstractEHCachingMaster<CacheTestDocument> {
    /**
     * @param name
     *          the name
     * @param underlying
     *          the underlying master
     * @param cacheManager
     *          the cache manager
     */
    EHCachingTestMaster(final String name, final AbstractChangeProvidingMaster<CacheTestDocument> underlying, final CacheManager cacheManager) {
      super(name, underlying, cacheManager);
    }
  }

  // -------------------------------------------------------------------------
  private CacheManager _cacheManager;

  /**
   *
   */
  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCachingMasterTest.class);
  }

  /**
   *
   */
  @BeforeMethod
  public void setUp() {
    EHCacheUtils.clear(_cacheManager);
  }

  /**
   *
   */
  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  /**
   *
   */
  @Test
  public void testGetUidVersioned() {
    final CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    final EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    // Assert returned documents
    assertEquals(_docB200V2000to2009, cachingMaster.get(_docB200V2000to2009.getUniqueId()));
    assertEquals(_docA100V1999to2010Cto2011, cachingMaster.get(_docA100V1999to2010Cto2011.getUniqueId()));
    assertEquals(_docA100V1999to2010Cto2011, cachingMaster.get(_docA100V1999to2010Cto2011.getUniqueId()));
    assertEquals(_docA100V1999to2010Cto2011, cachingMaster.get(_docA100V1999to2010Cto2011.getUniqueId()));
    assertEquals(_docB200V2000to2009, cachingMaster.get(_docB200V2000to2009.getUniqueId()));
    assertEquals(_docB200V2000to2009, cachingMaster.get(_docB200V2000to2009.getUniqueId()));
    assertEquals(_docA100V1999to2010Cto2011, cachingMaster.get(_docA100V1999to2010Cto2011.getUniqueId()));
    assertEquals(_docB200V2000to2009, cachingMaster.get(_docB200V2000to2009.getUniqueId()));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).get(_docA100V1999to2010Cto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docA200V2010to.getUniqueId());
    verify(mockUnderlyingMaster, times(1)).get(_docB200V2000to2009.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB400V2009to2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB500V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docC100Vto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docC300V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docA200V2010to.getObjectId(), VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(_docB500V2011to.getObjectId(), VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(_docC300V2011to.getObjectId(), VersionCorrection.LATEST);

    cachingMaster.shutdown();
  }

  /**
   *
   */
  @Test
  public void testGetUidUnversioned() {
    final CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    final EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    // Assert returned documents
    assertEquals(_docB500V2011to, cachingMaster.get(_docB200V2000to2009.getUniqueId().toLatest()));
    assertEquals(_docA200V2010to, cachingMaster.get(_docA100V1999to2010Cto2011.getUniqueId().toLatest()));
    assertEquals(_docA200V2010to, cachingMaster.get(_docA100V1999to2010Cto2011.getUniqueId().toLatest()));
    assertEquals(_docA200V2010to, cachingMaster.get(_docA100V1999to2010Cto2011.getUniqueId().toLatest()));
    assertEquals(_docB500V2011to, cachingMaster.get(_docB200V2000to2009.getUniqueId().toLatest()));
    assertEquals(_docB500V2011to, cachingMaster.get(_docB200V2000to2009.getUniqueId().toLatest()));
    assertEquals(_docA200V2010to, cachingMaster.get(_docA100V1999to2010Cto2011.getUniqueId().toLatest()));
    assertEquals(_docB500V2011to, cachingMaster.get(_docB200V2000to2009.getUniqueId().toLatest()));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).get(A300_UID.toLatest());
    verify(mockUnderlyingMaster, times(1)).get(B500_UID.toLatest());
    verify(mockUnderlyingMaster, times(0)).get(C300_UID.toLatest());
    verify(mockUnderlyingMaster, times(0)).get(_docA100V1999to2010Cto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docA200V2010to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB200V2000to2009.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB400V2009to2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB500V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docC100Vto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docC300V2011to.getUniqueId());

    cachingMaster.shutdown();
  }

  /**
   *
   */
  @Test
  public void testGetOidLatestVersionCorrection() {
    final CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    final EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    // Assert returned documents
    assertEquals(_docB500V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(_docA200V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(_docA200V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(_docA200V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(_docB500V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(_docB500V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(_docA200V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(_docB500V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).get(A_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(1)).get(B_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(C_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(_docA100V1999to2010Cto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docA200V2010to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB200V2000to2009.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB400V2009to2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB500V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docC100Vto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docC300V2011to.getUniqueId());

    cachingMaster.shutdown();
  }

  /**
   *
   */
  @Test
  public void testGetOidMixedVersionCorrection() {
    final CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    final EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    // TODO enhance testing of v/c range border cases

    // Assert returned documents
    assertEquals(_docB500V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(_docA100V1999to2010Cto2011,
        cachingMaster.get(A_OID, VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2009, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(),
            ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant())));
    assertEquals(_docA200V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(_docA300V1999to2010C2011to,
        cachingMaster.get(A_OID, VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2009, 6, 6, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(), NOW)));
    assertEquals(_docB500V2011to, cachingMaster.get(B_OID, VersionCorrection.of(NOW, NOW)));
    assertEquals(_docB500V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(_docA200V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(_docB500V2011to,
        cachingMaster.get(B_OID, VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2011, 6, 6, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(), NOW)));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).get(B_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(1)).get(A_OID,
        VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2009, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(),
            ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant()));
    verify(mockUnderlyingMaster, times(1)).get(A_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(1)).get(A_OID,
        VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2009, 6, 6, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(), NOW));
    verify(mockUnderlyingMaster, times(0)).get(B_OID, VersionCorrection.of(NOW, NOW));
    verify(mockUnderlyingMaster, times(0)).get(B_OID,
        VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2011, 6, 6, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(), NOW));
    verify(mockUnderlyingMaster, times(0)).get(C_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(_docA100V1999to2010Cto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docA200V2010to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docA300V1999to2010C2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB200V2000to2009.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB400V2009to2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docB500V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docC100Vto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docC300V2011to.getUniqueId());

    cachingMaster.shutdown();
  }

  // @Test
  // public void testCachedMiss() {
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }
  //
  //// -------------------------------------------------------------------------
  //
  // @Test
  // public void testUpdate() {
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }

  /**
   *
   */
  @Test
  public void testAdd() {
    final CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    final EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    // Assert returned documents
    assertEquals(_docAdded, cachingMaster.add(_docToAdd));

    // Assert cache contents
    assertEquals(_docAdded, cachingMaster.get(_docAdded.getUniqueId()));
    assertEquals(_docAdded, cachingMaster.get(_docAdded.getObjectId(), VersionCorrection.LATEST));
    assertEquals(_docAdded, cachingMaster.get(_docAdded.getObjectId(), VersionCorrection.of(NOW, NOW)));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).add(_docToAdd);
    verify(mockUnderlyingMaster, times(0)).add(_docAdded);
    verify(mockUnderlyingMaster, times(0)).get(_docAdded.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(_docToAdd.getUniqueId());

    cachingMaster.shutdown();
  }

  // @Test
  // public void testRemove() {
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }
  //
  // @Test
  // public void testCorrect() { // same as replaceVersion()
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }
  //
  // @Test
  // public void testReplaceVersion() {
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }
  //
  // @Test
  // public void testReplaceAllVersions() {
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }
  //
  // @Test
  // public void testReplaceVersions() {
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }
  //
  // @Test
  // public void testRemoveVersion() {
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }
  //
  // @Test
  // public void testAddVersion() {
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }
  //
  // @Test
  // public void testChangeProvider() {
  // TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  // EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  // //TODO
  //
  // cachingMaster.shutdown();
  // }

}
