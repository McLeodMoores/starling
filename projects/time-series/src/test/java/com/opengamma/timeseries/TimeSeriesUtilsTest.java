/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Unit tests for {@link TimeSeriesUtils}.
 */
public class TimeSeriesUtilsTest {

  /**
   * Tests the exception when the input array is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testToIntPrimitiveWithNullArray() {
    TimeSeriesUtils.toPrimitive((Integer[]) null);
  }

  /**
   * Tests the result when the input is empty.
   */
  @Test
  public void testToIntPrimitiveEmpty() {
    final Object out = TimeSeriesUtils.toPrimitive(new Integer[0]);
    assertTrue(out instanceof int[]);
    assertEquals(((int[]) out).length, 0);
  }

  /**
   * Tests the result when the array has a null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testToIntPrimitiveWithNull() {
    TimeSeriesUtils.toPrimitive(new Integer[] {1, null, 2});
  }

  /**
   * Tests conversion of an array.
   */
  @Test
  public void testToIntPrimitive() {
    final int n = 100;
    final Integer[] in = new Integer[n];
    final int[] out = new int[n];
    for (int i = 0; i < n; i++) {
      in[i] = i;
      out[i] = i;
    }
    final Object result = TimeSeriesUtils.toPrimitive(in);
    assertTrue(result instanceof int[]);
    assertArrayEquals((int[]) result, out);
  }

  /**
   * Tests the exception when the input array is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testToLongPrimitiveWithNullArray() {
    TimeSeriesUtils.toPrimitive((Long[]) null);
  }

  /**
   * Tests the result when the input is empty.
   */
  @Test
  public void testToLongPrimitiveEmpty() {
    final Object out = TimeSeriesUtils.toPrimitive(new Long[0]);
    assertTrue(out instanceof long[]);
    assertEquals(((long[]) out).length, 0);
  }

  /**
   * Tests the result when the array has a null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testToLongPrimitiveWithNull() {
    TimeSeriesUtils.toPrimitive(new Long[] {1L, null, 2L});
  }

  /**
   * Tests conversion of an array.
   */
  @Test
  public void testToLongPrimitive() {
    final int n = 100;
    final Long[] in = new Long[n];
    final long[] out = new long[n];
    for (int i = 0; i < n; i++) {
      in[i] = (long) i;
      out[i] = i;
    }
    final Object result = TimeSeriesUtils.toPrimitive(in);
    assertTrue(result instanceof long[]);
    assertArrayEquals((long[]) result, out);
  }

  /**
   * Tests the exception when the input array is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testToDoublePrimitiveWithNullArray() {
    TimeSeriesUtils.toPrimitive((Double[]) null);
  }

  /**
   * Tests the result when the input is empty.
   */
  @Test
  public void testToDoublePrimitiveEmpty() {
    final Object out = TimeSeriesUtils.toPrimitive(new Double[0]);
    assertTrue(out instanceof double[]);
    assertEquals(((double[]) out).length, 0);
  }

  /**
   * Tests the result when the array has a null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testToDoublePrimitiveWithNull() {
    TimeSeriesUtils.toPrimitive(new Double[] {1., null, 2.});
  }

  /**
   * Tests conversion of an array.
   */
  @Test
  public void testToDoublePrimitive() {
    final int n = 100;
    final Double[] in = new Double[n];
    final double[] out = new double[n];
    for (int i = 0; i < n; i++) {
      in[i] = (double) i;
      out[i] = i;
    }
    final Object result = TimeSeriesUtils.toPrimitive(in);
    assertTrue(result instanceof double[]);
    assertArrayEquals((double[]) result, out, 1e-15);
  }

  /**
   * Tests the exception when the input array is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testToIntObjectWithNullArray() {
    TimeSeriesUtils.toObject((int[]) null);
  }

  /**
   * Tests the result when the input is empty.
   */
  @Test
  public void testToIntObjectEmpty() {
    final Object out = TimeSeriesUtils.toObject(new int[0]);
    assertTrue(out instanceof Integer[]);
    assertEquals(((Integer[]) out).length, 0);
  }

