/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * A key for looking up a specific result for a given target.  This is equivalent to a 'column' name in a tabular result set.
 * Note that this differs from a ResultType in that it has a much weaker equality match - this allows you to specify a minimal key
 * that will successfully match a result with a ResultType that contains a ResultType with lots of extra meta-data in the form of
 * properties.
 */
public final class ResultKey {

  private final ResultType _resultType;
  private final String _columnSet;

  private ResultKey(final String columnSet, final ResultType resultType) {
    _columnSet = columnSet;
    _resultType = resultType;
  }

  /**
   * Static factory method used to create instances of a ResultKey assuming the result is in the default columnset (a.k.a calculation configuration)
   * @param resultType  the type of result, not null
   * @return the result key, not null
   */
  public static ResultKey of(final ResultType resultType) {
    ArgumentChecker.notNull(resultType, "resultType");
    return new ResultKey(ViewDefinition.DEFAULT_CALCULATION_CONFIGURATION_NAME, resultType);
  }

  /**
   * Static factory method used to create instances of a ResultKey when the desired result is in a non-default column set (a.k.a calculation configuration)
   * @param columnSet  the name of the column set (a.k.a calculation configuration)
   * @param resultType  the type of result, not null
   * @return the result key, not null
   */
  public static ResultKey of(final String columnSet, final ResultType resultType) {
    ArgumentChecker.notNull(columnSet, "columnSet");
    ArgumentChecker.notNull(resultType, "resultType");
    return new ResultKey(columnSet, resultType);
  }

  /**
   * @return the name of the column set/calculation configuration
   */
  public String getColumnSet() {
    return _columnSet;
  }

  /**
   * @return true, if the column set/calculation configuration is the default.  Useful for supressing display of column set name in simpler outputs.
   */
  public boolean isDefaultColumnSet() {
    return _columnSet.equals(ViewDefinition.DEFAULT_CALCULATION_CONFIGURATION_NAME);
  }

  /**
   * @return the full result type describing the result (desired or actual)
   */
  public ResultType getResultType() {
    return _resultType;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ResultKey resultKey = (ResultKey) o;

    if (!_resultType.getValueRequirementName().equals(resultKey._resultType.getValueRequirementName())) {
      return false;
    }
    if (!_columnSet.equals(resultKey._columnSet)) {
      return false;
    }
    return _resultType.getProperties().isSatisfiedBy(resultKey._resultType.getProperties()) ||
        resultKey._resultType.getProperties().isSatisfiedBy(_resultType.getProperties());
  }

  @Override
  public int hashCode() {
    int result = _resultType.getValueRequirementName().hashCode();
    result = 31 * result + _columnSet.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ResultKey[columnSet=" + _columnSet + ", resultType=" + _resultType + "]";
  }
}
