/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Tests for {@link ImmutableZonedDateTimeDoubleTimeSeries} and {@link ImmutableZonedDateTimeDoubleTimeSeriesBuilder}.
 */
@Test(groups = "unit")
public class ImmutableZonedDateTimeDoubleTimeSeriesTest extends ZonedDateTimeDoubleTimeSeriesTest {
  private static final ZonedDateTime ZDT_0 = Instant.ofEpochSecond(0).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_1111 = Instant.ofEpochSecond(1111).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_2222 = Instant.ofEpochSecond(2222).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_3333 = Instant.ofEpochSecond(3333).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_12345 = Instant.ofEpochSecond(12345).atZone(ZoneOffset.UTC);

  @Override
  protected ZonedDateTimeDoubleTimeSeries createEmptyTimeSeries() {
    return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeDoubleTimeSeries createStandardTimeSeries() {
    return (ZonedDateTimeDoubleTimeSeries) super.createStandardTimeSeries();
  }

  @Override
  protected ZonedDateTimeDoubleTimeSeries createStandardTimeSeries2() {
    return (ZonedDateTimeDoubleTimeSeries) super.createStandardTimeSeries2();
  }

  @Override
  protected ZonedDateTimeDoubleTimeSeries createTimeSeries(final ZonedDateTime[] times, final double[] values) {
    return ImmutableZonedDateTimeDoubleTimeSeries.of(times, values, ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeDoubleTimeSeries createTimeSeries(final List<ZonedDateTime> times, final List<Double> values) {
    return ImmutableZonedDateTimeDoubleTimeSeries.of(times, values, ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeDoubleTimeSeries createTimeSeries(final DoubleTimeSeries<ZonedDateTime> dts) {
    return ImmutableZonedDateTimeDoubleTimeSeries.from(dts, ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from a date and value.
   */
  @Test
  public void testOfZonedDateTimeDouble() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.of(ZDT_12345, 2.0);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), ZDT_12345);
    assertEquals(ts.getValueAtIndex(0), 2.0, 1e-15);
  }

  /**
   * Tests the behaviour when the date is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfZonedDateTimeDoubleNull() {
    ImmutableZonedDateTimeDoubleTimeSeries.of((ZonedDateTime) null, 2.0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of dates and values.
   */
  @Test
  public void testOfZonedDateTimeArrayDoubleArray() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final Double[] inValues = new Double[] {2.0, 3.0};
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0, 1e-15);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0, 1e-15);
    assertEquals(ts, ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(inDates, inValues));
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfZonedDateTimeArrayDoubleArrayWrongOrder() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    final Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfZonedDateTimeArrayDoubleArrayMismatchedArrays() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222};
    final Double[] inValues = new Double[] {2.0, 3.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfZonedDateTimeArrayDoubleArrayNullDates() {
    final Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of((ZonedDateTime[]) null, inValues, null);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfZonedDateTimeArrayDoubleArrayNullValues() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, (Double[]) null, null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of dates and doubles.
   */
  @Test
  public void testOfZonedDateTimeArrayPrimitiveDoubleArray() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final double[] inValues = new double[] {2.0, 3.0};
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0, 1e-15);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0, 1e-15);
    assertEquals(ts, ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(inDates, inValues));
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfZonedDateTimeArrayPrimitiveDoubleArrayWrongOrder() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfZonedDateTimeArrayPrimitiveDoubleArrayMismatchedArrays() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222};
    final double[] inValues = new double[] {2.0, 3.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfZonedDateTimeArrayPrimitiveDoubleArrayNullDates() {
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of((ZonedDateTime[]) null, inValues, null);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfZonedDateTimeArrayPrimitiveDoubleArrayNullValues() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, (double[]) null, null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from longs and values.
   */
  @Test
  public void testOfLongArrayDoubleArray() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {2.0, 3.0};
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0, 1e-15);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0, 1e-15);
    assertEquals(ts, ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(inDates, inValues));
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLongArrayDoubleArrayWrongOrder() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLongArrayDoubleArrayMismatchedArrays() {
    final long[] inDates = new long[] {2222_000_000_000L};
    final double[] inValues = new double[] {2.0, 3.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
  }

  /**
   * Tests that the array of longs cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLongArrayDoubleArrayNullDates() {
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of((long[]) null, inValues, ZoneOffset.UTC);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLongArrayDoubleArrayNullValues() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, (double[]) null, ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the intersection of two time series using the values from the first series.
   */
  @Test
  public void testIntersectionFirstValueSelectFirst() {
    final ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    final ZonedDateTimeDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final ZonedDateTimeDoubleTimeSeries dts3 = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();

    final ZonedDateTimeDoubleTimeSeries result1 = dts.intersectionFirstValue(dts3);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));

