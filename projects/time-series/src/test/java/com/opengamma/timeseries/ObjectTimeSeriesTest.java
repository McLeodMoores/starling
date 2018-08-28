/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;

/**
 * Abstract test class for {@code ObjectTimeSeries}.
 *
 * @param <T>  the time type
 * @param <V>  the value type
 */
@Test(groups = "unit")
public abstract class ObjectTimeSeriesTest<T, V> {

  /**
   * Creates an empty time series.
   *
   * @return  an empty time series
   */
  protected abstract ObjectTimeSeries<T, V> createEmptyTimeSeries();

  /**
   * Creates a time series.
   *
   * @param times  the times
   * @param values  the values
   * @return  a time series
   */
  protected abstract ObjectTimeSeries<T, V> createTimeSeries(T[] times, V[] values);

  /**
   * Creates a time series.
   *
   * @param times  the times
   * @param values  the values
   * @return  a time series
   */
  protected abstract ObjectTimeSeries<T, V> createTimeSeries(List<T> times, List<V> values);

  /**
   * Creates a time series.
   *
   * @param dts  the original time series
   * @return  a time series
   */
  protected abstract ObjectTimeSeries<T, V> createTimeSeries(ObjectTimeSeries<T, V> dts);

  /**
   * Creates an empty array of times of the appropriate type.
   *
   * @return   an empty array
   */
  protected abstract T[] emptyTimes();

  /**
   * Creates an array of times of the appropriate type.
   *
   * @return  an array of times
   */
  protected abstract T[] testTimes();

  /**
   * Creates an array of times of the appropriate type. Should be different from the array returned
   * from {@link #testTimes}.
   *
   * @return  an array of times
   */
  protected abstract T[] testTimes2();

  /**
   * Creates an empty array of values of the appropriate type.
   *
   * @return  an empty array
   */
  protected abstract V[] emptyValues();

  /**
   * Creates an array of values of the appropriate type.
   *
   * @return  an array of values
   */
  protected abstract V[] testValues();

  /**
   * Tests the array constructor.
   */
  @Test
  public void testArrayConstructor() {
    ObjectTimeSeries<T, V> dts = createTimeSeries(emptyTimes(), emptyValues());
    assertEquals(0, dts.size());
    final T[] times = testTimes();
    final V[] values = testValues();
    dts = createTimeSeries(times, values);
    assertEquals(6, dts.size());
    final Iterator<V> valuesIter = dts.valuesIterator();
    for (final V value : values) {
      assertTrue(Objects.equals(value, valuesIter.next()));
    }
  }

  /**
   * Tests the list constructor.
   */
  @Test
  public void testListConstructor() {
    ObjectTimeSeries<T, V> dts = createTimeSeries(new ArrayList<T>(), new ArrayList<V>());
    assertEquals(0, dts.size());
    final T[] times = testTimes();
    final V[] values = testValues();
    final List<T> timesList = new ArrayList<>();
    final List<V> valuesList = new ArrayList<>();
    for (int i = 0; i < times.length; i++) {
      timesList.add(times[i]);
      valuesList.add(values[i]);
    }
    dts = createTimeSeries(timesList, valuesList);
    assertEquals(6, dts.size());
    final Iterator<V> valuesIter = dts.valuesIterator();
    for (int i = 0; i < 6; i++) {
      assertTrue(Objects.equals(values[i], valuesIter.next()));
    }
  }

  /**
   * Tests the time series constructor.
   */
  @Test
  public void testTimeSeriesConstructor() {
    ObjectTimeSeries<T, V> dts = createEmptyTimeSeries();
    ObjectTimeSeries<T, V> dts2 = createTimeSeries(dts);
    assertEquals(0, dts2.size());
    final T[] times = testTimes();
    final V[] values = testValues();
    dts = createTimeSeries(times, values);
    dts2 = createTimeSeries(dts);
    assertEquals(6, dts2.size());
    final Iterator<V> valuesIter = dts2.valuesIterator();
    for (int i = 0; i < 6; i++) {
      assertTrue(Objects.equals(values[i], valuesIter.next()));
    }
  }

  /**
   * Creates a standard time series i.e. one that will not throw an exception on creation.
   *
   * @return  a time series
   */
  protected ObjectTimeSeries<T, V> createStandardTimeSeries() {
    final T[] times = testTimes();
    final V[] values = testValues();
    return createTimeSeries(times, values);
  }

