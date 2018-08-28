/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import static org.testng.Assert.assertEquals;

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
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateObjectTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;

/**
 * Test for {@link ImmutableInstantObjectTimeSeries} and {@link ImmutableInstantObjectTimeSeriesBuilder}.
 */
@Test(groups = "unit")
public class ImmutableInstantObjectTimeSeriesTest extends InstantObjectTimeSeriesTest {
  private static final Instant I_1111 = Instant.ofEpochSecond(1111);
  private static final Instant I_2222 = Instant.ofEpochSecond(2222);
  private static final Instant I_3333 = Instant.ofEpochSecond(3333);

  @Override
  protected InstantObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return ImmutableInstantObjectTimeSeries.ofEmpty();
  }

  @Override
  protected InstantObjectTimeSeries<BigDecimal> createStandardTimeSeries() {
    return (InstantObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries();
  }

  @Override
  protected InstantObjectTimeSeries<BigDecimal> createStandardTimeSeries2() {
    return (InstantObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries2();
  }

  @Override
  protected ObjectTimeSeries<Instant, BigDecimal> createTimeSeries(final Instant[] times, final BigDecimal[] values) {
    return ImmutableInstantObjectTimeSeries.of(times, values);
  }

  @Override
  protected InstantObjectTimeSeries<BigDecimal> createTimeSeries(final List<Instant> times, final List<BigDecimal> values) {
    return ImmutableInstantObjectTimeSeries.of(times, values);
  }

  @Override
  protected ObjectTimeSeries<Instant, BigDecimal> createTimeSeries(final ObjectTimeSeries<Instant, BigDecimal> dts) {
    return ImmutableInstantObjectTimeSeries.from(dts);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from an instant and value.
   */
  @Test
  public void testOfInstantValue() {
    final InstantObjectTimeSeries<Float> ts = ImmutableInstantObjectTimeSeries.of(Instant.ofEpochSecond(12345), 2.0f);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(12345));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
  }

  /**
   * Tests the behaviour when the date is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfInstantValueNull() {
    ImmutableInstantObjectTimeSeries.of((Instant) null, 2.0f);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from instants and values.
   */
  @Test
  public void testOfInstantArrayValueArray() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    final InstantObjectTimeSeries<Float> ts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  /**
   * Tests that the instants must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfInstantArrayValueArrayWrongOrder() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableInstantObjectTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfInstantArrayValueArrayMismatchedArrays() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222)};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableInstantObjectTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of instants cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfInstantArrayValueArrayNullDates() {
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableInstantObjectTimeSeries.of((Instant[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfInstantArrayValueArrayNullValues() {
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    ImmutableInstantObjectTimeSeries.of(inDates, (Float[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation from arrays of instants and doubles.
   */
  @Test
  public void testOfLongArrayValueArray() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    final InstantObjectTimeSeries<Float> ts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  /**
   * Tests that the instants must be in increasing order.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLongArrayValueArrayWrongOrder() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableInstantObjectTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the input arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfLongArrayValueArrayMismatchedArrays() {
    final long[] inDates = new long[] {2222_000_000_000L};
    final Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableInstantObjectTimeSeries.of(inDates, inValues);
  }

  /**
   * Tests that the array of instants cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLongArrayValueArrayNullDates() {
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableInstantObjectTimeSeries.of((long[]) null, inValues);
  }

  /**
   * Tests that the array of values cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testOfLongArrayValueArrayNullValues() {
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    ImmutableInstantObjectTimeSeries.of(inDates, (Float[]) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series using instants as the boundaries.
   */
  @Test
  public void testSubSeriesByInstantsSingle() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9)
        .build();
    final InstantObjectTimeSeries<Integer> singleMiddle = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 3, 9)));
    assertEquals(1, singleMiddle.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), singleMiddle.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), singleMiddle.getValueAtIndex(0));

    final InstantObjectTimeSeries<Integer> singleStart = dts.subSeries(toInstant(LocalDate.of(2010, 2, 8)), toInstant(LocalDate.of(2010, 2, 9)));
    assertEquals(1, singleStart.size());
    assertEquals(toInstant(LocalDate.of(2010, 2, 8)), singleStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), singleStart.getValueAtIndex(0));

    final InstantObjectTimeSeries<Integer> singleEnd = dts.subSeries(toInstant(LocalDate.of(2010, 6, 8)), toInstant(LocalDate.of(2010, 6, 9)));
    assertEquals(1, singleEnd.size());
    assertEquals(toInstant(LocalDate.of(2010, 6, 8)), singleEnd.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(9), singleEnd.getValueAtIndex(0));
  }

  /**
   * Tests an empty sub-series.
   */
  @Test
  public void testSubSeriesByInstantsEmpty() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final InstantObjectTimeSeries<Integer> sub = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 3, 8)));
    assertEquals(0, sub.size());
  }

  /**
   * Tests a sub-series using instants as the boundaries.
   */
  @Test
  public void testSubSeriesByInstantsRange() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9)
        .build();
    final InstantObjectTimeSeries<Integer> middle = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 5, 9)));
    assertEquals(3, middle.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), middle.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), middle.getValueAtIndex(0));
    assertEquals(toInstant(LocalDate.of(2010, 4, 8)), middle.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(5), middle.getValueAtIndex(1));
    assertEquals(toInstant(LocalDate.of(2010, 5, 8)), middle.getTimeAtIndex(2));
    assertEquals(Integer.valueOf(8), middle.getValueAtIndex(2));

    final InstantObjectTimeSeries<Integer> fromStart = dts.subSeries(toInstant(LocalDate.of(2010, 2, 8)), toInstant(LocalDate.of(2010, 4, 9)));
    assertEquals(3, fromStart.size());
    assertEquals(toInstant(LocalDate.of(2010, 2, 8)), fromStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), fromStart.getValueAtIndex(0));
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), fromStart.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(3), fromStart.getValueAtIndex(1));
    assertEquals(toInstant(LocalDate.of(2010, 4, 8)), fromStart.getTimeAtIndex(2));
    assertEquals(Integer.valueOf(5), fromStart.getValueAtIndex(2));

    final InstantObjectTimeSeries<Integer> preStart = dts.subSeries(toInstant(LocalDate.of(2010, 1, 8)), toInstant(LocalDate.of(2010, 3, 9)));
    assertEquals(2, preStart.size());
    assertEquals(toInstant(LocalDate.of(2010, 2, 8)), preStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), preStart.getValueAtIndex(0));
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), preStart.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(3), preStart.getValueAtIndex(1));

    final InstantObjectTimeSeries<Integer> postEnd = dts.subSeries(toInstant(LocalDate.of(2010, 5, 8)), toInstant(LocalDate.of(2010, 12, 9)));
    assertEquals(2, postEnd.size());
    assertEquals(toInstant(LocalDate.of(2010, 5, 8)), postEnd.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(8), postEnd.getValueAtIndex(0));
    assertEquals(toInstant(LocalDate.of(2010, 6, 8)), postEnd.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(9), postEnd.getValueAtIndex(1));
  }

  /**
   * Tests the behaviour when the second instant is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByInstantsBadRange1() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9)
        .build();
    dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 3, 7)));
  }

  /**
   * Tests the behaviour when the second instant is before the first.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSubSeriesByInstantsBadRange2() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9)
        .build();
    dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), toInstant(LocalDate.of(2010, 2, 7)));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series when the start and end times are equal and not in the time-series.
   */
  @Test
  public void testSubSeriesByTimeEqualStartAndEndNotInTS() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 8)
        .put(toInstant(LocalDate.of(2010, 6, 8)), 9)
        .build();
    assertEquals(dts.subSeries(toInstant(LocalDate.of(2010, 3, 18)), true, toInstant(LocalDate.of(2010, 3, 18)), true),
        ImmutableInstantObjectTimeSeries.ofEmpty());
  }

  /**
   * Tests a sub-series formed using inclusive start and end dates.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansTrueTrue() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final InstantObjectTimeSeries<Integer> sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), true, toInstant(LocalDate.of(2010, 3, 8)), true);
    assertEquals(1, sub1.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub1.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using inclusive start and exclusive end instants.
   */
  @Test
  public void testSubSeriesByInstantsAndBooleansTrueFalse() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final InstantObjectTimeSeries<Integer> sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), true, toInstant(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub1.size());

    final InstantObjectTimeSeries<Integer> sub2 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), true, toInstant(LocalDate.of(2010, 3, 9)), false);
    assertEquals(1, sub2.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub2.getValueAtIndex(0));

    final InstantObjectTimeSeries<Integer> sub3 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), true, toInstant(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub3.size());
  }

  /**
   * Tests a sub-series formed using exclusive start and inclusive end instants.
   */
  @Test
  public void testSubSeriesByInstantsAndBooleansFalseTrue() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final InstantObjectTimeSeries<Integer> sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), false, toInstant(LocalDate.of(2010, 3, 8)), true);
    assertEquals(0, sub1.size());

    final InstantObjectTimeSeries<Integer> sub2 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), false, toInstant(LocalDate.of(2010, 3, 9)), true);
    assertEquals(0, sub2.size());

    final InstantObjectTimeSeries<Integer> sub3 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), false, toInstant(LocalDate.of(2010, 3, 8)), true);
    assertEquals(1, sub3.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub3.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub3.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using exclusive start and end instants.
   */
  @Test
  public void testSubSeriesByInstantsAndBooleansFalseFalse() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final InstantObjectTimeSeries<Integer> sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), false, toInstant(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub1.size());

    final InstantObjectTimeSeries<Integer> sub2 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 8)), false, toInstant(LocalDate.of(2010, 3, 9)), false);
    assertEquals(0, sub2.size());

    final InstantObjectTimeSeries<Integer> sub3 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), false, toInstant(LocalDate.of(2010, 3, 8)), false);
    assertEquals(0, sub3.size());

    final InstantObjectTimeSeries<Integer> sub4 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), false, toInstant(LocalDate.of(2010, 3, 9)), false);
    assertEquals(1, sub4.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub4.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub4.getValueAtIndex(0));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a sub-series formed using the max end instant.
   */
  @Test
  public void testSubSeriesByInstantsAndBooleansMaxSimple() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5)
        .build();
    final InstantObjectTimeSeries<Integer> sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 9)), true, Instant.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(toInstant(LocalDate.of(2010, 4, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub1.getValueAtIndex(0));

    final InstantObjectTimeSeries<Integer> sub2 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 9)), true, Instant.MAX, true);
    assertEquals(1, sub2.size());
    assertEquals(toInstant(LocalDate.of(2010, 4, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub2.getValueAtIndex(0));
  }

  /**
   * Tests a sub-series formed using the max end instant.
   */
  @Test
  public void testSubSeriesByDatesAndBooleansMaxComplex() {
    final InstantObjectTimeSeries<Integer> dts = ImmutableInstantObjectTimeSeries.<Integer>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3)
        .put(Instant.MAX, 5)
        .build();
    final InstantObjectTimeSeries<Integer> sub1 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), true, Instant.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub1.getValueAtIndex(0));

    final InstantObjectTimeSeries<Integer> sub2 = dts.subSeries(toInstant(LocalDate.of(2010, 3, 7)), true, Instant.MAX, true);
    assertEquals(2, sub2.size());
    assertEquals(toInstant(LocalDate.of(2010, 3, 8)), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub2.getValueAtIndex(0));
    assertEquals(Instant.MAX, sub2.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(5), sub2.getValueAtIndex(1));

    final InstantObjectTimeSeries<Integer> sub3 = dts.subSeries(Instant.MAX, true, Instant.MAX, true);
    assertEquals(1, sub3.size());
    assertEquals(Instant.MAX, sub3.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub3.getValueAtIndex(0));

    final InstantObjectTimeSeries<Integer> sub4 = dts.subSeries(Instant.MAX, false, Instant.MAX, true);
    assertEquals(0, sub4.size());

    final InstantObjectTimeSeries<Integer> sub5 = dts.subSeries(Instant.MAX, true, Instant.MAX, false);
    assertEquals(0, sub5.size());

    final InstantObjectTimeSeries<Integer> sub6 = dts.subSeries(Instant.MAX, false, Instant.MAX, false);
    assertEquals(0, sub6.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    final InstantObjectTimeSeries<Float> ts = ImmutableInstantObjectTimeSeries.of(Instant.ofEpochSecond(2222), 2.0f);
    assertEquals("ImmutableInstantObjectTimeSeries[(" + Instant.ofEpochSecond(2222) + ", 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the time series created using the builder without adding dates.
   */
  @Test
  public void testBuilderNothingAdded() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    assertEquals(ImmutableInstantObjectTimeSeries.ofEmpty(), bld.build());
  }

  //-------------------------------------------------------------------------
  @Override
  public void testIterator() {
    final InstantObjectTimeSeries<Double> ts = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    final InstantObjectEntryIterator<Double> it = ts.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(I_1111, 1.0d), it.next());
    assertEquals(I_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue());
    assertEquals(I_2222, it.nextTime());
    assertEquals(I_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the builder iterator.
   */
  @Test
  public void testBuilderIterator() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantObjectEntryIterator<Double> it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(I_1111, 1.0d), it.next());
    assertEquals(I_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue());
    assertEquals(I_2222, it.nextTime());
    assertEquals(I_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNext() {
    final InstantObjectTimeSeries<Double> ts = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    final InstantObjectEntryIterator<Double> it = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      it.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNext() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantObjectEntryIterator<Double> it = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      it.next();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testHasNextTimeFast() {
    final InstantObjectTimeSeries<Double> ts = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    final InstantObjectEntryIterator<Double> it = ts.iterator();
    for (int i = 0; i < ts.size() + 1; i++) {
      it.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator runs over the end of the time series.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderHasNextTimeFast() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantObjectEntryIterator<Double> it = bld.iterator();
    for (int i = 0; i < bld.size() + 1; i++) {
      it.nextTimeFast();
    }
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTime() {
    final InstantObjectTimeSeries<Double> ts = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    ts.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTime() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    bld.iterator().currentTime();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentTimeFast() {
    final InstantObjectTimeSeries<Double> ts = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    ts.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentTimeFast() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    bld.iterator().currentTimeFast();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrentValue() {
    final InstantObjectTimeSeries<Double> ts = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    ts.iterator().currentValue();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderCurrentValue() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    bld.iterator().currentValue();
  }

  /**
   * Tests the current index.
   */
  @Test
  public void testCurrentIndex() {
    final InstantObjectTimeSeries<Double> ts = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    final InstantObjectEntryIterator<Double> iterator = ts.iterator();
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
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantObjectEntryIterator<Double> iterator = bld.iterator();
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
    final InstantObjectTimeSeries<Double> ts = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0).build();
    ts.iterator().remove();
  }

  /**
   * Tests the exception when the iterator has not been started.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilderRemove() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    bld.iterator().remove();
  }

  /**
   * Tests the exception when too many elements are removed.
   */
  @Test(expectedExceptions = NoSuchElementException.class)
  public void testBuilderRemoveTooMany() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final int n = bld.size();
    final InstantObjectEntryIterator<Double> iterator = bld.iterator();
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
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder();
    assertEquals(false, bld.iterator().hasNext());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveFirst() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder();
    bld.put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantObjectEntryIterator<Double> it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final Instant[] outDates = new Instant[] {I_2222, I_3333};
    final Double[] outValues = new Double[] {2.0, 3.0};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveMid() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder();
    bld.put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantObjectEntryIterator<Double> it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final Instant[] outDates = new Instant[] {I_1111, I_3333};
    final Double[] outValues = new Double[] {1.0, 3.0};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the remove method of the iterator.
   */
  @Test
  public void testIteratorRemoveLast() {
    final InstantObjectTimeSeriesBuilder<Double> bld = ImmutableInstantObjectTimeSeries.<Double>builder();
    bld.put(I_2222, 2.0).put(I_3333, 3.0).put(I_1111, 1.0);
    final InstantObjectEntryIterator<Double> it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    final Instant[] outDates = new Instant[] {I_1111, I_2222};
    final Double[] outValues = new Double[] {1.0, 2.0};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutInstant() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0f).put(Instant.ofEpochSecond(3333), 3.0f).put(Instant.ofEpochSecond(1111), 1.0f);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutInstantAlreadyThere() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0f).put(Instant.ofEpochSecond(3333), 3.0f).put(Instant.ofEpochSecond(2222), 1.0f);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLong() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(2222_000_000_000L, 2.0f).put(3333_000_000_000L, 3.0f).put(1111_000_000_000L, 1.0f);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLongAlreadyThere() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(2222_000_000_000L, 2.0f).put(3333_000_000_000L, 3.0f).put(2222_000_000_000L, 1.0f);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the put method of the builder.
   */
  @Test
  public void testBuilderPutLongBig() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] outDates = new long[600];
    final Float[] outValues = new Float[600];
    for (int i = 0; i < 600; i++) {
      bld.put(2222_000_000_000L + i, (float) i);
      outDates[i] = 2222_000_000_000L + i;
      outValues[i] = (float) i;
    }
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllInstant() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllInstantMismatchedArrays() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllLong() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    final Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder with mismatched array lengths.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPutAllLongMismatchedArrays() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
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
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeAllNonEmptyBuilder() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.put(Instant.ofEpochSecond(0), 0.5f).putAll(ddts, 0, 3);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0), Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {0.5f, 1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeFromStart() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 0, 1);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111)};
    final Float[] outValues = new Float[] {1.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeToEnd() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, 3);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllDDTSRangeEmpty() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.put(Instant.ofEpochSecond(0), 0.5f).putAll(ddts, 1, 1);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0)};
    final Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidLow() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, -1, 3);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartInvalidHigh() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 4, 2);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidLow() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, -1);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeEndInvalidHigh() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 4);
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testBuilderPutAllDDTSRangeStartEndOrder() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    final Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    final PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMap() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final Map<Instant, Float> map = new HashMap<>();
    map.put(Instant.ofEpochSecond(2222), 2.0f);
    map.put(Instant.ofEpochSecond(3333), 3.0f);
    map.put(Instant.ofEpochSecond(1111), 1.0f);
    bld.putAll(map);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    final Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  /**
   * Tests the putAll method of the builder.
   */
  @Test
  public void testBuilderPutAllMapEmpty() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    final Map<Instant, Float> map = new HashMap<>();
    bld.put(Instant.ofEpochSecond(0), 0.5f).putAll(map);
    final Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0)};
    final Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearEmpty() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.clear();
    assertEquals(ImmutableInstantObjectTimeSeries.ofEmpty(), bld.build());
  }

  /**
   * Tests the clear method of the builder.
   */
  @Test
  public void testBuilderClearSomething() {
    final InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(2222_000_000_000L, 1.0f).clear();
    assertEquals(ImmutableInstantObjectTimeSeries.ofEmpty(), bld.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toString method of the builder.
   */
  @Test
  public void testBuilderToString() {
    final InstantObjectTimeSeriesBuilder<BigDecimal> bld = ImmutableInstantObjectTimeSeries.builder();
    assertEquals("Builder[size=1]", bld.put(2222_000_000_000L, BigDecimal.valueOf(1.0)).toString());
  }

  /**
   * Tests the conversion of a time series to a builder.
   */
  @Test
  public void testToBuilder() {
    final InstantObjectTimeSeries<Double> dts = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .build();
    final InstantObjectTimeSeries<Double> expected = ImmutableInstantObjectTimeSeries.<Double>builder()
        .put(toInstant(LocalDate.of(2010, 2, 8)), 2d)
        .put(toInstant(LocalDate.of(2010, 3, 8)), 3d)
        .put(toInstant(LocalDate.of(2010, 4, 8)), 5d)
        .put(toInstant(LocalDate.of(2010, 5, 8)), 6d)
        .build();
    assertEquals(dts.toBuilder().put(toInstant(LocalDate.of(2010, 5, 8)), 6d).build(), expected);
  }

  /**
   * Tests a union operation.
   */
  @Test
  public void testUnionOperate() {
    final InstantObjectTimeSeries<Float> ts1 = ImmutableInstantObjectTimeSeries.<Float>builder()
        .put(toInstant(LocalDate.of(2018, 1, 1)), 1F).put(toInstant(LocalDate.of(2018, 2, 1)), 2F).build();
    final InstantObjectTimeSeries<Float> ts2 = ImmutableInstantObjectTimeSeries.<Float>builder()
        .put(toInstant(LocalDate.of(2018, 1, 1)), 10F).put(toInstant(LocalDate.of(2018, 3, 1)), 20F).build();
    final InstantObjectTimeSeries<Float> ets = ImmutableInstantObjectTimeSeries.<Float>builder()
        .put(toInstant(LocalDate.of(2018, 1, 1)), 11F).put(toInstant(LocalDate.of(2018, 2, 1)), 2F).put(toInstant(LocalDate.of(2018, 3, 1)), 20F).build();
    assertEquals(ts1.unionOperate(ts2, BIN), ets);
    assertEquals(ts2.unionOperate(ts1, BIN), ets);
    assertEquals(ts1.unionOperate(ImmutableInstantObjectTimeSeries.<Float>ofEmpty(), BIN), ts1);
  }

  /**
   * Tests an intersection operation.
   */
  @Test
  public void testIntersectionOperate() {
    final InstantObjectTimeSeries<Float> ts1 = ImmutableInstantObjectTimeSeries.<Float>builder()
        .put(toInstant(LocalDate.of(2018, 1, 1)), 1F).put(toInstant(LocalDate.of(2018, 2, 1)), 2F).build();
    final InstantObjectTimeSeries<Float> ts2 = ImmutableInstantObjectTimeSeries.<Float>builder()
        .put(toInstant(LocalDate.of(2018, 1, 1)), 10F).put(toInstant(LocalDate.of(2018, 3, 1)), 20F).build();
    final InstantObjectTimeSeries<Float> ets = ImmutableInstantObjectTimeSeries.<Float>builder()
        .put(toInstant(LocalDate.of(2018, 1, 1)), 11F).build();
    assertEquals(ts1.operate(ts2, BIN), ets);
    assertEquals(ts2.operate(ts1, BIN), ets);
    assertEquals(ts1.operate(ImmutableInstantObjectTimeSeries.<Float>ofEmpty(), BIN),
        ImmutableInstantObjectTimeSeries.<Float>ofEmpty());
  }

  /**
   * Tests a unary operation.
   */
  @Test
  public void testUnaryOperate() {
    final InstantObjectTimeSeries<Float> ts = ImmutableInstantObjectTimeSeries.<Float>builder()
        .put(toInstant(LocalDate.of(2018, 1, 1)), 1F).put(toInstant(LocalDate.of(2018, 2, 1)), 2F).build();
    final InstantObjectTimeSeries<Float> ets = ImmutableInstantObjectTimeSeries.<Float>builder()
        .put(toInstant(LocalDate.of(2018, 1, 1)), 2F).put(toInstant(LocalDate.of(2018, 2, 1)), 4F).build();
    assertEquals(ts.operate(UN), ets);
    assertEquals(ImmutableInstantObjectTimeSeries.<Float>ofEmpty().operate(UN),
        ImmutableInstantObjectTimeSeries.<Float>ofEmpty());
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

  private static Instant toInstant(final LocalDate ld) {
    return ZonedDateTime.of(LocalDateTime.of(ld, LocalTime.of(16, 0)), ZoneOffset.UTC).toInstant();
  }
}
