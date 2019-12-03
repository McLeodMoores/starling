/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesInfoMetaDataResult}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesInfoMetaDataResultTest extends AbstractFudgeBuilderTestCase {
  private static final List<String> DATA_FIELDS = Arrays.asList("CLOSE", "VOLUME", "HIGH");
  private static final List<String> DATA_SOURCES = Arrays.asList("X", "Y");
  private static final List<String> DATA_PROVIDERS = Arrays.asList("A", "B", "C", "D");
  private static final List<String> OBS_TIMES = Arrays.asList("LON");

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HistoricalTimeSeriesInfoMetaDataResult result = new HistoricalTimeSeriesInfoMetaDataResult();
    setFields(result);
    assertEquals(result, result);
    assertEquals(result.toString(), "HistoricalTimeSeriesInfoMetaDataResult{dataFields=[CLOSE, VOLUME, HIGH], "
        + "dataSources=[X, Y], dataProviders=[A, B, C, D], observationTimes=[LON]}");
    assertEquals(result.getDataFields(), DATA_FIELDS);
    assertEquals(result.getDataProviders(), DATA_PROVIDERS);
    assertEquals(result.getDataSources(), DATA_SOURCES);
    assertEquals(result.getObservationTimes(), OBS_TIMES);
    // no append
    result.setDataFields(DATA_PROVIDERS);
    result.setDataProviders(DATA_SOURCES);
    result.setDataSources(OBS_TIMES);
    result.setObservationTimes(DATA_FIELDS);
    assertEquals(result.getDataFields(), DATA_PROVIDERS);
    assertEquals(result.getDataProviders(), DATA_SOURCES);
    assertEquals(result.getDataSources(), OBS_TIMES);
    assertEquals(result.getObservationTimes(), DATA_FIELDS);
    setFields(result);
    final HistoricalTimeSeriesInfoMetaDataResult other = new HistoricalTimeSeriesInfoMetaDataResult();
    setFields(other);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDataFields(DATA_SOURCES);
    assertNotEquals(result, other);
    setFields(other);
    other.setDataSources(DATA_FIELDS);
    assertNotEquals(result, other);
    setFields(other);
    other.setDataProviders(DATA_FIELDS);
    assertNotEquals(result, other);
    setFields(other);
    other.setObservationTimes(DATA_FIELDS);
    assertNotEquals(result, other);
    setFields(other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final HistoricalTimeSeriesInfoMetaDataResult result = new HistoricalTimeSeriesInfoMetaDataResult();
    setFields(result);
    assertEquals(result.propertyNames().size(), 4);
    final Meta bean = result.metaBean();
    assertEquals(bean.dataFields().get(result), DATA_FIELDS);
    assertEquals(bean.dataProviders().get(result), DATA_PROVIDERS);
    assertEquals(bean.dataSources().get(result), DATA_SOURCES);
    assertEquals(bean.observationTimes().get(result), OBS_TIMES);
    assertEquals(result.property("dataFields").get(), DATA_FIELDS);
    assertEquals(result.property("dataProviders").get(), DATA_PROVIDERS);
    assertEquals(result.property("dataSources").get(), DATA_SOURCES);
    assertEquals(result.property("observationTimes").get(), OBS_TIMES);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HistoricalTimeSeriesInfoMetaDataResult result = new HistoricalTimeSeriesInfoMetaDataResult();
    setFields(result);
    assertEncodeDecodeCycle(HistoricalTimeSeriesInfoMetaDataResult.class, result);
  }

  private static void setFields(final HistoricalTimeSeriesInfoMetaDataResult result) {
    result.setDataFields(DATA_FIELDS);
    result.setDataProviders(DATA_PROVIDERS);
    result.setDataSources(DATA_SOURCES);
    result.setObservationTimes(OBS_TIMES);
  }
}
