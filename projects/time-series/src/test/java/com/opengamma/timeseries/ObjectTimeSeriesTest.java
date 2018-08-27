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

/**
 * Abstract test class for {@code ObjectTimeSeries}.
 *
 * @param <T>  the time type
 * @param <V>  the value type
 */
@Test(groups = "unit")
public abstract class ObjectTimeSeriesTest<T, V> {

  protected abstract ObjectTimeSeries<T, V> createEmptyTimeSeries();

  protected abstract ObjectTimeSeries<T, V> createTimeSeries(T[] times, V[] values);

  protected abstract ObjectTimeSeries<T, V> createTimeSeries(List<T> times, List<V> values);

  protected abstract ObjectTimeSeries<T, V> createTimeSeries(ObjectTimeSeries<T, V> dts);

  protected abstract T[] emptyTimes();

  protected abstract T[] testTimes();

  protected abstract T[] testTimes2();

  protected abstract V[] emptyValues();

  protected abstract V[] testValues();

  public void test_arrayConstructor() {
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

  public void test_listConstructor() {
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

  public void test_timeSeriesConstructor() {
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

  protected ObjectTimeSeries<T, V> createStandardTimeSeries() {
    final T[] times = testTimes();
    final V[] values = testValues();
    return createTimeSeries(times, values);
  }

  protected ObjectTimeSeries<T, V> createStandardTimeSeries2() {
    final T[] times = testTimes2();
    final V[] values = testValues();
    return createTimeSeries(times, values);
  }

  //-------------------------------------------------------------------------
  public void test_head() {
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

  public void test_tail() {
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
  public void test_size() {
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    assertEquals(6, dts.size());
    final ObjectTimeSeries<T, V> emptyTS = createEmptyTimeSeries();
    assertEquals(0, emptyTS.size());
  }

  public void test_isEmpty() {
    final ObjectTimeSeries<T, V> empty = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    assertTrue(empty.isEmpty());
    assertFalse(dts.isEmpty());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_containsTime() {
    final ObjectTimeSeries<T, V> emptyTS = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final T[] testDates = testTimes();
    for (int i = 0; i < 6; i++) {
      assertEquals(true, dts.containsTime(testDates[i]));
      assertEquals(false, emptyTS.containsTime(testDates[i]));
    }
  }

  @Test
  public void test_getValue() {
    final ObjectTimeSeries<T, V> emptyTS = createEmptyTimeSeries();
    final ObjectTimeSeries<T, V> dts = createStandardTimeSeries();
    final T[] testDates = testTimes();
    final V[] values = testValues();
    for (int i = 0; i < 6; i++) {
      assertEquals(values[i], dts.getValue(testDates[i]));
      assertEquals(null, emptyTS.getValue(testDates[i]));
    }
  }

  @Test
  public void test_getTimeAtIndex() {
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

  @Test
  public void test_getValueAtIndex() {
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
  public void test_getLatestTime() {
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

  public void test_getLatestValue() {
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

  public void test_getEarliestTime() {
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

  public void test_getEarliestValue() {
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
  public void test_timesIterator() {
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

  public void test_valuesIterator() {
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

  public void test_iterator() {
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
  @DataProvider(name = "subSeries")
  static Object[][] data_subSeries() {
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

  @SuppressWarnings("cast")
  @Test(dataProvider = "subSeries", dataProviderClass = ObjectTimeSeriesTest.class)
  public void test_subSeriesInstantProviderInstantProvider(final int startIndex, final boolean startInclude, final int endIndex, final boolean endInclude, final int expectedSize, final int expectedFirstIndex) {
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
    if (startInclude && endInclude == false) {
      sub = dts.subSeries(testDates[startIndex], testDates[endIndex]);
      assertEquals(expectedSize, sub.size());
      if (expectedFirstIndex >= 0) {
        assertEquals(testDates[expectedFirstIndex], sub.getTimeAtIndex(0));
      }
    }
  }

  public void test_hashCode() {
    assertEquals(createStandardTimeSeries().hashCode(), createStandardTimeSeries().hashCode());
    assertEquals(createEmptyTimeSeries().hashCode(), createEmptyTimeSeries().hashCode());
  }

  public void test_equals() {
    assertEquals(createStandardTimeSeries(), createStandardTimeSeries());
    assertFalse(createStandardTimeSeries().equals(createEmptyTimeSeries()));
    assertFalse(createEmptyTimeSeries().equals(createStandardTimeSeries()));
    assertEquals(createEmptyTimeSeries(), createEmptyTimeSeries());
  }

}
