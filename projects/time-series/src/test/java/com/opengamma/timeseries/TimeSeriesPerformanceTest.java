/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;


import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * Test to check performance.
 */
@Test(enabled = false)
public class TimeSeriesPerformanceTest {

  private static final int SMALL = 10;
  private static final int BIG = 10_000;

  /**
   * Tests adding small series with the same dates.
   */
  public void testAddSimpleSmall() {
    for (int i = 0; i < 5; i++) {
      addSimple(SMALL, "addSimple-small");
    }
  }

  /**
   * Tests adding large series with the same dates.
   */
  public void testAddSimpleBig() {
    for (int i = 0; i < 5; i++) {
      addSimple(BIG, "addSimple-big");
    }
  }

  /**
   * Creates two series of a given size with the same dates and adds them.
   *
   * @param size  the series size
   * @param name  the name of the test
   */
  void addSimple(final int size, final String name) {
    final int loop = 10_000_000 / size;
    final LocalDate base = LocalDate.now().minusYears(2);
    final LocalDate[] dates = new LocalDate[size];
    final double[] values = new double[size];
    for (int i = 0; i < size; i++) {
      dates[i] = base.plusDays(i);
      values[i] = i * 1.2d;
    }
    DoubleTimeSeries<LocalDate> ts = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    final long start = System.nanoTime();
    for (int j = 0; j < loop; j++) {
      ts = ts.add(1.5d);
    }
    final long end = System.nanoTime();
    final double diff = ((double) end - (double) start) / 1_000_000L;
    System.out.println(diff + " " + ts.getEarliestValue() + " " + name);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests adding small series with different dates.
   */
  public void testAddSeriesSmall() {
    for (int i = 0; i < 5; i++) {
      addSeries(SMALL, "addSeries-small");
    }
  }

  /**
   * Tests adding large series with different dates.
   */
  public void testAddSeriesBig() {
    for (int i = 0; i < 5; i++) {
      addSeries(BIG, "addSeries-big");
    }
  }

  /**
   * Creates two series of a given size with different dates and adds them.
   *
   * @param size  the series size
   * @param name  the name of the test
   */
  void addSeries(final int size, final String name) {
    final int loop = 10_000_000 / size;
    final LocalDate base = LocalDate.now().minusYears(2);
    final LocalDate[] dates1 = new LocalDate[size];
    final double[] values1 = new double[size];
    final LocalDate[] dates2 = new LocalDate[size];
    final double[] values2 = new double[size];
    for (int i = 0; i < size; i++) {
      dates1[i] = base.plusDays(i);
      values1[i] = i * 1.2d;
      dates2[i] = base.plusDays(i);
      values2[i] = i * 1.5d;
    }
    DoubleTimeSeries<LocalDate> ts1 = ImmutableLocalDateDoubleTimeSeries.of(dates1, values1);
    final DoubleTimeSeries<LocalDate> ts2 = ImmutableLocalDateDoubleTimeSeries.of(dates2, values2);
    final long start = System.nanoTime();
    for (int j = 0; j < loop; j++) {
      ts1 = ts1.add(ts2);
    }
    final long end = System.nanoTime();
    final double diff = ((double) end - (double) start) / 1_000_000L;
    System.out.println(diff + " " + ts1.getEarliestValue() + " " + name);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests multiplying small series.
   */
  public void testMultiplySeriesSmall() {
    for (int i = 0; i < 5; i++) {
      multiplySeries(SMALL, "multiplySeries-small");
    }
  }

  /**
   * Tests multiplying large series.
   */
  public void testMultiplySeriesBig() {
    for (int i = 0; i < 5; i++) {
      multiplySeries(BIG, "multiplySeries-big");
    }
  }

  /**
   * Creates two series of a given size and multiplies them.
   *
   * @param size  the series size
   * @param name  the name of the test
   */
  void multiplySeries(final int size, final String name) {
    final int loop = 10_000_000 / size;
    final LocalDate base = LocalDate.now().minusYears(2);
    final LocalDate[] dates1 = new LocalDate[size];
    final double[] values1 = new double[size];
    final LocalDate[] dates2 = new LocalDate[size];
    final double[] values2 = new double[size];
    for (int i = 0; i < size; i++) {
      dates1[i] = base.plusDays(i);
      values1[i] = i * 1.2d;
      dates2[i] = base.plusDays(i);
      values2[i] = i * 1.5d;
    }
    DoubleTimeSeries<LocalDate> ts1 = ImmutableLocalDateDoubleTimeSeries.of(dates1, values1);
    final DoubleTimeSeries<LocalDate> ts2 = ImmutableLocalDateDoubleTimeSeries.of(dates2, values2);
    final long start = System.nanoTime();
    for (int j = 0; j < loop; j++) {
      ts1 = ts1.multiply(ts2);
    }
    final long end = System.nanoTime();
    final double diff = ((double) end - (double) start) / 1_000_000L;
    System.out.println(diff + " " + ts1.getEarliestValue() + " " + name);
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the reciprocal of a small series.
   */
  public void testReciprocalSmall() {
    for (int i = 0; i < 5; i++) {
      multiplySeries(SMALL, "reciprocal-small");
    }
  }

  /**
   * Calculates the reciprocal of a large series.
   */
  public void testReciprocalBig() {
    for (int i = 0; i < 5; i++) {
      multiplySeries(BIG, "reciprocal-big");
    }
  }

  /**
   * Creates two series of a given size and calculates the reciprocal.
   *
   * @param size  the series size
   * @param name  the name of the test
   */
  void reciprocal(final int size, final String name) {
    final int loop = 10_000_000 / size;
    final LocalDate base = LocalDate.now().minusYears(2);
    final LocalDate[] dates = new LocalDate[size];
    final double[] values = new double[size];
    for (int i = 0; i < size; i++) {
      dates[i] = base.plusDays(i);
      values[i] = i * 1.2d;
    }
    DoubleTimeSeries<LocalDate> ts = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    final long start = System.nanoTime();
    for (int j = 0; j < loop; j++) {
      ts = ts.reciprocal();
    }
    final long end = System.nanoTime();
    final double diff = ((double) end - (double) start) / 1_000_000L;
    System.out.println(diff + " " + ts.getEarliestValue() + " " + name);
  }

}
