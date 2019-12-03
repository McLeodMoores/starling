/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test DbDateUtils.
 */
@Test(groups = TestGroup.UNIT)
public class DbDateUtilsTest {

  static {
    DateUtils.initTimeZone();
  }

  //-------------------------------------------------------------------------
  /**
   * Tests conversion to an SQL time stamp.
   */
  public void testToSqlTimestamp() {
    final Instant instant = Instant.now();
    final Timestamp ts = DbDateUtils.toSqlTimestamp(instant);
    assertEquals(instant.toEpochMilli(), ts.getTime());
    assertEquals(instant.getNano(), ts.getNanos());
  }

  /**
   * Tests that the time cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToSqlTimestampLocalTimeNull() {
    DbDateUtils.toSqlTimestamp((LocalTime) null);
  }

  /**
   * Tests that the instant cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToSqlTimestampInstantProviderNull() {
    DbDateUtils.toSqlTimestamp((Instant) null);
  }

  /**
   * Tests the conversion from an SQL time stamp.
   */
  public void testFromSqlTimestamp() {
    final Timestamp ts = new Timestamp(123456789L);
    ts.setNanos(789654321);
    final Instant instant = DbDateUtils.fromSqlTimestamp(ts);
    assertEquals(ts.getTime(), instant.toEpochMilli());
    assertEquals(ts.getNanos(), instant.getNano());
  }

  /**
   * Tests that the time stamp cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromSqlTimestampNull() {
    DbDateUtils.fromSqlTimestamp(null);
  }

  /**
   * Tests conversion from the max time stamp.
   */
  public void testFromSqlTimestampMax() {
    assertEquals(DbDateUtils.MAX_INSTANT, DbDateUtils.fromSqlTimestamp(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  /**
   * Tests conversion from null.
   */
  public void testFromSqlTimestampNullFarFuture() {
    final Timestamp ts = new Timestamp(123456789L);
    ts.setNanos(789654321);
    final Instant instant = DbDateUtils.fromSqlTimestampNullFarFuture(ts);
    assertEquals(ts.getTime(), instant.toEpochMilli());
    assertEquals(ts.getNanos(), instant.getNano());
  }

  /**
   * Tests that null is treated as the far future.
   */
  public void testFromSqlTimestampNullFarFutureMax() {
    assertNull(DbDateUtils.fromSqlTimestampNullFarFuture(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests conversion to a date time.
   */
  public void testToSqlDateTime() {
    assertEquals(new Timestamp(2005 - 1900, 11 - 1, 7, 12, 34, 56, 7), DbDateUtils.toSqlDateTime(LocalDateTime.of(2005, 11, 7, 12, 34, 56, 7)));
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToSqlDateTimeNull() {
    DbDateUtils.toSqlDateTime(null);
  }

  /**
   * Tests conversion from a date time.
   */
  public void testFromSqlDateTime() {
    assertEquals(LocalDateTime.of(2005, 11, 7, 12, 34, 56, 7), DbDateUtils.fromSqlDateTime(new Timestamp(2005 - 1900, 11 - 1, 7, 12, 34, 56, 7)));
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromSqlDateTimepNull() {
    DbDateUtils.fromSqlDateTime(null);
  }

  /**
   * Tests conversion from the max time.
   */
  public void testFromSqlDateTimeMax() {
    assertEquals(LocalDateTime.of(9999, 12, 31, 23, 59, 59, 0), DbDateUtils.fromSqlDateTime(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  /**
   * Tests conversion from a date in the far future.
   */
  public void testFromSqlDateTimeNullFarFuture() {
    assertEquals(LocalDateTime.of(2005, 11, 7, 12, 34, 56, 7), DbDateUtils.fromSqlDateTimeNullFarFuture(new Timestamp(2005 - 1900, 11 - 1, 7, 12, 34, 56, 7)));
  }

  /**
   * Tests that null is returned.
   */
  public void testFromSqlDateTimeNullFarFutureMax() {
    assertNull(DbDateUtils.fromSqlDateTimeNullFarFuture(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests conversion to and SQL date.
   */
  public void testToSqlDate() {
    assertEquals(new Date(2005 - 1900, 11 - 1, 12), DbDateUtils.toSqlDate(LocalDate.of(2005, 11, 12)));
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToSqlDateNull() {
    DbDateUtils.toSqlDate(null);
  }

  /**
   * Tests conversion from a date in a far future.
   */
  public void testToSqlDateNullFarFuture() {
    assertEquals(new Date(2005 - 1900, 11 - 1, 12), DbDateUtils.toSqlDateNullFarFuture(LocalDate.of(2005, 11, 12)));
    assertEquals(DbDateUtils.MAX_SQL_DATE, DbDateUtils.toSqlDateNullFarFuture(null));
  }

  /**
   * Tests conversion from a date in the far past.
   */
  public void testToSqlDateNullFarPast() {
    assertEquals(new Date(2005 - 1900, 11 - 1, 12), DbDateUtils.toSqlDateNullFarPast(LocalDate.of(2005, 11, 12)));
    assertEquals(DbDateUtils.MIN_SQL_DATE, DbDateUtils.toSqlDateNullFarPast(null));
  }

  /**
   * Tests conversion from a SQL date.
   */
  public void testFromSqlDate() {
    assertEquals(LocalDate.of(2005, 11, 12), DbDateUtils.fromSqlDate(new Date(2005 - 1900, 11 - 1, 12)));
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromSqlDateNull() {
    DbDateUtils.fromSqlDate(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests conversion to a time.
   */
  public void testToSqlTime() {
    assertEquals(new Time(12, 34, 56), DbDateUtils.toSqlTime(LocalTime.of(12, 34, 56)));
  }

  /**
   * Tests that the time cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToSqlTimeNull() {
    DbDateUtils.toSqlTime(null);
  }

  /**
   * Tests conversion from a SQL time.
   */
  public void testFromSqlTime() {
    assertEquals(LocalTime.of(12, 34, 56), DbDateUtils.fromSqlTime(new Time(12, 34, 56)));
  }

  /**
   * Tests that the time cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromSqlTimeTimeNull() {
    DbDateUtils.fromSqlTime((Time) null);
  }

  /**
   * Tests that the time cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromSqlTimeTimestampNull() {
    DbDateUtils.fromSqlTime((Timestamp) null);
  }

}
