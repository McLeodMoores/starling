/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link MarketDataInfo}.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataInfoTest {
  /** Time series meta data */
  private static final TimeSeriesMarketDataMetaData TS_META_DATA_1 = TimeSeriesMarketDataMetaData.builder()
      .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
      .ageLimit("50")
      .startDate(LocalDate.of(2015, 1, 1))
      .includeStart(true)
      .endDate(LocalDate.of(2016, 1, 1))
      .includeEnd(false)
      .type(LocalDateDoubleTimeSeries.class)
      .build();
  /** Time series meta data */
  private static final TimeSeriesMarketDataMetaData TS_META_DATA_2 = TimeSeriesMarketDataMetaData.builder()
      .adjust(new HistoricalTimeSeriesAdjustment.DivideBy(100).toString())
      .ageLimit("50")
      .startDate(LocalDate.of(2017, 1, 1))
      .includeStart(true)
      .endDate(LocalDate.of(2016, 1, 1))
      .includeEnd(false)
      .type(LocalDateDoubleTimeSeries.class)
      .build();
  /** A market data key */
  private static final MarketDataKey KEY_1 = MarketDataKey.of(ExternalId.of("TEST", "AUDUSD").toBundle(), DataField.of(MARKET_VALUE));
  /** A market data key */
  private static final MarketDataKey KEY_2 = MarketDataKey.of(ExternalId.of("TEST", "AUDEUR").toBundle(), DataField.of(MARKET_VALUE));

  /**
   * Tests the behaviour when the scalar data are null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullScalarData() {
    MarketDataInfo.of(null, Collections.<MarketDataKey, TimeSeriesMarketDataMetaData>emptyMap());
  }

  /**
   * Tests the behaviour when the time series data are null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimeSeriesData() {
    MarketDataInfo.of(Collections.<MarketDataKey, ScalarMarketDataMetaData>emptyMap(), null);
  }

  /**
   * Tests the behaviour when the data are null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MarketDataInfo.of(null);
  }

  /**
   * Tests the behaviour when the data contains an unhandled meta-data type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnhandledMetaDataType() {
    final MarketDataMetaData metaData = new MarketDataMetaData() {

      @Override
      public Class<?> getType() {
        return String.class;
      }
    };
    MarketDataInfo.of(Collections.singletonMap(KEY_1, metaData));
  }

  /**
   * Tests the behaviour when attempting to add a null key for a scalar data point.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKeyForScalar() {
    MarketDataInfo.empty().addScalarInfo(null, ScalarMarketDataMetaData.INSTANCE);
  }

  /**
   * Tests the behaviour when attempting to add null meta data for a scalar data point.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMetaDataForScalar() {
    MarketDataInfo.empty().addScalarInfo(KEY_1, null);
  }

  /**
   * Tests the behaviour when attempting to add a null key for a time series.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKeyForTimeSeries() {
    MarketDataInfo.empty().addTimeSeriesInfo(null, TS_META_DATA_1);
  }

  /**
   * Tests the behaviour when attempting to add null meta data for a time series.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMetaDataForTimeSeries() {
    MarketDataInfo.empty().addTimeSeriesInfo(KEY_1, null);
  }

  /**
   * Tests the behaviour when attempting to add a null key for a time series.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKeyForData() {
    MarketDataInfo.empty().addInfo(null, TS_META_DATA_1);
  }

  /**
   * Tests the behaviour when attempting to add null meta data for a time series.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMetaDataForData() {
    MarketDataInfo.empty().addInfo(KEY_1, null);
  }

  /**
   * Tests the behaviour when attempting to add null scalar data.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullScalarDataMap() {
    MarketDataInfo.empty().addScalarInfo(null);
  }

  /**
   * Tests the behaviour when attempting to add null time series data.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimeSeriesDataMap() {
    MarketDataInfo.empty().addTimeSeriesInfo(null);
  }

  /**
   * Tests the behaviour when attempting to add null data.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataMap() {
    MarketDataInfo.empty().addInfo(null);
  }

  /**
   * Tests the behaviour when data contains an unhandled meta-data type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddUnhandledMetaDataType() {
    final MarketDataMetaData metaData = new MarketDataMetaData() {

      @Override
      public Class<?> getType() {
        return String.class;
      }
    };
    MarketDataInfo.empty().addInfo(KEY_1, metaData);
  }

  /**
   * Tests the behaviour when data contains an unhandled meta-data type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddUnhandledMetaDataTypeInMap() {
    final MarketDataMetaData metaData = new MarketDataMetaData() {

      @Override
      public Class<?> getType() {
        return String.class;
      }
    };
    MarketDataInfo.empty().addInfo(Collections.singletonMap(KEY_1, metaData));
  }

  /**
   * Tests the creation of a mutable empty object.
   */
  @Test
  public void testEmpty() {
    final MarketDataInfo empty = MarketDataInfo.empty();
    assertEquals(empty.size(), 0);
    // tests that the object is mutable
    empty.addScalarInfo(KEY_1, ScalarMarketDataMetaData.INSTANCE);
    assertEquals(empty.size(), 1);
    empty.addTimeSeriesInfo(KEY_1, TS_META_DATA_1);
    assertEquals(empty.size(), 2);
  }

  /**
   * Tests construction of an object.
   */
  @Test
  public void testCreatedWithData() {
    Map<MarketDataKey, ScalarMarketDataMetaData> scalars = Collections.singletonMap(KEY_1, ScalarMarketDataMetaData.INSTANCE);
    Map<MarketDataKey, TimeSeriesMarketDataMetaData> timeSeries = Collections.emptyMap();
    final MarketDataInfo info = MarketDataInfo.of(scalars, timeSeries);
    assertEquals(info.size(), 1);
    // none of these keys were present in the map
    assertFalse(info.addScalarInfo(KEY_2, ScalarMarketDataMetaData.INSTANCE));
    assertFalse(info.addTimeSeriesInfo(KEY_1, TS_META_DATA_1));
    assertFalse(info.addTimeSeriesInfo(KEY_2, TS_META_DATA_2));
    assertEquals(info.size(), 4);
    // add keys that are already present
    assertTrue(info.addScalarInfo(KEY_2, ScalarMarketDataMetaData.INSTANCE));
    assertTrue(info.addTimeSeriesInfo(KEY_1, TS_META_DATA_1));
    assertEquals(info.size(), 4);
    // construction with maps
    scalars = new HashMap<>();
    scalars.put(KEY_1, ScalarMarketDataMetaData.INSTANCE);
    scalars.put(KEY_2, ScalarMarketDataMetaData.INSTANCE);
    timeSeries = new HashMap<>();
    timeSeries.put(KEY_1, TS_META_DATA_1);
    timeSeries.put(KEY_2, TS_META_DATA_2);
    assertEquals(MarketDataInfo.of(scalars, timeSeries), info);
    // construction with a single map
    final Map<MarketDataKey, MarketDataMetaData> mixedData = new HashMap<>();
    mixedData.put(KEY_1, ScalarMarketDataMetaData.INSTANCE);
    mixedData.put(KEY_2, TS_META_DATA_2);
    assertEquals(MarketDataInfo.of(mixedData), MarketDataInfo.of(Collections.singletonMap(KEY_1, ScalarMarketDataMetaData.INSTANCE), 
        Collections.singletonMap(KEY_2, TS_META_DATA_2)));
    final MarketDataInfo infoFromMixedData = MarketDataInfo.empty();
    assertFalse(infoFromMixedData.addInfo(KEY_1, ScalarMarketDataMetaData.INSTANCE));
    assertFalse(infoFromMixedData.addInfo(KEY_2, TS_META_DATA_2));
    assertTrue(infoFromMixedData.addInfo(KEY_1, ScalarMarketDataMetaData.INSTANCE));
    assertTrue(infoFromMixedData.addInfo(KEY_2, TS_META_DATA_2));
    assertEquals(infoFromMixedData, MarketDataInfo.of(mixedData));
  }

  /**
   * Tests adding multiple market data points.
   */
  @Test
  public void testMultipleAdd() {
    final Map<MarketDataKey, ScalarMarketDataMetaData> scalars = Collections.singletonMap(KEY_1, ScalarMarketDataMetaData.INSTANCE);
    final Map<MarketDataKey, TimeSeriesMarketDataMetaData> timeSeries = Collections.singletonMap(KEY_2, TS_META_DATA_1);
    final MarketDataInfo expected = MarketDataInfo.of(scalars, timeSeries);
    MarketDataInfo info = MarketDataInfo.empty();
    info.addScalarInfo(scalars);
    info.addTimeSeriesInfo(timeSeries);
    assertEquals(info, expected);
    info = MarketDataInfo.empty();
    info.addInfo(scalars);
    info.addInfo(timeSeries);
    assertEquals(info, expected);
    final Map<MarketDataKey, MarketDataMetaData> mixed = new HashMap<MarketDataKey, MarketDataMetaData>(scalars);
    mixed.putAll(timeSeries);
    info = MarketDataInfo.empty();
    info.addInfo(mixed);
    assertEquals(info, expected);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    final Map<MarketDataKey, ScalarMarketDataMetaData> scalars = new HashMap<>();
    scalars.put(KEY_1, ScalarMarketDataMetaData.INSTANCE);
    scalars.put(KEY_2, ScalarMarketDataMetaData.INSTANCE);
    final Map<MarketDataKey, TimeSeriesMarketDataMetaData> timeSeries = new HashMap<>();
    timeSeries.put(KEY_2, TS_META_DATA_1);
    final MarketDataInfo info = MarketDataInfo.of(scalars, timeSeries);
    assertEquals(info.getScalars(), scalars);
    assertEquals(info.getTimeSeries(), timeSeries);
  }

  /**
   * Tests the hashCode() and equals() methods.
   */
  @Test
  public void testHashCodeEquals() {
    final MarketDataInfo info = MarketDataInfo.of(Collections.singletonMap(KEY_1, ScalarMarketDataMetaData.INSTANCE), 
        Collections.singletonMap(KEY_2, TS_META_DATA_2));
    assertEquals(info, info);
    assertNotEquals(null, info);
    assertNotEquals(MarketDataSet.empty(), info);
    MarketDataInfo other = MarketDataInfo.of(Collections.singletonMap(KEY_1, ScalarMarketDataMetaData.INSTANCE), 
        Collections.singletonMap(KEY_2, TS_META_DATA_2));
    assertEquals(other, info);
    assertEquals(other.hashCode(), info.hashCode());
    other = MarketDataInfo.of(Collections.singletonMap(KEY_1, ScalarMarketDataMetaData.INSTANCE), Collections.singletonMap(KEY_2, TS_META_DATA_1));
    assertNotEquals(info, other);
    other = MarketDataInfo.of(Collections.singletonMap(KEY_2, ScalarMarketDataMetaData.INSTANCE), Collections.singletonMap(KEY_2, TS_META_DATA_2));
    assertNotEquals(info, other);
  }

}
