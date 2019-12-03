/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.convention.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link NarrowingConventionSource}.
 */
@Test(groups = TestGroup.UNIT)
public class NarrowingConventionSourceTest {
  private static final UniqueId UID_1 = UniqueId.of("TEST", "1");
  private static final UniqueId UID_2 = UniqueId.of("TEST", "2");
  private static final UniqueId UID_3 = UniqueId.of("TEST", "3");
  private static final UniqueId UID_4 = UniqueId.of("TEST", "4");
  private static final ExternalId EID_1 = ExternalId.of("TEST", "100");
  private static final ExternalId EID_2 = ExternalId.of("TEST", "200");
  private static final ExternalId EID_3 = ExternalId.of("TEST", "300");
  private static final ExternalId EID_4 = ExternalId.of("TEST", "400");
  private static final MockConvention CNV_1 = new MockConvention();
  private static final MockConvention CNV_2 = new MockConvention();
  private static final MockConvention CNV_3 = new MockConvention();
  private static final MockConvention CNV_4 = new MockConvention();
  static {
    CNV_1.setUniqueId(UID_1);
    CNV_1.setName("NAME 1");
    CNV_1.setExternalIdBundle(EID_1.toBundle());
    CNV_2.setUniqueId(UID_2);
    CNV_2.setName("NAME 2");
    CNV_2.setExternalIdBundle(EID_2.toBundle());
    CNV_3.setUniqueId(UID_3);
    CNV_3.setName("NAME 3");
    CNV_3.setExternalIdBundle(EID_3.toBundle());
    CNV_4.setUniqueId(UID_4);
    CNV_4.setName("NAME 4");
    CNV_4.setExternalIdBundle(EID_4.toBundle());
  }

  private static final ConventionSource UNDERLYING = Mockito.mock(ConventionSource.class);
  static {
    populateSource();
  }
  private static final ConventionSource NARROWING = new NarrowingConventionSource(UNDERLYING);

  private static void populateSource() {
    final Map<UniqueId, Convention> mUid = ImmutableMap.<UniqueId, Convention>of(
        UID_1, CNV_1, UID_2, CNV_2, UID_3, CNV_3, UID_4, CNV_4);
    final Map<ObjectId, Convention> mOid = ImmutableMap.<ObjectId, Convention>of(
        UID_1.getObjectId(), CNV_1, UID_2.getObjectId(), CNV_2, UID_3.getObjectId(), CNV_3, UID_4.getObjectId(), CNV_4);

    Mockito.when(UNDERLYING.get(UID_1)).thenReturn(CNV_1);
    Mockito.when(UNDERLYING.get(UID_2)).thenReturn(CNV_2);
    Mockito.when(UNDERLYING.get(UID_3)).thenReturn(CNV_3);
    Mockito.when(UNDERLYING.get(UID_4)).thenReturn(CNV_4);

    Mockito.when(UNDERLYING.get(UID_1, MockConvention.class)).thenReturn(CNV_1);
    Mockito.when(UNDERLYING.get(UID_2, MockConvention.class)).thenReturn(CNV_2);
    Mockito.when(UNDERLYING.get(UID_3, MockConvention.class)).thenReturn(CNV_3);
    Mockito.when(UNDERLYING.get(UID_4, MockConvention.class)).thenReturn(CNV_4);

    Mockito.when(UNDERLYING.get(Arrays.asList(UID_1, UID_2, UID_3, UID_4))).thenReturn(mUid);

    Mockito.when(UNDERLYING.get(UID_1.getObjectId(), VersionCorrection.LATEST)).thenReturn(CNV_1);
    Mockito.when(UNDERLYING.get(UID_2.getObjectId(), VersionCorrection.LATEST)).thenReturn(CNV_2);
    Mockito.when(UNDERLYING.get(UID_3.getObjectId(), VersionCorrection.LATEST)).thenReturn(CNV_3);
    Mockito.when(UNDERLYING.get(UID_4.getObjectId(), VersionCorrection.LATEST)).thenReturn(CNV_4);

    Mockito.when(UNDERLYING.get(UID_1.getObjectId(), VersionCorrection.LATEST, MockConvention.class)).thenReturn(CNV_1);
    Mockito.when(UNDERLYING.get(UID_2.getObjectId(), VersionCorrection.LATEST, MockConvention.class)).thenReturn(CNV_2);
    Mockito.when(UNDERLYING.get(UID_3.getObjectId(), VersionCorrection.LATEST, MockConvention.class)).thenReturn(CNV_3);
    Mockito.when(UNDERLYING.get(UID_4.getObjectId(), VersionCorrection.LATEST, MockConvention.class)).thenReturn(CNV_4);

    Mockito.when(UNDERLYING.get(Arrays.asList(UID_1.getObjectId(), UID_2.getObjectId(), UID_3.getObjectId(),
        UID_4.getObjectId()), VersionCorrection.LATEST)).thenReturn(mOid);

    Mockito.when(UNDERLYING.get(EID_1.toBundle(), VersionCorrection.LATEST)).thenReturn(Collections.<Convention>singleton(CNV_1));
    Mockito.when(UNDERLYING.get(EID_2.toBundle(), VersionCorrection.LATEST)).thenReturn(Collections.<Convention>singleton(CNV_2));
    Mockito.when(UNDERLYING.get(EID_3.toBundle(), VersionCorrection.LATEST)).thenReturn(Collections.<Convention>singleton(CNV_3));
    Mockito.when(UNDERLYING.get(EID_4.toBundle(), VersionCorrection.LATEST)).thenReturn(Collections.<Convention>singleton(CNV_4));
  }

