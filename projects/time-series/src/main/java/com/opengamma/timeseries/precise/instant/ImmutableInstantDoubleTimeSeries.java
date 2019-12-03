/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Standard immutable implementation of {@code InstantDoubleTimeSeries}.
 */
public final class ImmutableInstantDoubleTimeSeries
    extends AbstractInstantDoubleTimeSeries
    implements Serializable {

  /** Empty instance. */
  public static final ImmutableInstantDoubleTimeSeries EMPTY_SERIES = new ImmutableInstantDoubleTimeSeries(new long[0], new double[0]);

  /** Serialization version. */
  private static final long serialVersionUID = -43654613865187568L;

  /**
   * The times in the series.
   */
  private final long[] _times;
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
  public static InstantDoubleTimeSeriesBuilder builder() {
    return new ImmutableInstantDoubleTimeSeriesBuilder();
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from a single instant and value.
   *
   * @param instant  the singleton instant, not null
   * @param value  the singleton value
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(final Instant instant, final double value) {
    Objects.requireNonNull(instant, "instant");
    final long[] timesArray = new long[] {InstantToLongConverter.convertToLong(instant)};
    final double[] valuesArray = new double[] {value};
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param instants  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(final Instant[] instants, final Double[] values) {
    final long[] timesArray = convertToLongArray(instants);
    final double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(final Instant[] instants, final double[] values) {
    final long[] timesArray = convertToLongArray(instants);
    validate(timesArray, values);
    final double[] valuesArray = values.clone();
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(final long[] instants, final double[] values) {
    validate(instants, values);
    final long[] timesArray = instants.clone();
    final double[] valuesArray = values.clone();
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param instants  the instant list, not null
   * @param values  the value list, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(final Collection<Instant> instants, final Collection<Double> values) {
    final long[] timesArray = convertToLongArray(instants);
    final double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from another time-series.
   *
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(final PreciseDoubleTimeSeries<?> timeSeries) {
    if (timeSeries instanceof ImmutableInstantDoubleTimeSeries) {
      return (ImmutableInstantDoubleTimeSeries) timeSeries;
    }
    final PreciseDoubleTimeSeries<?> other = timeSeries;
    final long[] timesArray = other.timesArrayFast();
    final double[] valuesArray = other.valuesArrayFast();
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   *
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries from(final DoubleTimeSeries<Instant> timeSeries) {
    if (timeSeries instanceof PreciseDoubleTimeSeries) {
      return of((PreciseDoubleTimeSeries<?>) timeSeries);
    }
    final long[] timesArray = convertToLongArray(timeSeries.timesArray());
    final double[] valuesArray = timeSeries.valuesArrayFast();
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the data before creation.
   *
   * @param instants  the times, not null
   * @param values  the values, not null
   */
  private static void validate(final long[] instants, final double[] values) {
    if (instants == null || values == null) {
      throw new NullPointerException("Array must not be null");
    }
    // check lengths
    if (instants.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + instants.length + ", " + values.length);
    }
    // check dates are ordered
    long maxTime = Long.MIN_VALUE;
    for (final long time : instants) {
      if (time < maxTime) {
        throw new IllegalArgumentException("Instants must be ordered");
      }
      maxTime = time;
    }
  }

  /**
   * Creates an instance.
   *
   * @param nanos  the times, not null
   * @param values  the values, not null
   */
  ImmutableInstantDoubleTimeSeries(final long[] nanos, final double[] values) {
    _times = nanos;
    _values = values;
  }

  //-------------------------------------------------------------------------
  @Override
  long[] timesArrayFast0() {
    return _times;
  }

  @Override
  double[] valuesArrayFast0() {
    return _values;
  }

  @Override
  InstantDoubleTimeSeries newInstanceFast(final long[] instant, final double[] values) {
    return new ImmutableInstantDoubleTimeSeries(instant, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _times.length;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(final long instant) {
    final int binarySearch = Arrays.binarySearch(_times, instant);
    return binarySearch >= 0;
  }

  @Override
  public Double getValue(final long instant) {
    final int binarySearch = Arrays.binarySearch(_times, instant);
    if (binarySearch >= 0) {
      return _values[binarySearch];
    }
    return null;
  }

  @Override
  public long getTimeAtIndexFast(final int index) {
    return _times[index];
  }

  @Override
  public double getValueAtIndexFast(final int index) {
    return _values[index];
  }

  //-------------------------------------------------------------------------
  @Override
  public long getEarliestTimeFast() {
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
  public long getLatestTimeFast() {
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
  public long[] timesArrayFast() {
    return _times.clone();
  }

  @Override
  public double[] valuesArrayFast() {
    return _values.clone();
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries subSeriesFast(final long startTime, final boolean includeStart,
      final long endTime, final boolean includeEnd) {
    if (endTime < startTime) {
      throw new IllegalArgumentException("Invalid subSeries: endTime < startTime");
    }
    // special case for start equals end
    if (startTime == endTime) {
      if (includeStart && includeEnd) {
        final int pos = Arrays.binarySearch(_times, startTime);
        if (pos >= 0) {
          return new ImmutableInstantDoubleTimeSeries(new long[] {startTime}, new double[] {_values[pos]});
        }
      }
      return EMPTY_SERIES;
    }
    // special case when this is empty
    if (isEmpty()) {
      return EMPTY_SERIES;
    }
    long start = startTime;
    // normalize to include start and exclude end
    if (!includeStart) {
      start++;
    }
    long end = endTime;
    if (includeEnd) {
      if (end != Long.MAX_VALUE) {
        end++;
      }
    }
    // calculate
    int startPos = Arrays.binarySearch(_times, start);
    startPos = startPos >= 0 ? startPos : -(startPos + 1);
    int endPos = Arrays.binarySearch(_times, end);
    endPos = endPos >= 0 ? endPos : -(endPos + 1);
    if (includeEnd && end == Long.MAX_VALUE) {
      endPos = _times.length;
    }
    final long[] timesArray = Arrays.copyOfRange(_times, startPos, endPos);
    final double[] valuesArray = Arrays.copyOfRange(_values, startPos, endPos);
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries head(final int numItems) {
    if (numItems == size()) {
      return this;
    }
    final long[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    final double[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  @Override
  public InstantDoubleTimeSeries tail(final int numItems) {
    final int size = size();
    if (numItems == size) {
      return this;
    }
    final long[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    final double[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableInstantDoubleTimeSeries newInstance(final Instant[] instants, final Double[] values) {
    return of(instants, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries operate(final UnaryOperator operator) {
    final double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableInstantDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

  @Override
  public InstantDoubleTimeSeries operate(final double other, final BinaryOperator operator) {
    final double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableInstantDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeriesBuilder toBuilder() {
    return builder().putAll(this);
  }

}
