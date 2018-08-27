/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.opengamma.timeseries.TimeSeriesUtils;

/**
 * Abstract implementation of {@code DateDoubleTimeSeries}.
 *
 * @param <T>  the date type
 */
public abstract class AbstractDateDoubleTimeSeries<T> implements DateDoubleTimeSeries<T> {

  /**
   * Creates an instance.
   */
  public AbstractDateDoubleTimeSeries() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts the specified date to the {@code int} form.
   *
   * @param date  the date to convert, not null
   * @return the {@code int} date
   */
  protected abstract int convertToInt(T date);

  /**
   * Converts the specified date from the {@code int} form.
   *
   * @param date  the {@code int} date to convert
   * @return the date, not null
   */
  protected abstract T convertFromInt(int date);

  /**
   * Creates an array of the correct T type.
   *
   * @param size  the size of the array to create
   * @return the array, not null
   */
  protected abstract T[] createArray(int size);

  //-------------------------------------------------------------------------
  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(final T date) {
    return containsTime(convertToInt(date));
  }

  @Override
  public Double getValue(final T date) {
    return getValue(convertToInt(date));
  }

  @Override
  public T getTimeAtIndex(final int index) {
    return convertFromInt(getTimeAtIndexFast(index));
  }

  @Override
  public Double getValueAtIndex(final int index) {
    return getValueAtIndexFast(index);
  }

  //-------------------------------------------------------------------------
  @Override
  public T getEarliestTime() {
    return convertFromInt(getEarliestTimeFast());
  }

  @Override
  public Double getEarliestValue() {
    return getEarliestValueFast();
  }

  @Override
  public T getLatestTime() {
    return convertFromInt(getLatestTimeFast());
  }

  @Override
  public Double getLatestValue() {
    return getLatestValueFast();
  }

  //-------------------------------------------------------------------------
  @Override
  public Iterator<T> timesIterator() {
    return new Iterator<T>() {
      private int _index = -1;
      @Override
      public boolean hasNext() {
        return _index + 1 < size();
      }
      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements");
        }
        _index++;
        return getTimeAtIndex(_index);
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable");
      }
    };
  }

  @Override
  public List<T> times() {
    return new AbstractList<T>() {
      @Override
      public T get(final int index) {
        return getTimeAtIndex(index);
      }
      @Override
      public int size() {
        return AbstractDateDoubleTimeSeries.this.size();
      }
      @Override
      public Iterator<T> iterator() {
        return timesIterator();
      }
    };
  }

  @Override
  public T[] timesArray() {
    final int[] times = timesArrayFast();
    final T[] result = createArray(times.length);
    for (int i = 0; i < times.length; i++) {
      result[i] = convertFromInt(times[i]);
    }
    return result;
  }

  @Override
  public Iterator<Double> valuesIterator() {
    return new Iterator<Double>() {
      private int _index = -1;
      @Override
      public boolean hasNext() {
        return _index + 1 < size();
      }
      @Override
      public Double next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements");
        }
        _index++;
        return getValueAtIndex(_index);
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable");
      }
    };
  }

  @Override
  public List<Double> values() {
    return new AbstractList<Double>() {
      @Override
      public Double get(final int index) {
        return getValueAtIndex(index);
      }
      @Override
      public int size() {
        return AbstractDateDoubleTimeSeries.this.size();
      }
      @Override
      public Iterator<Double> iterator() {
        return valuesIterator();
      }
    };
  }

  @Override
  public Double[] valuesArray() {
    final double[] times = valuesArrayFast();
    final Double[] result = new Double[times.length];
    for (int i = 0; i < times.length; i++) {
      result[i] = times[i];
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof DateDoubleTimeSeries) {
      final DateDoubleTimeSeries<?> other = (DateDoubleTimeSeries<?>) obj;
      return Arrays.equals(timesArrayFast(), other.timesArrayFast())
             && Arrays.equals(valuesArrayFast(), other.valuesArrayFast());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(timesArrayFast()) ^ Arrays.hashCode(valuesArrayFast());
  }

  @Override
  public String toString() {
    return TimeSeriesUtils.toString(this);
  }

}
