/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;
import static com.opengamma.bbg.BloombergConstants.DATA_PROVIDER_UNKNOWN;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.bbg.historicaltimeseries.BloombergHistoricalTimeSeriesProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergHistoricalTimeSeriesSourceTest {

  private BloombergHistoricalTimeSeriesProvider _provider;
  private HistoricalTimeSeriesSource _source;
  private final ExternalIdBundle _secDes = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("IBM US Equity"));
  private static final String DEFAULT_DATA_PROVIDER = DATA_PROVIDER_UNKNOWN;
  private static final String DEFAULT_DATA_SOURCE = BLOOMBERG_DATA_SOURCE_NAME;
  private static final String PX_LAST = "PX_LAST";

  @BeforeMethod
  public void setUp() throws Exception {
    final BloombergConnector connector = BloombergTestUtils.getBloombergConnector();
    _provider = new BloombergHistoricalTimeSeriesProvider(connector);
    _source = new BloombergHistoricalTimeSeriesSource(_provider);
    _provider.start();
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_provider != null) {
      _provider.stop();
    }
    _source = null;
    _provider = null;
  }

  // -------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getHistoricalWithInvalidDates() throws Exception {
    // endDate before startDate
    final LocalDate startDate = LocalDate.of(2009, 11, 04);
    final LocalDate endDate = LocalDate.of(2009, 10, 29);
    _source.getHistoricalTimeSeries(_secDes, DEFAULT_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, startDate, true, endDate, true);
  }

  @Test
  public void getHistoricalWithSameDate() throws Exception {
    final LocalDate startDate = LocalDate.of(2009, 11, 04);
    final LocalDate endDate = LocalDate.of(2009, 11, 04);
    final HistoricalTimeSeries hts = _source.getHistoricalTimeSeries(_secDes, DEFAULT_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, startDate, true, endDate,
        true);
    assertNotNull(hts);
    assertNotNull(hts.getTimeSeries());
    assertEquals(1, hts.getTimeSeries().size());
  }

  @Test
  public void getSeriesMap() throws Exception {
    final LocalDate startDate = LocalDate.of(2009, 10, 04);
    final LocalDate endDate = LocalDate.of(2009, 11, 29);
    final Map<ExternalIdBundle, HistoricalTimeSeries> result = _source.getHistoricalTimeSeries(Collections.singleton(getTestBundle()), "BLOOMBERG", "CMPL",
        "PX_LAST", startDate, true, endDate, true);
    assertNotNull(result);
    final HistoricalTimeSeries hts = result.get(getTestBundle());
    assertNotNull(hts);
    assertFalse(hts.getTimeSeries().isEmpty());
  }

  private ExternalIdBundle getTestBundle() {
    return ExternalIdBundle.of(
        ExternalId.of("BLOOMBERG_BUID", "EQ0010121400001000"), ExternalId.of("BLOOMBERG_TICKER", "C US Equity"),
        ExternalId.of("CUSIP", "172967101"), ExternalId.of("ISIN", "US1729671016"), ExternalId.of("SEDOL1", "2297907"));
  }

  @Test
  public void getHistoricalTimeSeriesWithMaxPoints() throws Exception {
    final LocalDate endDate = LocalDate.of(2012, 03, 07);
    final LocalDate startDate = endDate.minusMonths(1);

    final HistoricalTimeSeries reference = _source.getHistoricalTimeSeries(_secDes, DEFAULT_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, startDate, true,
        endDate, true);
    for (final int maxPoints : new int[] { -4, -1 }) {
      final HistoricalTimeSeries hts = _source.getHistoricalTimeSeries(_secDes, DEFAULT_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, startDate, true, endDate,
          true, maxPoints);

      // do we have a time-series?
      assertNotNull(hts.getTimeSeries());

      // is it the right size?
      assertEquals(Math.min(Math.abs(maxPoints), reference.getTimeSeries().size()), hts.getTimeSeries().size());

      // does it contain the expected data points?
      assertEquals(
          Math.abs(maxPoints) > reference.getTimeSeries().size()
              ? reference.getTimeSeries()
              : maxPoints >= 0
                  ? reference.getTimeSeries().head(maxPoints)
                  : reference.getTimeSeries().tail(-maxPoints),
          hts.getTimeSeries());
    }
  }

  @Test
  public void getHistoricalTimeSeriesWithZeroMaxPoints() throws Exception {
    final LocalDate endDate = LocalDate.of(2012, 03, 07);
    final LocalDate startDate = endDate.minusMonths(1);

    final HistoricalTimeSeries reference = _source.getHistoricalTimeSeries(_secDes, DEFAULT_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, startDate, true,
        endDate, true);
    final HistoricalTimeSeries hts = _source.getHistoricalTimeSeries(_secDes, DEFAULT_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, startDate, true, endDate,
        true, 0);

    // do we have a time-series?
    assertNotNull(hts.getTimeSeries());

    // is it the right size?
    assertEquals(reference.getTimeSeries().size(), hts.getTimeSeries().size());

    // does it contain the expected data points?
    assertEquals(reference.getTimeSeries(), hts.getTimeSeries());
  }

  @Test
  public void getHistoricalTimeSeriesWithDates() throws Exception {
    final LocalDate startDate = LocalDate.of(2009, 10, 29);
    final LocalDate endDate = LocalDate.of(2009, 11, 04);
    final HistoricalTimeSeries hts = _source.getHistoricalTimeSeries(_secDes, DEFAULT_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, startDate, true, endDate,
        true);
    assertNotNull(hts);
    final LocalDateDoubleTimeSeries timeSeriesExpected = hts.getTimeSeries();
    assertNotNull(timeSeriesExpected);

    final ExecutorService threadPool = Executors.newFixedThreadPool(4);
    final List<Future<LocalDateDoubleTimeSeries>> results = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      results.add(
          threadPool.submit(new BHDPgetHistoricalTimeSeriesWithDates(_secDes, DEFAULT_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, startDate, endDate)));
    }

    for (final Future<LocalDateDoubleTimeSeries> future : results) {
      final LocalDateDoubleTimeSeries timeSeriesActual = future.get();
      assertNotNull(timeSeriesActual);
      assertEquals(timeSeriesExpected, timeSeriesActual);
    }
  }

  // -------------------------------------------------------------------------
  private class BHDPgetHistoricalTimeSeriesWithDates implements Callable<LocalDateDoubleTimeSeries> {
    private final ExternalIdBundle _secDes;
    private final String _dataSource;
    private final String _provider;
    private final String _field;
    private final LocalDate _startDate;
    private final LocalDate _endDate;

    BHDPgetHistoricalTimeSeriesWithDates(final ExternalIdBundle secDes, final String dataSource, final String dataProvider, final String field,
        final LocalDate startDate, final LocalDate endDate) {
      assertNotNull(secDes);
      assertNotNull(startDate);
      assertNotNull(endDate);
      _secDes = secDes;
      _dataSource = dataSource;
      _provider = dataProvider;
      _field = field;
      _startDate = startDate;
      _endDate = endDate;
    }

    @Override
    public LocalDateDoubleTimeSeries call() throws Exception {
      final HistoricalTimeSeries hts = _source.getHistoricalTimeSeries(_secDes, _dataSource, _provider, _field, _startDate, true, _endDate, true);
      return hts.getTimeSeries();
    }

  }

}
