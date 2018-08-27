/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Unit tests for {@link TimeSeriesIntersector}.
 */
public class TimeSeriesIntersectorTest {
  private static final LocalDateDoubleTimeSeries TS_1 = ImmutableLocalDateDoubleTimeSeries.of(
      LocalDate.of(2018, 1, 1),
      1.0);
  private static final LocalDateDoubleTimeSeries TS_2 =  ImmutableLocalDateDoubleTimeSeries.of(
      new LocalDate[] {LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 2)},
      new double[] {1., 2.});
  private static final LocalDateDoubleTimeSeries TS_3 =  ImmutableLocalDateDoubleTimeSeries.of(
      new LocalDate[] {LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 2), LocalDate.of(2018, 1, 3)},
      new double[] {1., 2., 3.});
  private static final LocalDateDoubleTimeSeries TS_4 =  ImmutableLocalDateDoubleTimeSeries.of(
      new LocalDate[] {LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 2), LocalDate.of(2018, 1, 3), LocalDate.of(2018, 1, 4)},
      new double[] {1., 2., 3., 4.});

  /**
   * Tests a null input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    TimeSeriesIntersector.intersect(null);
  }

  /**
   * Tests an empty array.
   */
  @Test
  public void testEmptyArray() {
    final LocalDateDoubleTimeSeries[] in = new LocalDateDoubleTimeSeries[0];
    final DoubleTimeSeries<?>[] out = TimeSeriesIntersector.intersect(in);
    assertSame(in, out);
  }

  /**
   * Tests an array with a single ts.
   */
  @Test
  public void testSingleTs() {
    final LocalDateDoubleTimeSeries[] in = new LocalDateDoubleTimeSeries[] { TS_1 };
    final DoubleTimeSeries<?>[] out = TimeSeriesIntersector.intersect(in);
    assertSame(in, out);
  }

  /**
   * Tests an array with non-overlapping timeseries.
   */
  @Test
  public void testNonOverlapping() {
    final LocalDateDoubleTimeSeries ts =  ImmutableLocalDateDoubleTimeSeries.of(
        new LocalDate[] {LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 2), LocalDate.of(2017, 1, 3)},
        new double[] {1., 2., 3.});
    final LocalDateDoubleTimeSeries[] in = new LocalDateDoubleTimeSeries[] { TS_3, ts };
    final DoubleTimeSeries<?>[] out = TimeSeriesIntersector.intersect(in);
    assertSame(in, out);
    assertEquals(out.length, 2);
    assertEquals(out[0].size(), 0);
    assertEquals(out[1].size(), 0);
  }

  /**
   * Tests intersection.
   */
  @Test
  public void testIntersection() {
    LocalDateDoubleTimeSeries[] in = new LocalDateDoubleTimeSeries[] { TS_1, TS_2, TS_3, TS_4};
    DoubleTimeSeries<?>[] out = TimeSeriesIntersector.intersect(in);
    assertSame(in, out);
    assertEquals(out.length, 4);
    assertEquals(out[0].size(), 1);
    assertEquals(out[0].getEarliestTime(), TS_1.getEarliestTime());
    assertEquals(out[1].getEarliestTime(), TS_2.getEarliestTime());
    assertEquals(out[2].getEarliestTime(), TS_3.getEarliestTime());
    assertEquals(out[3].getEarliestTime(), TS_4.getEarliestTime());
    assertEquals(out[0].getEarliestValue(), TS_1.getEarliestValue());
    assertEquals(out[1].getEarliestValue(), TS_2.getEarliestValue());
    assertEquals(out[2].getEarliestValue(), TS_3.getEarliestValue());
    assertEquals(out[3].getEarliestValue(), TS_4.getEarliestValue());
    in = new LocalDateDoubleTimeSeries[] { TS_4, TS_3, TS_2, TS_1};
    out = TimeSeriesIntersector.intersect(in);
    assertSame(in, out);
    assertEquals(out.length, 4);
    assertEquals(out[0].size(), 1);
    assertEquals(out[0].getEarliestTime(), TS_4.getEarliestTime());
    assertEquals(out[1].getEarliestTime(), TS_3.getEarliestTime());
    assertEquals(out[2].getEarliestTime(), TS_2.getEarliestTime());
    assertEquals(out[3].getEarliestTime(), TS_1.getEarliestTime());
    assertEquals(out[0].getEarliestValue(), TS_4.getEarliestValue());
    assertEquals(out[1].getEarliestValue(), TS_3.getEarliestValue());
    assertEquals(out[2].getEarliestValue(), TS_2.getEarliestValue());
    assertEquals(out[3].getEarliestValue(), TS_1.getEarliestValue());
  }
}
