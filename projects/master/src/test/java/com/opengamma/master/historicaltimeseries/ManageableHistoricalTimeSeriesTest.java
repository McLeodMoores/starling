/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries.Meta;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ManageableHistoricalTimeSeries}.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableHistoricalTimeSeriesTest extends AbstractFudgeBuilderTestCase {
  private static final UniqueId UID = UniqueId.of("hist", "1");
  private static final Instant VERSION_INSTANT = Instant.ofEpochSecond(1000000);
  private static final Instant VERSION_CORRECTION = Instant.ofEpochSecond(2000000);
  private static final LocalDateDoubleTimeSeries TS = ImmutableLocalDateDoubleTimeSeries.builder().put(LocalDate.of(2019, 1, 1), 1234.).build();

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ManageableHistoricalTimeSeries ts = new ManageableHistoricalTimeSeries();
    setFields(ts);
    assertEquals(ts.getCorrectionInstant(), VERSION_CORRECTION);
    assertEquals(ts.getTimeSeries(), TS);
    assertEquals(ts.getUniqueId(), UID);
    assertEquals(ts.getVersionInstant(), VERSION_INSTANT);
    assertEquals(ts, ts);
    assertEquals(ts.toString(), "ManageableHistoricalTimeSeries{uniqueId=hist~1, versionInstant=1970-01-12T13:46:40Z, "
        + "correctionInstant=1970-01-24T03:33:20Z, timeSeries=ImmutableLocalDateDoubleTimeSeries[(2019-01-01, 1234.0)]}");
    final ManageableHistoricalTimeSeries other = new ManageableHistoricalTimeSeries();
    setFields(other);
    assertEquals(ts, other);
    assertEquals(ts.hashCode(), other.hashCode());
    other.setCorrectionInstant(VERSION_INSTANT);
    assertNotEquals(ts, other);
    other.setCorrectionInstant(null);
    assertNotEquals(ts, other);
    setFields(other);
    other.setTimeSeries(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    assertNotEquals(ts, other);
    other.setTimeSeries(null);
    assertNotEquals(ts, other);
    setFields(other);
    other.setUniqueId(UniqueId.of("uid", "0"));
    assertNotEquals(ts, other);
    other.setUniqueId(null);
    assertNotEquals(ts, other);
    setFields(other);
    other.setVersionInstant(VERSION_CORRECTION);
    assertNotEquals(ts, other);
    other.setVersionInstant(null);
    assertNotEquals(ts, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ManageableHistoricalTimeSeries ts = new ManageableHistoricalTimeSeries();
    setFields(ts);
    assertEquals(ts.propertyNames().size(), 4);
    final Meta bean = ts.metaBean();
    assertEquals(bean.correctionInstant().get(ts), VERSION_CORRECTION);
    assertEquals(bean.timeSeries().get(ts), TS);
    assertEquals(bean.uniqueId().get(ts), UID);
    assertEquals(bean.versionInstant().get(ts), VERSION_INSTANT);
    assertEquals(ts.property("correctionInstant").get(), VERSION_CORRECTION);
    assertEquals(ts.property("timeSeries").get(), TS);
    assertEquals(ts.property("uniqueId").get(), UID);
    assertEquals(ts.property("versionInstant").get(), VERSION_INSTANT);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ManageableHistoricalTimeSeries ts = new ManageableHistoricalTimeSeries();
    setFields(ts);
    assertEquals(cycleObjectProxy(ManageableHistoricalTimeSeries.class, ts), ts);
    assertEquals(cycleObjectBytes(ManageableHistoricalTimeSeries.class, ts), ts);
    assertEquals(cycleObjectXml(ManageableHistoricalTimeSeries.class, ts), ts);
  }

  /**
   * A typed Joda-bean converter for the underlying time series is not
   * available.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCycleJodaBean() {
    final ManageableHistoricalTimeSeries ts = new ManageableHistoricalTimeSeries();
    setFields(ts);
    assertEquals(cycleObjectJodaXml(ManageableHistoricalTimeSeries.class, ts), ts);
  }

  private static void setFields(final ManageableHistoricalTimeSeries ts) {
    ts.setCorrectionInstant(VERSION_CORRECTION);
    ts.setTimeSeries(TS);
    ts.setUniqueId(UID);
    ts.setVersionInstant(VERSION_INSTANT);
  }
}
