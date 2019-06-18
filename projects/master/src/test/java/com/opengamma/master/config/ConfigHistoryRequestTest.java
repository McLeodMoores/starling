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

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigHistoryRequest.Meta;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingRule;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConfigHistoryRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigHistoryRequestTest extends AbstractFudgeBuilderTestCase {
  private static final List<HistoricalTimeSeriesRatingRule> RULES = Arrays.asList(
      HistoricalTimeSeriesRatingRule.of(HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME, "value1", 0),
      HistoricalTimeSeriesRatingRule.of(HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME, "value2", 1));
  private static final HistoricalTimeSeriesRating RATING = HistoricalTimeSeriesRating.of(RULES);
  private static final ConfigItem<HistoricalTimeSeriesRating> ITEM = ConfigItem.of(RATING);
  private static final UniqueId UID = UniqueId.of("uid", "1");
  private static final Instant VERSION = Instant.ofEpochSecond(1000);
  private static final Instant CORRECTION = Instant.ofEpochSecond(1500);
  static {
    ITEM.setUniqueId(UID);
  }

  /**
   * Tests that the object cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObject1() {
    new ConfigHistoryRequest<>(null, HistoricalTimeSeriesRating.class);
  }

  /**
   * Tests that the object cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObject2() {
    new ConfigHistoryRequest<>(null, VERSION, CORRECTION);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType() {
    new ConfigHistoryRequest<>(ITEM, null);
  }

  /**
   * Tests the default constructor.
   */
  @Test
  public void testDefaultConstructor() {
    final ConfigHistoryRequest<?> request = new ConfigHistoryRequest<>();
    assertNull(request.getCorrectionsFromInstant());
    assertNull(request.getCorrectionsToInstant());
    assertNull(request.getObjectId());
    assertEquals(request.getPagingRequest(), PagingRequest.ALL);
    assertNull(request.getType());
    assertNull(request.getVersionsFromInstant());
    assertNull(request.getVersionsToInstant());
  }

  /**
   * Tests a constructor.
   */
  @Test
  public void testConstructor1() {
    final ConfigHistoryRequest<?> request = new ConfigHistoryRequest<>(ITEM, HistoricalTimeSeriesRating.class);
    assertNull(request.getCorrectionsFromInstant());
    assertNull(request.getCorrectionsToInstant());
    assertEquals(request.getObjectId(), UID.getObjectId());
    assertEquals(request.getPagingRequest(), PagingRequest.ALL);
    assertEquals(request.getType(), HistoricalTimeSeriesRating.class);
    assertNull(request.getVersionsFromInstant());
    assertNull(request.getVersionsToInstant());
  }

  /**
   * Tests a constructor.
   */
  @Test
  public void testConstructor2() {
    final ConfigHistoryRequest<?> request = new ConfigHistoryRequest<>(ITEM, VERSION, CORRECTION);
    assertEquals(request.getCorrectionsFromInstant(), CORRECTION);
    assertEquals(request.getCorrectionsToInstant(), CORRECTION);
    assertEquals(request.getObjectId(), UID.getObjectId());
    assertEquals(request.getPagingRequest(), PagingRequest.ALL);
    assertNull(request.getType());
    assertEquals(request.getVersionsFromInstant(), VERSION);
    assertEquals(request.getVersionsToInstant(), VERSION);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConfigHistoryRequest<ObjectIdentifiable> request = new ConfigHistoryRequest<>(ITEM, VERSION, CORRECTION);
    final ConfigHistoryRequest<ObjectIdentifiable> other = new ConfigHistoryRequest<>(ITEM, VERSION, CORRECTION);
    assertEquals(request, request);
    assertEquals(request.toString(),
        "ConfigHistoryRequest{pagingRequest=PagingRequest[first=0, size=2147483647], objectId=uid~1, "
            + "versionsFromInstant=1970-01-01T00:16:40Z, versionsToInstant=1970-01-01T00:16:40Z, correctionsFromInstant=1970-01-01T00:25:00Z, "
            + "correctionsToInstant=1970-01-01T00:25:00Z, type=null}");
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setCorrectionsFromInstant(VERSION);
    assertNotEquals(request, other);
    other.setCorrectionsFromInstant(CORRECTION);
    other.setCorrectionsToInstant(VERSION);
    assertNotEquals(request, other);
    other.setCorrectionsToInstant(CORRECTION);
    other.setObjectId(ObjectId.of("oid", "1"));
    assertNotEquals(request, other);
    other.setObjectId(UID.getObjectId());
    other.setVersionsFromInstant(CORRECTION);
    assertNotEquals(request, other);
    other.setVersionsFromInstant(VERSION);
    other.setVersionsToInstant(CORRECTION);
    assertNotEquals(request, other);
    other.setType(ObjectIdentifiable.class);
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConfigHistoryRequest<ObjectIdentifiable> request = new ConfigHistoryRequest<>(ITEM, VERSION, CORRECTION);
    request.setType(ObjectIdentifiable.class);
    assertEquals(request.propertyNames().size(), 7);
    final Meta<ObjectIdentifiable> bean = request.metaBean();
    assertEquals(bean.correctionsFromInstant().get(request), CORRECTION);
    assertEquals(bean.correctionsToInstant().get(request), CORRECTION);
    assertEquals(bean.objectId().get(request), UID.getObjectId());
    assertEquals(bean.pagingRequest().get(request), PagingRequest.ALL);
    assertEquals(bean.type().get(request), ObjectIdentifiable.class);
    assertEquals(bean.versionsFromInstant().get(request), VERSION);
    assertEquals(bean.versionsToInstant().get(request), VERSION);
    assertEquals(request.property("correctionsFromInstant").get(), CORRECTION);
    assertEquals(request.property("correctionsToInstant").get(), CORRECTION);
    assertEquals(request.property("objectId").get(), UID.getObjectId());
    assertEquals(request.property("pagingRequest").get(), PagingRequest.ALL);
    assertEquals(request.property("type").get(), ObjectIdentifiable.class);
    assertEquals(request.property("versionsFromInstant").get(), VERSION);
    assertEquals(request.property("versionsToInstant").get(), VERSION);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ConfigHistoryRequest<ObjectIdentifiable> request = new ConfigHistoryRequest<>(ITEM, VERSION, CORRECTION);
    request.setType(ObjectIdentifiable.class);
    assertEncodeDecodeCycle(ConfigHistoryRequest.class, request);
  }
}
