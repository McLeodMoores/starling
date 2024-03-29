/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeriesOperators;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.AbstractPreciseObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;

/**
 * Standard immutable implementation of {@code InstantObjectTimeSeries}.
 *
 * @param <V>  the value being viewed over time
 */
public final class ImmutableInstantObjectTimeSeries<V>
    extends AbstractPreciseObjectTimeSeries<Instant, V>
    implements InstantObjectTimeSeries<V>, Serializable {

  /** Empty instance. */
  private static final ImmutableInstantObjectTimeSeries<?> EMPTY_SERIES = new ImmutableInstantObjectTimeSeries<>(new long[0], new Object[0]);

  /** Serialization version. */
  private static final long serialVersionUID = -43654613865187568L;

  /**
   * The times in the series.
   */
  private final long[] _times;
  /**
   * The values in the series.
   */
  private final V[] _values;

  //-------------------------------------------------------------------------
  /**
   * Creates an empty builder, used to create time-series.
   * <p>
   * The builder has methods to create and modify a time-series.
   *
   * @param <V>  the value being viewed over time
   * @return the time-series builder, not null
   */
  public static <V> InstantObjectTimeSeriesBuilder<V> builder() {
    return new ImmutableInstantObjectTimeSeriesBuilder<>();
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from a single date and value.
   *
   * @param <V>  the value being viewed over time
   * @return the time-series, not null
   */
  @SuppressWarnings("unchecked")
  public static <V> ImmutableInstantObjectTimeSeries<V> ofEmpty() {
    return (ImmutableInstantObjectTimeSeries<V>) EMPTY_SERIES;
  }

  /**
   * Obtains a time-series from a single instant and value.
   *
   * @param <V>  the value being viewed over time
   * @param instant  the singleton instant, not null
   * @param value  the singleton value
   * @return the time-series, not null
   */
  public static <V> ImmutableInstantObjectTimeSeries<V> of(final Instant instant, final V value) {
    Objects.requireNonNull(instant, "instant");
    final long[] timesArray = new long[] {InstantToLongConverter.convertToLong(instant)};
    @SuppressWarnings("unchecked")
    final
    V[] valuesArray = (V[]) new Object[] {value};
    return new ImmutableInstantObjectTimeSeries<>(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param <V>  the value being viewed over time
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableInstantObjectTimeSeries<V> of(final Instant[] instants, final V[] values) {
    final long[] timesArray = convertToLongArray(instants);
    final V[] valuesArray = values.clone();
    validate(timesArray, valuesArray);
    return new ImmutableInstantObjectTimeSeries<>(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param <V>  the value being viewed over time
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableInstantObjectTimeSeries<V> of(final long[] instants, final V[] values) {
    validate(instants, values);
    final long[] timesArray = instants.clone();
    final V[] valuesArray = values.clone();
    return new ImmutableInstantObjectTimeSeries<>(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param <V>  the value being viewed over time
   * @param instants  the instant list, not null
   * @param values  the value list, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableInstantObjectTimeSeries<V> of(final Collection<Instant> instants, final Collection<V> values) {
    final long[] timesArray = convertToLongArray(instants);
    @SuppressWarnings("unchecked")
    final
    V[] valuesArray = (V[]) values.toArray();
    validate(timesArray, valuesArray);
    return new ImmutableInstantObjectTimeSeries<>(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from another time-series.
   *
   * @param <V>  the value being viewed over time
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  @SuppressWarnings("unchecked")
  public static <V> ImmutableInstantObjectTimeSeries<V> of(final PreciseObjectTimeSeries<?, V> timeSeries) {
    if (timeSeries instanceof ImmutableInstantObjectTimeSeries) {
      return (ImmutableInstantObjectTimeSeries<V>) timeSeries;
    }
    final PreciseObjectTimeSeries<?, V> other = timeSeries;
    final long[] timesArray = other.timesArrayFast();
    final V[] valuesArray = other.valuesArray();
    return new ImmutableInstantObjectTimeSeries<>(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   *
   * @param <V>  the value being viewed over time
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableInstantObjectTimeSeries<V> from(final ObjectTimeSeries<Instant, V> timeSeries) {
    if (timeSeries instanceof PreciseObjectTimeSeries) {
      return of((PreciseObjectTimeSeries<Instant, V>) timeSeries);
    }
    final long[] timesArray = convertToLongArray(timeSeries.timesArray());
    final V[] valuesArray = timeSeries.valuesArray();
    return new ImmutableInstantObjectTimeSeries<>(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the data before creation.
   *
   * @param <V>  the value being viewed over time
   * @param instants  the times, not null
   * @param values  the values, not null
   */
  private static <V> void validate(final long[] instants, final V[] values) {
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
   * @param instants  the times, not null
   * @param values  the values, not null
   */
  ImmutableInstantObjectTimeSeries(final long[] instants, final V[] values) {
    _times = instants;
    _values = values;
  }

  //-------------------------------------------------------------------------
  /**
   * Convert a collection of instants to an array of long.
   *
   * @param instants  a collection of instants
   * @return  the instants as an array
   */
  static long[] convertToLongArray(final Collection<Instant> instants) {
    final long[] timesArray = new long[instants.size()];
    int i = 0;
    for (final Instant instant : instants) {
      timesArray[i++] = InstantToLongConverter.convertToLong(instant);
    }
    return timesArray;
  }

  /**
   * Convert an array of instants to an array of long.
   *
   * @param instants  an array of instants
   * @return  the instants as an array of long
   */
  static long[] convertToLongArray(final Instant[] instants) {
    final long[] timesArray = new long[instants.length];
    for (int i = 0; i < timesArray.length; i++) {
      timesArray[i] = InstantToLongConverter.convertToLong(instants[i]);
    }
    return timesArray;
  }

  /**
   * Creates an immutable entry for a key and value.
   *
   * @param key  the key
   * @param value  the value
   * @return  an entry
   * @param <V>  the type of the value
   */
  static <V> Entry<Instant, V> makeMapEntry(final Instant key, final V value) {
    return new SimpleImmutableEntry<>(key, value);
  }

  //-------------------------------------------------------------------------
  @Override
  protected long convertToLong(final Instant instant) {
    return InstantToLongConverter.convertToLong(instant);
  }

  @Override
  protected Instant convertFromLong(final long instant) {
    return InstantToLongConverter.convertToInstant(instant);
  }

  @Override
  protected Instant[] createArray(final int size) {
    return new Instant[size];
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
  public V getValue(final long instant) {
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
  public V getValueAtIndex(final int index) {
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
  public V getEarliestValue() {
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
  public V getLatestValue() {
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
  public V[] valuesArray() {
    return _values.clone();
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantObjectEntryIterator<V> iterator() {
    return new InstantObjectEntryIterator<V>() {
      private int _index = -1;

      @Override
      public boolean hasNext() {
        return _index + 1 < size();
      }

      @Override
      public Entry<Instant, V> next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        final long date = ImmutableInstantObjectTimeSeries.this.getTimeAtIndexFast(_index);
        final V value = ImmutableInstantObjectTimeSeries.this.getValueAtIndex(_index);
        return makeMapEntry(ImmutableInstantObjectTimeSeries.this.convertFromLong(date), value);
      }

      @Override
      public long nextTimeFast() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return ImmutableInstantObjectTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public Instant nextTime() {
        return ImmutableInstantObjectTimeSeries.this.convertFromLong(nextTimeFast());
      }

      @Override
      public long currentTimeFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return ImmutableInstantObjectTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public Instant currentTime() {
        return ImmutableInstantObjectTimeSeries.this.convertFromLong(currentTimeFast());
      }

      @Override
      public V currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return ImmutableInstantObjectTimeSeries.this.getValueAtIndex(_index);
      }

      @Override
      public int currentIndex() {
        return _index;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable iterator");
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantObjectTimeSeries<V> subSeries(final Instant startInstant, final Instant endInstant) {
    return subSeriesFast(convertToLong(startInstant), true, convertToLong(endInstant), false);
  }

  @Override
  public InstantObjectTimeSeries<V> subSeries(final Instant startInstant, final boolean includeStart, final Instant endInstant, final boolean includeEnd) {
    return subSeriesFast(convertToLong(startInstant), includeStart, convertToLong(endInstant), includeEnd);
  }

  @Override
  public InstantObjectTimeSeries<V> subSeriesFast(final long startInstant, final long endInstant) {
    return subSeriesFast(startInstant, true, endInstant, false);
  }

  @Override
  public InstantObjectTimeSeries<V> subSeriesFast(final long startInstant, final boolean includeStart,
      final long endInstant, final boolean includeEnd) {
    if (endInstant < startInstant) {
      throw new IllegalArgumentException("Invalid subSeries: endTime < startTime");
    }
    // special case for start equals end
    if (startInstant == endInstant) {
      if (includeStart && includeEnd) {
        final int pos = Arrays.binarySearch(_times, startInstant);
        if (pos >= 0) {
          return new ImmutableInstantObjectTimeSeries<>(new long[] {startInstant}, Arrays.copyOfRange(_values, pos, pos + 1));
        }
      }
      return ofEmpty();
    }
    // special case when this is empty
    if (isEmpty()) {
      return ofEmpty();
    }
    // normalize to include start and exclude end
    long start = startInstant;
    if (!includeStart) {
      start++;
    }
    long end = endInstant;
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
    final V[] valuesArray = Arrays.copyOfRange(_values, startPos, endPos);
    return new ImmutableInstantObjectTimeSeries<>(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantObjectTimeSeries<V> head(final int numItems) {
    if (numItems == size()) {
      return this;
    }
    final long[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    final V[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableInstantObjectTimeSeries<>(timesArray, valuesArray);
  }

  @Override
  public InstantObjectTimeSeries<V> tail(final int numItems) {
    final int size = size();
    if (numItems == size) {
      return this;
    }
    final long[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    final V[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableInstantObjectTimeSeries<>(timesArray, valuesArray);
  }

  @Override
  @SuppressWarnings("unchecked")
  public InstantObjectTimeSeries<V> lag(final int days) {
    final long[] times = timesArrayFast();
    final V[] values = valuesArray();
    if (days == 0) {
      return new ImmutableInstantObjectTimeSeries<>(times, values);
    } else if (days < 0) {
      if (-days < times.length) {
        final long[] resultTimes = new long[times.length + days]; // remember days is -ve
        System.arraycopy(times, 0, resultTimes, 0, times.length + days);
        final V[] resultValues = (V[]) new Object[times.length + days];
        System.arraycopy(values, -days, resultValues, 0, times.length + days);
        return new ImmutableInstantObjectTimeSeries<>(resultTimes, resultValues);
      }
      return ImmutableInstantObjectTimeSeries.ofEmpty();
    } else { // if (days > 0) {
      if (days < times.length) {
        final long[] resultTimes = new long[times.length - days]; // remember days is +ve
        System.arraycopy(times, days, resultTimes, 0, times.length - days);
        final V[] resultValues = (V[]) new Object[times.length - days];
        System.arraycopy(values, 0, resultValues, 0, times.length - days);
        return new ImmutableInstantObjectTimeSeries<>(resultTimes, resultValues);
      }
      return ImmutableInstantObjectTimeSeries.ofEmpty();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableInstantObjectTimeSeries<V> newInstance(final Instant[] dates, final V[] values) {
    return of(dates, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantObjectTimeSeries<V> operate(final UnaryOperator<V> operator) {
    final V[] valuesArray = valuesArray();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableInstantObjectTimeSeries<>(_times, valuesArray);  // immutable, so can share times
  }

  @Override
  public InstantObjectTimeSeries<V> operate(final V other, final BinaryOperator<V> operator) {
    final V[] valuesArray = valuesArray();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableInstantObjectTimeSeries<>(_times, valuesArray);  // immutable, so can share times
  }

  @Override
  @SuppressWarnings("unchecked")
  public InstantObjectTimeSeries<V> operate(final PreciseObjectTimeSeries<?, V> other, final BinaryOperator<V> operator) {
    final long[] aTimes = timesArrayFast();
    final V[] aValues = valuesArray();
    int aCount = 0;
    final long[] bTimes = other.timesArrayFast();
    final V[] bValues = other.valuesArray();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final V[] resValues = (V[]) new Object[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length && bCount < bTimes.length) {
      if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        bCount++;
      }
    }
    final long[] trimmedTimes = new long[resCount];
    final V[] trimmedValues = (V[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return new ImmutableInstantObjectTimeSeries<>(trimmedTimes, trimmedValues);
  }

  @SuppressWarnings("unchecked")
  @Override
  public InstantObjectTimeSeries<V> unionOperate(final PreciseObjectTimeSeries<?, V> other, final BinaryOperator<V> operator) {
    final long[] aTimes = timesArrayFast();
    final V[] aValues = valuesArray();
    int aCount = 0;
    final long[] bTimes = other.timesArrayFast();
    final V[] bValues = other.valuesArray();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final V[] resValues = (V[]) new Object[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length || bCount < bTimes.length) {
      if (aCount >= aTimes.length) {
        final int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length) {
        final int aRemaining = aTimes.length - aCount;
        System.arraycopy(aTimes, aCount, resTimes, resCount, aRemaining);
        System.arraycopy(aValues, aCount, resValues, resCount, aRemaining);
        resCount += aRemaining;
        break;
      } else if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = aValues[aCount];
        resCount++;
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        resTimes[resCount] = bTimes[bCount];
        resValues[resCount] = bValues[bCount];
        resCount++;
        bCount++;
      }
    }
    final long[] trimmedTimes = new long[resCount];
    final V[] trimmedValues = (V[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return new ImmutableInstantObjectTimeSeries<>(trimmedTimes, trimmedValues);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantObjectTimeSeries<V> intersectionFirstValue(final PreciseObjectTimeSeries<?, V> other) {
    return operate(other, ObjectTimeSeriesOperators.<V>firstOperator());
  }

  @Override
  public InstantObjectTimeSeries<V> intersectionSecondValue(final PreciseObjectTimeSeries<?, V> other) {
    return operate(other, ObjectTimeSeriesOperators.<V>secondOperator());
  }

  @Override
  public InstantObjectTimeSeries<V> noIntersectionOperation(final PreciseObjectTimeSeries<?, V> other) {
    return unionOperate(other, ObjectTimeSeriesOperators.<V>noIntersectionOperator());
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantObjectTimeSeriesBuilder<V> toBuilder() {
    return ImmutableInstantObjectTimeSeries.<V>builder().putAll(this);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ImmutableInstantObjectTimeSeries) {
      final ImmutableInstantObjectTimeSeries<?> other = (ImmutableInstantObjectTimeSeries<?>) obj;
      return Arrays.equals(_times, other._times)
             && Arrays.equals(_values, other._values);
    }
    if (obj instanceof PreciseObjectTimeSeries) {
      final PreciseObjectTimeSeries<?, ?> other = (PreciseObjectTimeSeries<?, ?>) obj;
      return Arrays.equals(timesArrayFast(), other.timesArrayFast())
             && Arrays.equals(valuesArray(), other.valuesArray());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(timesArrayFast()) ^ Arrays.hashCode(valuesArray());
  }

}
