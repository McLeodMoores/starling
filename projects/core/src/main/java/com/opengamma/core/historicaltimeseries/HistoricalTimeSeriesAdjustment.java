/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import java.util.Stack;

import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Historical time series adjustment operations that can be string encoded.
 */
public abstract class HistoricalTimeSeriesAdjustment {

  /**
   * Default constructor.
   */
  protected HistoricalTimeSeriesAdjustment() {
  }

  /**
   * Adjusts a time series.
   *
   * @param timeSeries  the time series to adjust, not null
   * @return  the adjusted time series
   */
  public abstract LocalDateDoubleTimeSeries adjust(LocalDateDoubleTimeSeries timeSeries);

  /**
   * Perform an adjustment of a single value.
   *
   * @param value  the value
   * @return  the adjusted value
   */
  public abstract double adjust(double value);

  /**
   * Adjusts a time series.
   *
   * @param timeSeries  the time series, not null
   * @return  the adjusted time series
   */
  public HistoricalTimeSeries adjust(final HistoricalTimeSeries timeSeries) {
    return new SimpleHistoricalTimeSeries(timeSeries.getUniqueId(), adjust(timeSeries.getTimeSeries()));
  }

  /**
   * The division operation. Every point in the time series is divided by an amount.
   */
  public static class DivideBy extends HistoricalTimeSeriesAdjustment {
    private final double _amountToDivideBy;

    /**
     * @param amountToDivideBy  the divisor
     */
    public DivideBy(final double amountToDivideBy) {
      _amountToDivideBy = amountToDivideBy;
    }

    /**
     * Gets the divisor.
     *
     * @return  the divisor
     */
    protected double getAmountToDivideBy() {
      return _amountToDivideBy;
    }

    @Override
    public LocalDateDoubleTimeSeries adjust(final LocalDateDoubleTimeSeries timeSeries) {
      return timeSeries.divide(getAmountToDivideBy());
    }

    @Override
    public double adjust(final double value) {
      return value / getAmountToDivideBy();
    }

    @Override
    public String toString() {
      return getAmountToDivideBy() + " /";
    }

  }

  /**
   * A subtraction operation. Every point in the time series has an amount subtracted.
   */
  public static class Subtract extends HistoricalTimeSeriesAdjustment {

    private final double _amountToSubtract;

    /**
     * @param amountToSubtract  the amount to subtract
     */
    public Subtract(final double amountToSubtract) {
      _amountToSubtract = amountToSubtract;
    }

    /**
     * Gets the amount to subtract.
     *
     * @return  the amount to subtract
     */
    protected double getAmountToSubtract() {
      return _amountToSubtract;
    }

    @Override
    public LocalDateDoubleTimeSeries adjust(final LocalDateDoubleTimeSeries timeSeries) {
      return timeSeries.subtract(getAmountToSubtract());
    }

    @Override
    public double adjust(final double value) {
      return value - getAmountToSubtract();
    }

    @Override
    public String toString() {
      return getAmountToSubtract() + " -";
    }

  }

  /**
   * A sequence of two operations. The first is applied to a time series and the second applies to the resulting time series.
   */
  public static final class Sequence extends HistoricalTimeSeriesAdjustment {
    private final HistoricalTimeSeriesAdjustment _first;
    private final HistoricalTimeSeriesAdjustment _second;

    /**
     * @param first  the first adjustment, not null
     * @param second  the second adjustment, not null
     */
    public Sequence(final HistoricalTimeSeriesAdjustment first, final HistoricalTimeSeriesAdjustment second) {
      _first = first;
      _second = second;
    }

    /**
     * Gets the first adjustment.
     *
     * @return  the adjustment
     */
    protected HistoricalTimeSeriesAdjustment getFirst() {
      return _first;
    }

    /**
     * Gets the second adjustment.
     *
     * @return  the adjustment
     */
    protected HistoricalTimeSeriesAdjustment getSecond() {
      return _second;
    }

    @Override
    public LocalDateDoubleTimeSeries adjust(final LocalDateDoubleTimeSeries timeSeries) {
      return getSecond().adjust(getFirst().adjust(timeSeries));
    }

    @Override
    public double adjust(final double value) {
      return getSecond().adjust(getFirst().adjust(value));
    }

    @Override
    public String toString() {
      return getFirst().toString() + " " + getSecond().toString();
    }

  }

  /**
   * A no-op. Every point in the time series is unchanged.
   */
  public static final class NoOp extends HistoricalTimeSeriesAdjustment {

    /**
     * Singleton.
     */
    public static final NoOp INSTANCE = new NoOp();

    private NoOp() {
    }

    @Override
    public LocalDateDoubleTimeSeries adjust(final LocalDateDoubleTimeSeries timeSeries) {
      return timeSeries;
    }

    @Override
    public HistoricalTimeSeries adjust(final HistoricalTimeSeries timeSeries) {
      return timeSeries;
    }

    @Override
    public double adjust(final double value) {
      return value;
    }

    @Override
    public String toString() {
      return "";
    }

  }

  /**
   * Produces an instance based on the string representation returned by {@link #toString}. Strings are not intended
   * to be human writable, but should be created by forming the adjustment objects and calling {@code toString} on them.
   *
   * @param str the string to parse, not null
   * @return the adjustment instance, not null
   */
  public static HistoricalTimeSeriesAdjustment parse(final String str) {
    final String[] elements = str.split("\\s+");
    final Stack<Object> stack = new Stack<>();
    for (final String element : elements) {
      if ("/".equals(element)) {
        stack.push(new DivideBy(Double.parseDouble((String) stack.pop())));
      } else if ("-".equals(element)) {
        stack.push(new Subtract(Double.parseDouble((String) stack.pop())));
      } else if ("".equals(element)) {
        stack.push(NoOp.INSTANCE);
      } else {
        stack.push(element);
      }
    }
    while (stack.size() > 1) {
      final HistoricalTimeSeriesAdjustment second = (HistoricalTimeSeriesAdjustment) stack.pop();
      final HistoricalTimeSeriesAdjustment first = (HistoricalTimeSeriesAdjustment) stack.pop();
      stack.push(new Sequence(first, second));
    }
    return (HistoricalTimeSeriesAdjustment) stack.pop();
  }

  /**
   * Produces a string representation that can be parsed by {@link #parse}.
   *
   * @return the string
   */
  @Override
  public abstract String toString();

}
