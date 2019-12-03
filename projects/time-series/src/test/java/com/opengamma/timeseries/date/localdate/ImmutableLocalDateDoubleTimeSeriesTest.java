/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import static org.testng.AssertJUnit.assertEquals;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;

/**
 * Tests for {@link ImmutableLocalDateDoubleTimeSeries} and {@link ImmutableLocalDateDoubleTimeSeriesBuilder}.
 */
@Test(groups = "unit")
public class ImmutableLocalDateDoubleTimeSeriesTest extends LocalDateDoubleTimeSeriesTest {

  @Override
  protected LocalDateDoubleTimeSeries createEmptyTimeSeries() {
    return ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  protected LocalDateDoubleTimeSeries createStandardTimeSeries() {
    return (LocalDateDoubleTimeSeries) super.createStandardTimeSeries();
  }

  @Override
  protected LocalDateDoubleTimeSeries createStandardTimeSeries2() {
    return (LocalDateDoubleTimeSeries) super.createStandardTimeSeries2();
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(final LocalDate[] times, final double[] values) {
    return ImmutableLocalDateDoubleTimeSeries.of(times, values);
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(final List<LocalDate> times, final List<Double> values) {
    return ImmutableLocalDateDoubleTimeSeries.of(times, values);
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(final DoubleTimeSeries<LocalDate> dts) {
    return ImmutableLocalDateDoubleTimeSeries.from(dts);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from a date and value.
   */
  @Test
  public void testOfLDDouble() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2012, 6, 30), 2.0);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0);
  }

  /**
   * Tests the behaviour when the date is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLDDoubleNull() {
    ImmutableLocalDateDoubleTimeSeries.of((LocalDate) null, 2.0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of dates and values.
   */
  @Test
  public void testOfLDArrayDoubleArray() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Double[] inValues = new Double[] {2.0, 3.0};
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLDArrayDoubleArrayWrongOrder() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    final Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLDArrayDoubleArrayMismatchedArrays() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30)};
    final Double[] inValues = new Double[] {2.0, 3.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLDArrayDoubleArrayNullDates() {
    final Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of((LocalDate[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLDArrayDoubleArrayNullValues() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, (Double[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of dates and doubles.
   */
  @Test
  public void testOfLDArrayPrimitiveDoubleArray() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] inValues = new double[] {2.0, 3.0};
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLDArrayPrimitiveDoubleArrayWrongOrder() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLDArrayPrimitivedoubleArrayMismatchedArrays() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30)};
    final double[] inValues = new double[] {2.0, 3.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLDArrayPrimitiveDoubleArrayNullDates() {
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of((LocalDate[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLDArrayPrimitiveDoubleArrayNullValues() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, (double[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from ints and values.
   */
  @Test
  public void testOfIntArrayDoubleArray() {
    final int[] inDates = new int[] {20120630, 20120701};
    final double[] inValues = new double[] {2.0, 3.0};
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfIntArrayDoubleArrayWrongOrder() {
    final int[] inDates = new int[] {20120630, 20120701, 20120601};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfIntArrayDoubleArrayMismatchedArrays() {
    final int[] inDates = new int[] {20120630};
    final double[] inValues = new double[] {2.0, 3.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of ints cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfIntArrayDoubleArrayNullDates() {
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of((int[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfIntArrayDoubleArrayNullValues() {
    final int[] inDates = new int[] {20120630, 20120701};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, (double[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the intersection of two time series using the values from the first series.
   */
  @Test
  public void testIntersectionFirstValueSelectFirst() {
    final LocalDateDoubleTimeSeries dts = createStandardTimeSeries();
    final LocalDateDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final LocalDateDoubleTimeSeries dts3 = ImmutableLocalDateDoubleTimeSeries.builder()
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();

    final LocalDateDoubleTimeSeries result1 = dts.intersectionFirstValue(dts3);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));

    final LocalDateDoubleTimeSeries result2 = dts3.intersectionFirstValue(dts);
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
    final LocalDateDoubleTimeSeries dts = createStandardTimeSeries();
    final LocalDateDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final LocalDateDoubleTimeSeries dts3 = ImmutableLocalDateDoubleTimeSeries.builder()
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();

    final LocalDateDoubleTimeSeries result2 = dts.intersectionSecondValue(dts3);
    assertEquals(3, result2.size());
    assertEquals(Double.valueOf(-1.0), result2.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result2.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result2.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result2.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result2.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result2.getTimeAtIndex(2));

    final LocalDateDoubleTimeSeries result1 = dts3.intersectionSecondValue(dts);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series using dates as the boundaries.
   */
  @Test
  public void testSubSeriesByLocalDatesSingle() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 8d)
        .put(LocalDate.of(2010, 6, 8), 9d)
        .build();
    final LocalDateDoubleTimeSeries singleMiddle = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 9));
    assertEquals(1, singleMiddle.size());
    assertEquals(LocalDate.of(2010, 3, 8), singleMiddle.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), singleMiddle.getValueAtIndex(0));

    final LocalDateDoubleTimeSeries singleStart = dts.subSeries(LocalDate.of(2010, 2, 8), LocalDate.of(2010, 2, 9));
    assertEquals(1, singleStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), singleStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), singleStart.getValueAtIndex(0));

    final LocalDateDoubleTimeSeries singleEnd = dts.subSeries(LocalDate.of(2010, 6, 8), LocalDate.of(2010, 6, 9));
    assertEquals(1, singleEnd.size());
    assertEquals(LocalDate.of(2010, 6, 8), singleEnd.getTimeAtIndex(0));
    assertEquals(Double.valueOf(9d), singleEnd.getValueAtIndex(0));
  }

  /**
   * Tests an empty sub-series.
   */
  @Test
  public void testSubSeriesByLocalDatesEmpty() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 8));
    assertEquals(0, sub.size());
  }

  /**
   * Tests a sub-series using dates as the boundaries.
   */
  @Test
  public void testSubSeriesByLocalDatesRange() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 8d)
        .put(LocalDate.of(2010, 6, 8), 9d)
        .build();
    final LocalDateDoubleTimeSeries middle = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 5, 9));
    assertEquals(3, middle.size());
    assertEquals(LocalDate.of(2010, 3, 8), middle.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), middle.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 4, 8), middle.getTimeAtIndex(1));
    assertEquals(Double.valueOf(5d), middle.getValueAtIndex(1));
    assertEquals(LocalDate.of(2010, 5, 8), middle.getTimeAtIndex(2));
    assertEquals(Double.valueOf(8d), middle.getValueAtIndex(2));

    final LocalDateDoubleTimeSeries fromStart = dts.subSeries(LocalDate.of(2010, 2, 8), LocalDate.of(2010, 4, 9));
    assertEquals(3, fromStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), fromStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), fromStart.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 3, 8), fromStart.getTimeAtIndex(1));
    assertEquals(Double.valueOf(3d), fromStart.getValueAtIndex(1));
    assertEquals(LocalDate.of(2010, 4, 8), fromStart.getTimeAtIndex(2));
    assertEquals(Double.valueOf(5d), fromStart.getValueAtIndex(2));

    final LocalDateDoubleTimeSeries preStart = dts.subSeries(LocalDate.of(2010, 1, 8), LocalDate.of(2010, 3, 9));
    assertEquals(2, preStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), preStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), preStart.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 3, 8), preStart.getTimeAtIndex(1));
    assertEquals(Double.valueOf(3d), preStart.getValueAtIndex(1));

    final LocalDateDoubleTimeSeries postEnd = dts.subSeries(LocalDate.of(2010, 5, 8), LocalDate.of(2010, 12, 9));
    assertEquals(2, postEnd.size());
    assertEquals(LocalDate.of(2010, 5, 8), postEnd.getTimeAtIndex(0));
    assertEquals(Double.valueOf(8d), postEnd.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 6, 8), postEnd.getTimeAtIndex(1));
    assertEquals(Double.valueOf(9d), postEnd.getValueAtIndex(1));
  }

  /**
   * Tests the behaviour when the second date is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByLocalDatesBadRange1() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 8d)
        .put(LocalDate.of(2010, 6, 8), 9d)
        .build();
    dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 7));
  }

  /**
   * Tests the behaviour when the second date is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByLocalDatesBadRange2() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 8d)
        .put(LocalDate.of(2010, 6, 8), 9d)
        .build();
    dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 2, 7));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series when the start and end times are equal and not in the time-series.
   */
  @Test
  public void testSubSeriesByTimeEqualStartAndEndNotInTS() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 8d)
        .put(LocalDate.of(2010, 6, 8), 9d)
        .build();
    assertEquals(dts.subSeries(LocalDate.of(2010, 3, 18), true, LocalDate.of(2010, 3, 18), true),
        ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  }

  /**
   * Tests a sub-series formed using inclusive start and end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansTrueTrue() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 8), true);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub1.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using inclusive start and exclusive end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansTrueFalse() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub1.size());

    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 9), false);
    assertEquals(1, sub2.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub2.getValueAtIndex(0));

    final LocalDateDoubleTimeSeries sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub3.size());
  }

  /**
   * Tests a sub-series formed using exclusive start and inclusive end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansFalseTrue() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 8), true);
    assertEquals(0, sub1.size());

    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 9), true);
    assertEquals(0, sub2.size());

    final LocalDateDoubleTimeSeries sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 8), true);
    assertEquals(1, sub3.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub3.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub3.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using exclusive start and end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansFalseFalse() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub1.size());

    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 9), false);
    assertEquals(0, sub2.size());

    final LocalDateDoubleTimeSeries sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub3.size());

    final LocalDateDoubleTimeSeries sub4 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 9), false);
    assertEquals(1, sub4.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub4.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub4.getValueAtIndex(0));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series formed using the max end date.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansMaxSimple() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 9), true, LocalDate.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 4, 8), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub1.getValueAtIndex(0));

    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 9), true, LocalDate.MAX, true);
    assertEquals(1, sub2.size());
    assertEquals(LocalDate.of(2010, 4, 8), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub2.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using the max end date.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansMaxComplex() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.MAX, 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub1.getValueAtIndex(0));

    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.MAX, true);
    assertEquals(2, sub2.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub2.getValueAtIndex(0));
    assertEquals(LocalDate.MAX, sub2.getTimeAtIndex(1));
    assertEquals(Double.valueOf(5d), sub2.getValueAtIndex(1));

    final LocalDateDoubleTimeSeries sub3 = dts.subSeries(LocalDate.MAX, true, LocalDate.MAX, true);
    assertEquals(1, sub3.size());
    assertEquals(LocalDate.MAX, sub3.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub3.getValueAtIndex(0));

    final LocalDateDoubleTimeSeries sub4 = dts.subSeries(LocalDate.MAX, false, LocalDate.MAX, true);
    assertEquals(0, sub4.size());

    final LocalDateDoubleTimeSeries sub5 = dts.subSeries(LocalDate.MAX, true, LocalDate.MAX, false);
    assertEquals(0, sub5.size());

    final LocalDateDoubleTimeSeries sub6 = dts.subSeries(LocalDate.MAX, false, LocalDate.MAX, false);
    assertEquals(0, sub6.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2012, 6, 30), 2.0);
    assertEquals("ImmutableLocalDateDoubleTimeSeries[(2012-06-30, 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the time series created using the builder without adding dates.
   */
  @Test
  public void testBuilderNothingAdded() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    assertEquals(0, bld.size());
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  //-------------------------------------------------------------------------
  @Override
  public void testIterator() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2012, 6, 30), 2.0)
        .put(LocalDate.of(2012, 7, 1), 3.0)
        .put(LocalDate.of(2012, 6, 1), 1.0)
        .build();
    final LocalDateDoubleEntryIterator it = ts.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(LocalDate.of(2012, 6, 1), 1.0d), it.next());
    assertEquals(LocalDate.of(2012, 6, 1), it.currentTime());
    assertEquals(20120601, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue());
    assertEquals(1.0d, it.currentValueFast());
    assertEquals(LocalDate.of(2012, 6, 30), it.nextTime());
    assertEquals(LocalDate.of(2012, 7, 1), it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the builder iterator.
   */
  @Test
  public void testBuilderIterator() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    final LocalDateDoubleEntryIterator it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(LocalDate.of(2012, 6, 1), 1.0d), it.next());
    assertEquals(LocalDate.of(2012, 6, 1), it.currentTime());
    assertEquals(20120601, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue());
    assertEquals(1.0d, it.currentValueFast());
    assertEquals(LocalDate.of(2012, 6, 30), it.nextTime());
    assertEquals(LocalDate.of(2012, 7, 1), it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNext() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0).build();
    final LocalDateDoubleEntryIterator iterator = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      iterator.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the builder.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNext() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    final LocalDateDoubleEntryIterator iterator = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      iterator.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNextTimeFast() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0).build();
    final LocalDateDoubleEntryIterator iterator = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      iterator.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the builder.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNextTimeFast() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    final LocalDateDoubleEntryIterator iterator = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      iterator.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTime() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0).build();
    ts.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTime() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    bld.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTimeFast() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    bld.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTimeFast() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0).build();
    ts.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentValue() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    bld.iterator().currentValue();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentValue() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0).build();
    ts.iterator().currentValue();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentValueFast() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0).build();
    ts.iterator().currentValueFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentValueFast() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    bld.iterator().currentValueFast();
  }

  /**
   * Tests the current index.
   */
  @Test
  public void testCurrentIndex() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0).build();
    final LocalDateDoubleEntryIterator iterator = ts.iterator();
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
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    final LocalDateDoubleEntryIterator iterator = bld.iterator();
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
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0).build();
    ts.iterator().remove();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderRemove() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    bld.iterator().remove();
  }

  /**
   * Tests the exception when too many elements are removed.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderRemoveTooMany() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    final int n = bld.size();
    final LocalDateDoubleEntryIterator iterator = bld.iterator();
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
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.builder().build();
    assertEquals(false, ts.iterator().hasNext());
  }

  /**
   * Tests iteration on an empty series.
   */
  @Test
  public void testBuilderIteratorEmpty() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    assertEquals(false, bld.iterator().hasNext());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testBuilderIteratorRemoveFirst() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    final LocalDateDoubleEntryIterator it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testBuilderIteratorRemoveMid() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    final LocalDateDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testBuilderIteratorRemoveLast() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    final LocalDateDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30)};
    final double[] outValues = new double[] {1.0, 2.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLD() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLDAlreadyThere() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 30), 1.0);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutInt() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(20120630, 2.0).put(20120701, 3.0).put(20120601, 1.0);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutIntAlreadyThere() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(20120630, 2.0).put(20120701, 3.0).put(20120630, 1.0);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutIntBig() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] outDates = new int[600];
    final double[] outValues = new double[600];
    LocalDate date = LocalDate.of(2012, 6, 30);
    for (int i = 0; i < 600; i++) {
      bld.put(date, i);
      outDates[i] = LocalDateToIntConverter.convertToInt(date);
      outValues[i] = i;
      date = date.plusDays(1);
    }
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllLD() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllLDMismatchedArrays() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllInt() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120630, 20120701, 20120601};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder with mismatched array lengths.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllIntMismatchedArrays() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120630, 20120701};
    final double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTS() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeAllNonEmptyBuilder() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.put(LocalDate.of(2012, 5, 1), 0.5).putAll(ddts, 0, 3);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1), LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {0.5, 1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeFromStart() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 0, 1);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1)};
    final double[] outValues = new double[] {1.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeToEnd() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, 3);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeEmpty() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.put(LocalDate.of(2012, 5, 1), 0.5).putAll(ddts, 1, 1);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1)};
    final double[] outValues = new double[] {0.5};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidLow() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, -1, 3);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidHigh() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 4, 2);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidLow() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, -1);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidHigh() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 4);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartEndOrder() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final double[] inValues = new double[] {1.0, 2.0, 3.0};
    final DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMap() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final Map<LocalDate, Double> map = new HashMap<>();
    map.put(LocalDate.of(2012, 6, 30), 2.0d);
    map.put(LocalDate.of(2012, 7, 1), 3.0d);
    map.put(LocalDate.of(2012, 6, 1), 1.0d);
    bld.putAll(map);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMapEmpty() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    final Map<LocalDate, Double> map = new HashMap<>();
    bld.put(LocalDate.of(2012, 5, 1), 0.5).putAll(map);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1)};
    final double[] outValues = new double[] {0.5};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearEmpty() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.clear();
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearSomething() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(20120630, 1.0).clear();
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method of the builder.
   */
  @Test
  public void testBuilderToString() {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    assertEquals("Builder[size=1]", bld.put(20120630, 1.0).toString());
  }

  /**
   * Tests the conversion of a time series to a builder.
   */
  @Test
  public void testToBuilder() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries expected = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 6d)
        .build();
    assertEquals(dts.toBuilder().put(LocalDate.of(2010, 5, 8), 6d).build(), expected);
  }
}
