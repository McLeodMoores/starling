/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import static org.testng.Assert.assertEquals;

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
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Tests for {@link ImmutableInstantDoubleTimeSeries} and {@link ImmutableInstantDoubleTimeSeriesBuilder}.
 */
@Test(groups = "unit")
public class ImmutableInstantDoubleTimeSeriesTest extends InstantDoubleTimeSeriesTest {
  private static final Instant I_1111 = Instant.ofEpochSecond(1111).atZone(ZoneOffset.UTC).toInstant();
  private static final Instant I_2222 = Instant.ofEpochSecond(2222).atZone(ZoneOffset.UTC).toInstant();
  private static final Instant I_3333 = Instant.ofEpochSecond(3333).atZone(ZoneOffset.UTC).toInstant();

  @Override
  protected InstantDoubleTimeSeries createEmptyTimeSeries() {
    return ImmutableInstantDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  protected InstantDoubleTimeSeries createStandardTimeSeries() {
    return (InstantDoubleTimeSeries) super.createStandardTimeSeries();
  }

  @Override
  protected InstantDoubleTimeSeries createStandardTimeSeries2() {
    return (InstantDoubleTimeSeries) super.createStandardTimeSeries2();
  }

  @Override
  protected InstantDoubleTimeSeries createTimeSeries(final Instant[] times, final double[] values) {
    return ImmutableInstantDoubleTimeSeries.of(times, values);
  }

  @Override
  protected InstantDoubleTimeSeries createTimeSeries(final List<Instant> times, final List<Double> values) {
    return ImmutableInstantDoubleTimeSeries.of(times, values);
  }

  @Override
  protected InstantDoubleTimeSeries createTimeSeries(final DoubleTimeSeries<Instant> dts) {
    return ImmutableInstantDoubleTimeSeries.from(dts);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from a date and value.
   */
  @Test
  public void testOfInstantDouble() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.of(Instant.ofEpochSecond(12345), 2.0);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(12345));
    assertEquals(ts.getValueAtIndex(0), 2.0, 1e-15);
  }

