/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesInfoMetaDataRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesInfoMetaDataRequestTest extends AbstractFudgeBuilderTestCase {
  private static final Boolean DATA_FIELDS = false;
  private static final Boolean DATA_SOURCES = false;
  private static final Boolean DATA_PROVIDERS = false;
  private static final Boolean OBS_TIMES = false;
  private static final String UID_SCHEME = "hist";

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HistoricalTimeSeriesInfoMetaDataRequest request = new HistoricalTimeSeriesInfoMetaDataRequest();
    assertTrue(request.isDataFields());
    assertTrue(request.isDataProviders());
    assertTrue(request.isDataSources());
    assertTrue(request.isObservationTimes());
    assertNull(request.getUniqueIdScheme());
    setFields(request);
    assertEquals(request, request);
    assertEquals(request.toString(),
        "HistoricalTimeSeriesInfoMetaDataRequest{uniqueIdScheme=hist, " + "dataFields=false, dataSources=false, dataProviders=false, observationTimes=false}");
    final HistoricalTimeSeriesInfoMetaDataRequest other = new HistoricalTimeSeriesInfoMetaDataRequest();
    setFields(other);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setDataFields(!DATA_FIELDS);
    assertNotEquals(request, other);
    setFields(other);
    other.setDataProviders(!DATA_PROVIDERS);
    assertNotEquals(request, other);
    setFields(other);
    other.setDataSources(!DATA_SOURCES);
    assertNotEquals(request, other);
    setFields(other);
    other.setObservationTimes(!OBS_TIMES);
    assertNotEquals(request, other);
    setFields(other);
    other.setUniqueIdScheme("uid");
    assertNotEquals(request, other);
  }

  /**
   * Test bean.
   */
  @Test
  public void testBean() {
    final HistoricalTimeSeriesInfoMetaDataRequest request = new HistoricalTimeSeriesInfoMetaDataRequest();
    setFields(request);
    assertEquals(request.propertyNames().size(), 5);
    final Meta bean = request.metaBean();
    assertEquals(bean.dataFields().get(request), DATA_FIELDS);
    assertEquals(bean.dataProviders().get(request), DATA_PROVIDERS);
    assertEquals(bean.dataSources().get(request), DATA_SOURCES);
    assertEquals(bean.observationTimes().get(request), OBS_TIMES);
    assertEquals(bean.uniqueIdScheme().get(request), UID_SCHEME);
    assertEquals(request.property("dataFields").get(), DATA_FIELDS);
    assertEquals(request.property("dataProviders").get(), DATA_PROVIDERS);
    assertEquals(request.property("dataSources").get(), DATA_SOURCES);
    assertEquals(request.property("observationTimes").get(), OBS_TIMES);
    assertEquals(request.property("uniqueIdScheme").get(), UID_SCHEME);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HistoricalTimeSeriesInfoMetaDataRequest request = new HistoricalTimeSeriesInfoMetaDataRequest();
    setFields(request);
    assertEncodeDecodeCycle(HistoricalTimeSeriesInfoMetaDataRequest.class, request);
  }

  private static void setFields(final HistoricalTimeSeriesInfoMetaDataRequest request) {
    request.setDataFields(DATA_FIELDS);
    request.setDataProviders(DATA_PROVIDERS);
    request.setDataSources(DATA_SOURCES);
    request.setObservationTimes(OBS_TIMES);
    request.setUniqueIdScheme(UID_SCHEME);
  }
}
