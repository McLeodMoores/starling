/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.historicaltimeseries;

import static com.mcleodmoores.quandl.QuandlConstants.LAST_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.QUANDL_DATA_SOURCE_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.RATE_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.SETTLE_FIELD_NAME;
import static com.mcleodmoores.quandl.QuandlConstants.VALUE_FIELD_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.classification.QuandlCodeClassifier;
import com.mcleodmoores.quandl.classification.QuandlDataUtils;
import com.mcleodmoores.quandl.classification.QuandlHistoricalTimeSeriesFieldAdjustmentMap;
import com.mcleodmoores.quandl.normalization.QuandlNormalizer;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingRule;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

/**
 * Unit tests for {@link QuandlFieldMappingHistoricalTimeSeriesResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlFieldMappingHistoricalTimeSeriesResolverTest {
  /** A dummy data source name */
  private static final String DUMMY_DATA_SOURCE_NAME = "Dummy";
  /** Quandl field adjustments */
  private static final QuandlHistoricalTimeSeriesFieldAdjustmentMap FIELD_ADJUSTMENTS1 =
      new QuandlHistoricalTimeSeriesFieldAdjustmentMap(QUANDL_DATA_SOURCE_NAME);
  /** Dummy field adjustments */
  private static final HistoricalTimeSeriesFieldAdjustmentMap FIELD_ADJUSTMENTS2 = new HistoricalTimeSeriesFieldAdjustmentMap(DUMMY_DATA_SOURCE_NAME);
  /** The field maps */
  private static final Collection<HistoricalTimeSeriesFieldAdjustmentMap> FIELD_MAPS = new HashSet<>();
  /** The id bundle for a Quandl value time series */
  private static final ExternalIdBundle ID_1 = QuandlConstants.ofCode("HTS1").toBundle();
  /** The id bundle for a Quandl rate time series */
  private static final ExternalIdBundle ID_2 = QuandlConstants.ofCode("HTS2").toBundle();
  /** The id bundle for a Quandl last time series */
  private static final ExternalIdBundle ID_3 = ExternalIdBundle.of("Dummy", "HTS3");
  /** The id bundle for a dummy value time series */
  private static final ExternalIdBundle ID_4 = ExternalIdBundle.of("Dummy", "HTS4");
  /** The id bundle for a dummy last time series */
  private static final ExternalIdBundle ID_5 = ExternalIdBundle.of("Dummy", "HTS5");
  /** The id bundle for a dummy bid time series */
  private static final ExternalIdBundle ID_6 = ExternalIdBundle.of("Dummy", "HTS6");
  /** The resolver */
  private static final QuandlFieldMappingHistoricalTimeSeriesResolver RESOLVER;
  /** A historical time series master */
  private static final HistoricalTimeSeriesMaster HTS_MASTER = new InMemoryHistoricalTimeSeriesMaster();
  /** A config master */
  private static final InMemoryConfigMaster CONFIG_MASTER = new InMemoryConfigMaster();
  /** A config source */
  private static final MasterConfigSource CONFIG_SOURCE = new MasterConfigSource(CONFIG_MASTER);
  /** A time series selector */
  private static final HistoricalTimeSeriesSelector HTS_SELECTOR = new DefaultHistoricalTimeSeriesSelector(CONFIG_SOURCE);

  static {
    final QuandlNormalizer normalizer = new QuandlNormalizer(new QuandlCodeClassifier(CacheManager.newInstance()));
    FIELD_ADJUSTMENTS1.addFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE, null, Arrays.asList(VALUE_FIELD_NAME, RATE_FIELD_NAME), normalizer);
    FIELD_ADJUSTMENTS1.addFieldAdjustment(MarketDataRequirementNames.LAST, null, LAST_FIELD_NAME, normalizer);
    FIELD_ADJUSTMENTS2.addFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE, null, "Dummy_Value", normalizer);
    FIELD_ADJUSTMENTS2.addFieldAdjustment(MarketDataRequirementNames.LAST, null, "Dummy_Last", normalizer);
    FIELD_ADJUSTMENTS2.addFieldAdjustment(MarketDataRequirementNames.BID, null, "Dummy_Bid", normalizer);
    FIELD_MAPS.add(FIELD_ADJUSTMENTS1);
    FIELD_MAPS.add(FIELD_ADJUSTMENTS2);
    final ManageableHistoricalTimeSeriesInfo htsInfo1 = new ManageableHistoricalTimeSeriesInfo();
    htsInfo1.setDataField(VALUE_FIELD_NAME);
    htsInfo1.setDataProvider(QUANDL_DATA_SOURCE_NAME);
    htsInfo1.setDataSource(QUANDL_DATA_SOURCE_NAME);
    htsInfo1.setExternalIdBundle(ExternalIdBundleWithDates.of(ID_1));
    htsInfo1.setName("HTS1");
    htsInfo1.setObservationTime("LONDON_CLOSE");
    final ManageableHistoricalTimeSeriesInfo htsInfo2 = new ManageableHistoricalTimeSeriesInfo();
    htsInfo2.setDataField(RATE_FIELD_NAME);
    htsInfo2.setDataProvider(QUANDL_DATA_SOURCE_NAME);
    htsInfo2.setDataSource(QUANDL_DATA_SOURCE_NAME);
    htsInfo2.setExternalIdBundle(ExternalIdBundleWithDates.of(ID_2));
    htsInfo2.setName("HTS2");
    htsInfo2.setObservationTime("LONDON_CLOSE");
    final ManageableHistoricalTimeSeriesInfo htsInfo3 = new ManageableHistoricalTimeSeriesInfo();
    htsInfo3.setDataField(LAST_FIELD_NAME);
    htsInfo3.setDataProvider(QUANDL_DATA_SOURCE_NAME);
    htsInfo3.setDataSource(QUANDL_DATA_SOURCE_NAME);
    htsInfo3.setExternalIdBundle(ExternalIdBundleWithDates.of(ID_3));
    htsInfo3.setName("HTS3");
    htsInfo3.setObservationTime("LONDON_CLOSE");
    final ManageableHistoricalTimeSeriesInfo htsInfo4 = new ManageableHistoricalTimeSeriesInfo();
    htsInfo4.setDataField("Dummy_Value");
    htsInfo4.setDataProvider(DUMMY_DATA_SOURCE_NAME);
    htsInfo4.setDataSource(DUMMY_DATA_SOURCE_NAME);
    htsInfo4.setExternalIdBundle(ExternalIdBundleWithDates.of(ID_4));
    htsInfo4.setName("HTS4");
    htsInfo4.setObservationTime("LONDON_CLOSE");
    final ManageableHistoricalTimeSeriesInfo htsInfo5 = new ManageableHistoricalTimeSeriesInfo();
    htsInfo5.setDataField("Dummy_Last");
    htsInfo5.setDataProvider(DUMMY_DATA_SOURCE_NAME);
    htsInfo5.setDataSource(DUMMY_DATA_SOURCE_NAME);
    htsInfo5.setExternalIdBundle(ExternalIdBundleWithDates.of(ID_5));
    htsInfo5.setName("HTS5");
    htsInfo5.setObservationTime("LONDON_CLOSE");
    final ManageableHistoricalTimeSeriesInfo htsInfo6 = new ManageableHistoricalTimeSeriesInfo();
    htsInfo6.setDataField("Dummy_Bid");
    htsInfo6.setDataProvider(DUMMY_DATA_SOURCE_NAME);
    htsInfo6.setDataSource(DUMMY_DATA_SOURCE_NAME);
    htsInfo6.setExternalIdBundle(ExternalIdBundleWithDates.of(ID_6));
    htsInfo6.setName("HTS6");
    htsInfo6.setObservationTime("LONDON_CLOSE");
    final ManageableHistoricalTimeSeriesInfo htsInfo7 = new ManageableHistoricalTimeSeriesInfo();
    htsInfo7.setDataField(VALUE_FIELD_NAME);
    htsInfo7.setDataProvider(DUMMY_DATA_SOURCE_NAME);
    htsInfo7.setDataSource(DUMMY_DATA_SOURCE_NAME);
    htsInfo7.setExternalIdBundle(ExternalIdBundleWithDates.of(ID_1));
    htsInfo7.setName("HTS1");
    htsInfo7.setObservationTime("LONDON_CLOSE");
    final HistoricalTimeSeriesInfoDocument htsDoc1 = new HistoricalTimeSeriesInfoDocument(htsInfo1);
    HTS_MASTER.add(htsDoc1);
    final HistoricalTimeSeriesInfoDocument htsDoc2 = new HistoricalTimeSeriesInfoDocument(htsInfo2);
    HTS_MASTER.add(htsDoc2);
    final HistoricalTimeSeriesInfoDocument htsDoc3 = new HistoricalTimeSeriesInfoDocument(htsInfo3);
    HTS_MASTER.add(htsDoc3);
    final HistoricalTimeSeriesInfoDocument htsDoc4 = new HistoricalTimeSeriesInfoDocument(htsInfo4);
    HTS_MASTER.add(htsDoc4);
    final HistoricalTimeSeriesInfoDocument htsDoc5 = new HistoricalTimeSeriesInfoDocument(htsInfo5);
    HTS_MASTER.add(htsDoc5);
    final HistoricalTimeSeriesInfoDocument htsDoc6 = new HistoricalTimeSeriesInfoDocument(htsInfo6);
    HTS_MASTER.add(htsDoc6);
    final HistoricalTimeSeriesInfoDocument htsDoc7 = new HistoricalTimeSeriesInfoDocument(htsInfo7);
    HTS_MASTER.add(htsDoc7);
    RESOLVER = new QuandlFieldMappingHistoricalTimeSeriesResolver(FIELD_MAPS, HTS_SELECTOR, HTS_MASTER);

    // historical time series rating config prefers the dummy source to Quandl
    final List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<>();
    rules.add(HistoricalTimeSeriesRatingRule.of(HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME, DUMMY_DATA_SOURCE_NAME, 0));
    rules.add(HistoricalTimeSeriesRatingRule.of(HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME, QUANDL_DATA_SOURCE_NAME, 1));
    rules.add(HistoricalTimeSeriesRatingRule.of(HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME, DUMMY_DATA_SOURCE_NAME, 0));
    rules.add(HistoricalTimeSeriesRatingRule.of(HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME, QUANDL_DATA_SOURCE_NAME, 1));
    final HistoricalTimeSeriesRating rating = HistoricalTimeSeriesRating.of(rules);
    CONFIG_MASTER.add(new ConfigDocument(ConfigItem.of(rating, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, HistoricalTimeSeriesRating.class)));
  }

  /**
   * Tests the behaviour when the field maps are null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullFieldMaps() {
    new QuandlFieldMappingHistoricalTimeSeriesResolver(null, HTS_SELECTOR, HTS_MASTER);
  }

  /**
   * Tests the behaviour when the selector is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullSelector() {
    new QuandlFieldMappingHistoricalTimeSeriesResolver(FIELD_MAPS, null, HTS_MASTER);
  }

  /**
   * Tests the behaviour when the master is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullMaster() {
    new QuandlFieldMappingHistoricalTimeSeriesResolver(FIELD_MAPS, HTS_SELECTOR, null);
  }

  /**
   * Tests the behaviour when multiple field adjustment maps with the same data source are supplied.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMultipleDataSourceMaps() {
    final QuandlNormalizer quandlNormalizer = new QuandlNormalizer(new QuandlCodeClassifier(CacheManager.newInstance()));
    final QuandlHistoricalTimeSeriesFieldAdjustmentMap marketValueAdjustmentMap = new QuandlHistoricalTimeSeriesFieldAdjustmentMap(QUANDL_DATA_SOURCE_NAME);
    final QuandlHistoricalTimeSeriesFieldAdjustmentMap settleAdjustmentMap = new QuandlHistoricalTimeSeriesFieldAdjustmentMap(QUANDL_DATA_SOURCE_NAME);
    marketValueAdjustmentMap.addFieldAdjustment(
        MarketDataRequirementNames.MARKET_VALUE,
        null,
        Arrays.asList(VALUE_FIELD_NAME, RATE_FIELD_NAME, LAST_FIELD_NAME),
        quandlNormalizer);
    settleAdjustmentMap.addFieldAdjustment(
        MarketDataRequirementNames.SETTLE_PRICE,
        null,
        Arrays.asList(SETTLE_FIELD_NAME),
        quandlNormalizer);
    final List<HistoricalTimeSeriesFieldAdjustmentMap> fieldMaps = new ArrayList<>();
    fieldMaps.add(marketValueAdjustmentMap);
    fieldMaps.add(settleAdjustmentMap);
    new QuandlFieldMappingHistoricalTimeSeriesResolver(fieldMaps, HTS_SELECTOR, HTS_MASTER);
  }

  /**
   * Tests the behaviour when the data field is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullDataField() {
    RESOLVER.resolve(ID_1, null, QUANDL_DATA_SOURCE_NAME, null, null, null);
  }

  /**
   * Tests the behaviour when there is no field mapping for a data source.
   */
  @Test
  public void testNoFieldMapping() {
    final HistoricalTimeSeriesResolutionResult result = RESOLVER.resolve(ID_6, null, QUANDL_DATA_SOURCE_NAME, null, MarketDataRequirementNames.BID, null);
    assertNull(result);
  }

  /**
   * Tests the behaviour when each resolution is expected to produce a single time series result.
   */
  @Test
  public void testSingleResult() {
    HistoricalTimeSeriesResolutionResult result = RESOLVER.resolve(ID_1, null, QUANDL_DATA_SOURCE_NAME, null,
        MarketDataRequirementNames.MARKET_VALUE, null);
    ManageableHistoricalTimeSeriesInfo info = result.getHistoricalTimeSeriesInfo();
    assertEquals(info.getDataField(), VALUE_FIELD_NAME);
    assertEquals(info.getDataSource(), QUANDL_DATA_SOURCE_NAME);
    result = RESOLVER.resolve(ID_2, null, QUANDL_DATA_SOURCE_NAME, null, MarketDataRequirementNames.MARKET_VALUE, null);
    info = result.getHistoricalTimeSeriesInfo();
    assertEquals(info.getDataField(), RATE_FIELD_NAME);
    assertEquals(info.getDataSource(), QUANDL_DATA_SOURCE_NAME);
    result = RESOLVER.resolve(ID_3, null, QUANDL_DATA_SOURCE_NAME, null, MarketDataRequirementNames.LAST, null);
    info = result.getHistoricalTimeSeriesInfo();
    assertEquals(info.getDataField(), LAST_FIELD_NAME);
    assertEquals(info.getDataSource(), QUANDL_DATA_SOURCE_NAME);
    result = RESOLVER.resolve(ID_4, null, DUMMY_DATA_SOURCE_NAME, null, MarketDataRequirementNames.MARKET_VALUE, null);
    info = result.getHistoricalTimeSeriesInfo();
    assertEquals(info.getDataField(), "Dummy_Value");
    assertEquals(info.getDataSource(), DUMMY_DATA_SOURCE_NAME);
    result = RESOLVER.resolve(ID_5, null, DUMMY_DATA_SOURCE_NAME, null, MarketDataRequirementNames.LAST, null);
    info = result.getHistoricalTimeSeriesInfo();
    assertEquals(info.getDataField(), "Dummy_Last");
    assertEquals(info.getDataSource(), DUMMY_DATA_SOURCE_NAME);
    result = RESOLVER.resolve(ID_6, null, DUMMY_DATA_SOURCE_NAME, null, MarketDataRequirementNames.BID, null);
    info = result.getHistoricalTimeSeriesInfo();
    assertEquals(info.getDataField(), "Dummy_Bid");
    assertEquals(info.getDataSource(), DUMMY_DATA_SOURCE_NAME);
  }

  /**
   * Tests the behaviour when the resolver could match either a series from either the Quandl or dummy source.
   */
  @Test
  public void testMultipleResults() {
    final HistoricalTimeSeriesResolutionResult result = RESOLVER.resolve(ID_1, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    final ManageableHistoricalTimeSeriesInfo info = result.getHistoricalTimeSeriesInfo();
    final String dataSource = info.getDataSource();
    if (!dataSource.equals(QUANDL_DATA_SOURCE_NAME)) {
      assertEquals(dataSource, DUMMY_DATA_SOURCE_NAME);
    }
    assertEquals(info.getDataField(), VALUE_FIELD_NAME);
  }

  /**
   * Tests that initializing the resolver with data from {@link QuandlDataUtils} works as expected.
   */
  @Test
  public void testFromQuandlDataUtils() {
    final Collection<HistoricalTimeSeriesFieldAdjustmentMap> fieldAdjustmentMaps = QuandlDataUtils.createFieldAdjustmentMap(CacheManager.newInstance());
    final HistoricalTimeSeriesSelector selector = new DefaultHistoricalTimeSeriesSelector(CONFIG_SOURCE);
    final QuandlFieldMappingHistoricalTimeSeriesResolver resolver =
        new QuandlFieldMappingHistoricalTimeSeriesResolver(fieldAdjustmentMaps, selector, HTS_MASTER);
    final Collection<HistoricalTimeSeriesFieldAdjustmentMap> fieldMaps = resolver.getFieldMaps();
    assertEquals(fieldMaps.size(), 1);
    final HistoricalTimeSeriesFieldAdjustmentMap fieldMap = Iterables.getOnlyElement(fieldMaps);
    assertEquals(fieldMap.getDataSource(), QUANDL_DATA_SOURCE_NAME);
    assertEquals(fieldMap.getFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE).getUnderlyingDataFields().size(), 3);
    assertEquals(fieldMap.getFieldAdjustment(MarketDataRequirementNames.SETTLE_PRICE).getUnderlyingDataFields().size(), 1);
    assertEquals(fieldMap.getFieldAdjustment(MarketDataRequirementNames.LOW).getUnderlyingDataFields().size(), 1);
    assertEquals(fieldMap.getFieldAdjustment(MarketDataRequirementNames.HIGH).getUnderlyingDataFields().size(), 1);
  }
}
