/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.RandomTimeSeriesGenerator;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;

/**
 * A performance test of time-series.
 */
@Test(enabled = false)
public class PerformanceTest extends AbstractDbTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceTest.class);

  private DbHistoricalTimeSeriesMaster _htsMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public PerformanceTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _htsMaster = new DbHistoricalTimeSeriesMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _htsMaster = null;
  }

  //-------------------------------------------------------------------------
  public void createUpdateReadLotsOfTimeSeries() {
    final long start = System.nanoTime();

    final int numSeries = 100;
    final int numPoints = 100;

    for (int i = 0; i < numSeries; i++) {
      final ExternalId id1 = ExternalId.of("sa" + i, "ida" + i);
      final ExternalIdBundle identifiers = ExternalIdBundle.of(id1);

      final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
      info.setName("BLOOMBERG CMPL");
      info.setDataField("CLOSE");
      info.setDataProvider("CMPL");
      info.setDataSource("BLOOMBERG");
      info.setObservationTime("LDN_CLOSE");
      info.setExternalIdBundle(ExternalIdBundleWithDates.of(identifiers));
      HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
      LOGGER.debug("adding timeseries {}", doc);
      doc = _htsMaster.add(doc);

      LocalDateDoubleTimeSeries randomPoints = RandomTimeSeriesGenerator.makeRandomTimeSeries(1);
      _htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), randomPoints);
      randomPoints = RandomTimeSeriesGenerator.makeRandomTimeSeries(numPoints);

      for (int j = 1; j < numPoints; j++) {
        final ImmutableLocalDateDoubleTimeSeries points = ImmutableLocalDateDoubleTimeSeries.of(
            Lists.newArrayList(randomPoints.getTimeAtIndex(j)),
            Lists.newArrayList(randomPoints.getValueAtIndex(j)));
        LOGGER.debug("adding data points {}", points);
        _htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), points);
      }
    }

    final long end = System.nanoTime();

    LOGGER.info("Creating {} series with {} points each took {} ms",
        new Object[] { numSeries, numPoints, (end - start) / 1E6 });
  }

}
