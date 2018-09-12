/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Data structure to hold a particular volatility surface's data points.
 * Note no interpolation or fitting is done in this code.
 *
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 */
public class VolatilitySurfaceData<X, Y> {
  /** Default name for the x axis. */
  public static final String DEFAULT_X_LABEL = "x";
  /** Default name for the y axis. */
  public static final String DEFAULT_Y_LABEL = "y";
  private static final Comparator<Pair<?, ?>> COMPARATOR = FirstThenSecondPairComparator.INSTANCE;
  private final String _definitionName;
  private final String _specificationName;
  private final UniqueIdentifiable _target;
  private final Map<Pair<X, Y>, Double> _values;
  private final X[] _xs;
  private final Y[] _ys;
  private final SortedSet<X> _uniqueXs;
  private final Map<X, List<ObjectsPair<Y, Double>>> _strips;
  private final String _xLabel;
  private final String _yLabel;

  /**
   * Constructs a surface with default x and y labels.
   *
   * @param definitionName  the surface definition name, not null
   * @param specificationName  the surface specification name, not null
   * @param target  the surface target, not null
   * @param xs  the x values, not null
   * @param ys  the y values, not null
   * @param values  the surface values, not null
   */
  public VolatilitySurfaceData(final String definitionName, final String specificationName, final UniqueIdentifiable target,
                               final X[] xs, final Y[] ys, final Map<Pair<X, Y>, Double> values) {
    this(definitionName, specificationName, target, xs, DEFAULT_X_LABEL, ys, DEFAULT_Y_LABEL, values);
  }


  /**
   * Constructs a surface.
   *
   * @param definitionName  the surface definition name, not null
   * @param specificationName  the surface specification name, not null
   * @param target  the surface target, not null
   * @param xs  the x values, not null
   * @param xLabel  the x-axis label, not null
   * @param ys  the y values, not null
   * @param yLabel  the y-axis label, not null
   * @param values  the surface values, not null
   */
  public VolatilitySurfaceData(final String definitionName, final String specificationName, final UniqueIdentifiable target,
                               final X[] xs, final String xLabel, final Y[] ys, final String yLabel, final Map<Pair<X, Y>, Double> values) {
    ArgumentChecker.notNull(definitionName, "Definition Name");
    ArgumentChecker.notNull(specificationName, "Specification Name");
    ArgumentChecker.notNull(target, "Target");
    ArgumentChecker.notNull(xs, "X axis values");
    ArgumentChecker.notNull(xLabel, "x label");
    ArgumentChecker.notNull(ys, "Y axis values");
    ArgumentChecker.notNull(yLabel, "y label");
    ArgumentChecker.notNull(values, "Volatility Values Map");
    _definitionName = definitionName;
    _specificationName = specificationName;
    _target = target;
    _values = Maps.newHashMap(values);
    _xs = xs;
    _xLabel = xLabel;
    _ys = ys;
    _yLabel = yLabel;
    _uniqueXs = new TreeSet<>();
    _strips = Maps.newHashMap();
    for (final Map.Entry<Pair<X, Y>, Double> entries : values.entrySet()) {
      if (_strips.containsKey(entries.getKey().getFirst())) {
        _strips.get(entries.getKey().getFirst()).add(ObjectsPair.of(entries.getKey().getSecond(), entries.getValue()));
      } else {
        _uniqueXs.add(entries.getKey().getFirst());
        final List<ObjectsPair<Y, Double>> list = Lists.newArrayList();
        list.add(ObjectsPair.of(entries.getKey().getSecond(), entries.getValue()));
        _strips.put(entries.getKey().getFirst(), list);
      }
    }
  }

  /**
   * Gets the number of values in the surface.
   *
   * @return  the number of values
   */
  public int size() {
    return _values.size();
  }

  /**
   * Gets the x-axis values.
   *
   * @return  the x-axis values
   */
  public X[] getXs() {
    return _xs;
  }

  /**
   * Gets the x-axis labels.
   *
   * @return  the x-axis labels
   */
  public String getXLabel() {
    return _xLabel;
  }

  /**
   * Gets the y-axis values.
   *
   * @return  the y-axis values
   */
  public Y[] getYs() {
    return _ys;
  }

  /**
   * Gets the y-axis labels.
   *
   * @return  the y-axis labels
   */
  public String getYLabel() {
    return _yLabel;
  }

  /**
   * Gets the volatility at a point.
   *
   * @param x  the x value
   * @param y  the y value
   * @return  the value or null if it is not present in the surface
   */
  public Double getVolatility(final X x, final Y y) {
    return _values.get(ObjectsPair.of(x, y));
  }

  /**
   * Gets the unique x values.
   *
   * @return  the unique x values
   */
  public SortedSet<X> getUniqueXValues() {
    return _uniqueXs;
  }

  /**
   * Gets all y and volatility values for an x value. Throws an exception if there are
   * no values for this x value.
   *
   * @param x  the x value, not null
   * @return  the y and volatility values
   */
  public List<ObjectsPair<Y, Double>> getYValuesForX(final X x) {
    ArgumentChecker.notNull(x, "x");
    if (!_strips.containsKey(x)) {
      throw new OpenGammaRuntimeException("Could not get strip for x value " + x);
    }
    final List<ObjectsPair<Y, Double>> result = _strips.get(x);
    Collections.sort(result, COMPARATOR);
    return result;
  }

  /**
   * Returns the values as a map.
   *
   * @return  a map
   */
  public Map<Pair<X, Y>, Double> asMap() {
    return _values;
  }

  /**
   * Gets the definition name.
   *
   * @return  the name
   */
  public String getDefinitionName() {
    return _definitionName;
  }

  /**
   * Gets the specification name.
   *
   * @return  the name
   */
  public String getSpecificationName() {
    return _specificationName;
  }

  /**
   * Gets the surface target.
   *
   * @return  the target
   */
  public UniqueIdentifiable getTarget() {
    return _target;
  }

  /**
   * @deprecated use getTarget()
   * @throws ClassCastException if target not a currency
   * @return currency assuming that the target is a currency
   */
  @Deprecated
  public Currency getCurrency() {
    return (Currency) _target;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof VolatilitySurfaceData)) {
      return false;
    }
    final VolatilitySurfaceData<?, ?> other = (VolatilitySurfaceData<?, ?>) o;
    return getDefinitionName().equals(other.getDefinitionName())
           && getSpecificationName().equals(other.getSpecificationName())
           && getTarget().equals(other.getTarget())
           && Arrays.equals(getXs(), other.getXs())
           && Arrays.equals(getYs(), other.getYs())
           && getXLabel().equals(other.getXLabel())
           && getYLabel().equals(other.getYLabel())
           && _values.equals(other._values);
  }

  @Override
  public int hashCode() {
    return getDefinitionName().hashCode() * getSpecificationName().hashCode() * getTarget().hashCode();
  }

  @Override
  public String toString() {
    return "VolatilitySurfaceData ["
        + "_definitionName='" + _definitionName + "'"
        + ", _specificationName='" + _specificationName + "'"
        + ", _target=" + _target
        + ", _xLabel='" + _xLabel + "'"
        + ", _yLabel='" + _yLabel + "'"
        + ", _xs=" + (_xs == null ? null : Arrays.asList(_xs))
        + ", _ys=" + (_ys == null ? null : Arrays.asList(_ys))
        + ", _values=" + _values
        + ", _uniqueXs=" + _uniqueXs
        + ", _strips=" + _strips
        + "]";
  }
}
