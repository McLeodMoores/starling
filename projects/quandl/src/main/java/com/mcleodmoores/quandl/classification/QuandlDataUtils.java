/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.classification;

import static com.mcleodmoores.quandl.QuandlConstants.HIGH_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.LAST_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.LOW_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.QUANDL_DATA_SOURCE_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.RATE_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.SETTLE_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.VALUE_FIELD_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mcleodmoores.quandl.normalization.QuandlNormalizer;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.CacheManager;

/**
 * Utilities class for Quandl data.
 */
public final class QuandlDataUtils {

  /**
   * Restricted constructor.
   */
  private QuandlDataUtils() {
  }

  /**
   * Creates a list of historical time series field adjustments, which map Quandl data fields to OpenGamma
   * equivalents and caches any results.
   * @param cacheManager  the cache manager, not null
   * @return  a list of field adjustment maps.
   */
  public static List<HistoricalTimeSeriesFieldAdjustmentMap> createFieldAdjustmentMap(final CacheManager cacheManager) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    final List<HistoricalTimeSeriesFieldAdjustmentMap> adjustmentMaps = new ArrayList<>();
    final QuandlNormalizer quandlNormalizer = new QuandlNormalizer(new QuandlCodeClassifier(cacheManager));
    final QuandlHistoricalTimeSeriesFieldAdjustmentMap fieldAdjustmentMap = new QuandlHistoricalTimeSeriesFieldAdjustmentMap(QUANDL_DATA_SOURCE_NAME);
    fieldAdjustmentMap.addFieldAdjustment(
        MarketDataRequirementNames.MARKET_VALUE,
        null,
        Arrays.asList(VALUE_FIELD_NAME, RATE_FIELD_NAME, LAST_FIELD_NAME),
        quandlNormalizer);
    fieldAdjustmentMap.addFieldAdjustment(
        MarketDataRequirementNames.SETTLE_PRICE,
        null,
        Arrays.asList(SETTLE_FIELD_NAME),
        quandlNormalizer);
    fieldAdjustmentMap.addFieldAdjustment(
        MarketDataRequirementNames.HIGH,
        null,
        Arrays.asList(HIGH_FIELD_NAME),
        quandlNormalizer);
    fieldAdjustmentMap.addFieldAdjustment(
        MarketDataRequirementNames.LOW,
        null,
        Arrays.asList(LOW_FIELD_NAME),
        quandlNormalizer);
    adjustmentMaps.add(fieldAdjustmentMap);
    return adjustmentMaps;
  }
}
