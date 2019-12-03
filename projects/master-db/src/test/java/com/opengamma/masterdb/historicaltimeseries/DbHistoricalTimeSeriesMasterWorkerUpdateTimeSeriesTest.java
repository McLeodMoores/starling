/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerUpdateTimeSeriesTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerUpdateTimeSeriesTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerUpdateTimeSeriesTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullOID() {
    _htsMaster.updateTimeSeriesDataPoints((ObjectId) null, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullSeries() {
    _htsMaster.updateTimeSeriesDataPoints(ObjectId.of("DbHts", "DP101"), null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_versioned_notFoundId() {
    final ObjectId oid = ObjectId.of("DbHts", "DP0");
    _htsMaster.updateTimeSeriesDataPoints(oid, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_beforeAllExistingPoints() {
    final LocalDate[] dates = {LocalDate.of(2010, 12, 1)};
    final double[] values = {0.9d};
    final LocalDateDoubleTimeSeries series = ImmutableLocalDateDoubleTimeSeries.of(dates, values);

    _htsMaster.updateTimeSeriesDataPoints(ObjectId.of("DbHts", "DP101"), series);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_atExistingPoint() {
    final LocalDate[] dates = {LocalDate.of(2011, 1, 3)};
    final double[] values = {0.9d};
    final LocalDateDoubleTimeSeries series = ImmutableLocalDateDoubleTimeSeries.of(dates, values);

    _htsMaster.updateTimeSeriesDataPoints(ObjectId.of("DbHts", "DP101"), series);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_update_102_startsEmpty() {
    final LocalDate[] dates = {LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 2), LocalDate.of(2011, 7, 4)};
    final double[] values = {1.1d, 2.2d, 3.3d};
    final LocalDateDoubleTimeSeries series = ImmutableLocalDateDoubleTimeSeries.of(dates, values);

    final ObjectId oid = ObjectId.of("DbHts", "DP102");
    final UniqueId uniqueId = _htsMaster.updateTimeSeriesDataPoints(oid, series);

    final ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(series, test.getTimeSeries());
  }

  @Test
  public void test_update_101_startsFull() {
    final LocalDate[] dates = {LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 2), LocalDate.of(2011, 7, 4)};
    final double[] values = {1.1d, 2.2d, 3.3d};
    final LocalDateDoubleTimeSeries series = ImmutableLocalDateDoubleTimeSeries.of(dates, values);

    final ObjectId oid = ObjectId.of("DbHts", "DP101");
    final UniqueId uniqueId = _htsMaster.updateTimeSeriesDataPoints(oid, series);

    final ManageableHistoricalTimeSeries testAdded = _htsMaster.getTimeSeries(uniqueId, HistoricalTimeSeriesGetFilter.ofRange(LocalDate.of(2011, 7, 1), null));
    assertEquals(uniqueId, testAdded.getUniqueId());
    assertEquals(series, testAdded.getTimeSeries());

    final ManageableHistoricalTimeSeries testAll = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId, testAll.getUniqueId());
    assertEquals(6, testAll.getTimeSeries().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
