/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesResolutionResult}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesResolutionResultTest {

  /**
   * Tests the object.
   */
  @Test
  public void test() {
    final HistoricalTimeSeriesInfo info = new TestHistoricalTimeSeriesInfo();
    final TestHistoricalTimeSeriesAdjuster adjuster = new TestHistoricalTimeSeriesAdjuster();
    HistoricalTimeSeriesResolutionResult result = new HistoricalTimeSeriesResolutionResult(info);
    assertEquals(result.getHistoricalTimeSeriesInfo(), info);
    assertNull(result.getAdjuster());
    result = new HistoricalTimeSeriesResolutionResult(info, adjuster);
    assertEquals(result.getHistoricalTimeSeriesInfo(), info);
    assertEquals(result.getAdjuster(), adjuster);
  }

  /**
   * Dummy class.
   */
  static class TestHistoricalTimeSeriesInfo implements HistoricalTimeSeriesInfo {

    @Override
    public UniqueId getUniqueId() {
      return UniqueId.of("uid", "1");
    }

    @Override
    public ExternalIdBundleWithDates getExternalIdBundle() {
      return ExternalIdBundleWithDates.of(ExternalIdBundle.of("eid", "10"));
    }

    @Override
    public String getName() {
      return "name";
    }

    @Override
    public String getDataField() {
      return "field";
    }

    @Override
    public String getDataSource() {
      return "source";
    }

    @Override
    public String getDataProvider() {
      return "provider";
    }

    @Override
    public String getObservationTime() {
      return "obs";
    }

    @Override
    public ObjectId getTimeSeriesObjectId() {
      return ObjectId.of("oid", "100");
    }

    @Override
    public int hashCode() {
      return 13;
    }

    @Override
    public boolean equals(final Object o) {
      return o instanceof TestHistoricalTimeSeriesInfo && ((TestHistoricalTimeSeriesInfo) o).getName().equals(getName());
    }
  }

  /**
   * Dummy class.
   */
  static class TestHistoricalTimeSeriesAdjuster implements HistoricalTimeSeriesAdjuster {

    @Override
    public HistoricalTimeSeries adjust(final ExternalIdBundle securityIdBundle, final HistoricalTimeSeries timeSeries) {
      return timeSeries;
    }

    @Override
    public HistoricalTimeSeriesAdjustment getAdjustment(final ExternalIdBundle securityIdBundle) {
      return HistoricalTimeSeriesAdjustment.NoOp.INSTANCE;
    }

    @Override
    public int hashCode() {
      return 17;
    }

    @Override
    public boolean equals(final Object o) {
      return o instanceof TestHistoricalTimeSeriesAdjuster;
    }
  }
}
