/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeriesOperators;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.AbstractPreciseObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;

/**
 * Standard immutable implementation of {@code ZonedDateTimeObjectTimeSeries}.
 *
 * @param <V>  the value being viewed over time
 */
public final class ImmutableZonedDateTimeObjectTimeSeries<V>
    extends AbstractPreciseObjectTimeSeries<ZonedDateTime, V>
    implements ZonedDateTimeObjectTimeSeries<V>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = -43654613865187568L;

  /**
   * The time-zone.
   */
  private final ZoneId _zone;
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
   * @param zone  the time-zone, not null
   * @return the time-series builder, not null
   */
  public static <V> ZonedDateTimeObjectTimeSeriesBuilder<V> builder(final ZoneId zone) {
    return new ImmutableZonedDateTimeObjectTimeSeriesBuilder<>(zone);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from a single date and value.
   *
   * @param <V>  the value being viewed over time
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   */
  @SuppressWarnings("unchecked")
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> ofEmpty(final ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    return new ImmutableZonedDateTimeObjectTimeSeries<>(new long[0], (V[]) new Object[0], zone);
  }

  /**
   * Obtains a time-series from a single instant and value.
   *
   * @param <V>  the value being viewed over time
   * @param instant  the singleton instant, not null
   * @param value  the singleton value
   * @return the time-series, not null
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(final ZonedDateTime instant, final V value) {
    Objects.requireNonNull(instant, "instant");
    final long[] timesArray = new long[] {ZonedDateTimeToLongConverter.convertToLong(instant)};
    @SuppressWarnings("unchecked")
    final
    V[] valuesArray = (V[]) new Object[] {value};
    return new ImmutableZonedDateTimeObjectTimeSeries<>(timesArray, valuesArray, instant.getZone());
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param <V>  the value being viewed over time
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @param zone  the time-zone, may be null if the arrays are non-empty
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(final ZonedDateTime[] instants, final V[] values, final ZoneId zone) {
    final long[] timesArray = convertToLongArray(instants);
    final V[] valuesArray = values.clone();
    validate(timesArray, valuesArray);
    final ZoneId zoneId = zone != null ? zone : instants[0].getZone();
    return new ImmutableZonedDateTimeObjectTimeSeries<>(timesArray, valuesArray, zoneId);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param <V>  the value being viewed over time
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(final long[] instants, final V[] values, final ZoneId zone) {
    validate(instants, values);
    Objects.requireNonNull(zone, "zone");
    final long[] timesArray = instants.clone();
    final V[] valuesArray = values.clone();
    return new ImmutableZonedDateTimeObjectTimeSeries<>(timesArray, valuesArray, zone);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   *
   * @param <V>  the value being viewed over time
   * @param instants  the instant list, not null
   * @param values  the value list, not null
   * @param zone  the time-zone, may be null if the collections are non-empty
   * @return the time-series, not null
   * @throws IllegalArgumentException if the collections are of different lengths
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(final Collection<ZonedDateTime> instants, final Collection<V> values, final ZoneId zone) {
    final long[] timesArray = convertToLongArray(instants);
    @SuppressWarnings("unchecked")
    final
    V[] valuesArray = (V[]) values.toArray();
    validate(timesArray, valuesArray);
    final ZoneId zoneId = zone != null ? zone : instants.iterator().next().getZone();
    return new ImmutableZonedDateTimeObjectTimeSeries<>(timesArray, valuesArray, zoneId);
  }

  /**
   * Obtains a time-series from another time-series.
   *
   * @param <V>  the value being viewed over time
   * @param timeSeries  the time-series, not null
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   */
  @SuppressWarnings("unchecked")
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(final PreciseObjectTimeSeries<?, V> timeSeries, final ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (timeSeries instanceof ImmutableZonedDateTimeObjectTimeSeries
        && ((ImmutableZonedDateTimeObjectTimeSeries<V>) timeSeries).getZone().equals(zone)) {
      return (ImmutableZonedDateTimeObjectTimeSeries<V>) timeSeries;
    }
    final PreciseObjectTimeSeries<?, V> other = timeSeries;
    final long[] timesArray = other.timesArrayFast();
    final V[] valuesArray = other.valuesArray();
    return new ImmutableZonedDateTimeObjectTimeSeries<>(timesArray, valuesArray, zone);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   *
   * @param <V>  the value being viewed over time
   * @param timeSeries  the time-series, not null
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> from(final ObjectTimeSeries<ZonedDateTime, V> timeSeries, final ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (timeSeries instanceof PreciseObjectTimeSeries) {
      return of((PreciseObjectTimeSeries<ZonedDateTime, V>) timeSeries, zone);
    }
    final long[] timesArray = convertToLongArray(timeSeries.timesArray());
    final V[] valuesArray = timeSeries.valuesArray();
    return new ImmutableZonedDateTimeObjectTimeSeries<>(timesArray, valuesArray, zone);
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
        throw new IllegalArgumentException("ZonedDateTimes must be ordered");
      }
      maxTime = time;
    }
  }

  /**
   * Creates an instance.
   *
   * @param instants  the times, not null
   * @param values  the values, not null
   * @param zone  the time-zone, not null
   */
  ImmutableZonedDateTimeObjectTimeSeries(final long[] instants, final V[] values, final ZoneId zone) {
    _times = instants;
    _values = values;
    _zone = zone;
  }

  //-------------------------------------------------------------------------
  /**
   * Convert a collection of dates to an array of long.
   *
   * @param instants  a collection of dates
   * @return  an array of long
   */
  static long[] convertToLongArray(final Collection<ZonedDateTime> instants) {
    final long[] timesArray = new long[instants.size()];
    int i = 0;
    for (final ZonedDateTime instant : instants) {
      timesArray[i++] = ZonedDateTimeToLongConverter.convertToLong(instant);
    }
    return timesArray;
  }

  /**
   * Converts an array of dates to an array of long.
   *
   * @param instants  an array of dates
   * @return  an array of long
   */
  static long[] convertToLongArray(final ZonedDateTime[] instants) {
    final long[] timesArray = new long[instants.length];
    for (int i = 0; i < timesArray.length; i++) {
      timesArray[i] = ZonedDateTimeToLongConverter.convertToLong(instants[i]);
    }
    return timesArray;
  }

  /**
   * Creates an immutable entry of date and value.
   *
   * @param key  the key
   * @param value  the value
   * @return  an entry
   * @param <V>  the type of the data
   */
  static <V> Entry<ZonedDateTime, V> makeMapEntry(final ZonedDateTime key, final V value) {
    return new SimpleImmutableEntry<>(key, value);
  }

  //-------------------------------------------------------------------------
  @Override
  protected long convertToLong(final ZonedDateTime instant) {
    return ZonedDateTimeToLongConverter.convertToLong(instant);
  }

  @Override
  protected ZonedDateTime convertFromLong(final long instant) {
    return ZonedDateTimeToLongConverter.convertToZonedDateTime(instant, getZone());
  }

  @Override
  protected ZonedDateTime[] createArray(final int size) {
    return new ZonedDateTime[size];
  }

  //-------------------------------------------------------------------------
  @Override
  public ZoneId getZone() {
    return _zone;
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> withZone(final ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (zone.equals(_zone)) {
      return this;
    }
    // immutable, so can share arrays
    return new ImmutableZonedDateTimeObjectTimeSeries<>(_times, _values, zone);
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
  public ZonedDateTimeObjectEntryIterator<V> iterator() {
    return new ZonedDateTimeObjectEntryIterator<V>() {
      private int _index = -1;

      @Override
      public boolean hasNext() {
        return _index + 1 < size();
      }

      @Override
      public Entry<ZonedDateTime, V> next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        final long date = ImmutableZonedDateTimeObjectTimeSeries.this.getTimeAtIndexFast(_index);
        final V value = ImmutableZonedDateTimeObjectTimeSeries.this.getValueAtIndex(_index);
        return makeMapEntry(ImmutableZonedDateTimeObjectTimeSeries.this.convertFromLong(date), value);
      }

      @Override
      public long nextTimeFast() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return ImmutableZonedDateTimeObjectTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public ZonedDateTime nextTime() {
        return ImmutableZonedDateTimeObjectTimeSeries.this.convertFromLong(nextTimeFast());
      }

      @Override
      public long currentTimeFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return ImmutableZonedDateTimeObjectTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public ZonedDateTime currentTime() {
        return ImmutableZonedDateTimeObjectTimeSeries.this.convertFromLong(currentTimeFast());
      }

      @Override
      public V currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return ImmutableZonedDateTimeObjectTimeSeries.this.getValueAtIndex(_index);
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
  public ZonedDateTimeObjectTimeSeries<V> subSeries(final ZonedDateTime startZonedDateTime, final ZonedDateTime endZonedDateTime) {
    return subSeriesFast(convertToLong(startZonedDateTime), true, convertToLong(endZonedDateTime), false);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> subSeries(final ZonedDateTime startZonedDateTime, final boolean includeStart,
      final ZonedDateTime endZonedDateTime, final boolean includeEnd) {
    return subSeriesFast(convertToLong(startZonedDateTime), includeStart, convertToLong(endZonedDateTime), includeEnd);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> subSeriesFast(final long startZonedDateTime, final long endZonedDateTime) {
    return subSeriesFast(startZonedDateTime, true, endZonedDateTime, false);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> subSeriesFast(final long startZonedDateTime, final boolean includeStart,
      final long endZonedDateTime, final boolean includeEnd) {
    if (endZonedDateTime < startZonedDateTime) {
      throw new IllegalArgumentException("Invalid subSeries: endTime < startTime");
    }
    // special case for start equals end
    if (startZonedDateTime == endZonedDateTime) {
      if (includeStart && includeEnd) {
        final int pos = Arrays.binarySearch(_times, startZonedDateTime);
        if (pos >= 0) {
          return new ImmutableZonedDateTimeObjectTimeSeries<>(new long[] {startZonedDateTime}, Arrays.copyOfRange(_values, pos, pos + 1), _zone);
        }
      }
      return ofEmpty(_zone);
    }
    // special case when this is empty
    if (isEmpty()) {
      return ofEmpty(_zone);
    }
    // normalize to include start and exclude end
    long start = startZonedDateTime;
    if (!includeStart) {
      start++;
    }
    long end = endZonedDateTime;
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
    return new ImmutableZonedDateTimeObjectTimeSeries<>(timesArray, valuesArray, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectTimeSeries<V> head(final int numItems) {
    if (numItems == size()) {
      return this;
    }
    final long[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    final V[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableZonedDateTimeObjectTimeSeries<>(timesArray, valuesArray, _zone);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> tail(final int numItems) {
    final int size = size();
    if (numItems == size) {
      return this;
    }
    final long[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    final V[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableZonedDateTimeObjectTimeSeries<>(timesArray, valuesArray, _zone);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ZonedDateTimeObjectTimeSeries<V> lag(final int days) {
    final long[] times = timesArrayFast();
    final V[] values = valuesArray();
    if (days == 0) {
      return new ImmutableZonedDateTimeObjectTimeSeries<>(times, values, _zone);
    } else if (days < 0) {
      if (-days < times.length) {
        final long[] resultTimes = new long[times.length + days]; // remember days is -ve
        System.arraycopy(times, 0, resultTimes, 0, times.length + days);
        final V[] resultValues = (V[]) new Object[times.length + days];
        System.arraycopy(values, -days, resultValues, 0, times.length + days);
        return new ImmutableZonedDateTimeObjectTimeSeries<>(resultTimes, resultValues, _zone);
      }
      return ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(_zone);
    } else { // if (days > 0) {
      if (days < times.length) {
        final long[] resultTimes = new long[times.length - days]; // remember days is +ve
        System.arraycopy(times, days, resultTimes, 0, times.length - days);
        final V[] resultValues = (V[]) new Object[times.length - days];
        System.arraycopy(values, 0, resultValues, 0, times.length - days);
        return new ImmutableZonedDateTimeObjectTimeSeries<>(resultTimes, resultValues, _zone);
      }
      return ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(_zone);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableZonedDateTimeObjectTimeSeries<V> newInstance(final ZonedDateTime[] dates, final V[] values) {
    return of(dates, values, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectTimeSeries<V> operate(final UnaryOperator<V> operator) {
    final V[] valuesArray = valuesArray();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableZonedDateTimeObjectTimeSeries<>(_times, valuesArray, _zone);  // immutable, so can share times
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> operate(final V other, final BinaryOperator<V> operator) {
    final V[] valuesArray = valuesArray();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableZonedDateTimeObjectTimeSeries<>(_times, valuesArray, _zone);  // immutable, so can share times
  }

  @Override
  @SuppressWarnings("unchecked")
  public ZonedDateTimeObjectTimeSeries<V> operate(final PreciseObjectTimeSeries<?, V> other, final BinaryOperator<V> operator) {
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
    return new ImmutableZonedDateTimeObjectTimeSeries<>(trimmedTimes, trimmedValues, _zone);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ZonedDateTimeObjectTimeSeries<V> unionOperate(final PreciseObjectTimeSeries<?, V> other, final BinaryOperator<V> operator) {
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
    return new ImmutableZonedDateTimeObjectTimeSeries<>(trimmedTimes, trimmedValues, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectTimeSeries<V> intersectionFirstValue(final PreciseObjectTimeSeries<?, V> other) {
    return operate(other, ObjectTimeSeriesOperators.<V>firstOperator());
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> intersectionSecondValue(final PreciseObjectTimeSeries<?, V> other) {
    return operate(other, ObjectTimeSeriesOperators.<V>secondOperator());
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> noIntersectionOperation(final PreciseObjectTimeSeries<?, V> other) {
    return unionOperate(other, ObjectTimeSeriesOperators.<V>noIntersectionOperator());
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectTimeSeriesBuilder<V> toBuilder() {
    return ImmutableZonedDateTimeObjectTimeSeries.<V>builder(_zone).putAll(this);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ImmutableZonedDateTimeObjectTimeSeries) {
      final ImmutableZonedDateTimeObjectTimeSeries<?> other = (ImmutableZonedDateTimeObjectTimeSeries<?>) obj;
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