  /**
   * Creates a standard time series i.e. one that will not throw an exception on creation.
   * Should be different from the series returned from {@link #createStandardTimeSeries()}.
   *
   * @return  a time series
   */
  protected ObjectTimeSeries<T, V> createStandardTimeSeries2() {
    final T[] times = testTimes2();
    final V[] values = testValues();
    return createTimeSeries(times, values);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the head() method.
   */
  public void testHead() {
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final ObjectTimeSeries<T, V> head5 = dts.head(5);
    final Iterator<Entry<T, V>> iterator = head5.iterator();
    for (int i = 0; i < 5; i++) {
      final Entry<T, V> entry = iterator.next();
      assertEquals(testTimes()[i], entry.getKey());
      assertEquals(testValues()[i], entry.getValue());
    }
    assertEquals(dts.head(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().head(0), createEmptyTimeSeries());
  }

  /**
   * Tests the tail() method.
   */
  @Test
  public void testTail() {
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final ObjectTimeSeries<T, V> tail5 = dts.tail(5);
    final Iterator<Entry<T, V>> iterator = tail5.iterator();
    for (int i = 1; i < 6; i++) {
      final Entry<T, V> entry = iterator.next();
      assertEquals(testTimes()[i], entry.getKey());
      assertEquals(testValues()[i], entry.getValue());
    }
    assertEquals(dts.tail(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().tail(0), createEmptyTimeSeries());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the size() method.
   */
  @Test
  public void testSize() {
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    assertEquals(6, dts.size());
    final ObjectTimeSeries<T, V> emptyTS = createEmptyTimeSeries();
    assertEquals(0, emptyTS.size());
  }

  /**
   * Tests the isEmpty() method.
   */
  @Test
  public void testIsEmpty() {
    final ObjectTimeSeries<T, V> empty = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    assertTrue(empty.isEmpty());
    assertFalse(dts.isEmpty());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the containsTime() method.
   */
  @Test
  public void testContainsTime() {
    final ObjectTimeSeries<T, V> emptyTS = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final T[] testDates = testTimes();
    for (int i = 0; i < 6; i++) {
      assertEquals(true, dts.containsTime(testDates[i]));
      assertEquals(false, emptyTS.containsTime(testDates[i]));
    }
  }

  /**
   * Tests the getValue() method.
   */
  @Test
  public void testGetValue() {
    final ObjectTimeSeries<T, V> emptyTS = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final T[] testDates = testTimes();
    final V[] values = testValues();
    for (int i = 0; i < 6; i++) {
      assertEquals(values[i], dts.getValue(testDates[i]));
      assertEquals(null, emptyTS.getValue(testDates[i]));
    }
  }

  /**
   * Tests the getTimeAtIndex() method.
   */
  @Test
  public void testGetTimeAtIndex() {
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final T[] testDates = testTimes();
    for (int i = 0; i < 6; i++) {
      final T val = dts.getTimeAtIndex(i);
      assertEquals(testDates[i], val);
    }
    try {
      dts.getTimeAtIndex(-1);
      fail();
    } catch (final IndexOutOfBoundsException ex) {
      // expected
    }
    try {
      dts.getTimeAtIndex(6);
      fail();
    } catch (final IndexOutOfBoundsException ex) {
      // expected
    }
  }

  /**
   * Tests the getValueAtIndex() method.
   */
  @Test
  public void testGetValueAtIndex() {
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final V[] values = testValues();
    for (int i = 0; i < 6; i++) {
      assertEquals(values[i], dts.getValueAtIndex(i));
    }
    try {
      dts.getValueAtIndex(-1);
      fail();
    } catch (final IndexOutOfBoundsException ex) {
      // expected
    }
    try {
      dts.getValueAtIndex(6);
      fail();
    } catch (final IndexOutOfBoundsException ex) {
      // expected
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the getLatestTime() method.
   */
  @Test
  public void testGetLatestTime() {
    final ObjectTimeSeries<T, V> empty = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final T[] testDates = testTimes();
    assertEquals(testDates[5], dts.getLatestTime());
    try {
      empty.getLatestTime();
      fail();
    } catch (final NoSuchElementException ex) {
      // expected
    }
  }

  /**
   * Tests the getLatestValue() method.
   */
  @Test
  public void testGetLatestValue() {
    final ObjectTimeSeries<T, V> empty = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final V[] values = testValues();
    assertEquals(values[values.length - 1], dts.getLatestValue());
    try {
      empty.getLatestValue();
      fail();
    } catch (final NoSuchElementException ex) {
      // expected
    }
  }

  /**
   * Tests the getEarliestTime() method.
   */
  @Test
  public void testGetEarliestTime() {
    final ObjectTimeSeries<T, V> empty = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final T[] testDates = testTimes();
    assertEquals(testDates[0], dts.getEarliestTime());
    try {
      empty.getEarliestTime();
      fail();
    } catch (final NoSuchElementException ex) {
      // expected
    }
  }

  /**
   * Tests the getEarliestValue() method.
   */
  @Test
  public void testGetEarliestValue() {
    final ObjectTimeSeries<T, V> empty = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final V[] values = testValues();
    assertEquals(values[0], dts.getEarliestValue());
    try {
      empty.getEarliestValue();
      fail();
    } catch (final NoSuchElementException ex) {
      // expected
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the timesIterator() method.
   */
  @Test
  public void testTimesIterator() {
    final Iterator<T> emptyTimesIter = createEmptyTimeSeries().timesIterator();
    final Iterator<T> dtsTimesIter = createStandardTimeSeries().timesIterator();
    final T[] testDates = testTimes();
    for (int i = 0; i < 6; i++) {
      assertTrue(dtsTimesIter.hasNext());
      final T time = dtsTimesIter.next();
      assertEquals(testDates[i], time);
    }
    try {
      dtsTimesIter.next();
    } catch (final NoSuchElementException nsee) {
      assertFalse(emptyTimesIter.hasNext());
      try {
        emptyTimesIter.next();
        fail();
      } catch (final NoSuchElementException nsuchee) {
        // expected
      }
    }
  }

  /**
   * Tests the valuesIterator() method.
   */
  @Test
  public void testValuesIterator() {
    final Iterator<V> emptyValuesIter = createEmptyTimeSeries().valuesIterator();
    final Iterator<V> dtsValuesIter = createStandardTimeSeries().valuesIterator();
    final V[] values = testValues();
    for (int i = 0; i < 6; i++) {
      assertTrue(dtsValuesIter.hasNext());
      final V val = dtsValuesIter.next();
      assertEquals(values[i], val);
    }
    try {
      dtsValuesIter.next();
    } catch (final NoSuchElementException nsee) {
      assertFalse(emptyValuesIter.hasNext());
      try {
        emptyValuesIter.next();
        fail();
      } catch (final NoSuchElementException nsuchee) {
        // expected
      }
    }
  }

  /**
   * Tests the iterator.
   */
  @Test
  public void testIterator() {
    final Iterator<Entry<T, V>> emptyIter = createEmptyTimeSeries().iterator();
    final Iterator<Entry<T, V>> dtsIter = createStandardTimeSeries().iterator();
    final T[] testDates = testTimes();
    final V[] testValues = testValues();
    for (int i = 0; i < 6; i++) {
      assertTrue(dtsIter.hasNext());
      final Entry<T, V> entry = dtsIter.next();
      final T time = entry.getKey();
      assertEquals(entry.getValue(), testValues[i]);
      assertEquals(testDates[i], time);
    }
    try {
      dtsIter.next();
    } catch (final NoSuchElementException nsee) {
      assertFalse(emptyIter.hasNext());
      try {
        emptyIter.next();
        fail();
      } catch (final NoSuchElementException nsuchee) {
        // expected
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a set of inputs for sub-series tests.
   *
   * @return  an array of (startIndex, startIndexInclusive, endIndex, endIndexInclusive, expectedSize, expectedFirstIndex} arrays
   */
  @DataProvider(name = "subSeries")
  static Object[][] dataSubSeries() {
    return new Object[][] {
        {0, true, 4, false, 4, 0},
        {0, true, 4, true, 5, 0},
        {0, false, 4, false, 3, 1},
        {0, false, 4, true, 4, 1},
        {4, true, 5, false, 1, 4},
        {4, true, 5, true, 2, 4},
        {4, false, 5, false, 0, -1},
        {4, false, 5, true, 1, 5},
        {4, true, 4, false, 0, -1},
        {4, true, 4, true, 1, 4},
        {4, false, 4, false, 0, -1 },  // matches TreeMap definition
        {4, false, 4, true, 0, -1 },
    };
  }

  /**
   * Tests the sub-series method.
   *
   * @param startIndex  the start index
   * @param startInclude  true to include the start index
   * @param endIndex  the end index
   * @param endInclude  true to include the end index
   * @param expectedSize  the expected size of the resulting time series
   * @param expectedFirstIndex  the expected first index of the resulting time series compared to the original
   */
  @Test(dataProvider = "subSeries", dataProviderClass = ObjectTimeSeriesTest.class)
  public void testSubSeriesInstantProviderInstantProvider(final int startIndex, final boolean startInclude,
      final int endIndex, final boolean endInclude, final int expectedSize, final int expectedFirstIndex) {
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final T[] testDates = testTimes();
    final V[] testValues = testValues();
    final ObjectTimeSeries<T, V> threeToFive = dts.subSeries(testDates[3], testDates[5]);
    assertEquals(2, threeToFive.size());
    final Iterator<Entry<T, V>> iterator = threeToFive.iterator();
    for (int i = 3; i < 5; i++) {
      final Entry<T, V> item = iterator.next();
      assertEquals(testDates[i], item.getKey());
      assertEquals(testValues[i], item.getValue());
    }
    ObjectTimeSeries<T, V> sub = dts.subSeries(testDates[startIndex], startInclude, testDates[endIndex], endInclude);
    assertEquals(expectedSize, sub.size());
    if (expectedFirstIndex >= 0) {
      assertEquals(testDates[expectedFirstIndex], sub.getTimeAtIndex(0));
    }
    if (startInclude && !endInclude) {
      sub = dts.subSeries(testDates[startIndex], testDates[endIndex]);
      assertEquals(expectedSize, sub.size());
      if (expectedFirstIndex >= 0) {
        assertEquals(testDates[expectedFirstIndex], sub.getTimeAtIndex(0));
      }
    }
  }

  /**
   * Tests the hashCode() method.
   */
  public void testHashCode() {
    assertEquals(createStandardTimeSeries().hashCode(), createStandardTimeSeries().hashCode());
    assertEquals(createEmptyTimeSeries().hashCode(), createEmptyTimeSeries().hashCode());
  }

  /**
   * Tests the equals() method.
   */
  public void testEquals() {
    assertEquals(createStandardTimeSeries(), createStandardTimeSeries());
    assertFalse(createStandardTimeSeries().equals(createEmptyTimeSeries()));
    assertFalse(createEmptyTimeSeries().equals(createStandardTimeSeries()));
    assertEquals(createEmptyTimeSeries(), createEmptyTimeSeries());
  }

  /**
   * Tests the lag operator.
   */
  @Test
  public void testLag() {
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    ObjectTimeSeries<T, V> lagged = dts.lag(0);
    assertOperationSuccessful(lagged, new Float[] {1F, 2F, 3F, 4F, 5F, 6F });
    assertEquals(lagged.getEarliestTime(), testTimes()[0]);
    assertEquals(lagged.getLatestTime(), testTimes()[5]);
    assertEquals(dts, lagged);
    lagged = dts.lag(1);
    assertOperationSuccessful(lagged, new Float[] { 1F, 2F, 3F, 4F, 5F });
    assertEquals(lagged.getEarliestTime(), testTimes()[1]);
    assertEquals(lagged.getLatestTime(), testTimes()[5]);
    lagged = dts.lag(-1);
    assertOperationSuccessful(lagged, new Float[] { 2F, 3F, 4F, 5F, 6F });
    assertEquals(lagged.getEarliestTime(), testTimes()[0]);
    assertEquals(lagged.getLatestTime(), testTimes()[4]);
    lagged = dts.lag(5);
    assertOperationSuccessful(lagged, new Float[] { 1F });
    lagged = dts.lag(-5);
    assertOperationSuccessful(lagged, new Float[] { 6F });
    lagged = dts.lag(6);
    assertOperationSuccessful(lagged, new Float[0]);
    lagged = dts.lag(-6);
    assertOperationSuccessful(lagged, new Float[0]);
    lagged = dts.lag(1000);
    assertOperationSuccessful(lagged, new Float[0]);
    lagged = dts.lag(-1000);
    assertOperationSuccessful(lagged, new Float[0]);
  }

  /**
   * Checks that the values of a time series are the same as those expected. If the values are Numbers,
   * they are converted to doubles before testing. Otherwise, the objects are compared.
   *
   * @param result  the result time series
   * @param expected  the expected values
   */
  protected static void assertOperationSuccessful(final ObjectTimeSeries<?, ?> result, final Object[] expected) {
    assertEquals(expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      final Object e = expected[i];
      final Object actual = result.getValueAtIndex(i);
      if (Number.class.isInstance(e) && Number.class.isInstance(actual)) {
        assertEquals(((Number) e).doubleValue(), ((Number) result.getValueAtIndex(i)).doubleValue(), 0.001);
      } else {
        assertEquals(e, result.getValueAtIndex(i));
      }
    }
  }

  /** Test binary operator instance. */
  protected static final BinaryOperator<Float> BIN = new TestBinaryOperator();
  /** Test unary operator instance. */
  protected static final UnaryOperator<Float> UN = new TestUnaryOperator();

  /**
   * Test binary operator.
   */
  protected static class TestBinaryOperator implements BinaryOperator<Float> {

    @Override
    public Float operate(final Float a, final Float b) {
      return a + b;
    }
  }

  /**
   * Test unary operator.
   */
  protected static class TestUnaryOperator implements UnaryOperator<Float> {

    @Override
    public Float operate(final Float a) {
      return a * 2;
    }

  }
}