  /**
   * Tests the values returned when requesting by unique id.
   */
  @Test
  public void testGetUniqueId() {
    assertEquals(NARROWING.get(UID_1), CNV_1);
    assertEquals(NARROWING.get(UID_2), CNV_2);
    assertEquals(NARROWING.get(UID_3), CNV_3);
    assertEquals(NARROWING.get(UID_4), CNV_4);
  }

  /**
   * Tests the values returned when requesting by unique id and type.
   */
  @Test
  public void testGetUniqueIdType() {
    assertEquals(NARROWING.get(UID_1, MockConvention.class), CNV_1);
    assertEquals(NARROWING.get(UID_2, MockConvention.class), CNV_2);
    assertEquals(NARROWING.get(UID_3, MockConvention.class), CNV_3);
    assertEquals(NARROWING.get(UID_4, MockConvention.class), CNV_4);
  }

  /**
   * Tests the values returned when requesting by object id and version/correction.
   */
  @Test
  public void testGetObjectId() {
    assertEquals(NARROWING.get(UID_1.getObjectId(), VersionCorrection.LATEST), CNV_1);
    assertEquals(NARROWING.get(UID_2.getObjectId(), VersionCorrection.LATEST), CNV_2);
    assertEquals(NARROWING.get(UID_3.getObjectId(), VersionCorrection.LATEST), CNV_3);
    assertEquals(NARROWING.get(UID_4.getObjectId(), VersionCorrection.LATEST), CNV_4);
  }

  /**
   * Tests the values returned when requesting by object id and version/correction and type.
   */
  @Test
  public void testGetObjectIdType() {
    assertEquals(NARROWING.get(UID_1.getObjectId(), VersionCorrection.LATEST, MockConvention.class), CNV_1);
    assertEquals(NARROWING.get(UID_2.getObjectId(), VersionCorrection.LATEST, MockConvention.class), CNV_2);
    assertEquals(NARROWING.get(UID_3.getObjectId(), VersionCorrection.LATEST, MockConvention.class), CNV_3);
    assertEquals(NARROWING.get(UID_4.getObjectId(), VersionCorrection.LATEST, MockConvention.class), CNV_4);
  }

  /**
   * Tests the correct values are returned when multiple values are requested.
   */
  @Test
  public void testGetCollectionUniqueId() {
    final Map<UniqueId, Convention> result = NARROWING.get(Arrays.asList(UID_1, UID_2, UID_3, UID_4));
    assertEquals(result.size(), 4);
    assertEquals(result.get(UID_1), CNV_1);
    assertEquals(result.get(UID_2), CNV_2);
    assertEquals(result.get(UID_3), CNV_3);
    assertEquals(result.get(UID_4), CNV_4);
  }

