/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.classification;

import static com.mcleodmoores.quandl.QuandlConstants.LAST_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.QUANDL_DATA_SOURCE_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.RATE_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.VALUE_FIELD_NAME;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.mcleodmoores.quandl.normalization.QuandlNormalizer;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustment;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

/**
 * Unit tests for {@link QuandlHistoricalTimeSeriesFieldAdjustmentMap}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlHistoricalTimeSeriesFieldAdjustmentMapTest {
  /** Field adjustment map */
  private static final QuandlHistoricalTimeSeriesFieldAdjustmentMap FIELD_ADJUSTMENTS =
      new QuandlHistoricalTimeSeriesFieldAdjustmentMap(QUANDL_DATA_SOURCE_NAME);

  static {
    final QuandlNormalizer normalizer = new QuandlNormalizer(new QuandlCodeClassifier(CacheManager.newInstance()));
    FIELD_ADJUSTMENTS.addFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE, null, Arrays.asList(VALUE_FIELD_NAME, RATE_FIELD_NAME), normalizer);
    FIELD_ADJUSTMENTS.addFieldAdjustment(MarketDataRequirementNames.LAST, null, LAST_FIELD_NAME, normalizer);
  }

  /**
   * Tests the behaviour when the data source name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataSourceName() {
    new QuandlHistoricalTimeSeriesFieldAdjustmentMap(null);
  }

  /**
   * Tests the field adjustments.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testFieldAdjustments() {
    HistoricalTimeSeriesFieldAdjustment adjustment = FIELD_ADJUSTMENTS.getFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE);
    assertEquals(adjustment.getUnderlyingDataFields(), Arrays.asList(VALUE_FIELD_NAME, RATE_FIELD_NAME));
    // hard-coded to get the first value
    assertEquals(adjustment.getUnderlyingDataField(), VALUE_FIELD_NAME);
    adjustment = FIELD_ADJUSTMENTS.getFieldAdjustment(MarketDataRequirementNames.LAST);
    assertEquals(adjustment.getUnderlyingDataFields(), Arrays.asList(LAST_FIELD_NAME));
  }
}
