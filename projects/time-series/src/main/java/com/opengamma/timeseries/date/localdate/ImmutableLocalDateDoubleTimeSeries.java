/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;

/**
 * Standard immutable implementation of {@code LocalDateDoubleTimeSeries}.
 */
public final class ImmutableLocalDateDoubleTimeSeries
    extends AbstractLocalDateDoubleTimeSeries
    implements Serializable {

  /** Empty instance. */
  public static final ImmutableLocalDateDoubleTimeSeries EMPTY_SERIES = new ImmutableLocalDateDoubleTimeSeries(new int[0], new double[0]);

  /** Serialization version. */
  private static final long serialVersionUID = -43654613865187568L;

  /**
   * The times in the series.
   */
  private final int[] _times;
  /**
   * The values in the series.
   */
  private final double[] _values;

  //-------------------------------------------------------------------------
  /**
   * Creates an empty builder, used to create time-series.
   * <p>
   * The builder has methods to create and modify a time-series.
   *
   * @return the time-series builder, not null
   */
  public static LocalDateDoubleTimeSeriesBuilder builder() {
    return new ImmutableLocalDateDoubleTimeSeriesBuilder();
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from a single date and value.
   *
   * @param date  the singleton date, not null
   * @param value  the singleton value
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(final LocalDate date, final double value) {
    Objects.requireNonNull(date, "date");
    final int[] timesArray = new int[] {LocalDateToIntConverter.convertToInt(date)};
    final double[] valuesArray = new double[] {value};
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   *
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(final LocalDate[] dates, final Double[] values) {
    final int[] timesArray = convertToIntArray(dates);
    final double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   *
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(final LocalDate[] dates, final double[] values) {
    final int[] timesArray = convertToIntArray(dates);
    validate(timesArray, values);
    final double[] valuesArray = values.clone();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   *
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(final int[] dates, final double[] values) {
    validate(dates, values);
    final int[] timesArray = dates.clone();
    final double[] valuesArray = values.clone();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   *
   * @param dates  the date list, not null
   * @param values  the value list, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(final Collection<LocalDate> dates, final Collection<Double> values) {
    final int[] timesArray = convertToIntArray(dates);
    final double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from another time-series.
   *
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(final DateDoubleTimeSeries<?> timeSeries) {
    if (timeSeries instanceof ImmutableLocalDateDoubleTimeSeries) {
      return (ImmutableLocalDateDoubleTimeSeries) timeSeries;
    }
    final DateDoubleTimeSeries<?> other = timeSeries;
    final int[] timesArray = other.timesArrayFast();
    final double[] valuesArray = other.valuesArrayFast();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   *
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries from(final DoubleTimeSeries<LocalDate> timeSeries) {
    if (timeSeries instanceof DateDoubleTimeSeries) {
      return of((DateDoubleTimeSeries<?>) timeSeries);
    }
    final int[] timesArray = convertToIntArray(timeSeries.timesArray());
    final double[] valuesArray = timeSeries.valuesArrayFast();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the data before creation.
   *
   * @param times  the times, not null
   * @param values  the values, not null
   */
  private static void validate(final int[] times, final double[] values) {
    if (times == null || values == null) {
      throw new NullPointerException("Array must not be null");
    }
    // check lengths
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    // check dates are ordered
    int maxTime = Integer.MIN_VALUE;
    for (final int time : times) {
      LocalDateToIntConverter.checkValid(time);
      if (time < maxTime) {
        throw new IllegalArgumentException("dates must be ordered");
      }
      maxTime = time;
    }
  }

  /**
   * Creates an instance.
   *
   * @param times  the times, not null
   * @param values  the values, not null
   */
  ImmutableLocalDateDoubleTimeSeries(final int[] times, final double[] values) {
    _times = times;
    _values = values;
  }

  //-------------------------------------------------------------------------
  @Override
  int[] timesArrayFast0() {
    return _times;
  }

  @Override
  double[] valuesArrayFast0() {
    return _values;
  }

  @Override
  LocalDateDoubleTimeSeries newInstanceFast(final int[] times, final double[] values) {
    return new ImmutableLocalDateDoubleTimeSeries(times, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _times.length;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(final int date) {
    final int binarySearch = Arrays.binarySearch(_times, date);
    return binarySearch >= 0;
  }

  @Override
  public Double getValue(final int date) {
    final int binarySearch = Arrays.binarySearch(_times, date);
    if (binarySearch >= 0) {
      return _values[binarySearch];
    }
    return null;
  }

  @Override
  public int getTimeAtIndexFast(final int index) {
    return _times[index];
  }

  @Override
  public double getValueAtIndexFast(final int index) {
    return _values[index];
  }

  //-------------------------------------------------------------------------
  @Override
  public int getEarliestTimeFast() {
    try {
      return _times[0];
    } catch (final IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public double getEarliestValueFast() {
    try {
      return _values[0];
    } catch (final IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public int getLatestTimeFast() {
    try {
      return _times[_times.length - 1];
    } catch (final IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public double getLatestValueFast() {
    try {
      return _values[_values.length - 1];
    } catch (final IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public int[] timesArrayFast() {
    return _times.clone();
  }

  @Override
  public double[] valuesArrayFast() {
    return _values.clone();
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries subSeriesFast(final int startTime, final boolean includeStart,
      final int endTime, final boolean includeEnd) {
    if (endTime < startTime) {
      throw new IllegalArgumentException("Invalid subSeries: endTime " + endTime + " < startTime " + startTime);
    }
    // special case when this is empty
    if (isEmpty()) {
      return EMPTY_SERIES;
    }
    // special case for start equals end
    if (startTime == endTime) {
      if (includeStart && includeEnd) {
        final int pos = Arrays.binarySearch(_times, startTime);
        if (pos >= 0) {
          return new ImmutableLocalDateDoubleTimeSeries(new int[] {startTime}, new double[] {_values[pos]});
        }
      }
      return EMPTY_SERIES;
    }
    // normalize to include start and exclude end
    int start = startTime;
    if (!includeStart) {
      start++;
    }
    int end = endTime;
    if (includeEnd) {
      if (end != Integer.MAX_VALUE) {
        end++;
      }
    }
    // calculate
    int startPos = Arrays.binarySearch(_times, start);
    startPos = startPos >= 0 ? startPos : -(startPos + 1);
    int endPos = Arrays.binarySearch(_times, end);
    endPos = endPos >= 0 ? endPos : -(endPos + 1);
    if (includeEnd && end == Integer.MAX_VALUE) {
      endPos = _times.length;
    }
    final int[] timesArray = Arrays.copyOfRange(_times, startPos, endPos);
    final double[] valuesArray = Arrays.copyOfRange(_values, startPos, endPos);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries head(final int numItems) {
    if (numItems == size()) {
      return this;
    }
    final int[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    final double[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  @Override
  public LocalDateDoubleTimeSeries tail(final int numItems) {
    final int size = size();
    if (numItems == size) {
      return this;
    }
    final int[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    final double[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableLocalDateDoubleTimeSeries newInstance(final LocalDate[] dates, final Double[] values) {
    return of(dates, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries operate(final UnaryOperator operator) {
    final double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableLocalDateDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

  @Override
  public LocalDateDoubleTimeSeries operate(final double other, final BinaryOperator operator) {
    final double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableLocalDateDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeriesBuilder toBuilder() {
    return builder().putAll(this);
  }

}
