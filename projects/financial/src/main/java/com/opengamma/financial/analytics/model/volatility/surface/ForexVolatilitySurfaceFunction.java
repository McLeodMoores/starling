/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.volatility.surface.DefaultVolatilitySurfaceShiftFunction;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceShiftFunction;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public abstract class ForexVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger LOGGER = LoggerFactory.getLogger(ForexVolatilitySurfaceFunction.class);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> interpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (interpolatorNames == null || interpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> leftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> rightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      LOGGER.error("Need one surface name; have " + surfaceNames);
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    return Collections.<ValueRequirement>singleton(getDataRequirement(surfaceName, target));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder resultProperties = createValueProperties()
        .withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (context.getViewCalculationConfiguration() != null) {
      final Set<String> shifts =
          context.getViewCalculationConfiguration().getDefaultProperties().getValues(DefaultVolatilitySurfaceShiftFunction.VOLATILITY_SURFACE_SHIFT);
      if (shifts != null && shifts.size() == 1) {
        resultProperties.with(VolatilitySurfaceShiftFunction.SHIFT, shifts.iterator().next());
      }
    }
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(),
        resultProperties.get()));
  }

  /**
   * Gets the quote type of the input volatility surface e.g. call, risk reversal/strangle.
   *
   * @return  the quote type
   */
  protected abstract String getVolatilitySurfaceQuoteType();

  /**
   * Estimates the time in years represented by a tenor assuming 12 months in a year and 365 days in a year,
   * and that the tenor represents a period in years, months or days only.
   *
   * @deprecated Use {@link #getTime(ZonedDateTime, Tenor)} to allow use of all tenor types and the system day-count
   * calculator.
   *
   * @param tenor  the tenor
   * @return  the estimated time
   */
  @Deprecated
  protected double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      return period.getYears();
    }
    if (period.getMonths() != 0) {
      return (double) period.getMonths() / 12;
    }
    if (period.getDays() != 0) {
      return (double) period.getDays() / 365;
    }
    throw new OpenGammaRuntimeException("Should never happen");
  }

  /**
   * Calculates the time to a tenor from the valuation date using the system day-count calculator in {@link TimeCalculator}.
   *
   * @param valuationDate  the valuation date
   * @param tenor  the tenor
   * @return  the estimated time
   */
  protected double getTime(final ZonedDateTime valuationDate, final Tenor tenor) {
    final ZonedDateTime maturityDate = TenorUtils.adjustDateByTenor(valuationDate, tenor);
    return TimeCalculator.getTimeBetween(valuationDate, maturityDate);
  }

  /**
   * Gets the raw data requirement for the surface name and target.
   *
   * @param surfaceName  the surface name
   * @param target  the target
   * @return  the value requirement
   */
  protected ValueRequirement getDataRequirement(final String surfaceName, final ComputationTarget target) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(),
        ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, getVolatilitySurfaceQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE).get());
  }

}
