/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 *
 */
//TODO urgently needs a rename
@Deprecated
public abstract class StandardOptionDataAnalyticOptionModelFunction extends AnalyticOptionModelFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(StandardOptionDataAnalyticOptionModelFunction.class);

  @SuppressWarnings("unchecked")
  @Override
  protected StandardOptionDataBundle getDataBundle(final Clock relevantTime, final EquityOptionSecurity option, final FunctionInputs inputs) {
    final ZonedDateTime now = ZonedDateTime.now(relevantTime);
    final Double spotAsObject = (Double) inputs.getValue(getUnderlyingMarketDataRequirement(option.getUnderlyingId()));
    if (spotAsObject == null) {
      s_logger.warn("Didn't have market value for {}", option.getUnderlyingId());
      throw new NullPointerException("No spot value for underlying instrument.");
    }
    final double spot = spotAsObject;
    final YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE);
    final double b = (Double) inputs.getValue(ValueRequirementNames.COST_OF_CARRY);
    return new StandardOptionDataBundle(discountCurve, b, volatilitySurface, spot, now);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if ((curveNames == null) || (curveNames.size() != 1)) {
      return null;
    }
    final String curveName = curveNames.iterator().next();
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getUnderlyingMarketDataRequirement(option.getUnderlyingId()));
    requirements.add(getYieldCurveMarketDataRequirement(option.getCurrency(), curveName));
    requirements.add(getVolatilitySurfaceMarketDataRequirement(option, curveName));
    requirements.add(getCostOfCarryMarketDataRequirement(option.getUniqueId(), curveName));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> originalResults = getResults(context, target);
    String curveName = null;
    for (final ValueSpecification input : inputs.keySet()) {
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getValueName())) {
        curveName = input.getProperty(ValuePropertyNames.CURVE);
      }
    }
    if (curveName == null) {
      // No yield curve in our inputs, so no yield curve in our output
      return originalResults;
    }
    final Set<ValueSpecification> newResults = Sets.newHashSetWithExpectedSize(originalResults.size());
    for (final ValueSpecification result : originalResults) {
      newResults.add(new ValueSpecification(result.getValueName(), result.getTargetSpecification(), result.getProperties().copy().withoutAny(ValuePropertyNames.CURVE)
          .with(ValuePropertyNames.CURVE, curveName).get()));
    }
    return newResults;
  }

}
