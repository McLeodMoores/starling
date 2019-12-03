/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.region.impl;

import static com.opengamma.id.VersionCorrection.LATEST;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.test.Assert;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Tests for {@link NarrowingRegionSource}.
 */
public class NarrowingRegionSourceTest {
  private static final ExternalId FR_ID = ExternalSchemes.countryRegionId(Country.FR);
  private static final ExternalId EUR_ID = ExternalSchemes.currencyRegionId(Currency.EUR);
  private static final ExternalId US_ID = ExternalSchemes.countryRegionId(Country.US);
  private static final UniqueId UID_1 = UniqueId.of("UID", "1");
  private static final UniqueId UID_2 = UniqueId.of("UID", "2");
  private static final UniqueId UID_3 = UniqueId.of("UID", "3");
  private static final ObjectId OID_1 = UID_1.getObjectId();
  private static final ObjectId OID_2 = UID_2.getObjectId();
  private static final ObjectId OID_3 = UID_3.getObjectId();
  private static final SimpleRegion REGION_1 = new SimpleRegion();
  private static final SimpleRegion REGION_2 = new SimpleRegion();
  private static final SimpleRegion REGION_3 = new SimpleRegion();
  static {
    REGION_1.setClassification(RegionClassification.INDEPENDENT_STATE);
    REGION_2.setClassification(RegionClassification.SUPER_NATIONAL);
    REGION_3.setClassification(RegionClassification.INDEPENDENT_STATE);
    REGION_1.setName("fr");
    REGION_2.setName("eu");
    REGION_3.setName("us");
  }
  private RegionSource _delegateSource;
  private RegionSource _narrowingSource;

  /**
   * Sets up the delegate before the tests are run.
   */
  @SuppressWarnings("unchecked")
  @BeforeClass
  public void setUp() {
    _delegateSource = Mockito.mock(RegionSource.class);
    _narrowingSource = new NarrowingRegionSource(_delegateSource);
    // by uid
    when(_delegateSource.get(UID_1)).thenReturn(REGION_1);
    when(_delegateSource.get(UID_2)).thenReturn(REGION_2);
    when(_delegateSource.get(UID_3)).thenReturn(REGION_3);
    when(_delegateSource.get(Collections.singleton(UID_1))).thenReturn(Collections.<UniqueId, Region>singletonMap(UID_1, REGION_1));
    when(_delegateSource.get(Collections.singleton(UID_2))).thenReturn(Collections.<UniqueId, Region>singletonMap(UID_2, REGION_2));
    when(_delegateSource.get(Collections.singleton(UID_3))).thenReturn(Collections.<UniqueId, Region>singletonMap(UID_3, REGION_3));
    // by oid
    when(_delegateSource.get(OID_1, LATEST)).thenReturn(REGION_1);
    when(_delegateSource.get(OID_2, LATEST)).thenReturn(REGION_2);
    when(_delegateSource.get(OID_3, LATEST)).thenReturn(REGION_3);
    when(_delegateSource.get(Collections.singleton(OID_1), LATEST)).thenReturn(Collections.<ObjectId, Region>singletonMap(OID_1, REGION_1));
    when(_delegateSource.get(Collections.singleton(OID_2), LATEST)).thenReturn(Collections.<ObjectId, Region>singletonMap(OID_2, REGION_2));
    when(_delegateSource.get(Collections.singleton(OID_3), LATEST)).thenReturn(Collections.<ObjectId, Region>singletonMap(OID_3, REGION_3));
    // by eid
    when(_delegateSource.get(FR_ID.toBundle())).thenReturn(Arrays.<Region>asList(REGION_2, REGION_1));
    when(_delegateSource.get(EUR_ID.toBundle())).thenReturn(Arrays.<Region>asList(REGION_2));
    when(_delegateSource.get(US_ID.toBundle())).thenReturn(Arrays.<Region>asList(REGION_3));
    when(_delegateSource.get(FR_ID.toBundle(), LATEST)).thenReturn(Arrays.<Region>asList(REGION_2, REGION_1));
    when(_delegateSource.get(EUR_ID.toBundle(), LATEST)).thenReturn(Arrays.<Region>asList(REGION_2));
    when(_delegateSource.get(US_ID.toBundle(), LATEST)).thenReturn(Arrays.<Region>asList(REGION_3));

    // these methods should not be called
    when(_delegateSource.getAll(Collections.singleton(FR_ID.toBundle()), LATEST)).thenThrow(UnsupportedOperationException.class);
    when(_delegateSource.getAll(Collections.singleton(EUR_ID.toBundle()), LATEST)).thenThrow(UnsupportedOperationException.class);
    when(_delegateSource.getAll(Collections.singleton(US_ID.toBundle()), LATEST)).thenThrow(UnsupportedOperationException.class);

    when(_delegateSource.getSingle(FR_ID.toBundle())).thenThrow(UnsupportedOperationException.class);
    when(_delegateSource.getSingle(EUR_ID.toBundle())).thenThrow(UnsupportedOperationException.class);
    when(_delegateSource.getSingle(US_ID.toBundle())).thenThrow(UnsupportedOperationException.class);

    when(_delegateSource.getSingle(FR_ID.toBundle(), LATEST)).thenThrow(UnsupportedOperationException.class);
    when(_delegateSource.getSingle(EUR_ID.toBundle(), LATEST)).thenThrow(UnsupportedOperationException.class);
    when(_delegateSource.getSingle(US_ID.toBundle(), LATEST)).thenThrow(UnsupportedOperationException.class);

    when(_delegateSource.getSingle(Collections.singleton(FR_ID.toBundle()), LATEST)).thenThrow(UnsupportedOperationException.class);
    when(_delegateSource.getSingle(Collections.singleton(EUR_ID.toBundle()), LATEST)).thenThrow(UnsupportedOperationException.class);
    when(_delegateSource.getSingle(Collections.singleton(US_ID.toBundle()), LATEST)).thenThrow(UnsupportedOperationException.class);
  }

