/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import static com.opengamma.timeseries.DoubleTimeSeriesOperators.ABS_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.ADD_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.AVERAGE_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.DIVIDE_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.FIRST_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.LOG10_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.LOG_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.MAXIMUM_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.MINIMUM_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.MULTIPLY_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.NEGATE_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.NO_INTERSECTION_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.POWER_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.RECIPROCAL_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.SECOND_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.SUBTRACT_OPERATOR;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.precise.AbstractPreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Abstract implementation of {@code InstantDoubleTimeSeries}.
 */
abstract class AbstractInstantDoubleTimeSeries
    extends AbstractPreciseDoubleTimeSeries<Instant>
    implements InstantDoubleTimeSeries {

  /**
   * Creates an instance.
   */
  public AbstractInstantDoubleTimeSeries() {
  }

  //-------------------------------------------------------------------------
  static long[] convertToLongArray(final Collection<Instant> instants) {
    final long[] timesArray = new long[instants.size()];
    int i = 0;
    for (final Instant time : instants) {
      timesArray[i++] = InstantToLongConverter.convertToLong(time);
    }
    return timesArray;
  }

  static long[] convertToLongArray(final Instant[] instants) {
    final long[] timesArray = new long[instants.length];
    for (int i = 0; i < timesArray.length; i++) {
      timesArray[i] = InstantToLongConverter.convertToLong(instants[i]);
    }
    return timesArray;
  }

  static double[] convertToDoubleArray(final Collection<Double> values) {
    final double[] valuesArray = new double[values.size()];
    int i = 0;
    for (final Double value : values) {
      valuesArray[i++] = value;
    }
    return valuesArray;
  }

  static double[] convertToDoubleArray(final Double[] values) {
    final double[] valuesArray = new double[values.length];
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = values[i];
    }
    return valuesArray;
  }

  static Entry<Instant, Double> makeMapEntry(final Instant key, final Double value) {
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
  /**
   * Gets the internal storage array without cloning.
   *
   * @return the array, not null
   */
  abstract long[] timesArrayFast0();

  /**
   * Gets the internal storage array without cloning.
   *
   * @return the array, not null
   */
  abstract double[] valuesArrayFast0();

  /**
   * Creates a new instance without cloning.
   *
   * @param instant  the times array, not null
   * @param values  the values array, not null
   * @return the new instance, not null
   */
  abstract InstantDoubleTimeSeries newInstanceFast(long[] instant, double[] values);

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleEntryIterator iterator() {
    return new InstantDoubleEntryIterator() {
      private int _index = -1;

      @Override
      public boolean hasNext() {
        return _index + 1 < size();
      }

      @Override
      public Entry<Instant, Double> next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        final long time = AbstractInstantDoubleTimeSeries.this.getTimeAtIndexFast(_index);
        final Double value = AbstractInstantDoubleTimeSeries.this.getValueAtIndex(_index);
        return makeMapEntry(AbstractInstantDoubleTimeSeries.this.convertFromLong(time), value);
      }

      @Override
      public long nextTimeFast() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return AbstractInstantDoubleTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public Instant nextTime() {
        return AbstractInstantDoubleTimeSeries.this.convertFromLong(nextTimeFast());
      }

      @Override
      public long currentTimeFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return AbstractInstantDoubleTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public Instant currentTime() {
        return AbstractInstantDoubleTimeSeries.this.convertFromLong(currentTimeFast());
      }

      @Override
      public Double currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return AbstractInstantDoubleTimeSeries.this.getValueAtIndex(_index);
      }

      @Override
      public double currentValueFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return AbstractInstantDoubleTimeSeries.this.getValueAtIndexFast(_index);
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
  public InstantDoubleTimeSeries subSeries(final Instant startInstant, final Instant endInstant) {
    return subSeriesFast(convertToLong(startInstant), true, convertToLong(endInstant), false);
  }

  @Override
  public InstantDoubleTimeSeries subSeries(final Instant startInstant, final boolean includeStart, final Instant endInstant, final boolean includeEnd) {
    return subSeriesFast(convertToLong(startInstant), includeStart, convertToLong(endInstant), includeEnd);
  }

  @Override
  public InstantDoubleTimeSeries subSeriesFast(final long startInstant, final long endInstant) {
    return subSeriesFast(startInstant, true, endInstant, false);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries lag(final int days) {
    final long[] times = timesArrayFast0();
    final double[] values = valuesArrayFast0();
    if (days == 0) {
      return newInstanceFast(times, values);
    } else if (days < 0) {
      if (-days < times.length) {
        final long[] resultTimes = new long[times.length + days]; // remember days is -ve
        System.arraycopy(times, 0, resultTimes, 0, times.length + days);
        final double[] resultValues = new double[times.length + days];
        System.arraycopy(values, -days, resultValues, 0, times.length + days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new long[0], new double[0]);
      }
    } else { // if (days > 0) {
      if (days < times.length) {
        final long[] resultTimes = new long[times.length - days]; // remember days is +ve
        System.arraycopy(times, days, resultTimes, 0, times.length - days);
        final double[] resultValues = new double[times.length - days];
        System.arraycopy(values, 0, resultValues, 0, times.length - days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new long[0], new double[0]);
      }
    }
  }

  //-------------------------------------------------------------------------
  private InstantDoubleTimeSeries operate(final DoubleTimeSeries<?> other, final BinaryOperator operator) {
    if (other instanceof PreciseDoubleTimeSeries) {
      return operate((PreciseDoubleTimeSeries<?>) other, operator);
    }
    throw new UnsupportedOperationException("Can only operate on a PreciseDoubleTimeSeries");
  }

  @Override
  public InstantDoubleTimeSeries operate(final PreciseDoubleTimeSeries<?> other, final BinaryOperator operator) {
    final long[] aTimes = timesArrayFast0();
    final double[] aValues = valuesArrayFast0();
    int aCount = 0;
    final long[] bTimes = other.timesArrayFast();
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[Math.max(aTimes.length, bTimes.length)];
    final double[] resValues = new double[resTimes.length];
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
    final double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  private InstantDoubleTimeSeries unionOperate(final DoubleTimeSeries<?> other, final BinaryOperator operator) {
    if (other instanceof PreciseDoubleTimeSeries) {
      return unionOperate((PreciseDoubleTimeSeries<?>) other, operator);
    }
    throw new UnsupportedOperationException("Can only operate on a PreciseDoubleTimeSeries");
  }

  @Override
  public InstantDoubleTimeSeries unionOperate(final PreciseDoubleTimeSeries<?> other, final BinaryOperator operator) {
    final long[] aTimes = timesArrayFast0();
    final double[] aValues = valuesArrayFast0();
    int aCount = 0;
    final long[] bTimes = other.timesArrayFast();
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
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
    final double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries add(final double amountToAdd) {
    return operate(amountToAdd, ADD_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries add(final DoubleTimeSeries<?> other) {
    return operate(other, ADD_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries unionAdd(final DoubleTimeSeries<?> other) {
    return unionOperate(other, ADD_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries subtract(final double amountToSubtract) {
    return operate(amountToSubtract, SUBTRACT_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries subtract(final DoubleTimeSeries<?> other) {
    return operate(other, SUBTRACT_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries unionSubtract(final DoubleTimeSeries<?> other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries multiply(final double amountToMultiplyBy) {
    return operate(amountToMultiplyBy, MULTIPLY_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries multiply(final DoubleTimeSeries<?> other) {
    return operate(other, MULTIPLY_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries unionMultiply(final DoubleTimeSeries<?> other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries divide(final double amountToDivideBy) {
    return operate(amountToDivideBy, DIVIDE_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries divide(final DoubleTimeSeries<?> other) {
    return operate(other, DIVIDE_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries unionDivide(final DoubleTimeSeries<?> other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries power(final double power) {
    return operate(power, POWER_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries power(final DoubleTimeSeries<?> other) {
    return operate(other, POWER_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries unionPower(final DoubleTimeSeries<?> other) {
    return unionOperate(other, POWER_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries minimum(final double minValue) {
    return operate(minValue, MINIMUM_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries minimum(final DoubleTimeSeries<?> other) {
    return operate(other, MINIMUM_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries unionMinimum(final DoubleTimeSeries<?> other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries maximum(final double maxValue) {
    return operate(maxValue, MAXIMUM_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries maximum(final DoubleTimeSeries<?> other) {
    return operate(other, MAXIMUM_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries unionMaximum(final DoubleTimeSeries<?> other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries average(final double value) {
    return operate(value, AVERAGE_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries average(final DoubleTimeSeries<?> other) {
    return operate(other, AVERAGE_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries unionAverage(final DoubleTimeSeries<?> other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries intersectionFirstValue(final DoubleTimeSeries<?> other) {
    // optimize PLAT-1590
    if (other instanceof AbstractInstantDoubleTimeSeries) {
      final long[] aTimes = timesArrayFast0();
      final double[] aValues = valuesArrayFast0();
      int aCount = 0;
      final long[] bTimes = ((AbstractInstantDoubleTimeSeries) other).timesArrayFast0();
      int bCount = 0;
      final long[] resTimes = new long[Math.min(aTimes.length, bTimes.length)];
      final double[] resValues = new double[resTimes.length];
      int resCount = 0;
      while (aCount < aTimes.length && bCount < bTimes.length) {
        if (aTimes[aCount] == bTimes[bCount]) {
          resTimes[resCount] = aTimes[aCount];
          resValues[resCount] = aValues[aCount];
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
      final double[] trimmedValues = new double[resCount];
      System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
      System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
      return newInstanceFast(trimmedTimes, trimmedValues);
    }
    return operate(other, FIRST_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries intersectionSecondValue(final DoubleTimeSeries<?> other) {
    return operate(other, SECOND_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries noIntersectionOperation(final DoubleTimeSeries<?> other) {
    return unionOperate(other, NO_INTERSECTION_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries negate() {
    return operate(NEGATE_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries reciprocal() {
    return operate(RECIPROCAL_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries log() {
    return operate(LOG_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries log10() {
    return operate(LOG10_OPERATOR);
  }

  @Override
  public InstantDoubleTimeSeries abs() {
    return operate(ABS_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public double maxValue() {
    if (isEmpty()) {
      throw new NoSuchElementException("Time-series is empty");
    }
    double max = Double.MIN_VALUE;
    for (final double value : valuesArrayFast0()) {
      max = Math.max(max, value);
    }
    return max;
  }

  @Override
  public double minValue() throws NoSuchElementException {
    if (isEmpty()) {
      throw new NoSuchElementException("Time-series is empty");
    }
    double min = Double.MAX_VALUE;
    for (final double value : valuesArrayFast0()) {
      min = Math.min(min, value);
    }
    return min;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof AbstractInstantDoubleTimeSeries) {
      final AbstractInstantDoubleTimeSeries other = (AbstractInstantDoubleTimeSeries) obj;
      return Arrays.equals(timesArrayFast0(), other.timesArrayFast0()) &&
              Arrays.equals(valuesArrayFast0(), other.valuesArrayFast0());
    }
    if (obj instanceof PreciseDoubleTimeSeries) {
      final PreciseDoubleTimeSeries<?> other = (PreciseDoubleTimeSeries<?>) obj;
      return Arrays.equals(timesArrayFast0(), other.timesArrayFast()) &&
              Arrays.equals(valuesArrayFast0(), other.valuesArrayFast());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(timesArrayFast0()) ^ Arrays.hashCode(valuesArrayFast0());
  }

}
