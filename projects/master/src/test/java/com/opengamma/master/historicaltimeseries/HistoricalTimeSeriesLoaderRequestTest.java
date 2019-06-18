/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesLoaderRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesLoaderRequestTest extends AbstractFudgeBuilderTestCase {
  private static final Set<ExternalId> EIDS = new HashSet<>(Arrays.asList(ExternalId.of("A", "1"), ExternalId.of("A", "2"), ExternalId.of("B", "1")));
  private static final String DATA_PROVIDER = "BBG";
  private static final String DATA_FIELD = "VOLUME";
  private static final LocalDate START_DATE = LocalDate.of(2016, 1, 1);
  private static final LocalDate END_DATE = LocalDate.of(2019, 1, 1);

  /**
   * Tests that the external ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidArray() {
    new HistoricalTimeSeriesLoaderRequest().addExternalIds((ExternalId[]) null);
  }

  /**
   * Tests that the external ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidIterable() {
    new HistoricalTimeSeriesLoaderRequest().addExternalIds((Iterable<ExternalId>) null);
  }

  /**
   * Tests constructor equivalence.
   */
  @Test
  public void testConstructor() {
    final HistoricalTimeSeriesLoaderRequest request = new HistoricalTimeSeriesLoaderRequest();
    request.addExternalIds(EIDS);
    assertEquals(request, HistoricalTimeSeriesLoaderRequest.create(EIDS, null, null, null, null));
  }

  /**
   * Tests adding external ids.
   */
  @Test
  public void testAddEids() {
    final HistoricalTimeSeriesLoaderRequest request = new HistoricalTimeSeriesLoaderRequest();
    request.addExternalIds(EIDS);
    assertEquals(request, HistoricalTimeSeriesLoaderRequest.create(EIDS, null, null, null, null));
    request.addExternalIds(EIDS.toArray(new ExternalId[0]));
    assertEquals(request, HistoricalTimeSeriesLoaderRequest.create(EIDS, null, null, null, null));
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HistoricalTimeSeriesLoaderRequest request = HistoricalTimeSeriesLoaderRequest.create(EIDS, DATA_PROVIDER, DATA_FIELD, START_DATE, END_DATE);
    HistoricalTimeSeriesLoaderRequest other = HistoricalTimeSeriesLoaderRequest.create(EIDS, DATA_PROVIDER, DATA_FIELD, START_DATE, END_DATE);
    assertEquals(request, request);
    assertEquals(request.toString(),
        "HistoricalTimeSeriesLoaderRequest{externalIds=[A~1, A~2, B~1], dataProvider=BBG, dataField=VOLUME, " + "startDate=2016-01-01, endDate=2019-01-01}");
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other = HistoricalTimeSeriesLoaderRequest.create(Collections.<ExternalId> emptySet(), DATA_PROVIDER, DATA_FIELD, START_DATE, END_DATE);
    assertNotEquals(request, other);
    other = HistoricalTimeSeriesLoaderRequest.create(EIDS, DATA_FIELD, DATA_FIELD, START_DATE, END_DATE);
    assertNotEquals(request, other);
    other = HistoricalTimeSeriesLoaderRequest.create(EIDS, DATA_PROVIDER, DATA_PROVIDER, START_DATE, END_DATE);
    assertNotEquals(request, other);
    other = HistoricalTimeSeriesLoaderRequest.create(EIDS, DATA_PROVIDER, DATA_FIELD, END_DATE, END_DATE);
    assertNotEquals(request, other);
    other = HistoricalTimeSeriesLoaderRequest.create(EIDS, DATA_PROVIDER, DATA_FIELD, START_DATE, START_DATE);
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final HistoricalTimeSeriesLoaderRequest request = HistoricalTimeSeriesLoaderRequest.create(EIDS, DATA_PROVIDER, DATA_FIELD, START_DATE, END_DATE);
    final Meta bean = request.metaBean();
    assertEquals(request.propertyNames().size(), 5);
    assertEquals(bean.dataField().get(request), DATA_FIELD);
    assertEquals(bean.dataProvider().get(request), DATA_PROVIDER);
    assertEquals(bean.endDate().get(request), END_DATE);
    assertEquals(bean.externalIds().get(request), EIDS);
    assertEquals(bean.startDate().get(request), START_DATE);
    assertEquals(request.property("dataField").get(), DATA_FIELD);
    assertEquals(request.property("dataProvider").get(), DATA_PROVIDER);
    assertEquals(request.property("endDate").get(), END_DATE);
    assertEquals(request.property("externalIds").get(), EIDS);
    assertEquals(request.property("startDate").get(), START_DATE);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HistoricalTimeSeriesLoaderRequest request = HistoricalTimeSeriesLoaderRequest.create(EIDS, DATA_PROVIDER, DATA_FIELD, START_DATE, END_DATE);
    assertEncodeDecodeCycle(HistoricalTimeSeriesLoaderRequest.class, request);
  }
}
