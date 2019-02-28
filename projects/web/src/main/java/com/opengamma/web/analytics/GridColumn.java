/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Specifies the header label of a column and the type of data it displays.
 */
/* package */ class GridColumn {

  /** The column header. */
  private final String _header;
  /** Description of the column. */
  private final String _description;
  /** Type of data displayed in the column, null if unknown or if different rows contain different types. */
  private final Class<?> _type;
  /** Converts cell data to strings or objects for display in the client. */
  private final CellRenderer _renderer;
  /** Specifies the analytics data displayed in the column, null if the column data doesn't come from the engine. */
  private final ColumnSpecification _columnSpec;
  /** Null if this column doesn't display exploded data, otherwise the key into the exploded data */
  private final Object _inlineKey;
  /** Null if this column doesn't display exploded data, otherwise the index in the set of exploded data columns */
  private final Integer _inlineIndex;

  /**
   * If a column's data can be displayed inline across multiple columns the type of data displayed
   * in the cells will not be the same as the column's underlying data type. e.g. if a column contains a vector
   * of double values the cells will contain doubles when but the underlying type of the first column will be a
   * vector.
   */
  private final Class<?> _underlyingType;

  /* package */ GridColumn(final String header, final String description, final Class<?> type, final CellRenderer renderer) {
    this(header, description, type, null, renderer, null, null, null);
  }

  /* package */ GridColumn(final String header,
      final String description,
      final Class<?> type,
      final CellRenderer renderer,
      final ColumnSpecification columnSpec) {
    this(header, description, type, null, renderer, columnSpec, null, null);
  }

  /* package */ GridColumn(final String header,
      final String description,
      final Class<?> type,
      final Class<?> underlyingType,
      final CellRenderer renderer,
      final ColumnSpecification columnSpec,
      final Object inlineKey,
      final Integer inlineIndex) {
    ArgumentChecker.notNull(header, "header");
    ArgumentChecker.notNull(renderer, "renderer");
    _inlineIndex = inlineIndex;
    _columnSpec = columnSpec;
    _header = header;
    _renderer = renderer;
    if (description != null) {
      _description = description;
    } else {
      _description = header;
    }
    _type = type;
    _underlyingType = underlyingType;
    _inlineKey = inlineKey;
  }

  /**
   * Factory method that creates a column for a column specification,
   * calculation configuration and data type.
   *
   * @param columnSpec
   *          The column specification
   * @param columnType
   *          Type of data displayed in the column
   * @param targetLookup
   *          allows the target to be looked up
   * @return A column for displaying data calculated for the requirement and
   *         calculation configuration
   */
  /* package */ static GridColumn forSpec(final ColumnSpecification columnSpec,
      final Class<?> columnType,
      final TargetLookup targetLookup) {
    return forSpec(columnSpec.getHeader(), columnSpec, columnType, null, targetLookup, null, null);
  }

  /**
   * Factory method to create a column for inlined values. These are single
   * values (e.g. vectors) displayed over multiple columns.
   *
   * @param header
   *          The column header
   * @param columnSpec
   *          Specification of the column's value
   * @param columnType
   *          The type displayed in the column
   * @param underlyingType
   *          The type of the column's underlying data, can be different from
   *          the type displayed in the column for types that are displayed
   *          inline, e.g. vectors of doubles where the double values are
   *          displayed but the underlying type is a vector
   * @param targetLookup
   *          For looking up values to populate the column
   * @param inlineKey
   *          the key of the data item
   * @param inlineIndex
   *          The index of the individual data item in this column. This is used
   *          to extract each cell's data from the value.
   * @return The column
   */
  /* package */ static GridColumn forSpec(final String header,
      final ColumnSpecification columnSpec,
      final Class<?> columnType,
      final Class<?> underlyingType,
      final TargetLookup targetLookup,
      final Object inlineKey,
      final Integer inlineIndex) {
    final CellRenderer renderer = new AnalyticsRenderer(columnSpec, targetLookup);
    return new GridColumn(header,
        createDescription(columnSpec.getValueProperties()),
        columnType,
        underlyingType,
        renderer,
        columnSpec,
        inlineKey,
        inlineIndex);
  }

  /**
   * @return Label text for the column header
   */
  /* package */ String getHeader() {
    return _header;
  }

  /**
   * @return Description of the column's data
   */
  /* package */ String getDescription() {
    return _description;
  }

  /**
   * @return Type of data displayed in the column, can be null if the type is unknown or can change
   */
  /* package */ Class<?> getType() {
    return _type;
  }

  /**
   * @return The specification of this column's analytics data or null if it displays static data.
   */
  /* package */ ColumnSpecification getSpecification() {
    return _columnSpec;
  }

  /* package */ Integer getInlineIndex() {
    return _inlineIndex;
  }

  /**
   * @return If this column's data can be displayed inline (e.g. a vector) this returns the underlying type which
   * won't be the type displayed in the cells. e.g. for a vector of doubles the underlying type is vector but the
   * type of values displayed in the cells is double. If the column's data can't be displayed inline this method
   * returns the same value as {@link #getType()}.
   */
  /* package */ Class<?> getUnderlyingType() {
    if (_underlyingType != null) {
      return _underlyingType;
    }
    return _type;
  }

  /* package */ ResultsCell buildResults(final int rowIndex, final TypeFormatter.Format format, final ResultsCache cache) {
    return _renderer.getResults(rowIndex, format, cache, _type, _inlineKey);
  }

  private static String createDescription(final ValueProperties constraints) {
    if (constraints.isEmpty()) {
      return "No constraints";
    }
    final StringBuilder sb = new StringBuilder();
    boolean firstProperty = true;
    for (final String propertyName : constraints.getProperties()) {
      if (ValuePropertyNames.FUNCTION.equals(propertyName)) {
        continue;
      }
      if (firstProperty) {
        firstProperty = false;
      } else {
        sb.append("; \n");
      }
      sb.append(propertyName).append("=");
      final Set<String> propertyValues = constraints.getValues(propertyName);
      final boolean isOptional = constraints.isOptional(propertyName);
      if (propertyValues.size() == 0) {
        sb.append("[empty]");
      } else if (propertyValues.size() == 1 && !isOptional) {
        sb.append(propertyValues.iterator().next());
      } else {
        sb.append("(");
        boolean firstValue = true;
        for (final String propertyValue : propertyValues) {
          if (firstValue) {
            firstValue = false;
          } else {
            sb.append(", ");
          }
          sb.append(propertyValue);
        }
        sb.append(")");
      }
      if (isOptional) {
        sb.append("?");
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return "AnalyticsColumn [" +
        "_header='" + _header + '\'' +
        ", _type=" + _type +
        ", _description='" + _description + '\'' +
        "]";
  }

  // TODO merge this into the AnalyticsColumn and create subclasses for each of the renderer classes
  /* package */ interface CellRenderer {

    ResultsCell getResults(int rowIndex,
        TypeFormatter.Format format,
        ResultsCache cache,
        Class<?> columnType,
        Object inlineKey);
  }
}
