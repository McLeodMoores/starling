/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesRelativeWeightedDifferenceOperator;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesWeightedVolatilityOperator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 *
 */
public class VolatilityWeightedYieldCurveNodeReturnSeriesFunction extends YieldCurveNodeReturnSeriesFunction {

  private static final TimeSeriesRelativeWeightedDifferenceOperator RELATIVE_WEIGHTED_DIFFERENCE = new TimeSeriesRelativeWeightedDifferenceOperator();

  @Override
  protected ValueProperties getResultProperties(final ComputationTarget target) {
    return VolatilityWeightingFunctionUtils.addVolatilityWeightingProperties(super.getResultProperties(target));
  }

  @Override
  protected String getYCHTSStart(final ValueProperties constraints) {
    final Set<String> startDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (startDates == null || startDates.size() != 1) {
      return null;
    }
    final String startDate = Iterables.getOnlyElement(startDates);

    final Set<String> volWeightingStartDates = constraints.getValues(VolatilityWeightingFunctionUtils.VOLATILITY_WEIGHTING_START_DATE_PROPERTY);
    if (volWeightingStartDates == null || volWeightingStartDates.size() != 1) {
      // NOTE jonathan 2013-04-29 -- should start a day earlier so the result after weighting starts at the startDate,
      // but need to know previous date with data
      return startDate;
    }
    return Iterables.getOnlyElement(volWeightingStartDates);
  }

  @Override
  protected LocalDateDoubleTimeSeries getReturnSeries(final LocalDateDoubleTimeSeries ts, final ValueRequirement desiredValue) {
    final double lambda = Double.parseDouble(desiredValue.getConstraint(VolatilityWeightingFunctionUtils.VOLATILITY_WEIGHTING_LAMBDA_PROPERTY));
    final TimeSeriesWeightedVolatilityOperator weightedVolOp = TimeSeriesWeightedVolatilityOperator.relative(lambda);
    final LocalDateDoubleTimeSeries weightedVolSeries = (LocalDateDoubleTimeSeries) weightedVolOp.evaluate(ts);
    return (LocalDateDoubleTimeSeries) RELATIVE_WEIGHTED_DIFFERENCE.evaluate(ts, weightedVolSeries);
  }

}