  /**
   * Tests the correct values are returned when multiple values are requested.
   */
  @Test
  public void testGetCollectionObjectId() {
    final Map<ObjectId, Convention> result = NARROWING.get(Arrays.asList(
        UID_1.getObjectId(), UID_2.getObjectId(), UID_3.getObjectId(), UID_4.getObjectId()), VersionCorrection.LATEST);
    assertEquals(result.size(), 4);
    assertEquals(result.get(UID_1.getObjectId()), CNV_1);
    assertEquals(result.get(UID_2.getObjectId()), CNV_2);
    assertEquals(result.get(UID_3.getObjectId()), CNV_3);
    assertEquals(result.get(UID_4.getObjectId()), CNV_4);
  }

  /**
   * Tests the values returned when requesting by external id.
   */
  @Test
  public void testGetSingleExternalId() {
    assertEquals(NARROWING.getSingle(EID_1), CNV_1);
    assertEquals(NARROWING.getSingle(EID_2), CNV_2);
    assertEquals(NARROWING.getSingle(EID_3), CNV_3);
    assertEquals(NARROWING.getSingle(EID_4), CNV_4);
  }

  /**
   * Tests the values returned when requesting by external id and type.
   */
  @Test
  public void testGetSingleExternalIdType() {
    assertEquals(NARROWING.getSingle(EID_1, MockConvention.class), CNV_1);
    assertEquals(NARROWING.getSingle(EID_2, MockConvention.class), CNV_2);
    assertEquals(NARROWING.getSingle(EID_3, MockConvention.class), CNV_3);
    assertEquals(NARROWING.getSingle(EID_4, MockConvention.class), CNV_4);
  }

  /**
   * Tests the values returned when requesting by external id bundle and version / correction.
   */
  @Test
  public void testGetSingleExternalIdBundleVersion() {
    Map<ExternalIdBundle, Convention> map = NARROWING.getSingle(Collections.singleton(EID_1.toBundle()), VersionCorrection.LATEST);
    assertEquals(map.size(), 1);
    assertEquals(map.get(EID_1.toBundle()), CNV_1);
    map = NARROWING.getSingle(Collections.singleton(EID_2.toBundle()), VersionCorrection.LATEST);
    assertEquals(map.size(), 1);
    assertEquals(map.get(EID_2.toBundle()), CNV_2);
    map = NARROWING.getSingle(Collections.singleton(EID_3.toBundle()), VersionCorrection.LATEST);
    assertEquals(map.size(), 1);
    assertEquals(map.get(EID_3.toBundle()), CNV_3);
    map = NARROWING.getSingle(Collections.singleton(EID_4.toBundle()), VersionCorrection.LATEST);
    assertEquals(map.size(), 1);
    assertEquals(map.get(EID_4.toBundle()), CNV_4);
  }

  /**
   * Tests the values returned when requesting by external id bundle and version / correction.
   */
  @Test
  public void testGetAllExternalIdBundleVersion() {
    Map<ExternalIdBundle, Collection<Convention>> map =
        NARROWING.getAll(Collections.singleton(EID_1.toBundle()), VersionCorrection.LATEST);
    assertEquals(map.size(), 1);
    assertEquals(map.get(EID_1.toBundle()).size(), 1);
    assertEquals(map.get(EID_1.toBundle()).iterator().next(), CNV_1);
    map = NARROWING.getAll(Collections.singleton(EID_2.toBundle()), VersionCorrection.LATEST);
    assertEquals(map.size(), 1);
    assertEquals(map.get(EID_2.toBundle()).size(), 1);
    assertEquals(map.get(EID_2.toBundle()).iterator().next(), CNV_2);
    map = NARROWING.getAll(Collections.singleton(EID_3.toBundle()), VersionCorrection.LATEST);
    assertEquals(map.size(), 1);
    assertEquals(map.get(EID_3.toBundle()).size(), 1);
    assertEquals(map.get(EID_3.toBundle()).iterator().next(), CNV_3);
    map = NARROWING.getAll(Collections.singleton(EID_4.toBundle()), VersionCorrection.LATEST);
    assertEquals(map.size(), 1);
    assertEquals(map.get(EID_4.toBundle()).size(), 1);
    assertEquals(map.get(EID_4.toBundle()).iterator().next(), CNV_4);
  }

  /**
   * Tests that the correct change manager is returned.
   */
  @Test
  public void testGetChangeManager() {
    assertNull(NARROWING.changeManager());
  }

}
