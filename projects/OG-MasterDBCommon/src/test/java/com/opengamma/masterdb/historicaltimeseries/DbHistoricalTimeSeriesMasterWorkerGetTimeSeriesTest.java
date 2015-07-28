/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerGetTimeSeriesTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerGetTimeSeriesTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerGetTimeSeriesTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_get_nullUID() {
    _htsMaster.getTimeSeries((UniqueId) null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbHts", "DP0");
    _htsMaster.getTimeSeries(uniqueId);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_get_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbHts", "DP101", "2010");
    _htsMaster.getTimeSeries(uniqueId);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_get_UID_101_latest() {
    UniqueId uniqueId = UniqueId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId.getObjectId(), test.getUniqueId().getObjectId());
    assertEquals(LocalDate.of(2011, 1, 1), _htsMaster.getTimeSeries(uniqueId, HistoricalTimeSeriesGetFilter.ofEarliestPoint()).getTimeSeries().getEarliestTime());
    assertEquals(LocalDate.of(2011, 1, 3), _htsMaster.getTimeSeries(uniqueId, HistoricalTimeSeriesGetFilter.ofLatestPoint()).getTimeSeries().getLatestTime());
    assertEquals(_version2Instant, test.getVersionInstant());
    assertEquals(_version4Instant, test.getCorrectionInstant());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAtIndex(0));
    assertEquals(3.1d, timeSeries.getValueAtIndex(0), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(1));
    assertEquals(3.22d, timeSeries.getValueAtIndex(1), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAtIndex(2));
    assertEquals(3.33d, timeSeries.getValueAtIndex(2), 0.001d);
  }

  @Test
  public void test_get_UID_102_latest() {
    UniqueId uniqueId = UniqueId.of("DbHts", "DP102");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId.getObjectId(), test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(0, timeSeries.size());
  }

  @Test
  public void test_get_UID_101_removed() {
    UniqueId uniqueId = UniqueId.of("DbHts", "101");
    _htsMaster.remove(uniqueId);   
    // the data-points should still be there after deleting the time series itself
    assertNotNull(_htsMaster.getTimeSeries(UniqueId.of("DbHts", "DP101")));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_get_OID_101_latest() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid, VersionCorrection.LATEST);
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAtIndex(0));
    assertEquals(3.1d, timeSeries.getValueAtIndex(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(1));
    assertEquals(3.22d, timeSeries.getValueAtIndex(1), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAtIndex(2));
    assertEquals(3.33d, timeSeries.getValueAtIndex(2), 0.0001d);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_OID_101_pre1() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    _htsMaster.getTimeSeries(oid, VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(1)));
  }

  @Test
  public void test_get_OID_101_post1() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid, VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(1)));
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(1, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAtIndex(0));
    assertEquals(3.1d, timeSeries.getValueAtIndex(0), 0.0001d);
  }

  @Test
  public void test_get_OID_101_post2() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid, VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(1)));
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAtIndex(0));
    assertEquals(3.1d, timeSeries.getValueAtIndex(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(1));
    assertEquals(3.22d, timeSeries.getValueAtIndex(1), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAtIndex(2));
    assertEquals(3.33d, timeSeries.getValueAtIndex(2), 0.0001d);
  }

  @Test
  public void test_get_OID_101_correctPost2() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid, VersionCorrection.of(_version2Instant.plusSeconds(1), _version2Instant.plusSeconds(1)));
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAtIndex(0));
    assertEquals(3.1d, timeSeries.getValueAtIndex(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(1));
    assertEquals(3.2d, timeSeries.getValueAtIndex(1), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAtIndex(2));
    assertEquals(3.3d, timeSeries.getValueAtIndex(2), 0.0001d);
  }

  @Test
  public void test_get_OID_101_correctPost3() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid, VersionCorrection.of(_version2Instant.plusSeconds(1), _version3Instant.plusSeconds(1)));
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAtIndex(0));
    assertEquals(3.1d, timeSeries.getValueAtIndex(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(1));
    assertEquals(3.21d, timeSeries.getValueAtIndex(1), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAtIndex(2));
    assertEquals(3.3d, timeSeries.getValueAtIndex(2), 0.0001d);
  }

  @Test
  public void test_get_UID_101_correctPost3() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries base = _htsMaster.getTimeSeries(oid, VersionCorrection.of(_version2Instant.plusSeconds(1), _version3Instant.plusSeconds(1)));
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(base.getUniqueId());
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_get_dateRangeFromStart() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid,
        VersionCorrection.of(_version2Instant.plusSeconds(1), _version3Instant.plusSeconds(1)),
        HistoricalTimeSeriesGetFilter.ofRange(null, LocalDate.of(2011, 1, 2)));
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(2, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAtIndex(0));
    assertEquals(3.1d, timeSeries.getValueAtIndex(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(1));
    assertEquals(3.21d, timeSeries.getValueAtIndex(1), 0.0001d);
  }

  @Test
  public void test_get_dateRangeToEnd() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid,
        VersionCorrection.of(_version2Instant.plusSeconds(1), _version3Instant.plusSeconds(1)),
        HistoricalTimeSeriesGetFilter.ofRange(LocalDate.of(2011, 1, 2), null));
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(2, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(0));
    assertEquals(3.21d, timeSeries.getValueAtIndex(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAtIndex(1));
    assertEquals(3.3d, timeSeries.getValueAtIndex(1), 0.0001d);
  }

  @Test
  public void test_get_dateRange() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid,
        VersionCorrection.of(_version2Instant.plusSeconds(1), _version3Instant.plusSeconds(1)),
        HistoricalTimeSeriesGetFilter.ofRange(LocalDate.of(2011, 1, 2), LocalDate.of(2011, 1, 2)));
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(1, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(0));
    assertEquals(3.21d, timeSeries.getValueAtIndex(0), 0.0001d);
  }

  @Test
  public void test_get_nPointsFromEarliest() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    HistoricalTimeSeriesGetFilter filter = new HistoricalTimeSeriesGetFilter();
    filter.setMaxPoints(2);
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid,
        VersionCorrection.of(_version2Instant.plusSeconds(1), _version3Instant.plusSeconds(1)), filter);
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(2, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAtIndex(0));
    assertEquals(3.1d, timeSeries.getValueAtIndex(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(1));
    assertEquals(3.21d, timeSeries.getValueAtIndex(1), 0.0001d);
  }

  @Test
  public void test_get_nPointsFromLatest() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofLatestPoint();
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid,
        VersionCorrection.of(_version2Instant.plusSeconds(1), _version3Instant.plusSeconds(1)), filter);
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(1, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAtIndex(0));
    assertEquals(3.3d, timeSeries.getValueAtIndex(0), 0.0001d);
  }

  @Test
  public void test_get_nPointsFromLatestWithinDateRange() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofRange(null, LocalDate.of(2011, 1, 2), -1);
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid,
        VersionCorrection.of(_version2Instant.plusSeconds(1), _version3Instant.plusSeconds(1)), filter);
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(1, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAtIndex(0));
    assertEquals(3.21d, timeSeries.getValueAtIndex(0), 0.0001d);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
