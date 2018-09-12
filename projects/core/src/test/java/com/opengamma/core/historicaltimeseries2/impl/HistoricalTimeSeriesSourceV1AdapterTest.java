/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.historicaltimeseries2.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.historicaltimeseries.impl.MockHistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries2.HistoricalDataRequest;
import com.opengamma.core.historicaltimeseries2.HistoricalTimeSeriesSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesSourceV1Adapter}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesSourceV1AdapterTest {
  private static final LocalDateDoubleTimeSeries TS_1;
  private static final LocalDateDoubleTimeSeries TS_2;
  static {
    final LocalDateDoubleTimeSeriesBuilder builder1 = ImmutableLocalDateDoubleTimeSeries.builder();
    final LocalDateDoubleTimeSeriesBuilder builder2 = ImmutableLocalDateDoubleTimeSeries.builder();
    for (int i = 0; i < 100; i++) {
      builder1.put(LocalDate.now().minusDays(100 + i), i);
      builder2.put(LocalDate.now().minusDays(100 + i), i * 1.01);
    }
    TS_1 = builder1.build();
    TS_2 = builder2.build();
  }
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("provider", "1");
  private static final MockHistoricalTimeSeriesSource SOURCE = new MockHistoricalTimeSeriesSource();
  static {
    SOURCE.storeHistoricalTimeSeries(EIDS, "SOURCE", "PROVIDER", MarketDataRequirementNames.ASK, TS_1);
    SOURCE.storeHistoricalTimeSeries(EIDS, "SOURCE", "PROVIDER", MarketDataRequirementNames.MARKET_VALUE, TS_2);
  }
  private static final HistoricalTimeSeriesSource WRAPPER = HistoricalTimeSeriesSourceV1Adapter.of(SOURCE);

  /**
   * Tests that the original source cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSource() {
    HistoricalTimeSeriesSourceV1Adapter.of(null);
  }

  /**
   * Tests the case where the data request does not specify the field.
   */
  @Test
  public void testNoField() {
    HistoricalDataRequest request = HistoricalDataRequest.builder()
        .bundle(EIDS)
        .build();
    assertEquals(WRAPPER.getHistoricalTimeSeries(request).getTimeSeries(), TS_2);
    request = HistoricalDataRequest.builder()
        .bundle(EIDS.withExternalId(ExternalId.of("provider", "2")))
        .build();
    assertNull(WRAPPER.getHistoricalTimeSeries(request));
  }

  /**
   * Tests the case where the data request specifies the field.
   */
  @Test
  public void testField() {
    HistoricalDataRequest request = HistoricalDataRequest.builder()
        .bundle(EIDS)
        .field(MarketDataRequirementNames.MARKET_VALUE)
        .build();
    assertEquals(WRAPPER.getHistoricalTimeSeries(request).getTimeSeries(), TS_2);
    request = HistoricalDataRequest.builder()
        .bundle(EIDS)
        .field(MarketDataRequirementNames.ASK)
        .build();
    assertEquals(WRAPPER.getHistoricalTimeSeries(request).getTimeSeries(), TS_1);
    request = HistoricalDataRequest.builder()
        .bundle(EIDS)
        .field(MarketDataRequirementNames.BID)
        .build();
    assertNull(WRAPPER.getHistoricalTimeSeries(request));
  }

  /**
   * Tests that the change manager is delegated to the underlying.
   */
  @Test
  public void testChangeManager() {
    assertTrue(WRAPPER.changeManager() instanceof BasicChangeManager);
  }
}
