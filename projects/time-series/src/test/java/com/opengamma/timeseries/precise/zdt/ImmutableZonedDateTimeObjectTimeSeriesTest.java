/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.math.BigDecimal;
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

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateObjectTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;

/**
 * Tests for {@link ImmutableZonedDateTimeObjectTimeSeries} and {@link ImmutableZonedDateTimeObjectTimeSeriesBuilder}.
 */
@Test(groups = "unit")
public class ImmutableZonedDateTimeObjectTimeSeriesTest extends ZonedDateTimeObjectTimeSeriesTest {
  private static final ZonedDateTime ZDT_0 = Instant.ofEpochSecond(0).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_1111 = Instant.ofEpochSecond(1111).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_2222 = Instant.ofEpochSecond(2222).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_3333 = Instant.ofEpochSecond(3333).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_12345 = Instant.ofEpochSecond(12345).atZone(ZoneOffset.UTC);

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createStandardTimeSeries() {
    return (ZonedDateTimeObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries();
  }

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createStandardTimeSeries2() {
    return (ZonedDateTimeObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries2();
  }

  @Override
  protected ObjectTimeSeries<ZonedDateTime, BigDecimal> createTimeSeries(final ZonedDateTime[] times, final BigDecimal[] values) {
    return ImmutableZonedDateTimeObjectTimeSeries.of(times, values, ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createTimeSeries(final List<ZonedDateTime> times, final List<BigDecimal> values) {
    return ImmutableZonedDateTimeObjectTimeSeries.of(times, values, ZoneOffset.UTC);
  }

  @Override
  protected ObjectTimeSeries<ZonedDateTime, BigDecimal> createTimeSeries(final ObjectTimeSeries<ZonedDateTime, BigDecimal> dts) {
    return ImmutableZonedDateTimeObjectTimeSeries.from(dts, ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from a date and value.
   */
  @Test
  public void testOfZonedDateTimeValue() {
    final ZonedDateTimeObjectTimeSeries<Float> ts = ImmutableZonedDateTimeObjectTimeSeries.of(ZDT_12345, 2.0f);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), ZDT_12345);
    assertEquals(ts.getValueAtIndex(0), 2.0f, 1e-15);
  }

  /**
   * Tests the behaviour when the date is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfZonedDateTimeValueNull() {
    ImmutableZonedDateTimeObjectTimeSeries.of((ZonedDateTime) null, 2.0f);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of dates and values.
   */
  @Test
  public void testOfZonedDateTimeArrayValueArray() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    final ZonedDateTimeObjectTimeSeries<Float> ts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, null);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0f, 1e-15);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0f, 1e-15);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfZonedDateTimeArrayValueArrayWrongOrder() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, null);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfZonedDateTimeArrayValueArrayMismatchedArrays() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, null);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfZonedDateTimeArrayValueArrayNullDates() {
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of((ZonedDateTime[]) null, inValues, null);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfZonedDateTimeArrayValueArrayNullValues() {
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, (Float[]) null, null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of dates and doubles.
   */
  @Test
  public void testOfLongArrayValueArray() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    final ZonedDateTimeObjectTimeSeries<Float> ts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0f, 1e-15);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0f, 1e-15);
  }

  /**
   * Tests that the dates must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLongArrayValueArrayWrongOrder() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLongArrayValueArrayMismatchedArrays() {
    final long[] inDates = new long[] {2222_000_000_000L};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
  }

  /**
   * Tests that the array of dates cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLongArrayValueArrayNullDates() {
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of((long[]) null, inValues, ZoneOffset.UTC);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLongArrayValueArrayNullValues() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, (Float[]) null, ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series using dates as the boundaries.
   */
  @Test
  public void testSubSeriesByDatesSingle() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9)
        .build();
    final ZonedDateTimeObjectTimeSeries<Integer> singleMiddle = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 3, 9)));
    assertEquals(1, singleMiddle.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), singleMiddle.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), singleMiddle.getValueAtIndex(0));

    final ZonedDateTimeObjectTimeSeries<Integer> singleStart = dts.subSeries(toZdt(LocalDate.of(2010, 2, 8)), toZdt(LocalDate.of(2010, 2, 9)));
    assertEquals(1, singleStart.size());
    assertEquals(toZdt(LocalDate.of(2010, 2, 8)), singleStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), singleStart.getValueAtIndex(0));

    final ZonedDateTimeObjectTimeSeries<Integer> singleEnd = dts.subSeries(toZdt(LocalDate.of(2010, 6, 8)), toZdt(LocalDate.of(2010, 6, 9)));
    assertEquals(1, singleEnd.size());
    assertEquals(toZdt(LocalDate.of(2010, 6, 8)), singleEnd.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(9), singleEnd.getValueAtIndex(0));
  }

  /**
   * Tests an empty sub-series.
   */
  @Test
  public void testSubSeriesByDatesEmpty() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final ZonedDateTimeObjectTimeSeries<Integer> sub = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 3, 8)));
    assertEquals(0, sub.size());
  }

  /**
   * Tests a sub-series using dates as the boundaries.
   */
  @Test
  public void testSubSeriesByDatesRange() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9)
        .build();
    final ZonedDateTimeObjectTimeSeries<Integer> middle = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 5, 9)));
    assertEquals(3, middle.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), middle.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), middle.getValueAtIndex(0));
    assertEquals(toZdt(LocalDate.of(2010, 4, 8)), middle.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(5), middle.getValueAtIndex(1));
    assertEquals(toZdt(LocalDate.of(2010, 5, 8)), middle.getTimeAtIndex(2));
    assertEquals(Integer.valueOf(8), middle.getValueAtIndex(2));

    final ZonedDateTimeObjectTimeSeries<Integer> fromStart = dts.subSeries(toZdt(LocalDate.of(2010, 2, 8)), toZdt(LocalDate.of(2010, 4, 9)));
    assertEquals(3, fromStart.size());
    assertEquals(toZdt(LocalDate.of(2010, 2, 8)), fromStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), fromStart.getValueAtIndex(0));
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), fromStart.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(3), fromStart.getValueAtIndex(1));
    assertEquals(toZdt(LocalDate.of(2010, 4, 8)), fromStart.getTimeAtIndex(2));
    assertEquals(Integer.valueOf(5), fromStart.getValueAtIndex(2));

    final ZonedDateTimeObjectTimeSeries<Integer> preStart = dts.subSeries(toZdt(LocalDate.of(2010, 1, 8)), toZdt(LocalDate.of(2010, 3, 9)));
    assertEquals(2, preStart.size());
    assertEquals(toZdt(LocalDate.of(2010, 2, 8)), preStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), preStart.getValueAtIndex(0));
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), preStart.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(3), preStart.getValueAtIndex(1));

    final ZonedDateTimeObjectTimeSeries<Integer> postEnd = dts.subSeries(toZdt(LocalDate.of(2010, 5, 8)), toZdt(LocalDate.of(2010, 12, 9)));
    assertEquals(2, postEnd.size());
    assertEquals(toZdt(LocalDate.of(2010, 5, 8)), postEnd.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(8), postEnd.getValueAtIndex(0));
    assertEquals(toZdt(LocalDate.of(2010, 6, 8)), postEnd.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(9), postEnd.getValueAtIndex(1));
  }

  /**
   * Tests the behaviour when the second date is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByLocalBadRange1() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9)
        .build();
    dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 3, 7)));
  }

  /**
   * Tests the behaviour when the second date is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByDatesBadRange2() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9)
        .build();
    dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), toZdt(LocalDate.of(2010, 2, 7)));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series when the start and end times are equal and not in the time-series.
   */
  @Test
  public void testSubSeriesByTimeEqualStartAndEndNotInTS() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .put(toZdt(LocalDate.of(2010, 5, 8)), 8)
        .put(toZdt(LocalDate.of(2010, 6, 8)), 9)
        .build();
    assertEquals(dts.subSeries(toZdt(LocalDate.of(2010, 3, 18)), true, toZdt(LocalDate.of(2010, 3, 18)), true),
        ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(ZoneOffset.UTC));
  }

  /**
   * Tests a sub-series formed using inclusive start and end dates.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansTrueTrue() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final ZonedDateTimeObjectTimeSeries<Integer> sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), true, toZdt(LocalDate.of(2010, 3, 8)), true);
    assertEquals(1, sub1.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub1.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using inclusive start and exclusive end dates.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansTrueFalse() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final ZonedDateTimeObjectTimeSeries<Integer> sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), true, toZdt(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub1.size());

    final ZonedDateTimeObjectTimeSeries<Integer> sub2 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), true, toZdt(LocalDate.of(2010, 3, 9)), false);
    assertEquals(1, sub2.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub2.getValueAtIndex(0));

    final ZonedDateTimeObjectTimeSeries<Integer> sub3 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), true, toZdt(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub3.size());
  }

  /**
   * Tests a sub-series formed using exclusive start and inclusive end dates.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansFalseTrue() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final ZonedDateTimeObjectTimeSeries<Integer> sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), false, toZdt(LocalDate.of(2010, 3, 8)), true);
    assertEquals(0, sub1.size());

    final ZonedDateTimeObjectTimeSeries<Integer> sub2 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), false, toZdt(LocalDate.of(2010, 3, 9)), true);
    assertEquals(0, sub2.size());

    final ZonedDateTimeObjectTimeSeries<Integer> sub3 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), false, toZdt(LocalDate.of(2010, 3, 8)), true);
    assertEquals(1, sub3.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub3.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub3.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using exclusive start and end dates.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansFalseFalse() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final ZonedDateTimeObjectTimeSeries<Integer> sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), false, toZdt(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub1.size());

    final ZonedDateTimeObjectTimeSeries<Integer> sub2 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 8)), false, toZdt(LocalDate.of(2010, 3, 9)), false);
    assertEquals(0, sub2.size());

    final ZonedDateTimeObjectTimeSeries<Integer> sub3 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), false, toZdt(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub3.size());

    final ZonedDateTimeObjectTimeSeries<Integer> sub4 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), false, toZdt(LocalDate.of(2010, 3, 9)), false);
    assertEquals(1, sub4.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub4.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub4.getValueAtIndex(0));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series formed using the max end date.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansMaxSimple() {
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final ZonedDateTimeObjectTimeSeries<Integer> sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 9)), true, toZdt(LocalDate.MAX), false);
    assertEquals(1, sub1.size());
    assertEquals(toZdt(LocalDate.of(2010, 4, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub1.getValueAtIndex(0));

    final ZonedDateTimeObjectTimeSeries<Integer> sub2 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 9)), true, toZdt(LocalDate.MAX), true);
    assertEquals(1, sub2.size());
    assertEquals(toZdt(LocalDate.of(2010, 4, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub2.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using the max end date.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansMaxComplex() {
    final ZonedDateTime max = ZonedDateTimeToLongConverter.convertToZonedDateTime(Long.MAX_VALUE, ZoneOffset.UTC);
    final ZonedDateTimeObjectTimeSeries<Integer> dts = ImmutableZonedDateTimeObjectTimeSeries.<Integer>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3)
        .put(max, 5)
        .build();
    final ZonedDateTimeObjectTimeSeries<Integer> sub1 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), true, max, false);
    assertEquals(1, sub1.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub1.getValueAtIndex(0));

    final ZonedDateTimeObjectTimeSeries<Integer> sub2 = dts.subSeries(toZdt(LocalDate.of(2010, 3, 7)), true, max, true);
    assertEquals(2, sub2.size());
    assertEquals(toZdt(LocalDate.of(2010, 3, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub2.getValueAtIndex(0));
    assertEquals(max, sub2.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(5), sub2.getValueAtIndex(1));

    final ZonedDateTimeObjectTimeSeries<Integer> sub3 = dts.subSeries(max, true, max, true);
    assertEquals(1, sub3.size());
    assertEquals(max, sub3.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub3.getValueAtIndex(0));

    final ZonedDateTimeObjectTimeSeries<Integer> sub4 = dts.subSeries(max, false, max, true);
    assertEquals(0, sub4.size());

    final ZonedDateTimeObjectTimeSeries<Integer> sub5 = dts.subSeries(max, true, max, false);
    assertEquals(0, sub5.size());

    final ZonedDateTimeObjectTimeSeries<Integer> sub6 = dts.subSeries(max, false, max, false);
    assertEquals(0, sub6.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    final ZonedDateTimeObjectTimeSeries<Float> ts = ImmutableZonedDateTimeObjectTimeSeries.of(ZDT_2222, 2.0f);
    assertEquals("ImmutableZonedDateTimeObjectTimeSeries[(" + ZDT_2222 + ", 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the time series created using the builder without adding dates.
   */
  @Test
  public void testBuilderNothingAdded() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  @Override
  public void testIterator() {
    final ZonedDateTimeObjectTimeSeries<Double> ts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    final ZonedDateTimeObjectEntryIterator<Double> it = ts.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(ZDT_1111, 1.0d), it.next());
    assertEquals(ZDT_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue(), 1e-15);
    assertEquals(ZDT_2222, it.nextTime());
    assertEquals(ZDT_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the builder iterator.
   */
  @Test
  public void testBuilderIterator() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeObjectEntryIterator<Double> it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(ZDT_1111, 1.0d), it.next());
    assertEquals(ZDT_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue(), 1e-15);
    assertEquals(ZDT_2222, it.nextTime());
    assertEquals(ZDT_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNext() {
    final ZonedDateTimeObjectTimeSeries<Double> ts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    final ZonedDateTimeObjectEntryIterator<Double> it = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      it.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNext() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeObjectEntryIterator<Double> it = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      it.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNextTimeFast() {
    final ZonedDateTimeObjectTimeSeries<Double> ts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    final ZonedDateTimeObjectEntryIterator<Double> it = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      it.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNextTimeFast() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeObjectEntryIterator<Double> it = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      it.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTime() {
    final ZonedDateTimeObjectTimeSeries<Double> ts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    ts.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTime() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    bld.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTimeFast() {
    final ZonedDateTimeObjectTimeSeries<Double> ts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    ts.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTimeFast() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    bld.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentValue() {
    final ZonedDateTimeObjectTimeSeries<Double> ts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    ts.iterator().currentValue();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentValue() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    bld.iterator().currentValue();
  }

  /**
   * Tests the current index.
   */
  @Test
  public void testCurrentIndex() {
    final ZonedDateTimeObjectTimeSeries<Double> ts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    final ZonedDateTimeObjectEntryIterator<Double> iterator = ts.iterator();
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
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeObjectEntryIterator<Double> iterator = bld.iterator();
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
    final ZonedDateTimeObjectTimeSeries<Double> ts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0).build();
    ts.iterator().remove();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderRemove() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    bld.iterator().remove();
  }

  /**
   * Tests the exception when too many elements are removed.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderRemoveTooMany() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final int n = bld.size();
    final ZonedDateTimeObjectEntryIterator<Double> iterator = bld.iterator();
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
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC);
    assertEquals(false, bld.iterator().hasNext());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveFirst() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeObjectEntryIterator<Double> it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final Double[] outValues = new Double[] {2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveMid() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeObjectEntryIterator<Double> it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_3333};
    final Double[] outValues = new Double[] {1.0, 3.0};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveLast() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Double> bld = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    final ZonedDateTimeObjectEntryIterator<Double> it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222};
    final Double[] outValues = new Double[] {1.0, 2.0};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutZDT() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0f).put(ZDT_3333, 3.0f).put(ZDT_1111, 1.0f);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutZonedDateTimeAlreadyThere() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0f).put(ZDT_3333, 3.0f).put(ZDT_2222, 1.0f);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLong() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 2.0f).put(3333_000_000_000L, 3.0f).put(1111_000_000_000L, 1.0f);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLongAlreadyThere() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 2.0f).put(3333_000_000_000L, 3.0f).put(2222_000_000_000L, 1.0f);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLongBig() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] outDates = new long[600];
    final Float[] outValues = new Float[600];
    for (int i = 0; i < 600; i++) {
      bld.put(2222_000_000_000L + i, (float) i);
      outDates[i] = 2222_000_000_000L + i;
      outValues[i] = (float) i;
    }
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllZDT() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllZonedDateTimeMismatchedArrays() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllLong() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder with mismatched array lengths.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllLongMismatchedArrays() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTS() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeAllNonEmptyBuilder() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.put(ZDT_0, 0.5f).putAll(ddts, 0, 3);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0, ZDT_1111, ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {0.5f, 1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeFromStart() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 0, 1);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111};
    final Float[] outValues = new Float[] {1.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeToEnd() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 1, 3);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeEmpty() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.put(ZDT_0, 0.5f).putAll(ddts, 1, 1);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0};
    final Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidLow() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, -1, 3);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidHigh() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 4, 2);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidLow() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 1, -1);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidHigh() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 3, 4);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartEndOrder() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMap() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final Map<ZonedDateTime, Float> map = new HashMap<>();
    map.put(ZDT_2222, 2.0f);
    map.put(ZDT_3333, 3.0f);
    map.put(ZDT_1111, 1.0f);
    bld.putAll(map);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMapEmpty() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    final Map<ZonedDateTime, Float> map = new HashMap<>();
    bld.put(ZDT_0, 0.5f).putAll(map);
    final ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0};
    final Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearEmpty() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.clear();
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearSomething() {
    final ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 1.0f).clear();
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method of the builder.
   */
  @Test
  public void testBuilderToString() {
    final ZonedDateTimeObjectTimeSeriesBuilder<BigDecimal> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    assertEquals("Builder[size=1]", bld.put(2222_000_000_000L, BigDecimal.valueOf(1.0)).toString());
  }

  /**
   * Tests the conversion of a time series to a builder.
   */
  @Test
  public void testToBuilder() {
    final ZonedDateTimeObjectTimeSeries<Double> dts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeObjectTimeSeries<Double> expected = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
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
    final ZonedDateTimeObjectTimeSeries<Double> dts = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final ZonedDateTimeObjectTimeSeries<Double> ets = ImmutableZonedDateTimeObjectTimeSeries.<Double>builder(newZone)
        .put(toZdt(LocalDate.of(2010, 2, 8)), 2d)
        .put(toZdt(LocalDate.of(2010, 3, 8)), 3d)
        .put(toZdt(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    assertSame(dts.withZone(ZoneOffset.UTC), dts);
    assertEquals(dts.withZone(newZone), ets);
  }

  /**
   * Tests a union operation.
   */
  @Test
  public void testUnionOperate() {
    final ZonedDateTimeObjectTimeSeries<Float> ts1 = ImmutableZonedDateTimeObjectTimeSeries.<Float>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2018, 1, 1)), 1F).put(toZdt(LocalDate.of(2018, 2, 1)), 2F).build();
    final ZonedDateTimeObjectTimeSeries<Float> ts2 = ImmutableZonedDateTimeObjectTimeSeries.<Float>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2018, 1, 1)), 10F).put(toZdt(LocalDate.of(2018, 3, 1)), 20F).build();
    final ZonedDateTimeObjectTimeSeries<Float> ets = ImmutableZonedDateTimeObjectTimeSeries.<Float>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2018, 1, 1)), 11F).put(toZdt(LocalDate.of(2018, 2, 1)), 2F).put(toZdt(LocalDate.of(2018, 3, 1)), 20F).build();
    assertEquals(ts1.unionOperate(ts2, BIN), ets);
    assertEquals(ts2.unionOperate(ts1, BIN), ets);
    assertEquals(ts1.unionOperate(ImmutableZonedDateTimeObjectTimeSeries.<Float>ofEmpty(ZoneOffset.UTC), BIN), ts1);
  }

  /**
   * Tests an intersection operation.
   */
  @Test
  public void testIntersectionOperate() {
    final ZonedDateTimeObjectTimeSeries<Float> ts1 = ImmutableZonedDateTimeObjectTimeSeries.<Float>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2018, 1, 1)), 1F).put(toZdt(LocalDate.of(2018, 2, 1)), 2F).build();
    final ZonedDateTimeObjectTimeSeries<Float> ts2 = ImmutableZonedDateTimeObjectTimeSeries.<Float>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2018, 1, 1)), 10F).put(toZdt(LocalDate.of(2018, 3, 1)), 20F).build();
    final ZonedDateTimeObjectTimeSeries<Float> ets = ImmutableZonedDateTimeObjectTimeSeries.<Float>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2018, 1, 1)), 11F).build();
    assertEquals(ts1.operate(ts2, BIN), ets);
    assertEquals(ts2.operate(ts1, BIN), ets);
    assertEquals(ts1.operate(ImmutableZonedDateTimeObjectTimeSeries.<Float>ofEmpty(ZoneOffset.UTC), BIN),
        ImmutableZonedDateTimeObjectTimeSeries.<Float>ofEmpty(ZoneOffset.UTC));
  }

  /**
   * Tests a unary operation.
   */
  @Test
  public void testUnaryOperate() {
    final ZonedDateTimeObjectTimeSeries<Float> ts = ImmutableZonedDateTimeObjectTimeSeries.<Float>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2018, 1, 1)), 1F).put(toZdt(LocalDate.of(2018, 2, 1)), 2F).build();
    final ZonedDateTimeObjectTimeSeries<Float> ets = ImmutableZonedDateTimeObjectTimeSeries.<Float>builder(ZoneOffset.UTC)
        .put(toZdt(LocalDate.of(2018, 1, 1)), 2F).put(toZdt(LocalDate.of(2018, 2, 1)), 4F).build();
    assertEquals(ts.operate(UN), ets);
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.<Float>ofEmpty(ZoneOffset.UTC).operate(UN),
        ImmutableZonedDateTimeObjectTimeSeries.<Float>ofEmpty(ZoneOffset.UTC));
  }

  /**
   * Tests an operation.
   */
  @Test
  public void testOperate() {
    final LocalDateObjectTimeSeries<Float> ts = ImmutableLocalDateObjectTimeSeries.<Float>builder()
        .put(LocalDate.of(2018, 1, 1), 1F).put(LocalDate.of(2018, 2, 1), 2F).build();
    final LocalDateObjectTimeSeries<Float> ets = ImmutableLocalDateObjectTimeSeries.<Float>builder()
        .put(LocalDate.of(2018, 1, 1), 6F).put(LocalDate.of(2018, 2, 1), 7F).build();
    assertEquals(ts.operate(5F, BIN), ets);
  }

  private static ZonedDateTime toZdt(final LocalDate ld) {
    return ZonedDateTime.of(LocalDateTime.of(ld, LocalTime.of(16, 0)), ZoneOffset.UTC);
  }

}