    final ZonedDateTimeDoubleTimeSeries result2 = dts3.intersectionFirstValue(dts);
    assertEquals(3, result2.size());
    assertEquals(Double.valueOf(-1.0), result2.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result2.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result2.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result2.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result2.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result2.getTimeAtIndex(2));
  }

  /**
   * Tests the intersection of two time series using the values from the second series.
   */
  @Test
  public void testIntersectionSecondValueSelectSecond() {
    final ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    final ZonedDateTimeDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final ZonedDateTimeDoubleTimeSeries dts3 = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();

    final ZonedDateTimeDoubleTimeSeries result2 = dts.intersectionSecondValue(dts3);
    assertEquals(3, result2.size());
    assertEquals(Double.valueOf(-1.0), result2.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result2.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result2.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result2.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result2.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result2.getTimeAtIndex(2));

    final ZonedDateTimeDoubleTimeSeries result1 = dts3.intersectionSecondValue(dts);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));
  }

  /**
   * Tests a sub-series using dates as the boundaries.
   */
  @Test
  public void testSubSeriesByDatesSingle() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8d)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    final ZonedDateTimeDoubleTimeSeries singleMiddle = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 3, 9)));
    assertEquals(1, singleMiddle.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), singleMiddle.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), singleMiddle.getValueAtIndex(0));

    final ZonedDateTimeDoubleTimeSeries singleStart = dts.subSeries(toZdt(LocalDate.of(2010, 2, 8)), toZdt(LocalDate.of(2010, 2, 9)));
    assertEquals(1, singleStart.size());
    assertEquals(toZdt(LocalDate.of(2010, 2, 8)), singleStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), singleStart.getValueAtIndex(0));

    final ZonedDateTimeDoubleTimeSeries singleEnd = dts.subSeries(toZdt(LocalDate.of(2010, 6, 8)), toZdt(LocalDate.of(2010, 6, 9)));
    assertEquals(1, singleEnd.size());
    assertEquals(toZdt(LocalDate.of(2010, 6, 8)), singleEnd.getTimeAtIndex(0));
    assertEquals(Double.valueOf(9d), singleEnd.getValueAtIndex(0));
  }

  /**
   * Tests an empty sub-series.
   */
  @Test
  public void testSubSeriesByDatesEmpty() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeDoubleTimeSeries sub = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 3, 8)));
    assertEquals(0, sub.size());
  }

  /**
   * Tests a sub-series using dates as the boundaries.
   */
  @Test
  public void testSubSeriesByDatesRange() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8d)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    final ZonedDateTimeDoubleTimeSeries middle = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 5, 9)));
    assertEquals(3, middle.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), middle.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), middle.getValueAtIndex(0));
    assertEquals(toZdt(LocalDate.of(2010, 4, 8)), middle.getTimeAtIndex(1));
    assertEquals(Double.valueOf(5d), middle.getValueAtIndex(1));
    assertEquals(toZdt(LocalDate.of(2010, 5, 8)), middle.getTimeAtIndex(2));
    assertEquals(Double.valueOf(8d), middle.getValueAtIndex(2));

    final ZonedDateTimeDoubleTimeSeries fromStart = dts.subSeries(toZdt(LocalDate.of(2010, 2, 8)), toZdt(LocalDate.of(2010, 4, 9)));
    assertEquals(3, fromStart.size());
    assertEquals(toZdt(LocalDate.of(2010, 2, 8)), fromStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), fromStart.getValueAtIndex(0));
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), fromStart.getTimeAtIndex(1));
    assertEquals(Double.valueOf(3d), fromStart.getValueAtIndex(1));
    assertEquals(toZdt(LocalDate.of(2010, 4, 8)), fromStart.getTimeAtIndex(2));
    assertEquals(Double.valueOf(5d), fromStart.getValueAtIndex(2));

    final ZonedDateTimeDoubleTimeSeries preStart = dts.subSeries(toZdt(LocalDate.of(2010, 1, 8)), toZdt(LocalDate.of(2010, 3, 9)));
    assertEquals(2, preStart.size());
    assertEquals(toZdt(LocalDate.of(2010, 2, 8)), preStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), preStart.getValueAtIndex(0));
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), preStart.getTimeAtIndex(1));
    assertEquals(Double.valueOf(3d), preStart.getValueAtIndex(1));

    final ZonedDateTimeDoubleTimeSeries postEnd = dts.subSeries(toZdt(LocalDate.of(2010, 5, 8)), toZdt(LocalDate.of(2010, 12, 9)));
    assertEquals(2, postEnd.size());
    assertEquals(toZdt(LocalDate.of(2010, 5, 8)), postEnd.getTimeAtIndex(0));
    assertEquals(Double.valueOf(8d), postEnd.getValueAtIndex(0));
    assertEquals(toZdt(LocalDate.of(2010, 6, 8)), postEnd.getTimeAtIndex(1));
    assertEquals(Double.valueOf(9d), postEnd.getValueAtIndex(1));
  }

  /**
   * Tests the behaviour when the second date is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByDatesBadRange1() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8d)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 3, 7)));
  }

  /**
   * Tests the behaviour when the second date is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByDatesBadRange2() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8d)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 2, 7)));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series when the start and end times are equal and not in the time-series.
   */
  @Test
  public void testSubSeriesByTimeEqualStartAndEndNotInTS() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8d)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    assertEquals(dts.subSeries(toZdt(LocalDate.of(2010, 3, 18)), true, toZdt(LocalDate.of(2010, 3, 18)), true),
        ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC());
  }

  /**
   * Tests a sub-series formed using inclusive start and end dates.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansTrueTrue() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeDoubleTimeSeries sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), true, toZdt(LocalDate.of(2010, 3, 8)), true);
    assertEquals(1, sub1.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub1.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using inclusive start and exclusive end dates.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansTrueFalse() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeDoubleTimeSeries sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), true, toZdt(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub1.size());

    final ZonedDateTimeDoubleTimeSeries sub2 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), true, toZdt(LocalDate.of(2010, 3, 9)), false);
    assertEquals(1, sub2.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub2.getValueAtIndex(0));

    final ZonedDateTimeDoubleTimeSeries sub3 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), true, toZdt(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub3.size());
  }

  /**
   * Tests a sub-series formed using exclusive start and inclusive end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansFalseTrue() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeDoubleTimeSeries sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), false, toZdt(LocalDate.of(2010, 3, 8)), true);
    assertEquals(0, sub1.size());

    final ZonedDateTimeDoubleTimeSeries sub2 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), false, toZdt(LocalDate.of(2010, 3, 9)), true);
    assertEquals(0, sub2.size());

    final ZonedDateTimeDoubleTimeSeries sub3 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), false, toZdt(LocalDate.of(2010, 3, 8)), true);
    assertEquals(1, sub3.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub3.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub3.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using exclusive start and end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansFalseFalse() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeDoubleTimeSeries sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), false, toZdt(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub1.size());

    final ZonedDateTimeDoubleTimeSeries sub2 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), false, toZdt(LocalDate.of(2010, 3, 9)), false);
    assertEquals(0, sub2.size());

    final ZonedDateTimeDoubleTimeSeries sub3 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), false, toZdt(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub3.size());

    final ZonedDateTimeDoubleTimeSeries sub4 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), false, toZdt(LocalDate.of(2010, 3, 9)), false);
    assertEquals(1, sub4.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub4.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub4.getValueAtIndex(0));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series formed using the max end date.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansMaxSimple() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeDoubleTimeSeries sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 9)), true, toZdt(LocalDate.MAX), false);
    assertEquals(1, sub1.size());
    assertEquals(toZdt(LocalDate.of(2010, 4, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub1.getValueAtIndex(0));

    final ZonedDateTimeDoubleTimeSeries sub2 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 9)), true, toZdt(LocalDate.MAX), true);
    assertEquals(1, sub2.size());
    assertEquals(toZdt(LocalDate.of(2010, 4, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub2.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using the max end date.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansMaxComplex() {
    final ZonedDateTime max = ZonedDateTimeToLongConverter.convertToZonedDateTime(Long.MAX_VALUE, ZoneOffset.UTC);
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(max, 5d)
        .build();
    final ZonedDateTimeDoubleTimeSeries sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), true, max, false);
    assertEquals(1, sub1.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub1.getValueAtIndex(0));

    final ZonedDateTimeDoubleTimeSeries sub2 =
        dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), true, max, true);
    assertEquals(2, sub2.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub2.getValueAtIndex(0));
    assertEquals(max, sub2.getTimeAtIndex(1));
    assertEquals(Double.valueOf(5d), sub2.getValueAtIndex(1));

    final ZonedDateTimeDoubleTimeSeries sub3 = dts.subSeries(max, true, max, true);
    assertEquals(1, sub3.size());
    assertEquals(max, sub3.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub3.getValueAtIndex(0));

    final ZonedDateTimeDoubleTimeSeries sub4 = dts.subSeries(max, false, max, true);
    assertEquals(0, sub4.size());

    final ZonedDateTimeDoubleTimeSeries sub5 = dts.subSeries(max, true, max, false);
    assertEquals(0, sub5.size());

    final ZonedDateTimeDoubleTimeSeries sub6 = dts.subSeries(max, false, max, false);
    assertEquals(0, sub6.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.of(ZDT_2222, 2.0);
    assertEquals("ImmutableZonedDateTimeDoubleTimeSeries[(" + ZDT_2222 + ", 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the time series created using the builder without adding dates.
   */
  @Test
  public void testBuilderNothingAdded() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  @Override
  public void testIterator() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    final ZonedDateTimeDoubleEntryIterator it = ts.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(ZDT_1111, 1.0d), it.next());
    assertEquals(ZDT_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue(), 1e-15);
    assertEquals(1.0d, it.currentValueFast());
    assertEquals(ZDT_2222, it.nextTime());
    assertEquals(ZDT_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the builder iterator.
   */
  @Test
  public void testBuilderIterator() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(ZDT_1111, 1.0d), it.next());
    assertEquals(ZDT_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue(), 1e-15);
    assertEquals(1.0d, it.currentValueFast());
    assertEquals(ZDT_2222, it.nextTime());
    assertEquals(ZDT_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNext() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    final ZonedDateTimeDoubleEntryIterator it = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      it.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNext() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      it.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNextTimeFast() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    final ZonedDateTimeDoubleEntryIterator it = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      it.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNextTimeFast() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      it.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTime() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    ts.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTime() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    bld.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTimeFast() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    ts.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTimeFast() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    bld.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentValue() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    ts.iterator().currentValue();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentValue() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    bld.iterator().currentValue();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentValueFast() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    ts.iterator().currentValueFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentValueFast() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    bld.iterator().currentValueFast();
  }

  /**
   * Tests the current index.
   */
  @Test
  public void testCurrentIndex() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    final ZonedDateTimeDoubleEntryIterator iterator = ts.iterator();
    for (int i = 0; i < ts.size(); i++) {
      iterator.next();
      assertEquals(iterator.currentIndex(), i);
    }
  }

  /**
   * Tests the current index.
   */
  @Test
  public void testBuilderCurrentIndex() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeDoubleEntryIterator iterator = bld.iterator();
    for (int i = 0; i < bld.size(); i++) {
      iterator.next();
      assertEquals(iterator.currentIndex(), i);
    }
  }

  /**
   * Tests the exception when trying to remove from an immutable time series.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testIteratorRemove() {
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    ts.iterator().remove();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderRemove() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    bld.iterator().remove();
  }

  /**
   * Tests the exception when too many elements are removed.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderRemoveTooMany() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final int n = bld.size();
    final ZonedDateTimeDoubleEntryIterator iterator = bld.iterator();
    for (int i = 0; i < n + 1; i++) {
      iterator.next();
      iterator.remove();
    }
  }

  /**
   * Tests iteration on an empty series.
   */
  @Test
  public void testIteratorEmpty() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    assertEquals(false, bld.iterator().hasNext());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveFirst() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveMid() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_3333};
    final double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveLast() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222};
    final double[] outValues = new double[] {1.0, 2.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutZDT() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutZonedDateTimeAlreadyThere() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_2222, 1.0);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLong() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 2.0).put(3333_000_000_000L, 3.0).put(1111_000_000_000L, 1.0);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLongAlreadyThere() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 2.0).put(3333_000_000_000L, 3.0).put(2222_000_000_000L, 1.0);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLongBig() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] outDates = new long[600];
    final double[] outValues = new double[600];
    for (int i = 0; i < 600; i++) {
      bld.put(2222_000_000_000L + i, i);
      outDates[i] = 2222_000_000_000L + i;
      outValues[i] = i;
    }
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllZDT() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllZonedDateTimeMismatchedArrays() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllLong() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder with mismatched array lengths.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllLongMismatchedArrays() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTS() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeAllNonEmptyBuilder() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.put(ZDT_0, 0.5).putAll(ddts, 0, 3);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0, ZDT_1111, ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {0.5, 1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeFromStart() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 0, 1);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111};
    final double[] outValues = new double[] {1.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeToEnd() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 1, 3);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeEmpty() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.put(ZDT_0, 0.5).putAll(ddts, 1, 1);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0};
    final double[] outValues = new double[] {0.5};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidLow() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, -1, 3);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidHigh() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 4, 2);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidLow() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 1, -1);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidHigh() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 3, 4);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartEndOrder() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMap() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final Map<ZonedDateTime, Double> map = new HashMap<>();
    map.put(ZDT_2222, 2.0d);
    map.put(ZDT_3333, 3.0d);
    map.put(ZDT_1111, 1.0d);
    bld.putAll(map);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMapEmpty() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    final Map<ZonedDateTime, Double> map = new HashMap<>();
    bld.put(ZDT_0, 0.5).putAll(map);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0};
    final double[] outValues = new double[] {0.5};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearEmpty() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.clear();
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearSomething() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 1.0).clear();
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method of the builder.
   */
  @Test
  public void testBuilderToString() {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    assertEquals("Builder[size=1]", bld.put(2222_000_000_000L, 1.0).toString());
  }

  /**
   * Tests the conversion of a time series to a builder.
   */
  @Test
  public void testToBuilder() {
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeDoubleTimeSeries expected = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 6d)
        .build();
    assertEquals(dts.toBuilder().put(toZdt(LocalDate.of(2010, 5, 8)), 6d).build(), expected);
  }

  /**
   * Tests a zone change.
   */
  @Test
  public void testZoneChange() {
    final ZoneId newZone = ZoneId.systemDefault();
    final ZonedDateTimeDoubleTimeSeries dts = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeDoubleTimeSeries ets = new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(newZone)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    assertSame(dts.withZone(ZoneOffset.UTC), dts);
    assertEquals(dts.withZone(newZone), ets);
  }

  /**
   * Tests operations on time series.
   */
  @Test
  public void testTsOperators() {
    final ZonedDateTimeDoubleTimeSeries ts1 = createStandardTimeSeries();
    final ZonedDateTimeDoubleTimeSeries ts2 = createStandardTimeSeries2();
    assertOperationSuccessful(ts1.power(ts2), new double[] {256.0, 3125.0, 46656.0 });
    assertOperationSuccessful(ts1.unionPower(ts2), new double[] {1.0, 2.0, 3.0, 256.0, 3125.0, 46656.0, 7.0, 8.0, 9.0 });
    assertOperationSuccessful(ts1.minimum(ts2), new double[] {4.0, 5.0, 6.0 });
    assertOperationSuccessful(ts1.unionMinimum(ts2), new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 });
    assertOperationSuccessful(ts1.maximum(ts2), new double[] {4.0, 5.0, 6.0 });
    assertOperationSuccessful(ts1.unionMaximum(ts2), new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 });
  }

  private static ZonedDateTime toZdt(final LocalDate ld) {
    return ZonedDateTime.of(LocalDateTime.of(ld, LocalTime.of(16, 0)), ZoneOffset.UTC);
  }
}
