/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * Test {@link SimpleMapTimeSeries}.
 */
@Test(groups = "unit")
public class SimpleMapTimeSeriesTest {

  private static final LocalDate DATE1 = LocalDate.of(2011, 6, 1);
  private static final LocalDate DATE2 = LocalDate.of(2011, 6, 2);
  private static final LocalDate DATE3 = LocalDate.of(2011, 6, 3);
  private static final LocalDate DATE4 = LocalDate.of(2011, 6, 4);

  /**
   * Tests construction with a null date type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateType() {
    new SimpleMapTimeSeries<>(null, Double.TYPE);
  }

  /**
   * Tests construction with a null value type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValueType() {
    new SimpleMapTimeSeries<>(LocalDate.class, null);
  }

  /**
   * Tests construction of an empty time series.
   */
  @Test
  public void testConstructorArraysEmptyTypes() {
    final SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<>(LocalDate.class, String.class);
    assertEquals(0, test.size());
    assertEquals(true, test.isEmpty());
    assertEquals(false, test.iterator().hasNext());
    assertEquals(false, test.timesIterator().hasNext());
    assertEquals(false, test.valuesIterator().hasNext());
    assertEquals(0, test.times().size());
    assertEquals(0, test.timesArray().length);
    assertEquals(0, test.values().size());
    assertEquals(0, test.valuesArray().length);
  }

  /**
   * Tests construction with a null date array.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateArray() {
    new SimpleMapTimeSeries<>(null, new Double[] { 1., 2., 3. });
  }

  /**
   * Tests construction with a null value array.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValueArray() {
    new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2 }, null);
  }

  /**
   * Tests construction with different length arrays.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentLengthArrays() {
    new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2 }, new Double[] { 1., 2., 3., 4. });
  }

  /**
   * Tests construction with an empty array.
   */
  @Test
  public void testConstructorArraysEmpty() {
    final SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<>(new LocalDate[0], new String[0]);
    assertEquals(0, test.size());
    assertEquals(true, test.isEmpty());
    assertEquals(false, test.iterator().hasNext());
    assertEquals(false, test.timesIterator().hasNext());
    assertEquals(false, test.valuesIterator().hasNext());
    assertEquals(0, test.times().size());
    assertEquals(0, test.timesArray().length);
    assertEquals(0, test.values().size());
    assertEquals(0, test.valuesArray().length);
  }

  /**
   * Tests construction from arrays.
   */
  @Test
  public void testConstructorArraysElements() {
    final SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<>(
        new LocalDate[] {DATE1, DATE2 }, new String[] {"A", "B" });
    assertEquals(2, test.size());
    assertEquals(false, test.isEmpty());
    assertEquals(true, test.iterator().hasNext());
    assertEquals(true, test.timesIterator().hasNext());
    assertEquals(true, test.valuesIterator().hasNext());
    assertEquals(2, test.times().size());
    assertEquals(DATE1, test.times().get(0));
    assertEquals(DATE2, test.times().get(1));
    assertEquals(2, test.timesArray().length);
    assertEquals(DATE1, test.timesArray()[0]);
    assertEquals(DATE2, test.timesArray()[1]);
    assertEquals(2, test.values().size());
    assertEquals(2, test.valuesArray().length);
    assertTrue(test.containsTime(DATE1));
    assertFalse(test.containsTime(LocalDate.MIN));
    assertEquals(test.getValue(DATE1), "A");
    assertEquals(test.getValue(DATE2), "B");
    assertEquals(test.getEarliestTime(), DATE1);
    assertEquals(test.getEarliestValue(), "A");
    assertEquals(test.getLatestTime(), DATE2);
    assertEquals(test.getLatestValue(), "B");
  }

  /**
   * Tests the lag operation.
   */
  @Test
  public void testLag() {
    final SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<>(new LocalDate[] {DATE1, DATE2 }, new String[] {"A", "B" });
    TimeSeries<LocalDate, String> lagged = test.lag(0);
    assertEquals(2, lagged.size());
    assertEquals(DATE1, lagged.getTimeAtIndex(0));
    assertEquals(DATE2, lagged.getTimeAtIndex(1));
    assertEquals("A", lagged.getValueAtIndex(0));
    assertEquals("B", lagged.getValueAtIndex(1));
    lagged = test.lag(1);
    assertEquals(1, lagged.size());
    assertEquals(DATE2, lagged.getTimeAtIndex(0));
    assertEquals("A", lagged.getValueAtIndex(0));
    lagged = test.lag(-1);
    assertEquals(1, lagged.size());
    assertEquals(DATE1, lagged.getTimeAtIndex(0));
    assertEquals("B", lagged.getValueAtIndex(0));
    lagged = test.lag(2);
    assertTrue(lagged.isEmpty());
    lagged = test.lag(-2);
    assertTrue(lagged.isEmpty());
    lagged = test.lag(1000);
    assertTrue(lagged.isEmpty());
    lagged = test.lag(-1000);
    assertTrue(lagged.isEmpty());
  }

