/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigSearchRequest.Meta;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConfigSearchRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigSearchRequestTest extends AbstractFudgeBuilderTestCase {
  private static final List<ObjectId> OIDS = Arrays.asList(ObjectId.of("oid", "1"), ObjectId.of("oid", "2"), ObjectId.of("oid", "3"));
  private static final String NAME = "name";
  private static final Class<?> TYPE = HistoricalTimeSeriesRating.class;
  private static final ConfigSearchSortOrder SEARCH_ORDER = ConfigSearchSortOrder.OBJECT_ID_ASC;
  private static final String UID_SCHEME = "uid";
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(1000), Instant.ofEpochSecond(2000));

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType() {
    new ConfigSearchRequest<>(null);
  }

  /**
   * Tests that the id to add cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigId() {
    new ConfigSearchRequest<>().addConfigId(null);
  }

  /**
   * Tests the addition of a config id.
   */
  @Test
  public void testAddConfigId() {
    final ConfigSearchRequest<?> request = new ConfigSearchRequest<>();
    assertNull(request.getConfigIds());
    for (int i = 0; i < OIDS.size(); i++) {
      request.addConfigId(OIDS.get(i));
      assertEquals(request.getConfigIds().size(), i + 1);
    }
    assertEquals(request.getConfigIds(), OIDS);
  }

  /**
   * Tests the default search order.
   */
  @Test
  public void testDefaultSearchOrder() {
    final ConfigSearchRequest<?> request = new ConfigSearchRequest<>();
    assertEquals(request.getSortOrder(), ConfigSearchSortOrder.VERSION_FROM_INSTANT_DESC);
  }

  /**
   * Tests the default type.
   */
  @Test
  public void testDefaultType() {
    final ConfigSearchRequest<?> request = new ConfigSearchRequest<>();
    assertEquals(request.getType(), Object.class);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConfigSearchRequest<?> request = new ConfigSearchRequest<>(TYPE);
    setFields(request);
    ConfigSearchRequest<?> other = new ConfigSearchRequest<>(TYPE);
    setFields(other);
    assertEquals(request, request);
    assertEquals(request.toString(),
        "ConfigSearchRequest{uniqueIdScheme=uid, pagingRequest=PagingRequest[first=0, size=0], "
            + "versionCorrection=V1970-01-01T00:16:40Z.C1970-01-01T00:33:20Z, configIds=[oid~1, oid~2, oid~3], name=name, "
            + "type=class com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating, sortOrder=OBJECT_ID_ASC}");
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setConfigIds(OIDS.subList(0, 1));
    assertNotEquals(request, other);
    setFields(other);
    other.setName(UID_SCHEME);
    assertNotEquals(request, other);
    setFields(other);
    other.setPagingRequest(PagingRequest.FIRST_PAGE);
    assertNotEquals(request, other);
    setFields(other);
    other.setSortOrder(ConfigSearchSortOrder.NAME_ASC);
    assertNotEquals(request, other);
    other.setType(Object.class);
    assertNotEquals(request, other);
    other = new ConfigSearchRequest<>(TYPE);
    other.setUniqueIdScheme(NAME);
    assertNotEquals(request, other);
    setFields(other);
    other.setVersionCorrection(VersionCorrection.LATEST);
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConfigSearchRequest<?> request = new ConfigSearchRequest<>(TYPE);
    setFields(request);
    assertEquals(request.propertyNames().size(), 7);
    final Meta<?> bean = request.metaBean();
    assertEquals(bean.configIds().get(request), OIDS);
    assertEquals(bean.name().get(request), NAME);
    assertEquals(bean.pagingRequest().get(request), PagingRequest.NONE);
    assertEquals(bean.sortOrder().get(request), SEARCH_ORDER);
    assertEquals(bean.type().get(request), TYPE);
    assertEquals(bean.uniqueIdScheme().get(request), UID_SCHEME);
    assertEquals(bean.versionCorrection().get(request), VC);
    assertEquals(request.property("configIds").get(), OIDS);
    assertEquals(request.property("name").get(), NAME);
    assertEquals(request.property("pagingRequest").get(), PagingRequest.NONE);
    assertEquals(request.property("sortOrder").get(), SEARCH_ORDER);
    assertEquals(request.property("type").get(), TYPE);
    assertEquals(request.property("uniqueIdScheme").get(), UID_SCHEME);
    assertEquals(request.property("versionCorrection").get(), VC);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ConfigSearchRequest<?> request = new ConfigSearchRequest<>(TYPE);
    setFields(request);
    assertEncodeDecodeCycle(ConfigSearchRequest.class, request);
  }

  private static void setFields(final ConfigSearchRequest<?> request) {
    request.setConfigIds(OIDS);
    request.setName(NAME);
    request.setPagingRequest(PagingRequest.NONE);
    request.setSortOrder(SEARCH_ORDER);
    request.setUniqueIdScheme(UID_SCHEME);
    request.setVersionCorrection(VC);
  }
}