  /**
   * Tests that the delegate cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDelegate() {
    new NarrowingRegionSource(null);
  }

  /**
   * Tests retrieval of the highest level region by external id.
   */
  @Test
  public void testGetHighestByExternalId() {
    assertEquals(_narrowingSource.getHighestLevelRegion(FR_ID), REGION_2);
    // EU is higher than FR
    assertEquals(_narrowingSource.getHighestLevelRegion(EUR_ID), REGION_2);
    assertEquals(_narrowingSource.getHighestLevelRegion(US_ID), REGION_3);
  }

  /**
   * Tests retrieval of the highest level region by external id bundle.
   */
  @Test
  public void testGetHighestByExternalIdBundle() {
    assertEquals(_narrowingSource.getHighestLevelRegion(FR_ID.toBundle()), REGION_2);
    // EU is higher than FR
    assertEquals(_narrowingSource.getHighestLevelRegion(EUR_ID.toBundle()), REGION_2);
    assertEquals(_narrowingSource.getHighestLevelRegion(US_ID.toBundle()), REGION_3);
  }

  /**
   * Tests retrieval of multiple regions by id bundle.
   */
  @Test
  public void testGetAll() {
    final Map<ExternalIdBundle, Collection<Region>> regions =
        _narrowingSource.getAll(Arrays.asList(FR_ID.toBundle(), EUR_ID.toBundle(), US_ID.toBundle()), LATEST);
    assertEquals(regions.size(), 3);
    Assert.assertEqualsNoOrder(regions.get(FR_ID.toBundle()), Arrays.<Region>asList(REGION_1, REGION_2));
    assertEquals(regions.get(EUR_ID.toBundle()), Collections.singleton(REGION_2));
    assertEquals(regions.get(US_ID.toBundle()), Collections.singleton(REGION_3));
  }

  /**
   * Tests retrieval of regions by id bundle.
   */
  @Test
  public void testGetByExternalIdBundle() {
    Assert.assertEqualsNoOrder(_narrowingSource.get(FR_ID.toBundle()), Arrays.<Region>asList(REGION_1, REGION_2));
    assertEquals(_narrowingSource.get(EUR_ID.toBundle()), Collections.singleton(REGION_2));
    assertEquals(_narrowingSource.get(US_ID.toBundle()), Collections.singleton(REGION_3));
  }

