/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

/**
 * Unit tests for {@link TimeSeriesMarketDataMetaData}.
 */
public class TimeSeriesMarketDataMetaDataTest {

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final TimeSeriesMarketDataMetaData metaData = TimeSeriesMarketDataMetaData.builder()
        .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
        .ageLimit("50")
        .startDate(LocalDate.of(2015, 1, 1))
        .includeStart(true)
        .endDate(LocalDate.of(2016, 1, 1))
        .includeEnd(false)
        .type(LocalDateDoubleTimeSeries.class)
        .build();
    assertEquals(metaData, metaData);
    assertNotEquals(null, metaData);
    assertNotEquals(ScalarMarketDataMetaData.INSTANCE, metaData);
    TimeSeriesMarketDataMetaData other = TimeSeriesMarketDataMetaData.builder()
        .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
        .ageLimit("50")
        .startDate(LocalDate.of(2015, 1, 1))
        .includeStart(true)
        .endDate(LocalDate.of(2016, 1, 1))
        .includeEnd(false)
        .type(LocalDateDoubleTimeSeries.class)
        .build();
    assertEquals(other, metaData);
    assertEquals(other.hashCode(), metaData.hashCode());
    other = TimeSeriesMarketDataMetaData.builder()
        .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(10000).toString())
        .ageLimit("50")
        .startDate(LocalDate.of(2015, 1, 1))
        .includeStart(true)
        .endDate(LocalDate.of(2016, 1, 1))
        .includeEnd(false)
        .type(LocalDateDoubleTimeSeries.class)
        .build();
    assertNotEquals(other, metaData);
    other = TimeSeriesMarketDataMetaData.builder()
        .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
        .ageLimit("100")
        .startDate(LocalDate.of(2015, 1, 1))
        .includeStart(true)
        .endDate(LocalDate.of(2016, 1, 1))
        .includeEnd(false)
        .type(LocalDateDoubleTimeSeries.class)
        .build();
    assertNotEquals(other, metaData);
    other = TimeSeriesMarketDataMetaData.builder()
        .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
        .ageLimit("50")
        .startDate(LocalDate.of(2015, 2, 1))
        .includeStart(true)
        .endDate(LocalDate.of(2016, 1, 1))
        .includeEnd(false)
        .type(LocalDateDoubleTimeSeries.class)
        .build();
    assertNotEquals(other, metaData);
    other = TimeSeriesMarketDataMetaData.builder()
        .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
        .ageLimit("50")
        .startDate(LocalDate.of(2015, 1, 1))
        .includeStart(false)
        .endDate(LocalDate.of(2016, 1, 1))
        .includeEnd(false)
        .type(LocalDateDoubleTimeSeries.class)
        .build();
    assertNotEquals(other, metaData);
    other = TimeSeriesMarketDataMetaData.builder()
        .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
        .ageLimit("50")
        .startDate(LocalDate.of(2015, 1, 1))
        .includeStart(true)
        .endDate(LocalDate.of(2016, 2, 1))
        .includeEnd(false)
        .type(LocalDateDoubleTimeSeries.class)
        .build();
    assertNotEquals(other, metaData);
    other = TimeSeriesMarketDataMetaData.builder()
        .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
        .ageLimit("50")
        .startDate(LocalDate.of(2015, 1, 1))
        .includeStart(true)
        .endDate(LocalDate.of(2016, 1, 1))
        .includeEnd(true)
        .type(LocalDateDoubleTimeSeries.class)
        .build();
    assertNotEquals(other, metaData);
    other = TimeSeriesMarketDataMetaData.builder()
        .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
        .ageLimit("50")
        .startDate(LocalDate.of(2015, 1, 1))
        .includeStart(true)
        .endDate(LocalDate.of(2016, 1, 1))
        .includeEnd(false)
        .type(ZonedDateTimeDoubleTimeSeries.class)
        .build();
    assertNotEquals(other, metaData);
  }
}