  /**
   * Tests the head method.
   */
  @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
  public void testHeadException() {
    final SimpleMapTimeSeries<LocalDate, Double> ts =
        new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2, DATE3, DATE4 }, new Double[] { 1., 2., 3., 4. });
    ts.head(ts.size() + 10);
  }

  /**
   * Tests the tail method.
   */
  @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
  public void testTailException() {
    final SimpleMapTimeSeries<LocalDate, Double> ts =
        new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2, DATE3, DATE4 }, new Double[] { 1., 2., 3., 4. });
    ts.tail(ts.size() + 10);
  }

  /**
   * Tests sub-series generation.
   */
  @Test
  public void testSubSeries() {
    final SimpleMapTimeSeries<LocalDate, Double> ts =
        new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2, DATE3, DATE4 }, new Double[] { 1., 2., 3., 4. });
    assertEquals(ts.subSeries(DATE1, DATE3), new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2 }, new Double[] { 1., 2. }));
    assertEquals(ts.subSeries(DATE1, DATE1), new SimpleMapTimeSeries<>(new LocalDate[0], new Double[0]));
    assertEquals(ts.subSeries(DATE1.minusDays(10), DATE1.minusDays(3)), new SimpleMapTimeSeries<>(new LocalDate[0], new Double[0]));

    assertEquals(ts.subSeries(DATE1, true, DATE3, false), new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2 }, new Double[] { 1., 2. }));
    assertEquals(ts.subSeries(DATE1, true, DATE3, true), new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2, DATE3 }, new Double[] { 1., 2., 3. }));
    assertEquals(ts.subSeries(DATE1, false, DATE3, false), new SimpleMapTimeSeries<>(new LocalDate[] { DATE2 }, new Double[] { 2. }));
    assertEquals(ts.subSeries(DATE1, false, DATE3, true), new SimpleMapTimeSeries<>(new LocalDate[] { DATE2, DATE3 }, new Double[] { 2., 3. }));

    assertEquals(ts.head(2), new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2, DATE3 }, new Double[] { 1., 2., 3. }));
    assertEquals(ts.tail(2), new SimpleMapTimeSeries<>(new LocalDate[] { DATE3, DATE4 }, new Double[] { 3., 4. }));
  }

  /**
   * Tests the new instance method.
   */
  @Test
  public void testNewInstance() {
    final LocalDate[] dates = new LocalDate[] { DATE1, DATE2, DATE3, DATE4 };
    final Double[] values = new Double[] { 1., 2., 3., 4. };
    final SimpleMapTimeSeries<LocalDate, Double> ts = new SimpleMapTimeSeries<>(dates, values);
    assertNotSame(ts.newInstance(dates, values), ts);
    assertEquals(ts.newInstance(dates, values), ts);
  }

  /**
   * Tests Object methods.
   */
  @Test
  public void testObject() {
    final LocalDate[] dates = new LocalDate[] { DATE1, DATE2, DATE3, DATE4 };
    final Double[] values = new Double[] { 1., 2., 3., 4. };
    final SimpleMapTimeSeries<LocalDate, Double> ts = new SimpleMapTimeSeries<>(dates, values);
    SimpleMapTimeSeries<LocalDate, Double> other = new SimpleMapTimeSeries<>(dates, values);
    assertNotEquals(null, ts);
    assertNotEquals(ImmutableLocalDateDoubleTimeSeries.of(dates, values), ts);
    assertEquals(ts, other);
    assertEquals(ts.hashCode(), other.hashCode());
    assertEquals(ts.toString(), TimeSeriesUtils.toString(ts));
    other = new SimpleMapTimeSeries<>(new LocalDate[] { DATE1, DATE2, DATE3, DATE4.plusDays(2) }, values);
    assertNotEquals(other, ts);
    other = new SimpleMapTimeSeries<>(dates, new Double[] { 1., 2., 3., 5. });
    assertNotEquals(other, ts);
  }

}