  /**
   * Tests retrieval of a single region by id bundle.
   */
  @Test
  public void testGetSingleByExternalIdBundle() {
    // single region is picked based on iterator.next()
    try {
      assertEquals(_narrowingSource.getSingle(FR_ID.toBundle()), REGION_1);
    } catch (final AssertionError e) {
      assertEquals(_narrowingSource.getSingle(FR_ID.toBundle()), REGION_2);
    }
    assertEquals(_narrowingSource.getSingle(EUR_ID.toBundle()), REGION_2);
    assertEquals(_narrowingSource.getSingle(US_ID.toBundle()), REGION_3);
  }

  /**
   * Tests retrieval of a single region by id bundle.
   */
  @Test
  public void testGetSingleByExternalIdBundleVersion() {
    // single region is picked based on iterator.next()
    try {
      assertEquals(_narrowingSource.getSingle(FR_ID.toBundle(), LATEST), REGION_1);
    } catch (final AssertionError e) {
      assertEquals(_narrowingSource.getSingle(FR_ID.toBundle(), LATEST), REGION_2);
    }
    assertEquals(_narrowingSource.getSingle(EUR_ID.toBundle(), LATEST), REGION_2);
    assertEquals(_narrowingSource.getSingle(US_ID.toBundle(), LATEST), REGION_3);
  }

  /**
   * Tests retrieval of multiple regions by id bundle.
   */
  @Test
  public void testMultipleGetSingle() {
    final Map<ExternalIdBundle, Region> regions =
        _narrowingSource.getSingle(Arrays.asList(FR_ID.toBundle(), EUR_ID.toBundle(), US_ID.toBundle()), LATEST);
    assertEquals(regions.size(), 3);
    // single region is picked based on iterator.next()
    try {
      assertEquals(regions.get(FR_ID.toBundle()), REGION_1);
    } catch (final AssertionError e) {
      assertEquals(regions.get(FR_ID.toBundle()), REGION_2);
    }
    assertEquals(regions.get(EUR_ID.toBundle()), REGION_2);
    assertEquals(regions.get(US_ID.toBundle()), REGION_3);
  }

  /**
   * Tests retrieval by unique id.
   */
  @Test
  public void testGetByUniqueId() {
    assertEquals(_narrowingSource.get(UID_1), REGION_1);
    assertEquals(_narrowingSource.get(UID_2), REGION_2);
    assertEquals(_narrowingSource.get(UID_3), REGION_3);
  }

  /**
   * Tests retrieval by object id.
   */
  @Test
  public void testGetByObjectId() {
    assertEquals(_narrowingSource.get(OID_1, LATEST), REGION_1);
    assertEquals(_narrowingSource.get(OID_2, LATEST), REGION_2);
    assertEquals(_narrowingSource.get(OID_3, LATEST), REGION_3);
  }

  /**
   * Tests retrieval by unique id collection.
   */
  @Test
  public void testGetByUniqueIdCollection() {
    assertEquals(_narrowingSource.get(Collections.singleton(UID_1)), Collections.singletonMap(UID_1, REGION_1));
    assertEquals(_narrowingSource.get(Collections.singleton(UID_2)), Collections.singletonMap(UID_2, REGION_2));
    assertEquals(_narrowingSource.get(Collections.singleton(UID_3)), Collections.singletonMap(UID_3, REGION_3));
  }

  /**
   * Tests retrieval by object id collection.
   */
  @Test
  public void testGetByObjectIdCollection() {
    assertEquals(_narrowingSource.get(Collections.singleton(OID_1), LATEST), Collections.singletonMap(OID_1, REGION_1));
    assertEquals(_narrowingSource.get(Collections.singleton(OID_2), LATEST), Collections.singletonMap(OID_2, REGION_2));
    assertEquals(_narrowingSource.get(Collections.singleton(OID_3), LATEST), Collections.singletonMap(OID_3, REGION_3));
  }
}
