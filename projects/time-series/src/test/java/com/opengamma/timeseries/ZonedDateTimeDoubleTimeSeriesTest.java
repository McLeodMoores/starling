/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

/**
 * Test.
 */
public abstract class ZonedDateTimeDoubleTimeSeriesTest extends DoubleTimeSeriesTest<ZonedDateTime> {

  protected ZonedDateTime makeDate(final int year, final int month, final int day) {
    final ZonedDateTime one = ZonedDateTime.of(LocalDateTime.of(year, month, day, 0, 0), ZoneOffset.UTC);//ZoneId.of(java.util.TimeZone.getDefault().getID()));
    return one;
  }

  @Override
  protected ZonedDateTime[] testTimes() {
    final ZonedDateTime one = makeDate(2010, 2, 8);
    final ZonedDateTime two = makeDate(2010, 2, 9);
    final ZonedDateTime three = makeDate(2010, 2, 10);
    final ZonedDateTime four = makeDate(2010, 2, 11);
    final ZonedDateTime five = makeDate(2010, 2, 12);
    final ZonedDateTime six = makeDate(2010, 2, 13);
    return new ZonedDateTime[] { one, two, three, four, five, six };
  }

  @Override
  protected ZonedDateTime[] testTimes2() {
    final ZonedDateTime one = makeDate(2010, 2, 11);
    final ZonedDateTime two = makeDate(2010, 2, 12);
    final ZonedDateTime three = makeDate(2010, 2, 13);
    final ZonedDateTime four = makeDate(2010, 2, 14);
    final ZonedDateTime five = makeDate(2010, 2, 15);
    final ZonedDateTime six = makeDate(2010, 2, 16);
    return new ZonedDateTime[] { one, two, three, four, five, six };
  }

  @Override
  protected ZonedDateTime[] emptyTimes() {
    return new ZonedDateTime[] {};
  }

}