  /**
   * Tests the behaviour when the date is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfInstantDoubleNull() {
    ImmutableInstantDoubleTimeSeries.of((Instant) null, 2.0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of dates and values.
   */
  @Test
  public void testOfInstantArrayDoubleArray() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Double[] inValues = new Double[] {2.0, 3.0};
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0, 1e-15);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0, 1e-15);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfInstantArrayDoubleArrayWrongOrder() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    final Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfInstantArrayDoubleArrayMismatchedArrays() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222)};
    final Double[] inValues = new Double[] {2.0, 3.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfInstantArrayDoubleArrayNullDates() {
    final Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of((Instant[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfInstantArrayDoubleArrayNullValues() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    ImmutableInstantDoubleTimeSeries.of(inDates, (Double[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of dates and doubles.
   */
  public void testOfInstantArrayPrimitiveDoubleArray() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] inValues = new double[] {2.0, 3.0};
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0, 1e-15);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0, 1e-15);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfInstantArrayPrimitiveDoubleArrayWrongOrder() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfInstantArrayPrimitiveDoubleArrayMismatchedArrays() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222)};
    final double[] inValues = new double[] {2.0, 3.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfInstantArrayPrimitiveDoubleArrayNullDates() {
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of((Instant[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfInstantArrayPrimitiveDoubleArrayNullValues() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    ImmutableInstantDoubleTimeSeries.of(inDates, (double[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from longs and values.
   */
  @Test
  public void testOfLongArrayPrimitiveDoubleArray() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {2.0, 3.0};
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0, 1e-15);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0, 1e-15);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLongArrayPrimitiveDoubleArrayWrongOrder() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLongArrayPrimitiveDoubleArrayMismatchedArrays() {
    final long[] inDates = new long[] {2222_000_000_000L};
    final double[] inValues = new double[] {2.0, 3.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of longs cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLongArrayPrimitiveDoubleArrayNullDates() {
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of((long[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLongArrayPrimitiveDoubleArrayNullValues() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    ImmutableInstantDoubleTimeSeries.of(inDates, (double[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the intersection of two time series using the values from the first series.
   */
  @Test
  public void testIntersectionFirstValueSelectFirst() {
    final InstantDoubleTimeSeries dts = createStandardTimeSeries();
    final InstantDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final InstantDoubleTimeSeries dts3 = ImmutableInstantDoubleTimeSeries.builder()
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();

    final InstantDoubleTimeSeries result1 = dts.intersectionFirstValue(dts3);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));

    final InstantDoubleTimeSeries result2 = dts3.intersectionFirstValue(dts);
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
    final InstantDoubleTimeSeries dts = createStandardTimeSeries();
    final InstantDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final InstantDoubleTimeSeries dts3 = ImmutableInstantDoubleTimeSeries.builder()
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();

    final InstantDoubleTimeSeries result2 = dts.intersectionSecondValue(dts3);
    assertEquals(3, result2.size());
    assertEquals(Double.valueOf(-1.0), result2.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result2.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result2.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result2.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result2.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result2.getTimeAtIndex(2));

    final InstantDoubleTimeSeries result1 = dts3.intersectionSecondValue(dts);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));
  }

  /**
   * Tests a sub-series using instants as the boundaries.
   */
  @Test
  public void testSubSeriesByDatesSingle() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8d)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    final InstantDoubleTimeSeries singleMiddle = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 3, 9)));
    assertEquals(1, singleMiddle.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), singleMiddle.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), singleMiddle.getValueAtIndex(0));

    final InstantDoubleTimeSeries singleStart = dts.subSeries(toInstant(LocalDate.of(2010, 2, 8)), toInstant(LocalDate.of(2010, 2, 9)));
    assertEquals(1, singleStart.size());
    assertEquals(toInstant(LocalDate.of(2010, 2, 8)), singleStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), singleStart.getValueAtIndex(0));

    final InstantDoubleTimeSeries singleEnd = dts.subSeries(toInstant(LocalDate.of(2010, 6, 8)), toInstant(LocalDate.of(2010, 6, 9)));
    assertEquals(1, singleEnd.size());
    assertEquals(toInstant(LocalDate.of(2010, 6, 8)), singleEnd.getTimeAtIndex(0));
    assertEquals(Double.valueOf(9d), singleEnd.getValueAtIndex(0));
  }

  /**
   * Tests an empty sub-series.
   */
  @Test
  public void testSubSeriesByDatesEmpty() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final InstantDoubleTimeSeries sub = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 3, 8)));
    assertEquals(0, sub.size());
  }

  /**
   * Tests a sub-series using instants as the boundaries.
   */
  @Test
  public void testSubSeriesByDatesRange() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8d)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    final InstantDoubleTimeSeries middle = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 5, 9)));
    assertEquals(3, middle.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), middle.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), middle.getValueAtIndex(0));
    assertEquals(toInstant(LocalDate.of(2010, 4, 8)), middle.getTimeAtIndex(1));
    assertEquals(Double.valueOf(5d), middle.getValueAtIndex(1));
    assertEquals(toInstant(LocalDate.of(2010, 5, 8)), middle.getTimeAtIndex(2));
    assertEquals(Double.valueOf(8d), middle.getValueAtIndex(2));

    final InstantDoubleTimeSeries fromStart = dts.subSeries(toInstant(LocalDate.of(2010, 2, 8)), toInstant(LocalDate.of(2010, 4, 9)));
    assertEquals(3, fromStart.size());
    assertEquals(toInstant(LocalDate.of(2010, 2, 8)), fromStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), fromStart.getValueAtIndex(0));
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), fromStart.getTimeAtIndex(1));
    assertEquals(Double.valueOf(3d), fromStart.getValueAtIndex(1));
    assertEquals(toInstant(LocalDate.of(2010, 4, 8)), fromStart.getTimeAtIndex(2));
    assertEquals(Double.valueOf(5d), fromStart.getValueAtIndex(2));

    final InstantDoubleTimeSeries preStart = dts.subSeries(toInstant(LocalDate.of(2010, 1, 8)), toInstant(LocalDate.of(2010, 3, 9)));
    assertEquals(2, preStart.size());
    assertEquals(toInstant(LocalDate.of(2010, 2, 8)), preStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), preStart.getValueAtIndex(0));
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), preStart.getTimeAtIndex(1));
    assertEquals(Double.valueOf(3d), preStart.getValueAtIndex(1));

    final InstantDoubleTimeSeries postEnd = dts.subSeries(toInstant(LocalDate.of(2010, 5, 8)), toInstant(LocalDate.of(2010, 12, 9)));
    assertEquals(2, postEnd.size());
    assertEquals(toInstant(LocalDate.of(2010, 5, 8)), postEnd.getTimeAtIndex(0));
    assertEquals(Double.valueOf(8d), postEnd.getValueAtIndex(0));
    assertEquals(toInstant(LocalDate.of(2010, 6, 8)), postEnd.getTimeAtIndex(1));
    assertEquals(Double.valueOf(9d), postEnd.getValueAtIndex(1));
  }

  /**
   * Tests the behaviour when the second instant is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByDatesBadRange1() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8d)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 3, 7)));
  }

  /**
   * Tests the behaviour when the second instant is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByDatesBadRange2() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8d)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 2, 7)));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series when the start and end times are equal and not in the time-series.
   */
  @Test
  public void testSubSeriesByTimeEqualStartAndEndNotInTS() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8d)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9d)
        .build();
    assertEquals(dts.subSeries(toInstant(LocalDate.of(2010, 3, 18)), true, toInstant(LocalDate.of(2010, 3, 18)), true),
        ImmutableInstantDoubleTimeSeries.EMPTY_SERIES);
  }

  /**
   * Tests a sub-series formed using inclusive start and end instants.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansTrueTrue() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final InstantDoubleTimeSeries sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), true, toInstant(LocalDate.of(2010, 3, 8)), true);
    assertEquals(1, sub1.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub1.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using inclusive start and exclusive end instants.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansTrueFalse() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final InstantDoubleTimeSeries sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), true, toInstant(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub1.size());

    final InstantDoubleTimeSeries sub2 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), true, toInstant(LocalDate.of(2010, 3, 9)), false);
    assertEquals(1, sub2.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub2.getValueAtIndex(0));

    final InstantDoubleTimeSeries sub3 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), true, toInstant(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub3.size());
  }

  /**
   * Tests a sub-series formed using exclusive start and inclusive end instants.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansFalseTrue() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final InstantDoubleTimeSeries sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), false, toInstant(LocalDate.of(2010, 3, 8)), true);
    assertEquals(0, sub1.size());

    final InstantDoubleTimeSeries sub2 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), false, toInstant(LocalDate.of(2010, 3, 9)), true);
    assertEquals(0, sub2.size());

    final InstantDoubleTimeSeries sub3 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), false, toInstant(LocalDate.of(2010, 3, 8)), true);
    assertEquals(1, sub3.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub3.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub3.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using exclusive start and end instants.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansFalseFalse() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final InstantDoubleTimeSeries sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), false, toInstant(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub1.size());

    final InstantDoubleTimeSeries sub2 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), false, toInstant(LocalDate.of(2010, 3, 9)), false);
    assertEquals(0, sub2.size());

    final InstantDoubleTimeSeries sub3 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), false, toInstant(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub3.size());

    final InstantDoubleTimeSeries sub4 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), false, toInstant(LocalDate.of(2010, 3, 9)), false);
    assertEquals(1, sub4.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub4.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub4.getValueAtIndex(0));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series formed using the max end instant.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansMaxSimple() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final InstantDoubleTimeSeries sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 9)), true, Instant.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(toInstant(LocalDate.of(2010, 4, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub1.getValueAtIndex(0));

    final InstantDoubleTimeSeries sub2 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 9)), true, Instant.MAX, true);
    assertEquals(1, sub2.size());
    assertEquals(toInstant(LocalDate.of(2010, 4, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub2.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using the max end instant.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansMaxComplex() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(Instant.MAX, 5d)
        .build();
    final InstantDoubleTimeSeries sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), true, Instant.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub1.getValueAtIndex(0));

    final InstantDoubleTimeSeries sub2 =
        dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), true, Instant.MAX, true);
    assertEquals(2, sub2.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub2.getValueAtIndex(0));
    assertEquals(Instant.MAX, sub2.getTimeAtIndex(1));
    assertEquals(Double.valueOf(5d), sub2.getValueAtIndex(1));

    final InstantDoubleTimeSeries sub3 = dts.subSeries(Instant.MAX, true, Instant.MAX, true);
    assertEquals(1, sub3.size());
    assertEquals(Instant.MAX, sub3.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub3.getValueAtIndex(0));

    final InstantDoubleTimeSeries sub4 = dts.subSeries(Instant.MAX, false, Instant.MAX, true);
    assertEquals(0, sub4.size());

    final InstantDoubleTimeSeries sub5 = dts.subSeries(Instant.MAX, true, Instant.MAX, false);
    assertEquals(0, sub5.size());

    final InstantDoubleTimeSeries sub6 = dts.subSeries(Instant.MAX, false, Instant.MAX, false);
    assertEquals(0, sub6.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.of(Instant.ofEpochSecond(2222), 2.0);
    assertEquals("ImmutableInstantDoubleTimeSeries[(" + Instant.ofEpochSecond(2222) + ", 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the time series created using the builder without adding dates.
   */
  @Test
  public void testBuilderNothingAdded() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    assertEquals(ImmutableInstantDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  //-------------------------------------------------------------------------
  @Override
  public void testIterator() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    final InstantDoubleEntryIterator it = ts.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(I_1111, 1.0d), it.next());
    assertEquals(I_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue(), 1e-15);
    assertEquals(1.0d, it.currentValueFast());
    assertEquals(I_2222, it.nextTime());
    assertEquals(I_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the builder iterator.
   */
  @Test
  public void testBuilderIterator() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantDoubleEntryIterator it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(I_1111, 1.0d), it.next());
    assertEquals(I_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue(), 1e-15);
    assertEquals(1.0d, it.currentValueFast());
    assertEquals(I_2222, it.nextTime());
    assertEquals(I_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNext() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    final InstantDoubleEntryIterator it = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      it.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNext() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantDoubleEntryIterator it = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      it.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNextTimeFast() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    final InstantDoubleEntryIterator it = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      it.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNextTimeFast() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantDoubleEntryIterator it = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      it.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTime() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    ts.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTime() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    bld.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTimeFast() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    ts.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTimeFast() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    bld.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentValue() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    ts.iterator().currentValue();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentValue() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    bld.iterator().currentValue();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentValueFast() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    ts.iterator().currentValueFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentValueFast() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    bld.iterator().currentValueFast();
  }

  /**
   * Tests the current index.
   */
  @Test
  public void testCurrentIndex() {
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    final InstantDoubleEntryIterator iterator = ts.iterator();
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
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantDoubleEntryIterator iterator = bld.iterator();
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
    final InstantDoubleTimeSeries ts = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    ts.iterator().remove();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderRemove() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    bld.iterator().remove();
  }

  /**
   * Tests the exception when too many elements are removed.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderRemoveTooMany() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final int n = bld.size();
    final InstantDoubleEntryIterator iterator = bld.iterator();
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
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    assertEquals(false, bld.iterator().hasNext());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveFirst() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantDoubleEntryIterator it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final Instant[] outDates = new Instant[] {I_2222, I_3333};
    final double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveMid() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final Instant[] outDates = new Instant[] {I_1111, I_3333};
    final double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveLast() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final Instant[] outDates = new Instant[] {I_1111, I_2222};
    final double[] outValues = new double[] {1.0, 2.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }


  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutInstant() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0).put(Instant.ofEpochSecond(3333), 3.0).put(Instant.ofEpochSecond(1111), 1.0);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutInstantAlreadyThere() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0).put(Instant.ofEpochSecond(3333), 3.0).put(Instant.ofEpochSecond(2222), 1.0);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLong() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(2222_000_000_000L, 2.0).put(3333_000_000_000L, 3.0).put(1111_000_000_000L, 1.0);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLongAlreadyThere() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(2222_000_000_000L, 2.0).put(3333_000_000_000L, 3.0).put(2222_000_000_000L, 1.0);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLongBig() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] outDates = new long[600];
    final double[] outValues = new double[600];
    for (int i = 0; i < 600; i++) {
      bld.put(2222_000_000_000L + i, i);
      outDates[i] = 2222_000_000_000L + i;
      outValues[i] = i;
    }
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllInstant() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllInstantMismatchedArrays() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllLong() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder with mismatched array lengths.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllLongMismatchedArrays() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
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
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeAllNonEmptyBuilder() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.put(Instant.ofEpochSecond(0), 0.5).putAll(ddts, 0, 3);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0), Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {0.5, 1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeFromStart() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 0, 1);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111)};
    final double[] outValues = new double[] {1.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeToEnd() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, 3);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeEmpty() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.put(Instant.ofEpochSecond(0), 0.5).putAll(ddts, 1, 1);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0)};
    final double[] outValues = new double[] {0.5};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidLow() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, -1, 3);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidHigh() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 4, 2);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidLow() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, -1);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidHigh() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 4);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartEndOrder() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMap() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final Map<Instant, Double> map = new HashMap<>();
    map.put(Instant.ofEpochSecond(2222), 2.0d);
    map.put(Instant.ofEpochSecond(3333), 3.0d);
    map.put(Instant.ofEpochSecond(1111), 1.0d);
    bld.putAll(map);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMapEmpty() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    final Map<Instant, Double> map = new HashMap<>();
    bld.put(Instant.ofEpochSecond(0), 0.5).putAll(map);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0)};
    final double[] outValues = new double[] {0.5};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearEmpty() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.clear();
    assertEquals(ImmutableInstantDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearSomething() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(2222_000_000_000L, 1.0).clear();
    assertEquals(ImmutableInstantDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method of the builder.
   */
  @Test
  public void testBuilderToString() {
    final InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    assertEquals("Builder[size=1]", bld.put(2222_000_000_000L, 1.0).toString());
  }

  /**
   * Tests the conversion of a time series to a builder.
   */
  @Test
  public void testToBuilder() {
    final InstantDoubleTimeSeries dts = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final InstantDoubleTimeSeries expected = ImmutableInstantDoubleTimeSeries.builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 6d)
        .build();
    assertEquals(dts.toBuilder().put(toInstant(LocalDate.of(2010, 5, 8)), 6d).build(), expected);
  }

  /**
   * Tests operations on time series.
   */
  @Test
  public void testTsOperators() {
    final InstantDoubleTimeSeries ts1 = createStandardTimeSeries();
    final InstantDoubleTimeSeries ts2 = createStandardTimeSeries2();
    assertOperationSuccessful(ts1.power(ts2), new double[] {256.0, 3125.0, 46656.0 });
    assertOperationSuccessful(ts1.unionPower(ts2), new double[] {1.0, 2.0, 3.0, 256.0, 3125.0, 46656.0, 7.0, 8.0, 9.0 });
    assertOperationSuccessful(ts1.minimum(ts2), new double[] {4.0, 5.0, 6.0 });
    assertOperationSuccessful(ts1.unionMinimum(ts2), new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 });
    assertOperationSuccessful(ts1.maximum(ts2), new double[] {4.0, 5.0, 6.0 });
    assertOperationSuccessful(ts1.unionMaximum(ts2), new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 });
  }

  private static Instant toInstant(final LocalDate ld) {
    return ZonedDateTime.of(LocalDateTime.of(ld, LocalTime.of(16, 0)), ZoneOffset.UTC).toInstant();
  }

}
