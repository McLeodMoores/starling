/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.normalization;

import static org.testng.Assert.assertEquals;
import net.sf.ehcache.CacheManager;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.classification.QuandlCodeClassifier;
import com.mcleodmoores.quandl.normalization.QuandlNormalizer;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * Unit tests for {@link QuandlNormalizer}.
 */
public class QuandlNormalizerTest {
  /** The normalizer */
  private static final HistoricalTimeSeriesAdjuster NORMALIZER = new QuandlNormalizer(new QuandlCodeClassifier(CacheManager.newInstance()));
  /** The id bundle */
  private static final ExternalIdBundle ID_BUNDLE = ExternalIdBundle.of(ExternalSchemes.syntheticSecurityId("FRED/USDONTD156N"),
      QuandlConstants.ofCode("FRED/USDONTD156N"));
  /** The time series */
  private static final HistoricalTimeSeries TS = new SimpleHistoricalTimeSeries(UniqueId.of("Test", "1"),
      ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2014, 12, 1)}, new double[] {123}));

  /**
   * Tests the behaviour when a null code classifier is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullClassifier() {
    new QuandlNormalizer(null);
  }

  /**
   * Tests the behaviour when a null id bundle is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullIdBundle1() {
    NORMALIZER.adjust(null, TS);
  }

  /**
   * Tests the behaviour when a null id bundle is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullIdBundle2() {
    NORMALIZER.getAdjustment(null);
  }

  /**
   * Tests the behaviour when a null time series is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullTimeSeries() {
    NORMALIZER.adjust(ID_BUNDLE, null);
  }

  /**
   * Tests the normalization of time series.
   */
  @Test
  public void testNormalization() {
    // cash rate, so divide by 100
    HistoricalTimeSeries expectedTs = new SimpleHistoricalTimeSeries(UniqueId.of("Test", "1"),
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2014, 12, 1)}, new double[] {1.23}));
    assertEquals(expectedTs, NORMALIZER.adjust(ID_BUNDLE, TS));
    assertEquals(expectedTs, NORMALIZER.getAdjustment(ID_BUNDLE).adjust(TS));
    // unclassifiable rate, so no change
    expectedTs = new SimpleHistoricalTimeSeries(UniqueId.of("Test", "1"),
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2014, 12, 1)}, new double[] {123}));
    assertEquals(expectedTs, NORMALIZER.adjust(QuandlConstants.ofCode("ABC/DEF").toBundle(), TS));
    assertEquals(expectedTs, NORMALIZER.getAdjustment(QuandlConstants.ofCode("ABC/DEF").toBundle()).adjust(TS));
    // no Quandl code, so no change
    expectedTs = new SimpleHistoricalTimeSeries(UniqueId.of("Test", "1"),
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2014, 12, 1)}, new double[] {123}));
    assertEquals(expectedTs, NORMALIZER.adjust(ExternalSchemes.syntheticSecurityId("FRED/USDONTD156N").toBundle(), TS));
    assertEquals(expectedTs, NORMALIZER.getAdjustment(ExternalSchemes.syntheticSecurityId("FRED/USDONTD156N").toBundle()).adjust(TS));
  }
}
