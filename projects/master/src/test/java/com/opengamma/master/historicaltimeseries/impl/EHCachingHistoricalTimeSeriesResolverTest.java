/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolverWithBasicChangeManager;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

/**
 * Test.
 */
@Test(groups = { TestGroup.UNIT, "ehcache" })
public class EHCachingHistoricalTimeSeriesResolverTest {

  private final LocalDate _date1 = LocalDate.now();
  private final LocalDate _date2 = _date1.plusDays(1);
  private final LocalDate _date3 = _date2.plusDays(1);

  private final ExternalId _id1 = ExternalId.of("Foo", "1");
  private final ExternalId _id2 = ExternalId.of("Foo", "2");
  private final ExternalId _id3 = ExternalId.of("Bar", "1");

  private final ExternalIdBundle _bundleAll = ExternalIdBundle.of(_id1, _id2);
  private final ExternalIdBundle _bundle1 = _id1.toBundle();
  private final ExternalIdBundle _bundle2 = _id2.toBundle();
  private final ExternalIdBundle _bundle3 = _id3.toBundle();

  private final ExternalIdWithDates _dateId1 = ExternalIdWithDates.of(_id1, null, null);
  private final ExternalIdWithDates _dateId2 = ExternalIdWithDates.of(_id2, _date2, _date3);

  private final ExternalIdBundleWithDates _dateBundle = ExternalIdBundleWithDates.of(_dateId1, _dateId2);

  private final String _source1 = "S1";
  private final String _source2 = "S2";

  private final String _provider1 = "P1";
  private final String _provider2 = "P2";

  private final String _field1 = "F1";
  private final String _field2 = "F2";

  private final String _key1 = "K1";
  private final String _key2 = "K2";

  private CacheManager _cacheManager;