  /**
   * Tests conversion of an array.
   */
  @Test
  public void testToIntObject() {
    final int n = 100;
    final int[] in = new int[n];
    final Integer[] out = new Integer[n];
    for (int i = 0; i < n; i++) {
      out[i] = i;
      in[i] = i;
    }
    final Object result = TimeSeriesUtils.toObject(in);
    assertTrue(result instanceof Integer[]);
    assertArrayEquals((Integer[]) result, out);
  }

  /**
   * Tests the exception when the input array is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testToLongObjectWithNullArray() {
    TimeSeriesUtils.toObject((long[]) null);
  }

  /**
   * Tests the result when the input is empty.
   */
  @Test
  public void testToLongObjecteEmpty() {
    final Object out = TimeSeriesUtils.toObject(new long[0]);
    assertTrue(out instanceof Long[]);
    assertEquals(((Long[]) out).length, 0);
  }

  /**
   * Tests conversion of an array.
   */
  @Test
  public void testToLongObject() {
    final int n = 100;
    final long[] in = new long[n];
    final Long[] out = new Long[n];
    for (int i = 0; i < n; i++) {
      in[i] = i;
      out[i] = (long) i;
    }
    final Object result = TimeSeriesUtils.toObject(in);
    assertTrue(result instanceof Long[]);
    assertArrayEquals((Long[]) result, out);
  }

  /**
   * Tests the exception when the input array is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testToDoubleObjectWithNullArray() {
    TimeSeriesUtils.toObject((double[]) null);
  }

  /**
   * Tests the result when the input is empty.
   */
  @Test
  public void testToDoubleObjectEmpty() {
    final Object out = TimeSeriesUtils.toObject(new double[0]);
    assertTrue(out instanceof Double[]);
    assertEquals(((Double[]) out).length, 0);
  }

  /**
   * Tests conversion of an array.
   */
  @Test
  public void testToDoubleObject() {
    final int n = 100;
    final double[] in = new double[n];
    final Double[] out = new Double[n];
    for (int i = 0; i < n; i++) {
      in[i] = i;
      out[i] = (double) i;
    }
    final Object result = TimeSeriesUtils.toObject(in);
    assertTrue(result instanceof Double[]);
    assertArrayEquals((Double[]) result, out);
  }

  /**
   * Tests close equals.
   */
  @Test
  public void testCloseEquals() {
    final double a = 1e-15;
    final double b = 3e-15;
    final double c = 1.2e-15;
    final double d = 2e-15;
    assertFalse(TimeSeriesUtils.closeEquals(a, b));
    assertTrue(TimeSeriesUtils.closeEquals(a, c));
    assertFalse(TimeSeriesUtils.closeEquals(a, d));
    assertFalse(TimeSeriesUtils.closeEquals(-a, -b));
    assertTrue(TimeSeriesUtils.closeEquals(-a, -c));
    assertFalse(TimeSeriesUtils.closeEquals(-a, -d));
    assertTrue(TimeSeriesUtils.closeEquals(a / 0, b / 0));
    assertFalse(TimeSeriesUtils.closeEquals(a / 0, a));
    assertFalse(TimeSeriesUtils.closeEquals(a / 0, -b / 0));
    assertTrue(TimeSeriesUtils.closeEquals(-a / 0, -b / 0));
  }

  /**
   * Tests conversion to a string.
   */
  @Test
  public void testToString() {
    final LocalDate[] dates = new LocalDate[] { LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 4), LocalDate.of(2018, 1, 7) };
    final double[] values = new double[] { 3.4, 2.3, 6.4 };
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    assertEquals(TimeSeriesUtils.toString(ts), "ImmutableLocalDateDoubleTimeSeries[(2018-01-01, 3.4), (2018-01-04, 2.3), (2018-01-07, 6.4)]");
  }

  /**
   * Test input checkers.
   */
  @Test
  public void testIsTrue() {
    TimeSeriesUtils.isTrue(true, "message");
  }

  /**
   * Test input checkers.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsNotTrue() {
    TimeSeriesUtils.isTrue(false, "message");
  }

  /**
   * Test input checkers.
   */
  @Test
  public void testIsNotNull() {
    TimeSeriesUtils.notNull(new Object(), "message");
  }

  /**
   * Test input checkers.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsNull() {
    TimeSeriesUtils.notNull(null, "message");
  }
}
