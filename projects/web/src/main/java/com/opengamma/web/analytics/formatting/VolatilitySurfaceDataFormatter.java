/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.conversion.LabelFormatter;

/**
 * Formatter.
 */
@SuppressWarnings("rawtypes")
/* package */ class VolatilitySurfaceDataFormatter extends AbstractFormatter<VolatilitySurfaceData> {

  protected VolatilitySurfaceDataFormatter() {
    super(VolatilitySurfaceData.class);
    addFormatter(new Formatter<VolatilitySurfaceData>(Format.EXPANDED) {
      @SuppressWarnings("unchecked")
      @Override
      protected Object formatValue(final VolatilitySurfaceData value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public String formatCell(final VolatilitySurfaceData value, final ValueSpecification valueSpec, final Object inlineKey) {
    final int xSize = value.getUniqueXValues().size();
    final int ySize = Sets.newHashSet(value.getYs()).size();
    return "Volatility Surface (" + xSize + " x " + ySize + ")";
  }

  @SuppressWarnings("unchecked")
  private <X, Y> Map<String, Object> formatExpanded(final VolatilitySurfaceData<X, Y> surface) {
    // the x and y values won't necessarily be unique and won't necessarily map to a rectangular grid
    // this projects them onto a grid and inserts nulls where there's no data available
    final Set<X> xVals = surface.getUniqueXValues();
    final Y[] yValues = surface.getYs();
    Set<Y> yVals;
    if (yValues.length > 0 && yValues[0] instanceof Pair) {
      //TODO emcleod This nastiness is here because ObjectsPair is now (2013/5/13) no longer Comparable
      final Pair<Object, Object> pair = (Pair) yValues[0];
      if (pair.getFirst() instanceof Integer && pair.getSecond() instanceof FXVolQuoteType) {
        final FirstThenSecondPairComparator<Integer, FXVolQuoteType> comparator = new FirstThenSecondPairComparator<>();
        final Set sortedSet = new TreeSet(comparator);
        sortedSet.addAll(Arrays.asList(surface.getYs()));
        yVals = sortedSet;
      } else {
        throw new UnsupportedOperationException("Cannot handle pairs of type " + pair);
      }
    } else {
      yVals = Sets.newTreeSet((Iterable) Arrays.asList(surface.getYs()));
    }
    final Map<String, Object> results = Maps.newHashMap();
    results.put(SurfaceFormatterUtils.X_LABELS, getAxisLabels(xVals));
    results.put(SurfaceFormatterUtils.Y_LABELS, getAxisLabels(yVals));
    if (isPlottable(surface)) {
      return formatForPlotting(surface, xVals, yVals, results);
    } else {
      return formatForGrid(surface, xVals, yVals, results);
    }
  }

  /**
   * Formats the surface data for display in a grid of text.
   * @param surface The surface data
   * @return The data formatted for display as text
   */
  private <X, Y> Map<String, Object> formatForGrid(final VolatilitySurfaceData<X, Y> surface,
                                                   final Set<X> xVals,
                                                   final Set<Y> yVals,
                                                   final Map<String, Object> baseResults) {
    final List<List<Double>> vol = Lists.newArrayListWithCapacity(yVals.size());
    for (final Y yVal : yVals) {
      final List<Double> volVals = Lists.newArrayListWithCapacity(xVals.size());
      for (final X xVal : xVals) {
        final Double volatility = surface.getVolatility(xVal, yVal);
        volVals.add(volatility);
      }
      vol.add(volVals);
    }
    final Map<String, Object> results = Maps.newHashMap(baseResults);
    results.put(LabelledMatrix2DFormatter.MATRIX, vol);
    results.put(LabelledMatrix2DFormatter.X_LABELS, SurfaceFormatterUtils.getAxisLabels(xVals));
    results.put(LabelledMatrix2DFormatter.Y_LABELS, SurfaceFormatterUtils.getAxisLabels(yVals));
    return results;
  }

  /**
   * Formats the surface data for display in the 3D surface viewer.. Returns a map containing the x-axis labels
   * and values, y-axis labels and values, axis titles and volatility values. The lists of axis labels are sorted and
   * have no duplicate values (which isn't necessarily true of the underlying data). The volatility data list contains
   * a value for every combination of x and y values. If there is no corresponding value in the underlying data the
   * volatility value will be null.
   * <p>
   * The axis values are numeric values which correspond to the axis labels. It is unspecified what they
   * actually represent but their relative sizes show the relationship between the label values.
   * This allows the labels to be properly laid out on the plot axes.
   * <p>
   * Not all volatility surfaces can be sensibly plotted as a surface and in that case the axis labels can't
   * be converted to a meaningful numeric value. For these surfaces one or both of the axis values will be missing
   * and the UI shouldn't attempt to plot the surface.
   *
   * @param surface The surface
   * @return {xLabels: [...],
   *          xValues: [...],
   *          xTitle: "X Axis Title",
   *          yLabels: [...],
   *          yValues: [...],
   *          yTitle: "Y Axis Title",
   *          vol: [x0y0, x1y0,... , x0y1, x1y1,...]}
   */
  private <X, Y> Map<String, Object> formatForPlotting(final VolatilitySurfaceData<X, Y> surface,
                                                       final Set<X> xVals,
                                                       final Set<Y> yVals,
                                                       final Map<String, Object> baseResults) {
    final Map<String, Object> results = Maps.newHashMap(baseResults);
    // the x and y values won't necessarily be unique and won't necessarily map to a rectangular grid
    // this projects them onto a grid and inserts nulls where there's no data available
    // numeric values corresponding to the axis labels to help with plotting the surface
    final List<Number> xAxisValues = Lists.newArrayListWithCapacity(xVals.size());
    final List<Number> yAxisValues = Lists.newArrayListWithCapacity(yVals.size());
    final List<Double> vol = Lists.newArrayListWithCapacity(xVals.size() * yVals.size());
    for (final Y yVal : yVals) {
      for (final X xVal : xVals) {
        vol.add(surface.getVolatility(xVal, yVal));
      }
      yAxisValues.add(getAxisValue(yVal));
    }
    for (final Object xVal : xVals) {
      xAxisValues.add(getAxisValue(xVal));
    }
    results.put(SurfaceFormatterUtils.Y_VALUES, yAxisValues);
    results.put(SurfaceFormatterUtils.X_VALUES, xAxisValues);
    results.put(SurfaceFormatterUtils.X_TITLE, surface.getXLabel());
    results.put(SurfaceFormatterUtils.Y_TITLE, surface.getYLabel());
    results.put(SurfaceFormatterUtils.VOL, vol);
    return results;
  }

  private List<String> getAxisLabels(final Collection values) {
    final List<String> labels = Lists.newArrayListWithCapacity(values.size());
    for (final Object value : values) {
      labels.add(LabelFormatter.format(value));
    }
    return labels;
  }

  /**
   * Returns a numeric value corresponding to a point on volatility surface axis to help with plotting the surface.
   * @param axisValue A point on the axis
   * @return A numeric value corresponding to the value or null if there's no meaningful numeric value
   */
  private Number getAxisValue(final Object axisValue) {
    if (axisValue instanceof Number) {
      return (Number) axisValue;
    } else if (axisValue instanceof LocalDate) {
      return ((LocalDate) axisValue).toEpochDay();
    } else if (axisValue instanceof Tenor) {
      final Period period = ((Tenor) axisValue).getPeriod();
      return DateUtils.estimatedDuration(period).getSeconds();
    }
    return null;
  }

  /**
   * Returns {@link DataType#UNKNOWN UNKNOWN} because the format type can be differ for different instances of
   * {@link VolatilitySurfaceData} depending on the axis types. The type for a given surface instance can
   * be obtained from {@link #getDataTypeForValue}
   * @return {@link DataType#UNKNOWN}
   */
  @Override
  public DataType getDataType() {
    return DataType.UNKNOWN;
  }

  /**
   * If the axis values can be sensibly converted to numbers this returns {@link DataType#SURFACE_DATA}, if not
   * it returns {@link DataType#LABELLED_MATRIX_2D}.
   * @param surfaceData The surface data
   * @return The format type for the surface data, {@link DataType#SURFACE_DATA} or
   * {@link DataType#LABELLED_MATRIX_2D} depending on the axis types of the data
   */
  @Override
  public DataType getDataTypeForValue(final VolatilitySurfaceData surfaceData) {
    if (isPlottable(surfaceData)) {
      return DataType.SURFACE_DATA;
    } else {
      return DataType.LABELLED_MATRIX_2D;
    }
  }

  /**
   * Returns true if the surface data can be sensibly plotted.
   *
   * @param surfaceData  the surface data
   * @return true if the data can be sensibly plotted
   */
  private boolean isPlottable(final VolatilitySurfaceData surfaceData) {
    final Object[] xVals = surfaceData.getXs();
    final Object[] yVals = surfaceData.getYs();

    if (xVals.length == 0) {
      return false;
    }
    if (yVals.length == 0) {
      return false;
    }
    if (getAxisValue(xVals[0]) == null || getAxisValue(yVals[0]) == null) {
      return false;
    }
    return true;
  }
}