  /**
   *
   */
  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCachingHistoricalTimeSeriesResolverTest.class);
  }

  /**
   *
   */
  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  // -------------------------------------------------------------------------
  private HistoricalTimeSeriesResolver createUnderlying(final AtomicInteger hits) {
    return new HistoricalTimeSeriesResolverWithBasicChangeManager() {

      @Override
      public HistoricalTimeSeriesResolutionResult resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
          final String dataSource, final String dataProvider, final String dataField, final String resolutionKey) {
        hits.incrementAndGet();
        if (dataSource != null && !_source1.equals(dataSource)) {
          return null;
        }
        if (dataProvider != null && !_provider1.equals(dataProvider)) {
          return null;
        }
        if (!_field1.equals(dataField)) {
          return null;
        }
        if (identifierBundle != null) {
          if (_key2.equals(resolutionKey)) {
            return null;
          }
          for (final ExternalIdWithDates id : _dateBundle.getExternalIds()) {
            if (identifierValidityDate == null || id.isValidOn(identifierValidityDate)) {
              if (identifierBundle.contains(id.getExternalId())) {
                final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
                info.setDataField(_field1);
                info.setDataProvider(_provider1);
                info.setDataSource(_source1);
                info.setExternalIdBundle(_dateBundle);
                info.setName("Foo");
                info.setObservationTime("Close");
                return new HistoricalTimeSeriesResolutionResult(info);
              }
            }
          }
          return null;
        }
        return new HistoricalTimeSeriesResolutionResult(null);
      }
    };
  }

  private EHCachingHistoricalTimeSeriesResolver createResolver(final boolean optimistic, final AtomicInteger hits) {
    final EHCachingHistoricalTimeSeriesResolver resolver = new EHCachingHistoricalTimeSeriesResolver(createUnderlying(hits), _cacheManager,
        getClass().getName());
    resolver.setOptimisticFieldResolution(optimistic);
    return resolver;
  }

  private void testResolveNull(final boolean optimistic, final Collection<EHCachingHistoricalTimeSeriesResolver> resolvers) {
    final AtomicInteger hits = new AtomicInteger();
    final EHCachingHistoricalTimeSeriesResolver resolver = createResolver(optimistic, hits);
    resolvers.add(resolver);
    for (int i = 0; i < 3; i++) {
      assertNotNull(resolver.resolve(null, null, null, null, _field1, null));
      assertNull(resolver.resolve(null, null, null, null, _field2, null));
      assertNotNull(resolver.resolve(null, null, null, _provider1, _field1, null));
      assertNull(resolver.resolve(null, null, null, _provider1, _field2, null));
      assertNull(resolver.resolve(null, null, null, _provider2, _field1, null));
      assertNull(resolver.resolve(null, null, null, _provider2, _field2, null));
      assertNotNull(resolver.resolve(null, null, _source1, null, _field1, null));
      assertNull(resolver.resolve(null, null, _source1, null, _field2, null));
      assertNull(resolver.resolve(null, null, _source2, null, _field1, null));
      assertNull(resolver.resolve(null, null, _source2, null, _field2, null));
      assertNotNull(resolver.resolve(null, null, _source1, _provider1, _field1, null));
      assertNull(resolver.resolve(null, null, _source1, _provider1, _field2, null));
      assertNull(resolver.resolve(null, null, _source2, _provider1, _field1, null));
      assertNull(resolver.resolve(null, null, _source2, _provider1, _field2, null));
      assertNull(resolver.resolve(null, null, _source1, _provider2, _field1, null));
      assertNull(resolver.resolve(null, null, _source1, _provider2, _field2, null));
      assertNull(resolver.resolve(null, null, _source2, _provider2, _field1, null));
      assertNull(resolver.resolve(null, null, _source2, _provider2, _field2, null));
      assertEquals(hits.get(), 18);
    }
  }

  /**
   *
   */
  public void testResolveNullOpt() {
    final List<EHCachingHistoricalTimeSeriesResolver> resolvers = new LinkedList<>();
    try {
      testResolveNull(true, resolvers);
    } finally {
      for (final EHCachingHistoricalTimeSeriesResolver resolver : resolvers) {
        resolver.shutdown();
      }
    }
  }

  /**
   *
   */
  public void testResolveNullPess() {
    final List<EHCachingHistoricalTimeSeriesResolver> resolvers = new LinkedList<>();
    try {
      testResolveNull(false, resolvers);
    } finally {
      for (final EHCachingHistoricalTimeSeriesResolver resolver : resolvers) {
        resolver.shutdown();
      }
    }
  }

  private void testResolveDates(final boolean optimistic, final String dataSource, final String dataProvider, final String resolutionKey,
      final boolean expectTS, final int hitMask, final Collection<EHCachingHistoricalTimeSeriesResolver> resolvers) {
    final AtomicInteger hits = new AtomicInteger();
    final EHCachingHistoricalTimeSeriesResolver resolver = createResolver(optimistic, hits);
    resolvers.add(resolver);
    for (int i = 0; i < 3; i++) {
      assertEquals(resolver.resolve(_bundleAll, _date1, dataSource, dataProvider, _field1, resolutionKey) != null, expectTS);
      assertEquals(hits.getAndSet(0), i == 0 ? (hitMask & 0x1) != 0 ? 1 : (hitMask & 0x2) != 0 ? 2 : 0 : 0);
      assertNull(resolver.resolve(_bundleAll, _date1, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x4) != 0 ? 1 : 0);
      assertEquals(resolver.resolve(_bundleAll, _date2, dataSource, dataProvider, _field1, resolutionKey) != null, expectTS);
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x8) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundleAll, _date2, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x10) != 0 ? 1 : 0);
      assertEquals(resolver.resolve(_bundle1, _date1, dataSource, dataProvider, _field1, resolutionKey) != null, expectTS);
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x20) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundle1, _date1, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x40) != 0 ? 1 : 0);
      assertEquals(resolver.resolve(_bundle1, _date2, dataSource, dataProvider, _field1, resolutionKey) != null, expectTS);
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x80) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundle1, _date2, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x100) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundle2, _date1, dataSource, dataProvider, _field1, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 ? (hitMask & 0x200) != 0 ? 1 : (hitMask & 0x400) != 0 ? 2 : 0 : 0);
      assertNull(resolver.resolve(_bundle2, _date1, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x800) != 0 ? 1 : 0);
      assertEquals(resolver.resolve(_bundle2, _date2, dataSource, dataProvider, _field1, resolutionKey) != null, expectTS);
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x1000) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundle2, _date2, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x2000) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundle3, _date1, dataSource, dataProvider, _field1, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x4000) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundle3, _date1, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x8000) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundle3, _date2, dataSource, dataProvider, _field1, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x10000) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundle3, _date2, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x20000) != 0 ? 1 : 0);
    }
  }

  /**
   *
   */
  public void testResolveDatesOpt() {
    final List<EHCachingHistoricalTimeSeriesResolver> resolvers = new LinkedList<>();
    try {
      testResolveDates(true, null, null, null, true, 0x3D215, resolvers);
      testResolveDates(true, null, null, _key1, true, 0x3D215, resolvers);
      testResolveDates(true, null, null, _key2, false, 0x3C01D, resolvers);
      testResolveDates(true, null, _provider1, null, true, 0x3C214, resolvers);
      testResolveDates(true, null, _provider1, _key1, true, 0x3C214, resolvers);
      testResolveDates(true, null, _provider1, _key2, false, 0x3C01D, resolvers);
      testResolveDates(true, null, _provider2, null, false, 0x3C01D, resolvers);
      testResolveDates(true, null, _provider2, _key1, false, 0x3C01D, resolvers);
      testResolveDates(true, null, _provider2, _key2, false, 0x3C01D, resolvers);
      testResolveDates(true, _source1, null, null, true, 0x3C214, resolvers);
      testResolveDates(true, _source1, null, _key1, true, 0x3C214, resolvers);
      testResolveDates(true, _source1, null, _key2, false, 0x3C01D, resolvers);
      testResolveDates(true, _source1, _provider1, null, true, 0x3C214, resolvers);
      testResolveDates(true, _source1, _provider1, _key1, true, 0x3C214, resolvers);
      testResolveDates(true, _source1, _provider1, _key2, false, 0x3C01D, resolvers);
      testResolveDates(true, _source1, _provider2, null, false, 0x3C01D, resolvers);
      testResolveDates(true, _source1, _provider2, _key1, false, 0x3C01D, resolvers);
      testResolveDates(true, _source1, _provider2, _key2, false, 0x3C01D, resolvers);
      testResolveDates(true, _source2, null, null, false, 0x3C01D, resolvers);
      testResolveDates(true, _source2, null, _key1, false, 0x3C01D, resolvers);
      testResolveDates(true, _source2, null, _key2, false, 0x3C01D, resolvers);
      testResolveDates(true, _source2, _provider1, null, false, 0x3C01D, resolvers);
      testResolveDates(true, _source2, _provider1, _key1, false, 0x3C01D, resolvers);
      testResolveDates(true, _source2, _provider1, _key2, false, 0x3C01D, resolvers);
      testResolveDates(true, _source2, _provider2, null, false, 0x3C01D, resolvers);
      testResolveDates(true, _source2, _provider2, _key1, false, 0x3C01D, resolvers);
      testResolveDates(true, _source2, _provider2, _key2, false, 0x3C01D, resolvers);
    } finally {
      for (final EHCachingHistoricalTimeSeriesResolver resolver : resolvers) {
        resolver.shutdown();
      }
    }
  }

  /**
   *
   */
  public void testResolveDatesPess() {
    final List<EHCachingHistoricalTimeSeriesResolver> resolvers = new LinkedList<>();
    try {
      testResolveDates(false, null, null, null, true, 0x15206, resolvers);
      testResolveDates(false, null, null, _key1, true, 0x15201, resolvers);
      testResolveDates(false, null, null, _key2, false, 0x14009, resolvers);
      testResolveDates(false, null, _provider1, null, true, 0x14404, resolvers);
      testResolveDates(false, null, _provider1, _key1, true, 0x14200, resolvers);
      testResolveDates(false, null, _provider1, _key2, false, 0x14009, resolvers);
      testResolveDates(false, null, _provider2, null, false, 0x5, resolvers);
      testResolveDates(false, null, _provider2, _key1, false, 0, resolvers);
      testResolveDates(false, null, _provider2, _key2, false, 0, resolvers);
      testResolveDates(false, _source1, null, null, true, 0x14404, resolvers);
      testResolveDates(false, _source1, null, _key1, true, 0x14200, resolvers);
      testResolveDates(false, _source1, null, _key2, false, 0x14009, resolvers);
      testResolveDates(false, _source1, _provider1, null, true, 0x14404, resolvers);
      testResolveDates(false, _source1, _provider1, _key1, true, 0x14200, resolvers);
      testResolveDates(false, _source1, _provider1, _key2, false, 0x14009, resolvers);
      testResolveDates(false, _source1, _provider2, null, false, 0x5, resolvers);
      testResolveDates(false, _source1, _provider2, _key1, false, 0, resolvers);
      testResolveDates(false, _source1, _provider2, _key2, false, 0, resolvers);
      testResolveDates(false, _source2, null, null, false, 0x5, resolvers);
      testResolveDates(false, _source2, null, _key1, false, 0, resolvers);
      testResolveDates(false, _source2, null, _key2, false, 0, resolvers);
      testResolveDates(false, _source2, _provider1, null, false, 0x5, resolvers);
      testResolveDates(false, _source2, _provider1, _key1, false, 0, resolvers);
      testResolveDates(false, _source2, _provider1, _key2, false, 0, resolvers);
      testResolveDates(false, _source2, _provider2, null, false, 0x5, resolvers);
      testResolveDates(false, _source2, _provider2, _key1, false, 0, resolvers);
      testResolveDates(false, _source2, _provider2, _key2, false, 0, resolvers);
    } finally {
      for (final EHCachingHistoricalTimeSeriesResolver resolver : resolvers) {
        resolver.shutdown();
      }
    }
  }

  private void testResolveNodates(final boolean optimistic, final String dataSource, final String dataProvider, final String resolutionKey,
      final boolean expectTS, final int hitMask, final Collection<EHCachingHistoricalTimeSeriesResolver> resolvers) {
    final AtomicInteger hits = new AtomicInteger();
    final EHCachingHistoricalTimeSeriesResolver resolver = createResolver(optimistic, hits);
    resolvers.add(resolver);
    for (int i = 0; i < 3; i++) {
      assertEquals(resolver.resolve(_bundleAll, null, dataSource, dataProvider, _field1, resolutionKey) != null, expectTS);
      assertEquals(hits.getAndSet(0), i == 0 ? (hitMask & 0x1) != 0 ? 1 : (hitMask & 0x2) != 0 ? 2 : 0 : 0);
      assertNull(resolver.resolve(_bundleAll, null, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x4) != 0 ? 1 : 0);
      assertEquals(resolver.resolve(_bundle1, null, dataSource, dataProvider, _field1, resolutionKey) != null, expectTS);
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x8) != 0 ? 1 : 0);
      assertNull(resolver.resolve(_bundle1, null, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x10) != 0 ? 1 : 0);
      assertEquals(resolver.resolve(_bundle2, null, dataSource, dataProvider, _field1, resolutionKey) != null, expectTS);
      assertEquals(hits.getAndSet(0), i == 0 ? (hitMask & 0x20) != 0 ? 1 : (hitMask & 0x40) != 0 ? 2 : 0 : 0);
      assertNull(resolver.resolve(_bundle2, null, dataSource, dataProvider, _field2, resolutionKey));
      assertEquals(hits.getAndSet(0), i == 0 && (hitMask & 0x80) != 0 ? 1 : 0);
    }
  }

  /**
   *
   */
  public void testResolveNodatesOpt() {
    final List<EHCachingHistoricalTimeSeriesResolver> resolvers = new LinkedList<>();
    try {
      testResolveNodates(true, null, null, null, true, 0x5, resolvers);
      testResolveNodates(true, null, null, _key1, true, 0x5, resolvers);
      testResolveNodates(true, null, null, _key2, false, 0x5, resolvers);
      testResolveNodates(true, null, _provider1, null, true, 0x4, resolvers);
      testResolveNodates(true, null, _provider1, _key1, true, 0x4, resolvers);
      testResolveNodates(true, null, _provider1, _key2, false, 0x5, resolvers);
      testResolveNodates(true, null, _provider2, null, false, 0x5, resolvers);
      testResolveNodates(true, null, _provider2, _key1, false, 0x5, resolvers);
      testResolveNodates(true, null, _provider2, _key2, false, 0x5, resolvers);
      testResolveNodates(true, _source1, null, null, true, 0x4, resolvers);
      testResolveNodates(true, _source1, null, _key1, true, 0x4, resolvers);
      testResolveNodates(true, _source1, null, _key2, false, 0x5, resolvers);
      testResolveNodates(true, _source1, _provider1, null, true, 0x4, resolvers);
      testResolveNodates(true, _source1, _provider1, _key1, true, 0x4, resolvers);
      testResolveNodates(true, _source1, _provider1, _key2, false, 0x5, resolvers);
      testResolveNodates(true, _source1, _provider2, null, false, 0x5, resolvers);
      testResolveNodates(true, _source1, _provider2, _key1, false, 0x5, resolvers);
      testResolveNodates(true, _source1, _provider2, _key2, false, 0x5, resolvers);
      testResolveNodates(true, _source2, null, null, false, 0x5, resolvers);
      testResolveNodates(true, _source2, null, _key1, false, 0x5, resolvers);
      testResolveNodates(true, _source2, null, _key2, false, 0x5, resolvers);
      testResolveNodates(true, _source2, _provider1, null, false, 0x5, resolvers);
      testResolveNodates(true, _source2, _provider1, _key1, false, 0x5, resolvers);
      testResolveNodates(true, _source2, _provider1, _key2, false, 0x5, resolvers);
      testResolveNodates(true, _source2, _provider2, null, false, 0x5, resolvers);
      testResolveNodates(true, _source2, _provider2, _key1, false, 0x5, resolvers);
      testResolveNodates(true, _source2, _provider2, _key2, false, 0x5, resolvers);
    } finally {
      for (final EHCachingHistoricalTimeSeriesResolver resolver : resolvers) {
        resolver.shutdown();
      }
    }
  }

  /**
   *
   */
  public void testResolveNodatesPess() {
    final List<EHCachingHistoricalTimeSeriesResolver> resolvers = new LinkedList<>();
    try {
      testResolveNodates(false, null, null, null, true, 0x6, resolvers);
      testResolveNodates(false, null, null, _key1, true, 0x1, resolvers);
      testResolveNodates(false, null, null, _key2, false, 0x1, resolvers);
      testResolveNodates(false, null, _provider1, null, true, 0x4, resolvers);
      testResolveNodates(false, null, _provider1, _key1, true, 0, resolvers);
      testResolveNodates(false, null, _provider1, _key2, false, 0x2, resolvers);
      testResolveNodates(false, null, _provider2, null, false, 0x5, resolvers);
      testResolveNodates(false, null, _provider2, _key1, false, 0, resolvers);
      testResolveNodates(false, null, _provider2, _key2, false, 0, resolvers);
      testResolveNodates(false, _source1, null, null, true, 0x4, resolvers);
      testResolveNodates(false, _source1, null, _key1, true, 0x0, resolvers);
      testResolveNodates(false, _source1, null, _key2, false, 0x2, resolvers);
      testResolveNodates(false, _source1, _provider1, null, true, 0x4, resolvers);
      testResolveNodates(false, _source1, _provider1, _key1, true, 0x0, resolvers);
      testResolveNodates(false, _source1, _provider1, _key2, false, 0x2, resolvers);
      testResolveNodates(false, _source1, _provider2, null, false, 0x5, resolvers);
      testResolveNodates(false, _source1, _provider2, _key1, false, 0, resolvers);
      testResolveNodates(false, _source1, _provider2, _key2, false, 0, resolvers);
      testResolveNodates(false, _source2, null, null, false, 0x5, resolvers);
      testResolveNodates(false, _source2, null, _key1, false, 0, resolvers);
      testResolveNodates(false, _source2, null, _key2, false, 0, resolvers);
      testResolveNodates(false, _source2, _provider1, null, false, 0x5, resolvers);
      testResolveNodates(false, _source2, _provider1, _key1, false, 0, resolvers);
      testResolveNodates(false, _source2, _provider1, _key2, false, 0, resolvers);
      testResolveNodates(false, _source2, _provider2, null, false, 0x5, resolvers);
      testResolveNodates(false, _source2, _provider2, _key1, false, 0, resolvers);
      testResolveNodates(false, _source2, _provider2, _key2, false, 0, resolvers);
    } finally {
      for (final EHCachingHistoricalTimeSeriesResolver resolver : resolvers) {
        resolver.shutdown();
      }
    }
  }

}
