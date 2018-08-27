/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.date.DateObjectTimeSeries;

/**
 * Tests for {@link ImmutableLocalDateObjectTimeSeries} and {@link ImmutableLocalDateObjectTimeSeriesBuilder}.
 */
@Test(groups = "unit")
public class ImmutableLocalDateObjectTimeSeriesTest extends LocalDateObjectTimeSeriesTest {

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return ImmutableLocalDateObjectTimeSeries.ofEmpty();
  }

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createStandardTimeSeries() {
    return (LocalDateObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries();
  }

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createStandardTimeSeries2() {
    return (LocalDateObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries2();
  }

  @Override
  protected ObjectTimeSeries<LocalDate, BigDecimal> createTimeSeries(final LocalDate[] times, final BigDecimal[] values) {
    return ImmutableLocalDateObjectTimeSeries.of(times, values);
  }

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(final List<LocalDate> times, final List<BigDecimal> values) {
    return ImmutableLocalDateObjectTimeSeries.of(times, values);
  }

  @Override
  protected ObjectTimeSeries<LocalDate, BigDecimal> createTimeSeries(final ObjectTimeSeries<LocalDate, BigDecimal> dts) {
    return ImmutableLocalDateObjectTimeSeries.from(dts);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from a date and value.
   */
  @Test
  public void testOfLDValue() {
    final LocalDateObjectTimeSeries<Float> ts = ImmutableLocalDateObjectTimeSeries.of(LocalDate.of(2012, 6, 30), 2.0f);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
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
  public void testOfLDArrayValueArray() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    final LocalDateObjectTimeSeries<Float> ts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLDArrayValueArrayWrongOrder() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLDArrayValueArrayMismatchedArrays() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30)};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLDArrayValueArrayNullDates() {
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableLocalDateObjectTimeSeries.of((LocalDate[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLDArrayValueArrayNullValues() {
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    ImmutableLocalDateObjectTimeSeries.of(inDates, (Float[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of dates and doubles.
   */
  @Test
  public void testOfIntArrayValueArray() {
    final int[] inDates = new int[] {20120630, 20120701};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    final LocalDateObjectTimeSeries<Float> ts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfIntArrayValueArrayWrongOrder() {
    final int[] inDates = new int[] {20120630, 20120701, 20120601};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfIntArrayValueArrayMismatchedArrays() {
    final int[] inDates = new int[] {20120630};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfIntArrayValueArrayNullDates() {
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableLocalDateObjectTimeSeries.of((int[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfIntArrayValueArrayNullValues() {
    final int[] inDates = new int[] {20120630, 20120701};
    ImmutableLocalDateObjectTimeSeries.of(inDates, (Float[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series using dates as the boundaries.
   */
  @Test
  public void testSubSeriesByLocalDatesSingle() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .put(LocalDate.of(2010, 5, 8), 8)
        .put(LocalDate.of(2010, 6, 8), 9)
        .build();
    final LocalDateObjectTimeSeries<Integer> singleMiddle = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 9));
    assertEquals(1, singleMiddle.size());
    assertEquals(LocalDate.of(2010, 3, 8), singleMiddle.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), singleMiddle.getValueAtIndex(0));

    final LocalDateObjectTimeSeries<Integer> singleStart = dts.subSeries(LocalDate.of(2010, 2, 8), LocalDate.of(2010, 2, 9));
    assertEquals(1, singleStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), singleStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), singleStart.getValueAtIndex(0));

    final LocalDateObjectTimeSeries<Integer> singleEnd = dts.subSeries(LocalDate.of(2010, 6, 8), LocalDate.of(2010, 6, 9));
    assertEquals(1, singleEnd.size());
    assertEquals(LocalDate.of(2010, 6, 8), singleEnd.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(9), singleEnd.getValueAtIndex(0));
  }

  /**
   * Tests an empty sub-series.
   */
  @Test
  public void testSubSeriesByLocalDatesEmpty() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 8));
    assertEquals(0, sub.size());
  }

  /**
   * Tests a sub-series using dates as the boundaries.
   */
  @Test
  public void testSubSeriesByLocalDatesRange() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .put(LocalDate.of(2010, 5, 8), 8)
        .put(LocalDate.of(2010, 6, 8), 9)
        .build();
    final LocalDateObjectTimeSeries<Integer> middle = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 5, 9));
    assertEquals(3, middle.size());
    assertEquals(LocalDate.of(2010, 3, 8), middle.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), middle.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 4, 8), middle.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(5), middle.getValueAtIndex(1));
    assertEquals(LocalDate.of(2010, 5, 8), middle.getTimeAtIndex(2));
    assertEquals(Integer.valueOf(8), middle.getValueAtIndex(2));

    final LocalDateObjectTimeSeries<Integer> fromStart = dts.subSeries(LocalDate.of(2010, 2, 8), LocalDate.of(2010, 4, 9));
    assertEquals(3, fromStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), fromStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), fromStart.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 3, 8), fromStart.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(3), fromStart.getValueAtIndex(1));
    assertEquals(LocalDate.of(2010, 4, 8), fromStart.getTimeAtIndex(2));
    assertEquals(Integer.valueOf(5), fromStart.getValueAtIndex(2));

    final LocalDateObjectTimeSeries<Integer> preStart = dts.subSeries(LocalDate.of(2010, 1, 8), LocalDate.of(2010, 3, 9));
    assertEquals(2, preStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), preStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), preStart.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 3, 8), preStart.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(3), preStart.getValueAtIndex(1));

    final LocalDateObjectTimeSeries<Integer> postEnd = dts.subSeries(LocalDate.of(2010, 5, 8), LocalDate.of(2010, 12, 9));
    assertEquals(2, postEnd.size());
    assertEquals(LocalDate.of(2010, 5, 8), postEnd.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(8), postEnd.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 6, 8), postEnd.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(9), postEnd.getValueAtIndex(1));
  }

  /**
   * Tests the behaviour when the second date is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByLocalDatesBadRange1() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .put(LocalDate.of(2010, 5, 8), 8)
        .put(LocalDate.of(2010, 6, 8), 9)
        .build();
    dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 7));
  }

  /**
   * Tests the behaviour when the second date is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByLocalDatesBadRange2() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .put(LocalDate.of(2010, 5, 8), 8)
        .put(LocalDate.of(2010, 6, 8), 9)
        .build();
    dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 2, 7));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series when the start and end times are equal and not in the time-series.
   */
  @Test
  public void testSubSeriesByTimeEqualStartAndEndNotInTS() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .put(LocalDate.of(2010, 5, 8), 8)
        .put(LocalDate.of(2010, 6, 8), 9)
        .build();
    assertEquals(dts.subSeries(LocalDate.of(2010, 3, 18), true, LocalDate.of(2010, 3, 18), true),
        ImmutableLocalDateObjectTimeSeries.ofEmpty());
  }

  /**
   * Tests a sub-series formed using inclusive start and end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansTrueTrue() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 8), true);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub1.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using inclusive start and exclusive end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansTrueFalse() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub1.size());

    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 9), false);
    assertEquals(1, sub2.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub2.getValueAtIndex(0));

    final LocalDateObjectTimeSeries<Integer> sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub3.size());
  }

  /**
   * Tests a sub-series formed using exclusive start and inclusive end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansFalseTrue() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 8), true);
    assertEquals(0, sub1.size());

    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 9), true);
    assertEquals(0, sub2.size());

    final LocalDateObjectTimeSeries<Integer> sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 8), true);
    assertEquals(1, sub3.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub3.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub3.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using exclusive start and end dates.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansFalseFalse() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub1.size());

    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 9), false);
    assertEquals(0, sub2.size());

    final LocalDateObjectTimeSeries<Integer> sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub3.size());

    final LocalDateObjectTimeSeries<Integer> sub4 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 9), false);
    assertEquals(1, sub4.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub4.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub4.getValueAtIndex(0));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series formed using the max end date.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansMaxSimple() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 9), true, LocalDate.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 4, 8), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub1.getValueAtIndex(0));

    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 9), true, LocalDate.MAX, true);
    assertEquals(1, sub2.size());
    assertEquals(LocalDate.of(2010, 4, 8), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub2.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using the max end date.
   */
  @Test
  public void testSubSeriesByLocalDatesAndBooleansMaxComplex() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.MAX, 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub1.getValueAtIndex(0));

    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.MAX, true);
    assertEquals(2, sub2.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub2.getValueAtIndex(0));
    assertEquals(LocalDate.MAX, sub2.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(5), sub2.getValueAtIndex(1));

    final LocalDateObjectTimeSeries<Integer> sub3 = dts.subSeries(LocalDate.MAX, true, LocalDate.MAX, true);
    assertEquals(1, sub3.size());
    assertEquals(LocalDate.MAX, sub3.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub3.getValueAtIndex(0));

    final LocalDateObjectTimeSeries<Integer> sub4 = dts.subSeries(LocalDate.MAX, false, LocalDate.MAX, true);
    assertEquals(0, sub4.size());

    final LocalDateObjectTimeSeries<Integer> sub5 = dts.subSeries(LocalDate.MAX, true, LocalDate.MAX, false);
    assertEquals(0, sub5.size());

    final LocalDateObjectTimeSeries<Integer> sub6 = dts.subSeries(LocalDate.MAX, false, LocalDate.MAX, false);
    assertEquals(0, sub6.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    final LocalDateObjectTimeSeries<Float> ts = ImmutableLocalDateObjectTimeSeries.of(LocalDate.of(2012, 6, 30), 2.0f);
    assertEquals("ImmutableLocalDateObjectTimeSeries[(2012-06-30, 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the time series created using the builder without adding dates.
   */
  @Test
  public void testBuilderNothingAdded() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    assertEquals(ImmutableLocalDateObjectTimeSeries.ofEmpty(), bld.build());
  }

  //-------------------------------------------------------------------------
  @Override
  public void testIterator() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    final LocalDateObjectEntryIterator<Float> it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(LocalDate.of(2012, 6, 1), 1.0f), it.next());
    assertEquals(LocalDate.of(2012, 6, 1), it.currentTime());
    assertEquals(20120601, it.currentTimeFast());
    assertEquals(1.0f, it.currentValue());
    assertEquals(LocalDate.of(2012, 6, 30), it.nextTime());
    assertEquals(LocalDate.of(2012, 7, 1), it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the exception when the iterator runs over the end of the builder.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNext() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    final LocalDateObjectEntryIterator<Float> iterator = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      iterator.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the builder.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNextTimeFast() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    final LocalDateObjectEntryIterator<Float> iterator = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      iterator.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTime() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    bld.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTimeFast() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    bld.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentValue() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    bld.iterator().currentValue();
  }

  /**
   * Tests the current index.
   */
  @Test
  public void testCurrentIndex() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    final LocalDateObjectEntryIterator<Float> iterator = bld.iterator();
    for (int i = 0; i < bld.size(); i++) {
      iterator.next();
      assertEquals(iterator.currentIndex(), i);
    }
  }

  /**
   * Tests the exception the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testRemove() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    bld.iterator().remove();;
  }

  /**
   * Tests the exception when too many elements are removed.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testRemoveTooMany() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    final int n = bld.size();
    final LocalDateObjectEntryIterator<Float> iterator = bld.iterator();
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
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    assertEquals(false, bld.iterator().hasNext());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveFirst() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    final LocalDateObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveMid() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    final LocalDateObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveLast() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    final LocalDateObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30)};
    final Float[] outValues = new Float[] {1.0f, 2.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLD() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLDAlreadyThere() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 30), 1.0f);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutInt() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(20120630, 2.0f).put(20120701, 3.0f).put(20120601, 1.0f);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutIntAlreadyThere() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(20120630, 2.0f).put(20120701, 3.0f).put(20120630, 1.0f);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutIntBig() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] outDates = new int[600];
    final Float[] outValues = new Float[600];
    for (int i = 0; i < 600; i++) {
      bld.put(20120630 + i, (float) i);
      outDates[i] = 20120630 + i;
      outValues[i] = (float) i;
    }
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllLD() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllLDMismatchedArrays() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllInt() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120630, 20120701, 20120601};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllIntMismatchedArrays() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120630, 20120701};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTS() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeAllNonEmptyBuilder() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.put(LocalDate.of(2012, 5, 1), 0.5f).putAll(ddts, 0, 3);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1), LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {0.5f, 1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeFromStart() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 0, 1);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1)};
    final Float[] outValues = new Float[] {1.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeToEnd() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, 3);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeEmpty() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.put(LocalDate.of(2012, 5, 1), 0.5f).putAll(ddts, 1, 1);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1)};
    final Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidLow() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, -1, 3);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidHigh() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 4, 2);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidLow() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, -1);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidHigh() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 4);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartEndOrder() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final int[] inDates = new int[] {20120601, 20120630, 20120701};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMap() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final Map<LocalDate, Float> map = new HashMap<>();
    map.put(LocalDate.of(2012, 6, 30), 2.0f);
    map.put(LocalDate.of(2012, 7, 1), 3.0f);
    map.put(LocalDate.of(2012, 6, 1), 1.0f);
    bld.putAll(map);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMapEmpty() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    final Map<LocalDate, Float> map = new HashMap<>();
    bld.put(LocalDate.of(2012, 5, 1), 0.5f).putAll(map);
    final LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1)};
    final Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearEmpty() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.clear();
    assertEquals(ImmutableLocalDateObjectTimeSeries.ofEmpty(), bld.build());
  }

  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearSomething() {
    final LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(20120630, 1.0f).clear();
    assertEquals(ImmutableLocalDateObjectTimeSeries.ofEmpty(), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method of the builder.
   */
  @Test
  public void testBuilderToString() {
    final LocalDateObjectTimeSeriesBuilder<BigDecimal> bld = ImmutableLocalDateObjectTimeSeries.builder();
    assertEquals("Builder[size=1]", bld.put(20120630, BigDecimal.valueOf(1.0f)).toString());
  }

  /**
   * Tests the conversion of a time series to a builder.
   */
  @Test
  public void testToBuilder() {
    final LocalDateObjectTimeSeries<Float> dts = ImmutableLocalDateObjectTimeSeries.<Float>builder()
        .put(LocalDate.of(2010, 2, 8), 2F)
        .put(LocalDate.of(2010, 3, 8), 3F)
        .put(LocalDate.of(2010, 4, 8), 5F)
        .build();
    final LocalDateObjectTimeSeries<Float> expected = ImmutableLocalDateObjectTimeSeries.<Float>builder()
        .put(LocalDate.of(2010, 2, 8), 2F)
        .put(LocalDate.of(2010, 3, 8), 3F)
        .put(LocalDate.of(2010, 4, 8), 5F)
        .put(LocalDate.of(2010, 5, 8), 6F)
        .build();
    assertEquals(dts.toBuilder().put(LocalDate.of(2010, 5, 8), 6F).build(), expected);
  }

}
