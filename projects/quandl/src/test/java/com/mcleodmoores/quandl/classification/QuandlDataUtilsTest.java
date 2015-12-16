/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.classification;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustment;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

/**
 * Unit tests for {@link QuandlDataUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlDataUtilsTest {

  /**
   * Tests the behaviour when a null cache manager is supplied.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCacheManager() {
    QuandlDataUtils.createFieldAdjustmentMap(null);
  }

  /**
   * Tests the field normalization maps.
   */
  @Test
  public void testFieldMapping() {
    final List<HistoricalTimeSeriesFieldAdjustmentMap> fieldMappings = QuandlDataUtils.createFieldAdjustmentMap(CacheManager.newInstance());
    assertEquals(fieldMappings.size(), 1);
    for (final HistoricalTimeSeriesFieldAdjustmentMap fieldMapping : fieldMappings) {
      assertEquals(QuandlConstants.QUANDL_DATA_SOURCE_NAME, fieldMapping.getDataSource());
      final HistoricalTimeSeriesFieldAdjustment marketValuefieldAdjustment = fieldMapping.getFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE);
      final HistoricalTimeSeriesFieldAdjustment settleValuefieldAdjustment = fieldMapping.getFieldAdjustment(MarketDataRequirementNames.SETTLE_PRICE);
      final HistoricalTimeSeriesFieldAdjustment highValuefieldAdjustment = fieldMapping.getFieldAdjustment(MarketDataRequirementNames.HIGH);
      final HistoricalTimeSeriesFieldAdjustment lowValuefieldAdjustment = fieldMapping.getFieldAdjustment(MarketDataRequirementNames.LOW);
      if (marketValuefieldAdjustment != null) {
        assertNull(marketValuefieldAdjustment.getUnderlyingDataProvider());
        assertEquals(marketValuefieldAdjustment.getUnderlyingDataFields().size(), 3);
        assertEquals(Arrays.asList(QuandlConstants.VALUE_FIELD_NAME, QuandlConstants.RATE_FIELD_NAME, QuandlConstants.LAST_FIELD_NAME),
            marketValuefieldAdjustment.getUnderlyingDataFields());
      } else if (settleValuefieldAdjustment != null) {
        assertNull(settleValuefieldAdjustment.getUnderlyingDataProvider());
        assertEquals(settleValuefieldAdjustment.getUnderlyingDataFields().size(), 1);
        assertEquals(Arrays.asList(QuandlConstants.SETTLE_FIELD_NAME), settleValuefieldAdjustment.getUnderlyingDataFields());
      } else if (highValuefieldAdjustment != null) {
        assertNull(highValuefieldAdjustment.getUnderlyingDataProvider());
        assertEquals(highValuefieldAdjustment.getUnderlyingDataFields().size(), 1);
        assertEquals(Arrays.asList(QuandlConstants.HIGH_FIELD_NAME), highValuefieldAdjustment.getUnderlyingDataFields());
      } else if (lowValuefieldAdjustment != null) {
        assertNull(lowValuefieldAdjustment.getUnderlyingDataProvider());
        assertEquals(lowValuefieldAdjustment.getUnderlyingDataFields().size(), 1);
        assertEquals(Arrays.asList(QuandlConstants.LOW_FIELD_NAME), lowValuefieldAdjustment.getUnderlyingDataFields());
      } else {
        // another type of field mapping has been added that is not tested
        fail();
      }
    }
  }

  /**
   * Tests normalization of Quandl codes.
   */
  @Test
  public void testNormalization() {
    final List<HistoricalTimeSeriesFieldAdjustmentMap> fieldMappings = QuandlDataUtils.createFieldAdjustmentMap(CacheManager.newInstance());
    assertEquals(fieldMappings.size(), 1);
    boolean tested = false;
    for (final HistoricalTimeSeriesFieldAdjustmentMap fieldMapping : fieldMappings) {
      final HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMapping.getFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE);
      if (fieldAdjustment != null) {
        final SimpleHistoricalTimeSeries ts = new SimpleHistoricalTimeSeries(UniqueId.of("Test", "1"),
            ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2014, 12, 1)}, new double[] {123}));
        // cash rate, should be divided by 100
        SimpleHistoricalTimeSeries expectedTs = new SimpleHistoricalTimeSeries(UniqueId.of("Test", "1"),
            ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2014, 12, 1)}, new double[] {1.23}));
        HistoricalTimeSeries adjustedTs = fieldAdjustment.getAdjuster().adjust(QuandlConstants.ofCode("FRED/USDONTD156N").toBundle(), ts);
        assertEquals(expectedTs.getUniqueId(), adjustedTs.getUniqueId());
        assertEquals(expectedTs.getTimeSeries(), adjustedTs.getTimeSeries());
        // unclassifiable code, should not be adjusted
        expectedTs = new SimpleHistoricalTimeSeries(UniqueId.of("Test", "1"),
            ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2014, 12, 1)}, new double[] {123}));
        adjustedTs = fieldAdjustment.getAdjuster().adjust(QuandlConstants.ofCode("ABC/DEF").toBundle(), ts);
        assertEquals(adjustedTs.getUniqueId(), expectedTs.getUniqueId());
        assertEquals(adjustedTs.getTimeSeries(), expectedTs.getTimeSeries());
        tested = true;
      }
    }
    assertTrue(tested);
  }

  /**
   * Tests that series with no Quandl code in the identifiers are unadjusted.
   */
  @Test
  public void testNonQuandlCodes() {
    final List<HistoricalTimeSeriesFieldAdjustmentMap> fieldMappings = QuandlDataUtils.createFieldAdjustmentMap(CacheManager.newInstance());
    assertEquals(fieldMappings.size(), 1);
    boolean tested = false;
    for (final HistoricalTimeSeriesFieldAdjustmentMap fieldMapping : fieldMappings) {
      final HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMapping.getFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE);
      if (fieldAdjustment != null) {
        final SimpleHistoricalTimeSeries ts = new SimpleHistoricalTimeSeries(UniqueId.of("Test", "1"),
            ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2014, 12, 1)}, new double[] {123}));
        // cash rate, should be divided by 100
        final SimpleHistoricalTimeSeries expectedTs = new SimpleHistoricalTimeSeries(UniqueId.of("Test", "1"),
            ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2014, 12, 1)}, new double[] {123}));
        final HistoricalTimeSeries adjustedTs = fieldAdjustment.getAdjuster().adjust(ExternalSchemes.syntheticSecurityId("FRED/USDONTD156N").toBundle(), ts);
        assertEquals(adjustedTs.getUniqueId(), expectedTs.getUniqueId());
        assertEquals(adjustedTs.getTimeSeries(), expectedTs.getTimeSeries());
        tested = true;
      }
    }
    assertTrue(tested);
  }
}
